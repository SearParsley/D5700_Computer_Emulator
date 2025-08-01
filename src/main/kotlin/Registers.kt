object Registers {
    var R0: UByte   = 0u
    var R1: UByte   = 0u
    var R2: UByte   = 0u
    var R3: UByte   = 0u
    var R4: UByte   = 0u
    var R5: UByte   = 0u
    var R6: UByte   = 0u
    var R7: UByte   = 0u
    var  P: UShort  = 0u
    var  T: UByte   = 0u
    var  A: UShort  = 0u
    var  M: Boolean = false

    internal fun resetForTesting() {
        R0 = 0u
        R1 = 0u
        R2 = 0u
        R3 = 0u
        R4 = 0u
        R5 = 0u
        R6 = 0u
        R7 = 0u
         P = 0u
         T = 0u
         A = 0u
         M = false
    }
}
