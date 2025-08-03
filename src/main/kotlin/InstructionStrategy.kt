interface InstructionStrategy {
    val opcode: Int
    fun execute(cpu: CPU, byte1: UByte, byte2: UByte)
}