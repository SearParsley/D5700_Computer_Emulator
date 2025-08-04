import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Registers Object Tests")
class RegistersTest {

    private var registers = Registers

    @BeforeEach
    fun setup() {
        registers.resetForTesting()
    }

    @Test
    @DisplayName("Registers initialize to default values (0 or false)")
    fun testInitialState() {
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(0), "R0 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(1), "R1 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(2), "R2 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(3), "R3 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(4), "R4 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(5), "R5 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(6), "R6 should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.getGeneralPurposeRegister(7), "R7 should initialize to 0")
        assertEquals(0x0000u.toUShort(), registers.P, "P should initialize to 0")
        assertEquals(0x00u.toUByte(), registers.T, "T should initialize to 0")
        assertEquals(0x0000u.toUShort(), registers.A, "A should initialize to 0")
        assertFalse(registers.M, "M should initialize to false")
    }

    @Test
    @DisplayName("P (Program Counter) can be read and written")
    fun testPRegister() {
        val testValue = 0x1234u.toUShort()
        registers.P = testValue
        assertEquals(testValue, registers.P)
    }

    @Test
    @DisplayName("T (Timer) can be read and written")
    fun testTRegister() {
        val testValue = 0x5Au.toUByte()
        registers.T = testValue
        assertEquals(testValue, registers.T)
    }

    @Test
    @DisplayName("A (Address) can be read and written")
    fun testARegister() {
        val testValue = 0xABCDu.toUShort()
        registers.A = testValue
        assertEquals(testValue, registers.A)
    }

    @Test
    @DisplayName("M (Memory Flag) can be read and written")
    fun testMRegister() {
        registers.M = true
        assertTrue(registers.M)
        registers.M = false
        assertFalse(registers.M)
    }

    @Test
    @DisplayName("Individual general purpose registers (R0-R7) can be read and written")
    fun testIndividualGeneralPurposeRegisters() {
        registers.setGeneralPurposeRegister(0, 0x11u.toUByte())
        registers.setGeneralPurposeRegister(1, 0x22u.toUByte())
        registers.setGeneralPurposeRegister(2, 0x33u.toUByte())
        registers.setGeneralPurposeRegister(3, 0x44u.toUByte())
        registers.setGeneralPurposeRegister(4, 0x55u.toUByte())
        registers.setGeneralPurposeRegister(5, 0x66u.toUByte())
        registers.setGeneralPurposeRegister(6, 0x77u.toUByte())
        registers.setGeneralPurposeRegister(7, 0xEEu.toUByte())

        assertEquals(0x11u.toUByte(), registers.getGeneralPurposeRegister(0))
        assertEquals(0x22u.toUByte(), registers.getGeneralPurposeRegister(1))
        assertEquals(0x33u.toUByte(), registers.getGeneralPurposeRegister(2))
        assertEquals(0x44u.toUByte(), registers.getGeneralPurposeRegister(3))
        assertEquals(0x55u.toUByte(), registers.getGeneralPurposeRegister(4))
        assertEquals(0x66u.toUByte(), registers.getGeneralPurposeRegister(5))
        assertEquals(0x77u.toUByte(), registers.getGeneralPurposeRegister(6))
        assertEquals(0xEEu.toUByte(), registers.getGeneralPurposeRegister(7))
    }
}