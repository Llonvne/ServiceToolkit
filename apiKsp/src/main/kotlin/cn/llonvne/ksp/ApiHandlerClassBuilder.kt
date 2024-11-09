package cn.llonvne.ksp

import cn.llonvne.ServiceImpl
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName

class ApiHandlerClassBuilder(
    private val resolver: Resolver,
    private val environment: SymbolProcessorEnvironment,
    private val ksClassDeclaration: KSClassDeclaration
) {
    private val api = ksClassDeclaration.annotations.toList().filter {
        it.shortName.asString() == ServiceImpl::class.simpleName
    }.toList().first().arguments[0].value as KSType

    private val clsName = ksClassDeclaration.toClassName().simpleName + "Handler"

    private val instanceName = "inner"

    private val apiHostClassRef = "cn.llonvne.type.ApiHost"

    private val httpHandlerClassRef = "org.http4k.core.HttpHandler"

    private val routesFuncRef = MemberName("org.http4k.routing", "routes")

    private val bindFuncRef = MemberName("org.http4k.routing", "bind")

    private val MethodClassRef = ClassName.bestGuess("org.http4k.core.Method")

    private val PostMemeberRef = MemberName(MethodClassRef, "POST")

    private val notIncludedFunctions = setOf("toString", "hashCode", "equals")

    private val ResponseClassRef = ClassName.bestGuess("org.http4k.core.Response")

    private val StatusClassRef = ClassName.bestGuess("org.http4k.core.Status")

    private val OkStatusRef = MemberName(StatusClassRef, "OK")

    private fun primaryConstructor() = FunSpec.constructorBuilder()
        .addParameter(instanceName, ksClassDeclaration.toClassName())
        .build()

    private fun resolveParameter(
        ksFunctionDeclaration: KSFunctionDeclaration
    ): CodeBlock {
        return CodeBlock.builder()
            .addStatement(
                "val req = Body.auto<%L>().toLens()(it);",
                requestClsName(ksFunctionDeclaration)
            )
            .build()
    }

    private fun resolveCalling(ksFunctionDeclaration: KSFunctionDeclaration): CodeBlock {
        return CodeBlock.builder()
            .addStatement(
                "val resp = inner.%L(%L);",
                ksFunctionDeclaration,
                CodeBlock.builder()
                    .add(ksFunctionDeclaration.parameters.joinToString(",") { "req." + it.name?.asString() })
                    .build()
            ).apply {
                val retType = ksFunctionDeclaration.returnType?.resolve()?.declaration?.qualifiedName?.asString()
                if (retType != null && retType != "kotlin.Unit") {
                    addStatement(
                        "%T(%M).with(Body.auto<%T>().toLens() of resp)",
                        ResponseClassRef,
                        OkStatusRef,
                        ClassName.bestGuess(retType)
                    )
                } else {
                    addStatement(
                        "%T(%M)",
                        ResponseClassRef,
                        OkStatusRef,
                    )
                }
            }
            .build()
    }

    private fun buildHandler(
        ksType: KSClassDeclaration,
        ksFunctionDeclaration: KSFunctionDeclaration
    ): CodeBlock {
        return CodeBlock.builder()
            .apply {
                if (ksFunctionDeclaration.parameters.isNotEmpty()){
                    add(resolveParameter(ksFunctionDeclaration))
                }
            }
            .add(resolveCalling(ksFunctionDeclaration))

            .build()

    }

    private fun requestClsName(ksFunctionDeclaration: KSFunctionDeclaration): String {
        return ksFunctionDeclaration.simpleName.asString() + "Request"
    }

    private fun TypeSpec.Builder.buildMapping(ksType: KSType): TypeSpec.Builder {

        addSuperinterface(ClassName.bestGuess(apiHostClassRef))

        val decl = ksType.declaration
        if (decl !is KSClassDeclaration) {
            throw RuntimeException("${decl.simpleName.asString()} should be class")
        }
        val functions = decl.getAllFunctions()
            .filter { it.simpleName.asString() !in notIncludedFunctions }

        val mapping = functions.map { fn ->
            CodeBlock.of(
                "%S bind %M to %L",
                fn, PostMemeberRef, CodeBlock.of("{ %L }", buildHandler(decl, fn))
            )
        }.joinToString(",") { it.toString() }

        functions.mapNotNull {
            if (it.parameters.isNotEmpty()) {
                buildRequestCls(it)
            } else {
                null
            }
        }.forEach { addType(it) }

        addFunction(
            FunSpec.builder("getHandler")
                .addModifiers(KModifier.OVERRIDE)
                .returns(ClassName.bestGuess(httpHandlerClassRef))
                .addCode(
                    CodeBlock.of(
                        "return %M(%L)", routesFuncRef, mapping
                    )
                )
                .build()
        )
        return this
    }

    private fun buildRequestCls(ksFunctionDeclaration: KSFunctionDeclaration): TypeSpec {
        val parameters = ksFunctionDeclaration.parameters.associate {
            it.name?.asString() to it.type
        }
        return TypeSpec.classBuilder(requestClsName(ksFunctionDeclaration)).addModifiers(KModifier.DATA)
            .apply {
                parameters.forEach { (name, type) ->
                    addProperty(
                        PropertySpec.builder(name!!, type.resolve().toClassName())
                            .initializer(name)
                            .build()
                    )
                }
                primaryConstructor(FunSpec.constructorBuilder()
                    .apply {
                        parameters.map { (name, type) ->
                            ParameterSpec.builder(name!!, type.resolve().toClassName())
                                .build()
                        }.forEach { addParameter(it) }
                    }
                    .build())
            }
            .build()
    }

    fun build(): TypeSpec {
        return TypeSpec.classBuilder(clsName)
            .primaryConstructor(primaryConstructor())
            .addProperty(
                PropertySpec.builder(instanceName, ksClassDeclaration.toClassName())
                    .initializer(instanceName)
                    .build()
            )
            .buildMapping(api)
            .build()
    }
}