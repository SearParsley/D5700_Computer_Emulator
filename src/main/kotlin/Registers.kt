object Registers {
    val r: MutableList<UByte> = MutableList(8) { 0u }
    var P: UShort  = 0u
    var T: UByte   = 0u
    var A: UShort  = 0u
    var M: Boolean = false

    fun setGeneralPurposeRegister(index: Int, value: UByte) {
        if (index < 0 || index >= r.size) {
            throw IllegalArgumentException("Invalid general purpose register index: r$index. Must be between 0 and 7.")
        }
        r[index] = value
    }

    fun getGeneralPurposeRegister(index: Int): UByte {
        if (index < 0 || index >= r.size) {
            throw IllegalArgumentException("Invalid general purpose register index: r$index. Must be between 0 and 7.")
        }
        return r[index]
    }

    internal fun resetForTesting() {
        r[0] = 0u
        r[1] = 0u
        r[2] = 0u
        r[3] = 0u
        r[4] = 0u
        r[5] = 0u
        r[6] = 0u
        r[7] = 0u
        P = 0u
        T = 0u
        A = 0u
        M = false
    }
}
