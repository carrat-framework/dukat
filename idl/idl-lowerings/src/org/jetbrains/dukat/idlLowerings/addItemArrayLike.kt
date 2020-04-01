package org.jetbrains.dukat.idlLowerings

import org.jetbrains.dukat.astCommon.toNameEntity
import org.jetbrains.dukat.idlDeclarations.IDLArgumentDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLAttributeDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLFileDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLGetterDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLInterfaceDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLOperationDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLSimpleExtendedAttributeDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLSingleTypeDeclaration
import org.jetbrains.dukat.idlDeclarations.IDLSourceSetDeclaration
import org.jetbrains.dukat.idlDeclarations.InterfaceKind
import org.jetbrains.dukat.idlDeclarations.toNotNullable
import org.jetbrains.dukat.stdlib.TSLIBROOT

private class ItemArrayLikeLowering : IDLLowering {

    override fun lowerInterfaceDeclaration(declaration: IDLInterfaceDeclaration, owner: IDLFileDeclaration): IDLInterfaceDeclaration {
        val lengthAttribute = declaration.attributes.find {
            it.name == "length" && it.type == IDLSingleTypeDeclaration("unsignedlong", null, false)
        }
        val itemGetter = declaration.getters.find {
            it.name == "item" &&
                    it.key.type == IDLSingleTypeDeclaration("unsignedlong", null, false)
        }
        if (lengthAttribute != null && itemGetter != null) {
            return declaration.copy(parents = declaration.parents + IDLSingleTypeDeclaration(
                    name = "ItemArrayLike",
                    typeParameter = itemGetter.valueType.toNotNullable(),
                    nullable = false
            ))
        }
        return declaration
    }

    override fun lowerSourceSetDeclaration(sourceSet: IDLSourceSetDeclaration): IDLSourceSetDeclaration {
        val newSourceSet = super.lowerSourceSetDeclaration(sourceSet)
        val newFiles = newSourceSet.files.toMutableList()
        val itemArrayLikeDeclaration = IDLInterfaceDeclaration(
                name = "ItemArrayLike",
                attributes = listOf(IDLAttributeDeclaration(
                        name = "length",
                        type = IDLSingleTypeDeclaration("unsignedlong", null, false),
                        static = false,
                        readOnly = true,
                        override = false
                )),
                operations = listOf(),
                primaryConstructor = null,
                constructors = listOf(),
                parents = listOf(),
                unions = listOf(),
                extendedAttributes = listOf(
                        IDLSimpleExtendedAttributeDeclaration("NoInterfaceObject")
                ),
                getters = listOf(IDLGetterDeclaration(
                        name = "item",
                        valueType = IDLSingleTypeDeclaration("any", null, true),
                        key = IDLArgumentDeclaration(
                                name = "index",
                                type = IDLSingleTypeDeclaration("unsignedlong", null, false),
                                defaultValue = null,
                                optional = false,
                                variadic = false
                        )
                )),
                setters = listOf(),
                callback = false,
                generated = true,
                partial = false,
                mixin = false,
                kind = InterfaceKind.INTERFACE
        )
        if (newFiles.none { it.packageName == libRootPackageName.toNameEntity() }) {
            newFiles += IDLFileDeclaration(
                    fileName = libRootPackageName,
                    declarations = listOf(),
                    referencedFiles = listOf(),
                    packageName = libRootPackageName.toNameEntity()
            )
        }
        return newSourceSet.copy(
                files = newFiles.map {
                    if (it.packageName == libRootPackageName.toNameEntity()) {
                        it.copy(declarations = it.declarations + itemArrayLikeDeclaration)
                    } else {
                        it
                    }
                }
        )
    }

    companion object {
        private val libRootPackageName = "${TSLIBROOT.value}.org.w3c.dom"
    }
}

fun IDLSourceSetDeclaration.addItemArrayLike(): IDLSourceSetDeclaration {
    return ItemArrayLikeLowering().lowerSourceSetDeclaration(this)
}