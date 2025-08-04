object Registers : IRegisters {
    override val r: MutableList<UByte> = MutableList(8) { 0u }
    override var P: UShort  = 0u
    override var T: UByte   = 0u
    override var A: UShort  = 0u
    override var M: Boolean = false

    override fun setGeneralPurposeRegister(index: Int, value: UByte) {
        if (index < 0 || index >= r.size) {
            throw IllegalArgumentException("Invalid general purpose register index: r$index. Must be between 0 and 7.")
        }
        r[index] = value
    }

    override fun getGeneralPurposeRegister(index: Int): UByte {
        if (index < 0 || index >= r.size) {
            throw IllegalArgumentException("Invalid general purpose register index: r$index. Must be between 0 and 7.")
        }
        return r[index]
    }

    internal fun resetForTesting() {
        r.fill(0u)
        P = 0u
        T = 0u
        A = 0u
        M = false
    }
}
