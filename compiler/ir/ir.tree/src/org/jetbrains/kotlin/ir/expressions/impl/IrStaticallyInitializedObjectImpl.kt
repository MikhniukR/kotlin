/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.expressions.impl

import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.fqNameForIrSerialization
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.util.transformInPlace
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.utils.SmartList

class IrStaticallyInitializedConstantImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var value: IrConst<*>,
    override var isBoxed: Boolean = false
) : IrStaticallyInitializedConstant() {
    override fun contentEquals(other: IrStaticallyInitializedValue) =
        other is IrStaticallyInitializedConstantImpl &&
                isBoxed == other.isBoxed &&
                value.kind == other.value.kind &&
                value.value == other.value

    override fun contentHashCode() =
        (isBoxed.hashCode() * 31 + value.kind.hashCode()) * 31 + value.value.hashCode()

    override var type = value.type

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        value.accept(visitor, data)
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        value = value.transform(transformer, data) as IrConst<*>
    }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitStaticallyInitializedConstant(this, data)
    }
}

class IrStaticallyInitializedObjectImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    fields_: Map<IrFieldSymbol, IrStaticallyInitializedValue>,
    override var isBoxed: Boolean = false
) : IrStaticallyInitializedObject() {
    override val fields = fields_.toMutableMap()
    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitStaticallyInitializedObject(this, data)
    }

    override fun putField(field: IrFieldSymbol, value: IrStaticallyInitializedValue) {
        fields[field] = value
    }

    override fun contentEquals(other: IrStaticallyInitializedValue): Boolean =
        other is IrStaticallyInitializedObjectImpl &&
                other.type == type &&
                isBoxed == other.isBoxed &&
                fields.size == other.fields.size &&
                fields.all { (field, value) -> other.fields[field]?.contentEquals(value) == true }

    override fun contentHashCode(): Int {
        var res = isBoxed.hashCode()
        res = res * 31 + type.hashCode()
        fun IrFieldSymbol.name() = owner.kotlinFqName.toString()
        fun IrFieldSymbol.parentName() = owner.parent.kotlinFqName.toString()
        for ((_, value) in fields.entries.sortedWith(compareBy({ it.key.parentName() }, { it.key.name() }))) {
            res = res * 31 + value.contentHashCode()
        }
        return res
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        fields.forEach { (_, value) -> value.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        for ((field, value) in fields) {
            fields[field] = value.transform(transformer, data) as IrStaticallyInitializedValue
        }
    }
}

class IrStaticallyInitializedArrayImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var type: IrType,
    initValues: List<IrStaticallyInitializedValue>,
    override var isBoxed: Boolean = false
) : IrStaticallyInitializedArray() {
    override val values = SmartList(initValues)
    override fun putElement(index: Int, value: IrStaticallyInitializedValue) {
        values[index] = value
    }

    override fun contentEquals(other: IrStaticallyInitializedValue): Boolean =
        other is IrStaticallyInitializedArrayImpl &&
                other.type == type &&
                isBoxed == other.isBoxed &&
                values.size == other.values.size &&
                values.indices.all { values[it].contentEquals(other.values[it]) }

    override fun contentHashCode(): Int {
        var res = isBoxed.hashCode()
        res = res * 31 + type.hashCode()
        for (value in values) {
            res = res * 31 + value.contentHashCode()
        }
        return res
    }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitStaticallyInitializedArray(this, data)
    }

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        values.forEach { value -> value.accept(visitor, data) }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        values.transformInPlace { value -> value.transform(transformer, data) }
    }
}

/**
 * This ir node represents either some custom constants, which can't be represented in IR,
 * (e.g. TypeInfo pointer in Native backend) and handled directly in code generator,
 * or any expression, which would be lowered to constant later.
 *
 * In second case, when it's lowered, this node would be automatically replaced by its child
 * to simplify matching patterns in IR for further lowerings and code generator.
 * @see transform method for details.
 */
class IrStaticallyInitializedIntrinsicImpl(
    override val startOffset: Int,
    override val endOffset: Int,
    override var expression: IrExpression,
    override var isBoxed: Boolean = false
) : IrStaticallyInitializedIntrinsic() {
    override fun contentEquals(other: IrStaticallyInitializedValue): Boolean {
        if (other == this) return true
        if (other !is IrStaticallyInitializedIntrinsicImpl) return false
        val expr = expression
        val otherExpr = other.expression
        if (expr !is IrCall || otherExpr !is IrCall) return false
        if (expr.valueArgumentsCount != 0 || otherExpr.valueArgumentsCount != 0) return false
        if (expr.typeArgumentsCount != otherExpr.typeArgumentsCount) return false
        return isBoxed == other.isBoxed &&
                expr.symbol == otherExpr.symbol &&
                (0 until expr.typeArgumentsCount).all { expr.getTypeArgument(it) == otherExpr.getTypeArgument(it) }
    }

    override fun contentHashCode(): Int {
        val expr = expression
        if (expr !is IrCall) return hashCode()
        if (expr.valueArgumentsCount != 0) return hashCode()
        var res = isBoxed.hashCode()
        res = res * 31 + expr.symbol.hashCode()
        for (i in 0 until expr.typeArgumentsCount) {
            res = res * 31 + expr.getTypeArgument(i).hashCode()
        }
        return res
    }

    override var type = expression.type

    override fun <D> acceptChildren(visitor: IrElementVisitor<Unit, D>, data: D) {
        expression.accept(visitor, data)
    }

    override fun <D> transform(transformer: IrElementTransformer<D>, data: D): IrExpression {
        return super.transform(transformer, data).let {
            if (it is IrStaticallyInitializedIntrinsicImpl && it.expression is IrStaticallyInitializedValue) {
                it.expression
            } else {
                it
            }
        }
    }

    override fun <D> transformChildren(transformer: IrElementTransformer<D>, data: D) {
        expression = expression.transform(transformer, data)
    }

    override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D): R {
        return visitor.visitStaticallyInitializedIntrinsic(this, data)
    }
}
