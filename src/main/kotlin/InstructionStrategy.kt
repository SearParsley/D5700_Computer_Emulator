interface InstructionStrategy {
    val opcode: Int
    val mnemonic: String
    fun execute(cpu: CPU, byte1: UByte, byte2: UByte)
}