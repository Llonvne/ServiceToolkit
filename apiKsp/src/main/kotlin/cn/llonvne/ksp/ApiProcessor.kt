package cn.llonvne.ksp

import cn.llonvne.ServiceImpl
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.Import
import com.squareup.kotlinpoet.ksp.writeTo

class ApiProcessor(
    private val environment: SymbolProcessorEnvironment
) : SymbolProcessor {

    private val handlerFileSpec = FileSpec.builder("cn.llonvne", "services")
        .addImport("org.http4k.routing", "bind")
        .addImport("org.http4k.core", "Body", "with")
        .addImport("org.http4k.format.Jackson", "auto")


    override fun finish() {
        handlerFileSpec.build().writeTo(environment.codeGenerator, true)
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val ksAnnotated = resolver.getSymbolsWithAnnotation(ServiceImpl::class.qualifiedName!!).toList()

        for (annotated in ksAnnotated) {
            if (annotated !is KSClassDeclaration) {
                environment.logger.error("should be a class", annotated)
            } else {
                ApiHandlerClassBuilder(resolver, environment, annotated).build().let {
                    handlerFileSpec.addType(it)
                }
            }
        }

        return listOf()
    }
}