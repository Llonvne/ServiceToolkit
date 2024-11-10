package cn.llonvne.ksp

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName

class ApiTypeResolver(private val env: SymbolProcessorEnvironment) {
    private val notIncludedFunctions = setOf("toString", "hashCode", "equals")

    private val httpHandlerType = ClassName.bestGuess("org.http4k.client.DualSyncAsyncHttpHandler")

    private val metaType = ClassName.bestGuess("cn.llonvne.ApiMeta")

    fun resolve(type: KSType): TypeSpec {

        val clsName = type.declaration.simpleName.asString() + "Proxy"

        val generated =
            TypeSpec.classBuilder(clsName)
                .addSuperinterface(type.toClassName())
                .addFunctions(implementMethods(type))
                .addProperty(
                    PropertySpec.builder("handler", httpHandlerType)
                        .initializer("handler")
                        .build()
                )
                .addProperty(
                    PropertySpec.builder("meta", metaType).initializer("meta").build()
                )
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter(
                            ParameterSpec.builder("handler", httpHandlerType)
                                .build()
                        )
                        .addParameter(
                            ParameterSpec.builder("meta", metaType).build()
                        )
                        .build()
                )
                .build()

        return generated
    }

    private fun implementMethods(type: KSType): List<FunSpec> {
        val clsDeclaration = type.declaration as KSClassDeclaration
        return clsDeclaration
            .getAllFunctions()
            .filter { it.simpleName.asString() !in notIncludedFunctions }
            .map { implementMethod(type, it) }
            .toList()
    }

    private fun implementMethod(
        type: KSType,
        ksFunctionDeclaration: KSFunctionDeclaration
    ): FunSpec {
        return FunSpec.builder(ksFunctionDeclaration.simpleName.asString())
            .addModifiers(KModifier.OVERRIDE)
            .returns(ksFunctionDeclaration.returnType?.resolve()?.toClassName()!!)
            .addParameters(toParameterSpec(ksFunctionDeclaration.parameters))
            .addCode(implementMethodBody(type, ksFunctionDeclaration))
            .build()
    }

    private fun implementMethodBody(
        type: KSType,
        ksFunctionDeclaration: KSFunctionDeclaration
    ): CodeBlock {
        val returnType: KSType =
            ksFunctionDeclaration.returnType?.resolve() ?: throw RuntimeException("returnType cannot be null")
        val returnTypeFqName = returnType.declaration.qualifiedName?.asString()
            ?: throw RuntimeException("returnType fqname cannot be null")

        val handlerCode = implementHandlerRequest(type, ksFunctionDeclaration)

        return if (returnTypeFqName != "kotlin.Unit") {
            CodeBlock.builder().addStatement("return %L", implementBodyLens(returnType, handlerCode)).build()
        } else {
            handlerCode
        }
    }

    private fun implementHandlerRequest(type: KSType, ksFunctionDeclaration: KSFunctionDeclaration): CodeBlock {
        return CodeBlock.builder()
            .addStatement("handler(Request(POST,meta.uri(%S)))", ksFunctionDeclaration.simpleName.asString())
            .build()
    }

    private fun implementBodyLens(retType: KSType, respCode: CodeBlock): CodeBlock {
        return CodeBlock.builder()
            .add("Body.auto<%T>().toLens()(%L)", retType.toClassName(), respCode)
            .build()
    }


    private fun toParameterSpec(parameters: List<KSValueParameter>): List<ParameterSpec> {
        return parameters.map {
            ParameterSpec.builder(it.name?.getShortName()!!, it.type.resolve().toClassName())
                .build()
        }
    }
}
