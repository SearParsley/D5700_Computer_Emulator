import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertThrows
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ProgramTerminationException(message: String) : RuntimeException(message)


@DisplayName("CPU Class Tests")
class CPUTest {

    // Dependencies
    private lateinit var timerUnit: TimerUnit
    private lateinit var cpu: CPU

    // Helper for I/O and Console Output
    private val standardOut = System.out
    private val standardIn = System.`in`
    private val outputStreamCaptor = ByteArrayOutputStream()

    // Test Memory Map Setup
    private val ROM_TEST_START = 0x0000u.toUShort()
    private val RAM_TEST_START = 0x1000u.toUShort()
    private val RAM_SIZE = 4096u.toUShort()
    private val KEYBOARD_START_ADDRESS = 0x2000u.toUShort()
    private val PC_START = (RAM_TEST_START + 0x100.toUShort()).toUShort()

    @BeforeEach
    fun setUp() {
        // Reset singletons for a clean slate before each test
        Registers.resetForTesting()
        MemoryController.resetForTesting()

        // Set up the memory controller with a base ROM and RAM
        val romData = List<UByte>(Constants.ROM_SIZE_BYTES) { 0u }.toList()
        MemoryController.initializeMemoryMap(ROM_TEST_START, romData, RAM_TEST_START, RAM_SIZE)

        // Add I/O devices to the memory map
        MemoryController.addDevice(KeyboardInputDevice(KEYBOARD_START_ADDRESS, Constants.KEYBOARD_BUFFER_SIZE_BYTES.toUShort()))
        MemoryController.addDevice(AsciiDisplayDevice(Constants.ASCII_DISPLAY_START_ADDRESS, Constants.ASCII_DISPLAY_BUFFER_SIZE_BYTES.toUShort()))

        // Create the TimerUnit and CPU instances
        timerUnit = TimerUnit()
        cpu = CPU(timerUnit)

        System.setOut(PrintStream(outputStreamCaptor)) // Redirect System.out

        Registers.P = PC_START
    }

    @AfterEach
    fun tearDown() {
        // Restore original system streams
        System.setOut(standardOut)
        System.setIn(standardIn)
        timerUnit.shutdown()
    }

    // --- Helper function to place instruction bytes and run one cycle ---
    private fun executeInstruction(byte1: UByte, byte2: UByte) {
        val currentPC = Registers.P
        MemoryController.writeByte(currentPC, byte1)
        MemoryController.writeByte((currentPC + 1u).toUShort(), byte2)
        cpu.fetchDecodeExecuteCycle()
    }

    // --- Test Cases for each Instruction ---

    @Test
    @DisplayName("STORE instruction stores byte bb in register rX")
    fun testStoreInstruction() {
        val testValue = 0x5Au.toUByte()
        val targetRegisterIndex = 0x3 // r3
        val byte1 = (0x0 shl 4 or targetRegisterIndex).toUByte()
        val byte2 = testValue

        executeInstruction(byte1, byte2)

        assertEquals(testValue, Registers.getGeneralPurposeRegister(targetRegisterIndex))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("ADD instruction adds two register values and stores in rZ")
    fun testAddInstruction() {
        Registers.setGeneralPurposeRegister(0, 0x03u) // r0 = 3
        Registers.setGeneralPurposeRegister(1, 0x05u) // r1 = 5
        val byte1 = (0x1 shl 4 or 0).toUByte() // ADD r0, r1, r2
        val byte2 = (1 shl 4 or 2).toUByte() // rY=1, rZ=2

        executeInstruction(byte1, byte2)

        assertEquals(0x08u.toUByte(), Registers.getGeneralPurposeRegister(2))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("SUB instruction subtracts two register values and stores in rZ")
    fun testSubInstruction() {
        Registers.setGeneralPurposeRegister(0, 0x0Au) // r0 = 10
        Registers.setGeneralPurposeRegister(1, 0x03u) // r1 = 3
        val byte1 = (0x2 shl 4 or 0).toUByte() // SUB r0, r1, r2
        val byte2 = (1 shl 4 or 2).toUByte() // rY=1, rZ=2

        executeInstruction(byte1, byte2)

        assertEquals(0x07u.toUByte(), Registers.getGeneralPurposeRegister(2))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("READ instruction reads from memory address A into rX")
    fun testReadInstruction() {
        Registers.A = (RAM_TEST_START)
        val testValue = 0xCDu.toUByte()
        MemoryController.writeByte((RAM_TEST_START), testValue) // Write to RAM at address A

        val byte1 = (0x3 shl 4 or 0).toUByte() // READ r0, 00
        val byte2 = 0x00u.toUByte()

        executeInstruction(byte1, byte2)

        assertEquals(testValue, Registers.getGeneralPurposeRegister(0))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("WRITE instruction writes from rX to memory address A")
    fun testWriteInstruction() {
        Registers.A = RAM_TEST_START
        val testValue = 0xABu.toUByte()
        Registers.setGeneralPurposeRegister(1, testValue) // r1 = 0xAB

        val byte1 = (0x4 shl 4 or 1).toUByte() // WRITE r1, 00
        val byte2 = 0x00u.toUByte()

        executeInstruction(byte1, byte2)

        assertEquals(testValue, MemoryController.readByte(0x1000u))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("JUMP instruction sets P to address aaa")
    fun testJumpInstruction() {
        val jumpAddress = 0x01F2u.toUShort()
        val byte1 = (0x5 shl 4 or 0x01).toUByte() // JUMP 0x01F2
        val byte2 = 0xF2u.toUByte()

        executeInstruction(byte1, byte2)

        assertEquals(jumpAddress, Registers.P)
    }

    @Test
    @DisplayName("JUMP instruction throws error for odd addresses")
    fun testJumpInstructionErrorOnOddAddress() {
        val oddAddress = 0x01F3u.toUShort()
        val byte1 = (0x5 shl 4 or 0x01).toUByte()
        val byte2 = 0xF3u.toUByte()

        val exception = assertThrows<ProgramTerminationException> {
            executeInstruction(byte1, byte2)
        }
        assertTrue(exception.message!!.contains("Attempted to jump to odd-numbered address"))
    }

    @Test
    @DisplayName("READ_KEYBOARD instruction reads from input and stores in rX")
    fun testReadKeyBoardInstruction() {
        // Redirect System.in to simulate user input
        val inputString = "AB"
        val inputStream = ByteArrayInputStream(inputString.toByteArray())
        System.setIn(inputStream)

        val targetRegisterIndex = 0x2 // r2
        val byte1 = (0x6 shl 4 or targetRegisterIndex).toUByte() // READ_KEYBOARD r2
        val byte2 = 0x00u.toUByte()

        // Capture console output to verify messages
        outputStreamCaptor.reset()
        executeInstruction(byte1, byte2)

        assertEquals(0xABu.toUByte(), Registers.getGeneralPurposeRegister(targetRegisterIndex))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
        assertTrue(outputStreamCaptor.toString().contains("Keyboard: awaiting user input"))
    }

    @Test
    @DisplayName("SWITCH_MEMORY instruction toggles the M register")
    fun testSwitchMemoryInstruction() {
        Registers.M = false
        val byte1 = 0x70u.toUByte() // SWITCH_MEMORY 000
        val byte2 = 0x00u.toUByte()

        executeInstruction(byte1, byte2)
        assertTrue(Registers.M)

        executeInstruction(byte1, byte2)
        assertFalse(Registers.M)

        assertEquals((PC_START + 4u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("SKIP_EQUAL instruction skips next instruction if rX equals rY")
    fun testSkipEqualInstruction() {
        // Condition is true: r1 == r2
        Registers.setGeneralPurposeRegister(1, 0x0Au)
        Registers.setGeneralPurposeRegister(2, 0x0Au)
        val byte1 = (0x8 shl 4 or 1).toUByte() // SKIP_EQUAL r1, r2
        val byte2 = (2 shl 4 or 0).toUByte()

        executeInstruction(byte1, byte2)

        assertEquals((PC_START + 4u).toUShort(), Registers.P)

        // Condition is false: r1 != r2
        Registers.P = PC_START
        Registers.setGeneralPurposeRegister(2, 0x0Bu)
        executeInstruction(byte1, byte2)

        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("SKIP_NOT_EQUAL instruction skips next instruction if rX is not equal to rY")
    fun testSkipNotEqualInstruction() {
        // Condition is true: r1 != r2
        Registers.setGeneralPurposeRegister(1, 0x0Au)
        Registers.setGeneralPurposeRegister(2, 0x0Bu)
        val byte1 = (0x9 shl 4 or 1).toUByte() // SKIP_NOT_EQUAL r1, r2
        val byte2 = (2 shl 4 or 0).toUByte()

        executeInstruction(byte1, byte2)

        assertEquals((PC_START + 4u).toUShort(), Registers.P)

        // Condition is false: r1 == r2
        Registers.P = PC_START
        Registers.setGeneralPurposeRegister(2, 0x0Au)
        executeInstruction(byte1, byte2)

        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("SET_A instruction sets Address register A to aaa")
    fun testSetAInstruction() {
        val testAddress = 0x01F2u.toUShort()
        val byte1 = (0xA shl 4 or 0x01).toUByte() // SET_A 0x01F2
        val byte2 = 0xF2u.toUByte()

        executeInstruction(byte1, byte2)

        assertEquals(testAddress, Registers.A)
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("SET_T instruction sets Timer register T to bb")
    fun testSetTInstruction() {
        val testValue = 0x0Au.toUByte()
        val byte1 = 0xB0u.toUByte() // SET_T bb
        val byte2 = testValue

        executeInstruction(byte1, byte2)

        assertEquals(testValue, Registers.T)
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("READ_T instruction reads Timer register T into rX")
    fun testReadTInstruction() {
        val testValue = 0x05u.toUByte()
        Registers.T = testValue
        val targetRegisterIndex = 0x1 // r1
        val byte1 = (0xC shl 4 or targetRegisterIndex).toUByte() // READ_T r1
        val byte2 = 0x00u.toUByte()

        executeInstruction(byte1, byte2)

        assertEquals(testValue, Registers.getGeneralPurposeRegister(targetRegisterIndex))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("CONVERT_TO_BASE_10 converts rX to digits in memory at address A")
    fun testConvertToBase10Instruction() {
        Registers.A = RAM_TEST_START
        Registers.setGeneralPurposeRegister(1, 255u) // r1 = 255

        val byte1 = (0xD shl 4 or 1).toUByte() // CONVERT_TO_BASE_10 r1
        val byte2 = 0x00u.toUByte()

        executeInstruction(byte1, byte2)

        assertEquals(2u.toUByte(), MemoryController.readByte(RAM_TEST_START))
        assertEquals(5u.toUByte(), MemoryController.readByte((RAM_TEST_START + 1u).toUShort()))
        assertEquals(5u.toUByte(), MemoryController.readByte((RAM_TEST_START + 2u).toUShort()))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("CONVERT_BYTE_TO_ASCII converts digit in rX to ASCII in rY")
    fun testConvertByteToAsciiInstruction() {
        Registers.setGeneralPurposeRegister(0, 0x0Au) // r0 = 10 (hex A)
        val byte1 = (0xE shl 4 or 0).toUByte() // CONVERT_BYTE_TO_ASCII r0, r1
        val byte2 = (1 shl 4 or 0).toUByte() // rY = 1

        executeInstruction(byte1, byte2)

        assertEquals('A'.code.toUByte(), Registers.getGeneralPurposeRegister(1))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("CONVERT_BYTE_TO_ASCII throws error if rX is > 0xF")
    fun testConvertByteToAsciiInstructionError() {
        Registers.setGeneralPurposeRegister(0, 0x10u) // r0 = 16 (invalid hex digit)
        val byte1 = (0xE shl 4 or 0).toUByte()
        val byte2 = (1 shl 4 or 0).toUByte()

        val exception = assertThrows<ProgramTerminationException> {
            executeInstruction(byte1, byte2)
        }
        assertTrue(exception.message!!.contains("Attempted to convert byte greater than 0xF to ASCII"))
    }

    @Test
    @DisplayName("DRAW instruction writes ASCII character to screen RAM")
    fun testDrawInstruction() {
        val testChar = 'C'.code.toUByte()
        val testRow = 1u.toUByte()
        val testCol = 5u.toUByte()

        Registers.setGeneralPurposeRegister(1, testChar) // r1 = 'C'
        Registers.setGeneralPurposeRegister(2, testRow) // r2 = 1 (row)
        Registers.setGeneralPurposeRegister(3, testCol) // r3 = 5 (col)

        val byte1 = (0xF shl 4 or 1).toUByte() // DRAW r1, r2, r3
        val byte2 = (2 shl 4 or 3).toUByte()

        val expectedScreenAddress = (Constants.ASCII_DISPLAY_START_ADDRESS + (testRow.toInt() * Constants.ASCII_DISPLAY_WIDTH_CHARS).toUInt() + testCol.toUInt()).toUShort()

        System.setOut(standardOut)
        System.setIn(standardIn)

        executeInstruction(byte1, byte2)

        assertEquals(testChar, MemoryController.readByte(expectedScreenAddress))
        assertEquals((PC_START + 2u).toUShort(), Registers.P)
    }

    @Test
    @DisplayName("DRAW instruction throws error if character is > 0x7F")
    fun testDrawInstructionErrorOnInvalidChar() {
        Registers.setGeneralPurposeRegister(1, 0x80u) // r1 = invalid char
        val byte1 = (0xF shl 4 or 1).toUByte()
        val byte2 = (2 shl 4 or 3).toUByte()

        val exception = assertThrows<ProgramTerminationException> {
            executeInstruction(byte1, byte2)
        }
        assertTrue(exception.message!!.contains("Attempted to draw an invalid byte to the screen"))
    }

    @Test
    @DisplayName("DRAW instruction throws error if row or column are invalid")
    fun testDrawInstructionErrorOnInvalidLocation() {
        Registers.setGeneralPurposeRegister(1, 'A'.code.toUByte())
        Registers.setGeneralPurposeRegister(2, 8u) // r2 = invalid row
        Registers.setGeneralPurposeRegister(3, 0u)
        val byte1 = (0xF shl 4 or 1).toUByte()
        val byte2 = (2 shl 4 or 3).toUByte()

        val exception = assertThrows<ProgramTerminationException> {
            executeInstruction(byte1, byte2)
        }
        assertTrue(exception.message!!.contains("Attempted to draw to an invalid screen location"))
    }}