import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("InstructionSet Tests")
class InstructionSetTest {

    @Test
    @DisplayName("Total number of instructions is 16")
    fun testTotalInstructionCount() {
        assertEquals(16, InstructionSet.entries.size)
    }

    @ParameterizedTest(name = "Opcode 0x{0} should map to {1}")
    @CsvSource(
        "0, STORE", "1, ADD", "2, SUB", "3, READ", "4, WRITE", "5, JUMP",
        "6, READ_KEYBOARD", "7, SWITCH_MEMORY", "8, SKIP_EQUAL", "9, SKIP_NOT_EQUAL",
        "10, SET_A", "11, SET_T", "12, READ_T", "13, CONVERT_TO_BASE_10",
        "14, CONVERT_BYTE_TO_ASCII", "15, DRAW"
    )
    @DisplayName("fromOpcode returns correct InstructionSet enum constant")
    fun testFromOpcodeValid(opcodeInt: Int, expectedMnemonic: String) {
        val instruction = InstructionSet.fromOpcode(opcodeInt)
        assertNotNull(instruction, "Instruction should not be null for opcode 0x${opcodeInt.toString(16)}")
        assertEquals(expectedMnemonic, instruction?.mnemonic)
        assertEquals(opcodeInt, instruction?.opcode)
    }

    @Test
    @DisplayName("fromOpcode returns null for invalid opcode")
    fun testFromOpcodeInvalid() {
        assertNull(InstructionSet.fromOpcode(0x10)) // Out of range (16)
        assertNull(InstructionSet.fromOpcode(-1))   // Negative
        assertNull(InstructionSet.fromOpcode(0xFF)) // Arbitrary large hex
    }

    @Test
    @DisplayName("PC increment is 2 for most instructions")
    fun testPcIncrementDefault() {
        InstructionSet.entries.filter { it != InstructionSet.JUMP }.forEach { instruction ->
            assertEquals(2, instruction.pcIncrement, "Instruction ${instruction.mnemonic} should increment PC by 2")
        }
    }

    @Test
    @DisplayName("JUMP's PC increment is 0")
    fun testJumpPcIncrement() {
        assertEquals(0, InstructionSet.JUMP.pcIncrement)
    }

    @ParameterizedTest(name = "parseRx: byte1 0x{0} -> rX {1}")
    @CsvSource(
        "00, 0", "01, 1", "07, 7", "F0, 0", "F5, 5", "A0, 0", "A7, 7"
    )
    @DisplayName("parseRx extracts correct rX index from low nibble of byte1")
    fun testParseRx(byte1Hex: String, expectedRx: Int) {
        val byte1 = byte1Hex.toUByte(16)
        // We can pick any instruction, parseRx is a static method on the enum class
        assertEquals(expectedRx, InstructionSet.STORE.parseRx(byte1))
        assertEquals(expectedRx, InstructionSet.ADD.parseRx(byte1))
    }

    @ParameterizedTest(name = "parseRy: byte2 0x{0} -> rY {1}")
    @CsvSource(
        "00, 0", "10, 1", "70, 7", "0F, 0", "1F, 1", "7F, 7"
    )
    @DisplayName("parseRy extracts correct rY index from high nibble of byte2")
    fun testParseRy(byte2Hex: String, expectedRy: Int) {
        val byte2 = byte2Hex.toUByte(16)
        assertEquals(expectedRy, InstructionSet.ADD.parseRy(byte2))
        assertEquals(expectedRy, InstructionSet.CONVERT_BYTE_TO_ASCII.parseRy(byte2))
    }

    @ParameterizedTest(name = "parseRz: byte2 0x{0} -> rZ {1}")
    @CsvSource(
        "00, 0", "01, 1", "07, 7", "F0, 0", "F5, 5", "A0, 0", "A7, 7"
    )
    @DisplayName("parseRz extracts correct rZ index from low nibble of byte2")
    fun testParseRz(byte2Hex: String, expectedRz: Int) {
        val byte2 = byte2Hex.toUByte(16)
        assertEquals(expectedRz, InstructionSet.ADD.parseRz(byte2))
        assertEquals(expectedRz, InstructionSet.DRAW.parseRz(byte2))
    }

    @ParameterizedTest(name = "parseBb: byte2 0x{0} -> bb 0x{0}")
    @CsvSource(
        "00", "0A", "FF", "55", "AA"
    )
    @DisplayName("parseBb extracts correct byte bb from byte2")
    fun testParseBb(byte2Hex: String) {
        val byte2 = byte2Hex.toUByte(16)
        assertEquals(byte2, InstructionSet.STORE.parseBb(byte2))
        assertEquals(byte2, InstructionSet.SET_T.parseBb(byte2))
    }

    @ParameterizedTest(name = "parseAaa: byte1 0x{0}, byte2 0x{1} -> aaa 0x{2}")
    @CsvSource(
        "50, 00, 0000", // JUMP 0x0000
        "51, F2, 01F2", // JUMP 0x01F2 (from spec example)
        "A0, FF, 00FF", // SET_A 0x00FF (from spec example A255)
        "A5, 5A, 055A", // SET_A 0x055A
        "5F, FF, 0FFF"  // JUMP 0x0FFF
    )
    @DisplayName("parseAaa extracts correct 16-bit address aaa from byte1 and byte2")
    fun testParseAaa(byte1Hex: String, byte2Hex: String, expectedAaaHex: String) {
        val byte1 = byte1Hex.toUByte(16)
        val byte2 = byte2Hex.toUByte(16)
        val expectedAaa = expectedAaaHex.toUShort(16)

        // Test with JUMP (opcode 0x5)
        assertEquals(expectedAaa, InstructionSet.JUMP.parseAaa(byte1, byte2))
        // Test with SET_A (opcode 0xA)
        assertEquals(expectedAaa, InstructionSet.SET_A.parseAaa(byte1, byte2))
    }
}