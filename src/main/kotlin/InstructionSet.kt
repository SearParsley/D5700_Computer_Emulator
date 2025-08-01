enum class InstructionSet(
    val opcode: Int,
    val mnemonic: String,
    val operandFormat: String,
    val pcIncrement: Int = 2
) {
    STORE(opcode = 0x0, mnemonic = "STORE", operandFormat = "rX, bb"),

    ADD(opcode = 0x1, mnemonic = "ADD", operandFormat = "rX, rY, rZ"),

    SUB(opcode = 0x2, mnemonic = "SUB", operandFormat = "rX, rY, rZ"),

    READ(opcode = 0x3, mnemonic = "READ", operandFormat = "rX, 00"),

    WRITE(opcode = 0x4, mnemonic = "WRITE", operandFormat = "rX, 00"),

    JUMP(opcode = 0x5, mnemonic = "JUMP", operandFormat = "aaa", pcIncrement = 0),

    READ_KEYBOARD(opcode = 0x6, mnemonic = "READ_KEYBOARD", operandFormat = "rX, 00"),

    SWITCH_MEMORY(opcode = 0x7, mnemonic = "SWITCH_MEMORY", operandFormat = "000"),

    SKIP_EQUAL(opcode = 0x8, mnemonic = "SKIP_EQUAL", operandFormat = "rX, rY, 0"),

    SKIP_NOT_EQUAL(opcode = 0x9, mnemonic = "SKIP_NOT_EQUAL", operandFormat = "rX, rY, 0"),

    SET_A(opcode = 0xA, mnemonic = "SET_A", operandFormat = "aaa"),

    SET_T(opcode = 0xB, mnemonic = "SET_T", operandFormat = "bb, 0"),

    READ_T(opcode = 0xC, mnemonic = "READ_T", operandFormat = "rX, 00"),

    CONVERT_TO_BASE_10(opcode = 0xD, mnemonic = "CONVERT_TO_BASE_10", operandFormat = "rX, 00"),

    CONVERT_BYTE_TO_ASCII(opcode = 0xE, mnemonic = "CONVERT_BYTE_TO_ASCII", operandFormat = "rX, rY, 0"),

    DRAW(opcode = 0xF, mnemonic = "DRAW", operandFormat = "rX, rY, rZ");

    companion object {
        private val opcodeMap = entries.associateBy { it.opcode }

        fun fromOpcode(opcodeNibble: Int): InstructionSet? {
            return opcodeMap[opcodeNibble]
        }
    }

    fun parseRx(byte1: UByte): Int = (byte1.toInt() and 0x0F)

    fun parseRy(byte2: UByte): Int = (byte2.toInt() ushr 4) and 0x7

    fun parseRz(byte2: UByte): Int = (byte2.toInt() and 0x0F)

    fun parseBb(byte2: UByte): UByte = byte2

    fun parseAaa(byte1: UByte, byte2: UByte): UShort {
        val highByteFromByte1 = (byte1.toInt() and 0x0F)
        val lowByteFromByte2 = byte2.toInt()
        return ((highByteFromByte1 shl 8) or lowByteFromByte2).toUShort()
    }
}