abstract class MemoryDevice(
    val startAddress: UShort,
    val size: UShort
) {
    internal fun isInRange(address: UShort): Boolean {
        return address >= startAddress && address < (startAddress + size).toUShort()
    }

    internal fun getLocalOffset(address: UShort): UShort {
        return (address - startAddress).toUShort()
    }

    abstract fun readByte(address: UShort): UByte
    abstract fun writeByte(address: UShort, value: UByte)
}