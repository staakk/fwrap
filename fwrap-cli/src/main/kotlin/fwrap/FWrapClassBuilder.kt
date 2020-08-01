package fwrap

import io.github.staakk.fwrap.FunctionWrap
import io.github.staakk.fwrap.Wrap
import io.github.staakk.fwrap.WrapRegistry
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import java.lang.reflect.Type

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

/*
       LINENUMBER 9 L0
    GETSTATIC io/github/staakk/fwrap/WrapRegistry.INSTANCE : Lio/github/staakk/fwrap/WrapRegistry;
    INVOKEVIRTUAL io/github/staakk/fwrap/WrapRegistry.getWraps ()Ljava/util/Map;
    LDC "testId"
    INVOKEINTERFACE java/util/Map.get (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    DUP
    IFNONNULL L1
    INVOKESTATIC kotlin/jvm/internal/Intrinsics.throwNpe ()V
   L1
    CHECKCAST io/github/staakk/fwrap/FunctionWrap
    INVOKEINTERFACE io/github/staakk/fwrap/FunctionWrap.before ()V (itf)
   L2

 */

private fun InstructionAdapter.onEnterFunction(function: FunctionDescriptor, wrapperId: String) {
    getstatic("io/github/staakk/fwrap/WrapRegistry", "INSTANCE", "Lio/github/staakk/fwrap/WrapRegistry;")
    invokevirtual("io/github/staakk/fwrap/WrapRegistry", "getWraps", "()Ljava/util/Map;", false)
    visitLdcInsn(wrapperId)
    invokeinterface("java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;")
    checkcast(org.jetbrains.org.objectweb.asm.Type.getType(FunctionWrap::class.java))
    invokeinterface("io/github/staakk/fwrap/FunctionWrap", "before", "()V")
}

/*
    LINENUMBER 10 L1
    GETSTATIC io/github/staakk/fwrap/WrapRegistry.INSTANCE : Lio/github/staakk/fwrap/WrapRegistry;
    INVOKEVIRTUAL io/github/staakk/fwrap/WrapRegistry.getWraps ()Ljava/util/Map;
    LDC "testId"
    INVOKEINTERFACE java/util/Map.get (Ljava/lang/Object;)Ljava/lang/Object; (itf)
    DUP
    IFNONNULL L2
    INVOKESTATIC kotlin/jvm/internal/Intrinsics.throwNpe ()V
   L2
    CHECKCAST io/github/staakk/fwrap/FunctionWrap
    INVOKEINTERFACE io/github/staakk/fwrap/FunctionWrap.after ()V (itf)
   L3

 */
private fun InstructionAdapter.onExitFunction(function: FunctionDescriptor, wrapperId: String) {
    getstatic("io/github/staakk/fwrap/WrapRegistry", "INSTANCE", "Lio/github/staakk/fwrap/WrapRegistry;")
    invokevirtual("io/github/staakk/fwrap/WrapRegistry", "getWraps", "()Ljava/util/Map;", false)
    visitLdcInsn(wrapperId)
    invokeinterface("java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;")
    checkcast(org.jetbrains.org.objectweb.asm.Type.getType(FunctionWrap::class.java))
    invokeinterface("io/github/staakk/fwrap/FunctionWrap", "after", "()V")
}