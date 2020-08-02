package fwrap

import io.github.staakk.fwrap.FunctionInvocation
import io.github.staakk.fwrap.FunctionWrap
import io.github.staakk.fwrap.Wrap
import io.github.staakk.fwrap.FunctionWrapProviderRegistry
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.js.descriptorUtils.nameIfStandardType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import org.jetbrains.kotlin.types.typeUtil.isUnit
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

private val RETURN_OPCODES = arrayOf(
        Opcodes.RETURN,
        Opcodes.ARETURN,
        Opcodes.IRETURN,
        Opcodes.FRETURN,
        Opcodes.LRETURN,
        Opcodes.DRETURN
)

private val LOAD_OPCODES = mapOf(
        "Boolean" to Opcodes.ILOAD,
        "Byte" to Opcodes.ILOAD,
        "Short" to Opcodes.ILOAD,
        "Int" to Opcodes.ILOAD,
        "Long" to Opcodes.LLOAD,
        "Float" to Opcodes.FLOAD,
        "Double" to Opcodes.DLOAD
)

private fun getLoadOpcode(type: KotlinType?) = when {
    type == null -> Opcodes.ALOAD
    type.isPrimitiveNumberType() -> LOAD_OPCODES[type.toString()] ?: Opcodes.ALOAD
    else -> Opcodes.ALOAD
}

private val STORE_OPCODES = mapOf(
        "Boolean" to Opcodes.ISTORE,
        "Byte" to Opcodes.ISTORE,
        "Short" to Opcodes.ISTORE,
        "Int" to Opcodes.ISTORE,
        "Long" to Opcodes.LSTORE,
        "Float" to Opcodes.FSTORE,
        "Double" to Opcodes.DSTORE
)

private fun getStoreOpcode(type: KotlinType?) = when {
    type == null -> Opcodes.ASTORE
    type.isPrimitiveNumberType() -> STORE_OPCODES[type.toString()] ?: Opcodes.ASTORE
    else -> Opcodes.ASTORE
}

private val TYPE_OBJECT = Type.getType(Object::class.java)
private val TYPE_MAP = Type.getType(Map::class.java)
private val TYPE_HASHMAP = Type.getType(HashMap::class.java)
private val TYPE_STRING = Type.getType(String::class.java)
private val TYPE_FUNCTION_INVOCATION = Type.getType(FunctionInvocation::class.java)
private val TYPE_FUNCTION_WRAP = Type.getType(FunctionWrap::class.java)
private val TYPE_FUNCTION_WRAP_FACTORY_REGISTRY = Type.getType(FunctionWrapProviderRegistry::class.java)

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

        if (function.kind == CallableMemberDescriptor.Kind.SYNTHESIZED) {
            return original
        }

        val wrapsIds = annotation
                .allValueArguments[Name.identifier("ids")]
                ?.value
                ?.cast<ArrayList<StringValue>>()
                ?.map { it.value }
                ?: return original

        return FWrapMethodVisitor(original, function, wrapsIds)
    }
}

private class FWrapMethodVisitor(
        original: MethodVisitor,
        private val function: FunctionDescriptor,
        private val wrapsIds: List<String>
) : MethodVisitor(Opcodes.ASM7, original) {
    override fun visitCode() {
        super.visitCode()
        InstructionAdapter(this).onEnterFunction(function, wrapsIds)
    }

    override fun visitInsn(opcode: Int) {
        if (RETURN_OPCODES.contains(opcode) || opcode == Opcodes.ATHROW)
            InstructionAdapter(this).onExitFunction(function, wrapsIds)
        super.visitInsn(opcode)
    }
}

private fun InstructionAdapter.onEnterFunction(function: FunctionDescriptor, wrapsIds: List<String>) {
    val firstUnusedIndex = getFirstUnusedIndex(function)

    createParamMap()
    val paramMapIdx = firstUnusedIndex + 1
    store(paramMapIdx, TYPE_MAP)
    putFunctionParamsInMap(function, paramMapIdx)

    val wrapsLocalIdxStart = paramMapIdx + 1
    wrapsIds.forEachIndexed { index, wrapId ->
        loadWrap(wrapId)
        // Duplicate wrap reference to use on exit
        dup()
        store(wrapsLocalIdxStart + index, TYPE_FUNCTION_WRAP)
    }

    val functionInvocationIdx = wrapsLocalIdxStart + wrapsIds.size + 1
    createFunctionInvocation(function, paramMapIdx)
    store(functionInvocationIdx, TYPE_FUNCTION_INVOCATION)

    wrapsIds.forEachIndexed { index, _ ->
        load(wrapsLocalIdxStart + index, TYPE_FUNCTION_WRAP)
        load(functionInvocationIdx, TYPE_FUNCTION_INVOCATION)
        // Call FunctionWrap#before
        invokeinterface(TYPE_FUNCTION_WRAP.internalName, "before", "(L${TYPE_FUNCTION_INVOCATION.internalName};)V")
    }
}

private fun InstructionAdapter.onExitFunction(function: FunctionDescriptor, wrapsIds: List<String>) {
    val isVoid = function.returnType == null || function.returnType!!.isUnit()
    val typeName = function.returnType?.nameIfStandardType
    val firstUnusedIndex = getFirstUnusedIndex(function)
    val wrapLocalIdxStart = firstUnusedIndex + 2

    if (!isVoid) {
        // Store return value and put it back on stack
        visitVarInsn(getStoreOpcode(function.returnType), firstUnusedIndex)
        wrapsIds.forEachIndexed { index, _ ->
            // Get wrap reference from stack and store it
            visitVarInsn(Opcodes.ASTORE, wrapLocalIdxStart + index)
        }
        // Put return value back on stack
        visitVarInsn(getLoadOpcode(function.returnType), firstUnusedIndex)
    }

    wrapsIds.forEachIndexed { index, _ ->
        // Load wrap on stack
        visitVarInsn(Opcodes.ALOAD, wrapLocalIdxStart + index)

        if (isVoid) {
            visitLdcInsn("Unit")
        } else {
            // Put FunctionWrap#after argument on stack
            visitVarInsn(getLoadOpcode(function.returnType), firstUnusedIndex)
            if (function.returnType!!.isPrimitiveNumberType()) {
                primitiveTypeToObject(typeName)
            }
        }
        invokeinterface(TYPE_FUNCTION_WRAP.internalName, "after", "(L${TYPE_OBJECT.internalName};)V")
    }
}

private fun getFirstUnusedIndex(function: FunctionDescriptor) =
        function.valueParameters
                .map { getTypeLength(it.type) }
                .sum() + 1

private fun getTypeLength(type: KotlinType?) = when {
    type == null -> 1
    type.isPrimitiveNumberType() && (type.toString().let { it == "Double" || it == "Long" }) -> 2
    else -> 1
}

private fun InstructionAdapter.primitiveTypeToObject(typeName: Name?) {
    when (typeName.toString()) {
        "Boolean" -> invokestatic("java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false)
        "Byte" -> invokestatic("java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false)
        "Char" -> invokestatic("java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false)
        "Short" -> invokestatic("java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false)
        "Int" -> invokestatic("java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false)
        "Long" -> invokestatic("java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false)
        "Float" -> invokestatic("java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false)
        "Double" -> invokestatic("java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false)
    }
}

private fun InstructionAdapter.invokeMapPut() = invokeinterface(
        TYPE_MAP.internalName,
        "put",
        "(L${TYPE_OBJECT.internalName};L${TYPE_OBJECT.internalName};)L${TYPE_OBJECT.internalName};"
)

private fun InstructionAdapter.loadWrap(wrapId: String) {
    getstatic(TYPE_FUNCTION_WRAP_FACTORY_REGISTRY.internalName, "INSTANCE", "L${TYPE_FUNCTION_WRAP_FACTORY_REGISTRY.internalName};")
    visitLdcInsn(wrapId)
    invokevirtual(TYPE_FUNCTION_WRAP_FACTORY_REGISTRY.internalName, "get", "(L${TYPE_STRING.internalName};)L${TYPE_FUNCTION_WRAP.internalName};", false)
    checkcast(TYPE_FUNCTION_WRAP)
}

private fun InstructionAdapter.putFunctionParamsInMap(function: FunctionDescriptor, paramMapIdx: Int) {
    var currentOffset = if (function.isStatic()) 0 else 1
    function.valueParameters.forEach { param ->
        val typeName = param.type.unwrap().nameIfStandardType

        load(paramMapIdx, TYPE_MAP)
        checkcast(TYPE_MAP)

        visitLdcInsn(param.name.toString())
        visitVarInsn(getLoadOpcode(param.type), currentOffset)
        if (param.type.isPrimitiveNumberType()) {
            primitiveTypeToObject(typeName)
        }

        invokeMapPut()
        pop()

        currentOffset += getTypeLength(param.type)
    }
}

private fun InstructionAdapter.createFunctionInvocation(function: FunctionDescriptor, paramMapIdx: Int) {
    anew(TYPE_FUNCTION_INVOCATION)
    dup()
    // 1st arg - function name
    visitLdcInsn(function.name.toString())
    // 2nd arg - receiver
    if (function.isStatic()) {
        aconst(null)
    } else {
        load(0, TYPE_OBJECT)
    }
    // 3rd arg - params map
    load(paramMapIdx, TYPE_MAP)
    checkcast(TYPE_MAP)
    invokespecial(TYPE_FUNCTION_INVOCATION.internalName, "<init>", "(L${TYPE_STRING.internalName};L${TYPE_OBJECT.internalName};L${TYPE_MAP.internalName};)V", false)
}

private fun InstructionAdapter.createParamMap() {
    anew(TYPE_HASHMAP)
    dup()
    invokespecial(TYPE_HASHMAP.internalName, "<init>", "()V", false)
}

private fun FunctionDescriptor.isStatic() =
        containingDeclaration.toString().startsWith("package")