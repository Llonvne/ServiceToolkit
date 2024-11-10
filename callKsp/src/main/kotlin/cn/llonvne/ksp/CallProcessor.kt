package cn.llonvne.ksp

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.writeTo

class CallProcessor(val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val clientFileSpec = FileSpec.builder("cn.llonvne", "api")
        .addImport("org.http4k.core", "Request")
        .addImport("org.http4k.core.Method", "GET", "POST")
        .addImport("org.http4k.core", "Body")
        .addImport("org.http4k.format.Jackson", "auto")

    override fun finish() {
        clientFileSpec.build().writeTo(environment.codeGenerator, true)
    }

    private val typeResolver = ApiTypeResolver(environment)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val types = resolver.getSymbolsWithAnnotation("cn.llonvne.GenerateCallerProxy")
            .flatMap { it.annotations }
            .filter {
                it.shortName.asString() == "GenerateCallerProxy"
            }.map {
                it.arguments[0].value as KSType
            }.distinctBy { it.declaration.qualifiedName?.asString() }
            .map {
                clientFileSpec.addType(typeResolver.resolve(it))
            }
            .toList()


        return emptyList()
    }
}