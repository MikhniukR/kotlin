/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api.fir.components

import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.typeContext
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.idea.fir.low.level.api.api.getOrBuildFir
import org.jetbrains.kotlin.idea.fir.low.level.api.api.getOrBuildFirOfType
import org.jetbrains.kotlin.idea.fir.low.level.api.api.throwUnexpectedFirElementError
import org.jetbrains.kotlin.idea.frontend.api.components.KtBuiltinTypes
import org.jetbrains.kotlin.idea.frontend.api.components.KtTypeProvider
import org.jetbrains.kotlin.idea.frontend.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.idea.frontend.api.fir.symbols.KtFirNamedClassOrObjectSymbol
import org.jetbrains.kotlin.idea.frontend.api.fir.types.KtFirType
import org.jetbrains.kotlin.idea.frontend.api.fir.types.PublicTypeApproximator
import org.jetbrains.kotlin.idea.frontend.api.fir.utils.toConeNullability
import org.jetbrains.kotlin.idea.frontend.api.symbols.KtNamedClassOrObjectSymbol
import org.jetbrains.kotlin.idea.frontend.api.tokens.ValidityToken
import org.jetbrains.kotlin.idea.frontend.api.types.KtType
import org.jetbrains.kotlin.idea.frontend.api.types.KtTypeNullability
import org.jetbrains.kotlin.idea.frontend.api.withValidityAssertion
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.KtDoubleColonExpression
import org.jetbrains.kotlin.psi.KtTypeReference

internal class KtFirTypeProvider(
    override val analysisSession: KtFirAnalysisSession,
    override val token: ValidityToken,
) : KtTypeProvider(), KtFirAnalysisSessionComponent {
    override val builtinTypes: KtBuiltinTypes = KtFirBuiltInTypes(rootModuleSession.builtinTypes, firSymbolBuilder, token)

    override fun approximateToSuperPublicDenotableType(type: KtType): KtType? {
        require(type is KtFirType)
        val coneType = type.coneType
        val approximatedConeType = PublicTypeApproximator.approximateTypeToPublicDenotable(
            coneType,
            rootModuleSession
        )

        return approximatedConeType?.asKtType()
    }

    override fun buildSelfClassType(symbol: KtNamedClassOrObjectSymbol): KtType {
        require(symbol is KtFirNamedClassOrObjectSymbol)
        val type = symbol.firRef.withFir(FirResolvePhase.SUPER_TYPES) { firClass ->
            ConeClassLikeTypeImpl(
                firClass.symbol.toLookupTag(),
                firClass.typeParameters.map { ConeTypeParameterTypeImpl(it.symbol.toLookupTag(), isNullable = false) }.toTypedArray(),
                isNullable = false
            )
        }
        return type.asKtType()
    }

    override fun commonSuperType(types: Collection<KtType>): KtType? {
        return analysisSession.rootModuleSession.typeContext
            .commonSuperTypeOrNull(types.map { it.coneType })
            ?.asKtType()
    }

    override fun getKtType(ktTypeReference: KtTypeReference): KtType = withValidityAssertion {
        ktTypeReference.getOrBuildFirOfType<FirResolvedTypeRef>(firResolveState).coneType.asKtType()
    }

    override fun getReceiverTypeForDoubleColonExpression(expression: KtDoubleColonExpression): KtType? = withValidityAssertion {
        when (val fir = expression.getOrBuildFir(firResolveState)) {
            is FirGetClassCall ->
                fir.typeRef.coneType.getReceiverOfReflectionType()?.asKtType()
            is FirCallableReferenceAccess ->
                fir.typeRef.coneType.getReceiverOfReflectionType()?.asKtType()
            else -> throwUnexpectedFirElementError(fir, expression)
        }
    }

    private fun ConeKotlinType.getReceiverOfReflectionType(): ConeKotlinType? {
        if (this !is ConeClassLikeType) return null
        if (lookupTag.classId.packageFqName != StandardClassIds.BASE_REFLECT_PACKAGE) return null
        return typeArguments.firstOrNull()?.type
    }

    override fun withNullability(type: KtType, newNullability: KtTypeNullability): KtType {
        require(type is KtFirType)
        return type.coneType.withNullability(newNullability.toConeNullability(), rootModuleSession.typeContext).asKtType()
    }
}

