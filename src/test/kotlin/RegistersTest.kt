import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Registers Object Tests")
class RegistersTest {

    @BeforeEach
    fun setup() {
        Registers.resetForTesting()
    }

    @Test
    @DisplayName("Registers initialize to default values (0 or false)")
    fun testInitialState() {
        assertEquals(0x00u.toUByte(), Registers.R0, "R0 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R1, "R1 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R2, "R2 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R3, "R3 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R4, "R4 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R5, "R5 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R6, "R6 should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.R7, "R7 should initialize to 0")
        assertEquals(0x0000u.toUShort(), Registers.P, "P should initialize to 0")
        assertEquals(0x00u.toUByte(), Registers.T, "T should initialize to 0")
        assertEquals(0x0000u.toUShort(), Registers.A, "A should initialize to 0")
        assertFalse(Registers.M, "M should initialize to false")
    }

    @Test
    @DisplayName("P (Program Counter) can be read and written")
    fun testPRegister() {
        val testValue = 0x1234u.toUShort()
        Registers.P = testValue
        assertEquals(testValue, Registers.P)
    }

    @Test
    @DisplayName("T (Timer) can be read and written")
    fun testTRegister() {
        val testValue = 0x5Au.toUByte()
        Registers.T = testValue
        assertEquals(testValue, Registers.T)
    }

    @Test
    @DisplayName("A (Address) can be read and written")
    fun testARegister() {
        val testValue = 0xABCDu.toUShort()
        Registers.A = testValue
        assertEquals(testValue, Registers.A)
    }

    @Test
    @DisplayName("M (Memory Flag) can be read and written")
    fun testMRegister() {
        Registers.M = true
        assertTrue(Registers.M)
        Registers.M = false
        assertFalse(Registers.M)
    }

    @Test
    @DisplayName("Individual general purpose registers (R0-R7) can be read and written")
    fun testIndividualGeneralPurposeRegisters() {
        Registers.R0 = 0x11u.toUByte()
        Registers.R1 = 0x22u.toUByte()
        Registers.R2 = 0x33u.toUByte()
        Registers.R3 = 0x44u.toUByte()
        Registers.R4 = 0x55u.toUByte()
        Registers.R5 = 0x66u.toUByte()
        Registers.R6 = 0x77u.toUByte()
        Registers.R7 = 0xEEu.toUByte()

        assertEquals(0x11u.toUByte(), Registers.R0)
        assertEquals(0x22u.toUByte(), Registers.R1)
        assertEquals(0x33u.toUByte(), Registers.R2)
        assertEquals(0x44u.toUByte(), Registers.R3)
        assertEquals(0x55u.toUByte(), Registers.R4)
        assertEquals(0x66u.toUByte(), Registers.R5)
        assertEquals(0x77u.toUByte(), Registers.R6)
        assertEquals(0xEEu.toUByte(), Registers.R7)
    }
}