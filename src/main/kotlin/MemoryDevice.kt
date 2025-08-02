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

    fun readByte(address: UShort): UByte {
        if (!isInRange(address)) {
            throw IndexOutOfBoundsException("Address 0x${address.toString(16).uppercase().padStart(4, '0')} is not in range for device starting at 0x${startAddress.toString(16).uppercase().padStart(4, '0')}.")
        }
        val offset = getLocalOffset(address)
        return doReadByte(offset)
    }

    fun writeByte(address: UShort, value: UByte) {
        if (!isInRange(address)) {
            throw IndexOutOfBoundsException("Address 0x${address.toString(16).uppercase().padStart(4, '0')} is not in range for device starting at 0x${startAddress.toString(16).uppercase().padStart(4, '0')}.")
        }
        val offset = getLocalOffset(address)
        return doWriteByte(offset, value)
    }

    protected abstract fun doReadByte(offset: UShort): UByte
    protected abstract fun doWriteByte(offset: UShort, value: UByte)
}