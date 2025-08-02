class KeyboardInputDevice(startAddress: UShort, size: UShort) : MemoryDevice(startAddress, size) {

    override fun doReadByte(offset: UShort): UByte {
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

    override fun doWriteByte(offset: UShort, value: UByte) {
        throw KeyboardWriteAttemptException("Attempted to write to read-only KeyboardInputDevice at 0x${(startAddress + offset).toString(16).uppercase().padStart(4, '0')}.")
    }
}

class KeyboardWriteAttemptException(message: String) : RuntimeException(message)