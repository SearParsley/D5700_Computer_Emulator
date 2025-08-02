class KeyboardInputDevice(startAddress: UShort, size: UShort) : MemoryDevice(startAddress, size) {

    override fun readByte(address: UShort): UByte {
        val offset = getLocalOffset(address)
        if (offset >= Constants.KEYBOARD_BUFFER_SIZE_BYTES.toUShort()) {
            println("Warning: Reading from invalid offset 0x${offset.toString(16).uppercase().padStart(4, '0')} in KeyboardInputDevice.")
            return 0xFFu
        }
        println("Keyboard: awaiting user input")
        val line = readLine() ?: ""
        println("Keyboard: input received")
        val trimmedLine = line.trim()
        val hexString = if (trimmedLine.startsWith("0x", ignoreCase = true)) {
            trimmedLine.substring(2)
        } else {
            trimmedLine
        }
        val effectiveHexString = hexString.takeLast(2)
        var byteValueToOffer: UByte = 0u
        if (effectiveHexString.isEmpty()) {
            byteValueToOffer = 0u
        } else {
            try {
                val parsedValue = effectiveHexString.toInt(16)
                byteValueToOffer = parsedValue.toUByte()
            } catch (e: NumberFormatException) {
                byteValueToOffer = 0u
            }
        }
        return byteValueToOffer
    }

    override fun writeByte(address: UShort, value: UByte) {
        throw KeyboardWriteAttemptException("Attempted to write to read-only KeyboardInputDevice at 0x${address.toString(16).uppercase().padStart(4, '0')}.")
    }
}

class KeyboardWriteAttemptException(message: String) : RuntimeException(message)