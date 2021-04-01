package com.wutsi.codegen.kotlin

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.wutsi.codegen.Context
import com.wutsi.codegen.model.Field
import com.wutsi.codegen.model.Type
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema

abstract class AbstractDtoCodeGenerator(protected val mapper: KotlinMapper) : AbstractKotlinCodeGenerator() {
    abstract fun parameterAnnotationSpecs(field: Field): List<AnnotationSpec>

    override fun generate(openAPI: OpenAPI, context: Context) {
        val models = loadModels(openAPI, context)
        models.forEach { generateModel(it, context) }
    }

    fun loadModels(spec: OpenAPI, context: Context): List<Type> {
        // Sort the key
        val stack = mutableListOf<String>()
        spec.components.schemas.map { push(it.key, it.value, stack) }

        // Load
        val result = mutableListOf<Type>()
        stack.forEach {
            val key = "#/components/schemas/$it"
            if (context.getType(key) == null) {
                val schema = spec.components.schemas[it]
                val type = mapper.toType(it, schema!!)
                result.add(type)

                context.register(key, type)
            }
        }
        spec.components.schemas.map { mapper.toType(it.key, it.value) }

        return result
    }

    private fun push(key: String, schema: Schema<*>, stack: MutableList<String>) {
        schema.properties.forEach {
            if (it.value.type == "array" && it.value.`$ref` != null)
                push(it.value.`$ref`, it.value, stack)
        }
        stack.add(0, key)
    }

    private fun generateModel(type: Type, context: Context) {
        val file = getSourceDirectory(context)
        System.out.println("Generating ${type.packageName}.${type.name} to $file")
        FileSpec.builder(type.packageName, type.name)
            .addType(toModelTypeSpec(type))
            .build()
            .writeTo(file)
    }

    fun toModelTypeSpec(type: Type): TypeSpec {
        val spec = TypeSpec.classBuilder(type.name)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameters(type.fields.map { toParameterSpec(it) })
                    .build()
            )
            .addProperties(type.fields.map { toPropertySpec(it) })
            .build()
        return spec
    }

    private fun toPropertySpec(field: Field): PropertySpec =
        PropertySpec.builder(field.name, toTypeName(field).copy(field.nullable))
            .initializer(field.name)
            .mutable(false)
            .build()

    private fun toTypeName(field: Field): TypeName {
        val type = field.type.asTypeName()
        return if (field.parametrizedType != null)
            type.parameterizedBy(ClassName(field.parametrizedType.packageName, field.parametrizedType.name))
        else
            type
    }

    fun toParameterSpec(field: Field): ParameterSpec {
        val builder = ParameterSpec.builder(field.name, toTypeName(field).copy(field.nullable))
            .addAnnotations(parameterAnnotationSpecs(field))

        val default = defaultValue(field, true)
        if (default != null)
            builder.defaultValue(default)

        return builder.build()
    }
}
