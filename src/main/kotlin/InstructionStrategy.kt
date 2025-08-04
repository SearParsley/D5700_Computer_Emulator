interface InstructionStrategy {
    val opcode: Int
    val mnemonic: String
    val registers: IRegisters
    fun execute(cpu: CPU, byte1: UByte, byte2: UByte)
}