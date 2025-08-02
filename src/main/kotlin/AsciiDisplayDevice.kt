class AsciiDisplayDevice(startAddress: UShort, size: UShort) : MemoryDevice(startAddress, size) {
    private val screenBuffer = MutableList<UByte>(size.toInt()) { Constants.ASCII_DISPLAY_DEFAULT_CHAR_CODE.toUByte() }

    override fun doReadByte(offset: UShort): UByte {
        return screenBuffer[offset.toInt()]
    }

    override fun doWriteByte(offset: UShort, value: UByte) {
        screenBuffer[offset.toInt()] = value
    }

    fun getScreenContent(): List<UByte> = screenBuffer.toList()

    fun getRenderedScreen(): String {
        val output = StringBuilder()
        output.append("--- D5700 ASCII Display (0xF000) ---\n")
        for (row in 0 until Constants.ASCII_DISPLAY_HEIGHT_CHARS) {
            for (col in 0 until Constants.ASCII_DISPLAY_WIDTH_CHARS) {
                val index = (row * Constants.ASCII_DISPLAY_WIDTH_CHARS) + col
                val charCode = screenBuffer[index].toInt()

                if (charCode >= 0x20 && charCode <= 0x7E) {
                    output.append(charCode.toChar())
                } else {
                    output.append('.')
                }
            }
            output.append('\n')
        }
        output.append("------------------------------------\n")
        return output.toString()
    }
}