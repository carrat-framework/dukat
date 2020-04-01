package org.jetbrains.dukat.commonLowerings

import org.jetbrains.dukat.astCommon.leftMost
import org.jetbrains.dukat.astCommon.rightMost
import org.jetbrains.dukat.astModel.ModuleModel
import org.jetbrains.dukat.astModel.SourceSetModel
import org.jetbrains.dukat.astModel.transform
import org.jetbrains.dukat.stdlib.KotlinStdlibEntities
import org.jetbrains.dukat.stdlib.TSLIBROOT

private fun ModuleModel.filterOutKotlinStdlibEntities(): ModuleModel {
    val declarationsResolved = declarations.filter {
        if (it.name.leftMost() == TSLIBROOT) {
            !KotlinStdlibEntities.contains(it.name.rightMost())
        } else {
            !KotlinStdlibEntities.contains(it.name)
        }
    }
    return copy(declarations = declarationsResolved, submodules = submodules.map { it.filterOutKotlinStdlibEntities() })
}

fun SourceSetModel.filterOutKotlinStdlibEntities(): SourceSetModel {
    return transform { it.filterOutKotlinStdlibEntities() }
}