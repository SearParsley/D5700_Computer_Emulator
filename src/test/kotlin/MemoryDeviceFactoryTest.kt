import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("MemoryDeviceFactory Tests")
class MemoryDeviceFactoryTest {

    @Test
    @DisplayName("createRAM returns a correctly initialized RAM object")
    fun testCreateRam() {
        val testStartAddress = 0x1000u.toUShort()
        val testSize = 4096u.toUShort()
        val ram = MemoryDeviceFactory.createRAM(testStartAddress, testSize)
        assertInstanceOf(RAM::class.java, ram)
        assertEquals(testStartAddress, ram.startAddress)
        assertEquals(testSize, ram.size)
    }

    @Test
    @DisplayName("createROM returns a correctly initialized ROM object")
    fun testCreateRom() {
        val testStartAddress = 0x0000u.toUShort()
        val testSize = 4096u.toUShort()
        val testData = listOf<UByte>(0xAAu, 0xBBu, 0xCCu)
        val rom = MemoryDeviceFactory.createROM(testStartAddress, testSize, testData)
        assertInstanceOf(ROM::class.java, rom)
        assertEquals(testStartAddress, rom.startAddress)
        assertEquals(testSize, rom.size)
        assertEquals(0xAAu.toUByte(), rom.readByte(testStartAddress))
        assertEquals(0xBBu.toUByte(), rom.readByte((testStartAddress + 1u).toUShort()))
    }

    @Test
    @DisplayName("createAsciiDisplayDevice returns a correctly initialized AsciiDisplayDevice object")
    fun testCreateAsciiDisplayDevice() {
        val testStartAddress = 0xF000u.toUShort()
        val testSize = 64u.toUShort()
        val display = MemoryDeviceFactory.createAsciiDisplayDevice(testStartAddress, testSize)
        assertInstanceOf(AsciiDisplayDevice::class.java, display)
        assertEquals(testStartAddress, display.startAddress)
        assertEquals(testSize, display.size)
    }

    @Test
    @DisplayName("createKeyboardInputDevice returns a correctly initialized KeyboardInputDevice object")
    fun testCreateKeyboardInputDevice() {
        val testStartAddress = 0xFE00u.toUShort()
        val testSize = 1u.toUShort()
        val keyboard = MemoryDeviceFactory.createKeyboardInputDevice(testStartAddress, testSize)
        assertInstanceOf(KeyboardInputDevice::class.java, keyboard)
        assertEquals(testStartAddress, keyboard.startAddress)
        assertEquals(testSize, keyboard.size)
    }
}