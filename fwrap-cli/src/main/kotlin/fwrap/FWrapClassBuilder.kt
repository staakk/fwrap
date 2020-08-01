package fwrap

import io.github.staakk.fwrap.FunctionInvocation
import io.github.staakk.fwrap.FunctionWrap
import io.github.staakk.fwrap.Wrap
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

class FWrapClassBuilder(
        delegateBuilder: ClassBuilder
) : DelegatingClassBuilder(delegateBuilder) {

    override fun newMethod(
            origin: JvmDeclarationOrigin,
            access: Int,
            name: String,
            desc: String,
            signature: String?,
            exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        val function = origin.descriptor as? FunctionDescriptor ?: return original
        val annotation = function.annotations.findAnnotation(FqName(Wrap::class.qualifiedName!!))
                ?: return original

        val wrapperId = annotation
                .allValueArguments[Name.identifier("id")]
                ?.stringTemplateValue()
                ?: return original

        return object : MethodVisitor(Opcodes.ASM7, original) {

            override fun visitCode() {
                super.visitCode()
                InstructionAdapter(this).onEnterFunction(function, wrapperId)
            }

            override fun visitInsn(opcode: Int) {
                when (opcode) {
                    Opcodes.RETURN,
                    Opcodes.ARETURN,
                    Opcodes.IRETURN,
                    Opcodes.FRETURN,
                    Opcodes.LRETURN,
                    Opcodes.DRETURN -> InstructionAdapter(this).onExitFunction(function, wrapperId)
                }
                super.visitInsn(opcode)
            }
        }
    }
}

private fun InstructionAdapter.onEnterFunction(function: FunctionDescriptor, wrapperId: String) {
    val firstUnusedIndex = firstUnusedIndex(function)

    // Create map
    anew(Type.getType(HashMap::class.java))
    dup()
    invokespecial("java/util/HashMap", "<init>", "()V", false)
    val paramMapIdx = firstUnusedIndex + 1
    store(paramMapIdx, Type.getType(HashMap::class.java))

    // Put params into map
    var currentOffset = 1
    function.valueParameters.forEach { param ->
        val typeName = param.type.unwrap().nameIfStandardType.toString()

        load(paramMapIdx, Type.getType(HashMap::class.java))
        checkcast(Type.getType(HashMap::class.java))
        visitLdcInsn(param.name.toString())

        val loadOp = when (typeName) {
            "Boolean",
            "Byte",
            "Short",
            "Int" -> Opcodes.ILOAD
            "Long" -> Opcodes.LLOAD
            "Float" -> Opcodes.FLOAD
            "Double" -> Opcodes.DLOAD
            else -> Opcodes.ALOAD
        }

        visitVarInsn(loadOp, currentOffset)
        when (typeName) {
            "Boolean" -> invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
            "Byte" -> invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
            "Short" -> invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false)
            "Int" -> invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
            "Long" -> invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
            "Float" -> invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false)
            "Double" -> invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
        }

        invokeinterface("java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
        pop()

        currentOffset += when (typeName) {
            "Double",
            "Long"-> 2
            else -> 1
        }
    }

    getstatic("io/github/staakk/fwrap/WrapRegistry", "INSTANCE", "Lio/github/staakk/fwrap/WrapRegistry;")
    invokevirtual("io/github/staakk/fwrap/WrapRegistry", "getWraps", "()Ljava/util/Map;", false)
    visitLdcInsn(wrapperId)
    invokeinterface("java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;")
    checkcast(Type.getType(FunctionWrap::class.java))

    // Create FunctionInvocation
    anew(Type.getType(FunctionInvocation::class.java))
    dup()
    visitLdcInsn(function.name.toString())
    load(0, Type.getType(Object::class.java))
    load(paramMapIdx, Type.getType(HashMap::class.java))
    checkcast(Type.getType(Map::class.java))
    invokespecial("io/github/staakk/fwrap/FunctionInvocation", "<init>", "(Ljava/lang/String;Ljava/lang/Object;Ljava/util/Map;)V", false)

    invokeinterface("io/github/staakk/fwrap/FunctionWrap", "before", "(Lio/github/staakk/fwrap/FunctionInvocation;)V")
}

private fun InstructionAdapter.firstUnusedIndex(function: FunctionDescriptor) =
    function.valueParameters
            .map {
                when (it.type.unwrap().nameIfStandardType.toString()) {
                    "Double",
                    "Long"-> 2
                    else -> 1
                }
            }.sum() + 1

private fun InstructionAdapter.onExitFunction(function: FunctionDescriptor, wrapperId: String) {
    getstatic("io/github/staakk/fwrap/WrapRegistry", "INSTANCE", "Lio/github/staakk/fwrap/WrapRegistry;")
    invokevirtual("io/github/staakk/fwrap/WrapRegistry", "getWraps", "()Ljava/util/Map;", false)
    visitLdcInsn(wrapperId)
    invokeinterface("java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;")
    checkcast(Type.getType(FunctionWrap::class.java))
    invokeinterface("io/github/staakk/fwrap/FunctionWrap", "after", "()V")
}