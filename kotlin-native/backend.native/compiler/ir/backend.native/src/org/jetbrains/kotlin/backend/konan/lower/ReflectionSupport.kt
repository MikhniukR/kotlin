/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.konan.InteropFqNames
import org.jetbrains.kotlin.backend.konan.KonanBackendContext
import org.jetbrains.kotlin.backend.konan.ir.typeWithStarProjections
import org.jetbrains.kotlin.backend.konan.isObjCClass
import org.jetbrains.kotlin.backend.konan.llvm.computeFullName
import org.jetbrains.kotlin.backend.konan.reportCompilationError
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.irCall
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.types.Variance

private fun IrBuilderWithScope.irStaticString(string: String) = irStaticallyInitializedConstant(irString(string))
private fun IrBuilderWithScope.irStaticInt(int: Int) = irStaticallyInitializedConstant(irInt(int))
private fun IrBuilderWithScope.irStaticBoolean(boolean: Boolean) = irStaticallyInitializedConstant(irBoolean(boolean))

internal class KTypeGenerator(
        val context: KonanBackendContext,
        val irFile: IrFile,
        val irElement: IrElement,
        val needExactTypeParameters: Boolean = false
) {
    private val symbols = context.ir.symbols

    fun IrBuilderWithScope.irKType(type: IrType, leaveReifiedForLater: Boolean = false) =
            irKType(type, leaveReifiedForLater, mutableSetOf())

    private class RecursiveBoundsException(message: String) : Throwable(message)

    private fun IrBuilderWithScope.irKType(
            type: IrType,
            leaveReifiedForLater: Boolean,
            seenTypeParameters: MutableSet<IrTypeParameter>
    ): IrStaticallyInitializedValue {
        if (type !is IrSimpleType) {
            // Represent as non-denotable type:
            return irKTypeImpl(
                    kClassifier = irStaticallyInitializedConstant(irNull()),
                    irTypeArguments = emptyList(),
                    isMarkedNullable = false,
                    leaveReifiedForLater = leaveReifiedForLater,
                    seenTypeParameters = seenTypeParameters
            )
        }
        try {
            val kClassifier = when (val classifier = type.classifier) {
                is IrClassSymbol -> irKClass(classifier)
                is IrTypeParameterSymbol -> {
                    if (classifier.owner.isReified && leaveReifiedForLater) {
                        // Leave as is for reification.
                        return irStaticallyInitializedIntrinsic(irCall(symbols.typeOf).apply { putTypeArgument(0, type) })
                    }

                    // Leave upper bounds of non-reified type parameters as is, even if they are reified themselves.
                    irKTypeParameter(classifier.owner, leaveReifiedForLater = false, seenTypeParameters = seenTypeParameters)
                }
                else -> TODO("Unexpected classifier: $classifier")
            }

            return irKTypeImpl(
                    kClassifier = kClassifier,
                    irTypeArguments = type.arguments,
                    isMarkedNullable = type.hasQuestionMark,
                    leaveReifiedForLater = leaveReifiedForLater,
                    seenTypeParameters = seenTypeParameters
            )
        } catch (t: RecursiveBoundsException) {
            if (needExactTypeParameters)
                this@KTypeGenerator.context.reportCompilationError(t.message!!, irFile, irElement)
            return irStaticallyInitializedObject(symbols.kTypeImplForTypeParametersWithRecursiveBounds.defaultType, emptyMap())
        }
    }

    private fun IrBuilderWithScope.irKTypeImpl(
            kClassifier: IrStaticallyInitializedValue,
            irTypeArguments: List<IrTypeArgument>,
            isMarkedNullable: Boolean,
            leaveReifiedForLater: Boolean,
            seenTypeParameters: MutableSet<IrTypeParameter>
    ): IrStaticallyInitializedValue = irStaticallyInitializedObject(symbols.kTypeImpl.owner.defaultType, mapOf(
            "classifier" to kClassifier,
            "arguments" to irKTypeProjectionsList(irTypeArguments, leaveReifiedForLater, seenTypeParameters),
            "isMarkedNullable" to irStaticallyInitializedConstant(irBoolean(isMarkedNullable)),
    ))

    private fun IrBuilderWithScope.irKClass(symbol: IrClassSymbol) = irKClass(this@KTypeGenerator.context, symbol)

    private fun IrBuilderWithScope.irKTypeParameter(
            typeParameter: IrTypeParameter,
            leaveReifiedForLater: Boolean,
            seenTypeParameters: MutableSet<IrTypeParameter>
    ): IrStaticallyInitializedValue {
        if (!seenTypeParameters.add(typeParameter))
            throw RecursiveBoundsException("Non-reified type parameters with recursive bounds are not supported yet: ${typeParameter.render()}")
        val result = irStaticallyInitializedObject(symbols.kTypeParameterImpl.defaultType, mapOf(
                "name" to irStaticString(typeParameter.name.asString()),
                "containerFqName" to irStaticString(typeParameter.parentUniqueName),
                "upperBounds" to irKTypeList(typeParameter.superTypes, leaveReifiedForLater, seenTypeParameters),
                "varianceId" to irStaticInt(mapVariance(typeParameter.variance)),
                "isReified" to irStaticBoolean(typeParameter.isReified),
        ))
        seenTypeParameters.remove(typeParameter)
        return result
    }

    private val IrTypeParameter.parentUniqueName
        get() = when (val parent = parent) {
            is IrFunction -> parent.computeFullName()
            else -> parent.fqNameForIrSerialization.asString()
        }

    private fun IrBuilderWithScope.irKTypeList(
            types: List<IrType>,
            leaveReifiedForLater: Boolean,
            seenTypeParameters: MutableSet<IrTypeParameter>
    ): IrStaticallyInitializedValue {
        val itemType = symbols.kType.defaultType
        val elements = irStaticallyInitializedArray(symbols.array.typeWith(itemType),
                types.map { irKType(it, leaveReifiedForLater, seenTypeParameters) }
        )
        return irStaticallyInitializedObject(symbols.arrayAsList.typeWith(itemType), mapOf(
                "array" to elements
        ))
    }

    // this constants are copypasted from KVarianceMapper.Companion in KTypeImpl.kt
    private fun mapVariance(variance: Variance) = when (variance) {
        Variance.INVARIANT -> 0
        Variance.IN_VARIANCE -> 1
        Variance.OUT_VARIANCE -> 2
    }

    private fun IrBuilderWithScope.irKTypeProjectionsList(
            irTypeArguments: List<IrTypeArgument>,
            leaveReifiedForLater: Boolean,
            seenTypeParameters: MutableSet<IrTypeParameter>
    ): IrStaticallyInitializedValue {
        val variance = irStaticallyInitializedArray(
                symbols.intArrayType,
                irTypeArguments.map { argument ->
                    when (argument) {
                        is IrStarProjection -> irStaticInt(-1)
                        is IrTypeProjection -> irStaticInt(mapVariance(argument.variance))
                        else -> error("Unexpected IrTypeArgument: $argument (${argument::class})")
                    }
                })
        val type = irStaticallyInitializedArray(
                symbols.array.typeWith(symbols.kType.defaultType.makeNullable()),
                irTypeArguments.map { argument ->
                    when (argument) {
                        is IrStarProjection -> irStaticallyInitializedConstant(irNull())
                        is IrTypeProjection -> irKType(argument.type, leaveReifiedForLater, seenTypeParameters)
                        else -> error("Unexpected IrTypeArgument: $argument (${argument::class})")
                    }
                })
        return irStaticallyInitializedObject(
                symbols.kTypeProjectionList.defaultType,
                mapOf(
                        "variance" to variance,
                        "type" to type
                ))
    }
}

internal fun IrBuilderWithScope.irKClass(context: KonanBackendContext, symbol: IrClassSymbol): IrStaticallyInitializedValue {
    val symbols = context.ir.symbols
    return when {
        symbol.descriptor.isObjCClass() ->
            irKClassUnsupported(context, "KClass for Objective-C classes is not supported yet")

        symbol.descriptor.getAllSuperClassifiers().any {
            it is ClassDescriptor && it.fqNameUnsafe == InteropFqNames.nativePointed
        } -> irKClassUnsupported(context, "KClass for interop types is not supported yet")

        else -> irStaticallyInitializedObject(symbols.kClassImpl.defaultType, mapOf(
                "typeInfo" to irStaticallyInitializedIntrinsic(irCall(symbols.getClassTypeInfo, listOf(symbol.typeWithStarProjections)))
        ))
    }
}

private fun IrBuilderWithScope.irKClassUnsupported(context: KonanBackendContext, message: String) =
        irStaticallyInitializedObject(context.ir.symbols.kClassUnsupportedImpl.defaultType, mapOf(
                "message" to irStaticString(message)
        ))
