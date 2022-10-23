package com.github.kotlin_di.annotation_processor.files

import com.github.kotlin_di.common.plugins.IPlugin
import com.github.kotlin_di.ioc.IoC
import com.squareup.kotlinpoet.*

class MainFile(packageName: String, private val className: String) : IFile(packageName, className) {

    private val importList = mutableListOf(
        ClassName("com.github.kotlin_di", "resolve"),
        ClassName("com.github.kotlin_di.common.types", "by"),
        ClassName("com.github.kotlin_di.common.command", "Command"),
        IoC::class.asClassName()
    )

    private var loadScript = ""

    fun addDependencyClass(key: Pair<ClassName, String>, className: ClassName) {
        importList.add(className)
        importList.add(key.first)
        loadScript += "resolve(IoC.REGISTER,${key.first.simpleName}.${key.second} by ${className.simpleName}())()\n"
    }

    fun addDependencyFun(key: Pair<ClassName, String>, fName: ClassName) {
        importList.add(fName)
        importList.add(key.first)
        loadScript += "resolve(IoC.REGISTER,${key.first.simpleName}.${key.second} by ::${fName.simpleName})()\n"
    }

    override fun build(file: FileSpec.Builder): FileSpec {
        return file.apply {
            importList.forEach {
                addImport(it.packageName, it.simpleName)
            }
            addType(
                TypeSpec.classBuilder(className).apply {
                    addSuperinterface(IPlugin::class)
                    addFunction(
                        FunSpec.builder("load").apply {
                            addModifiers(KModifier.OVERRIDE)
                            addStatement(loadScript)
                        }.build()
                    )
                }.build()
            )
        }.build()
    }
}
