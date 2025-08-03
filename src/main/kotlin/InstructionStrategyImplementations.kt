class StoreInstruction() : InstructionStrategy {
    override val opcode: Int = 0x0
    override val mnemonic: String = "STORE"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.STORE
        val rx = instructionSet.parseRx(byte1)
        val bb = instructionSet.parseBb(byte2)
        Registers.setGeneralPurposeRegister(rx, bb)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class AddInstruction : InstructionStrategy {
    override val opcode = 0x1
    override val mnemonic = "ADD"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.ADD
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rz = instructionSet.parseRz(byte2)
        val valX = Registers.getGeneralPurposeRegister(rx)
        val valY = Registers.getGeneralPurposeRegister(ry)
        val result = (valX + valY).toUByte()
        Registers.setGeneralPurposeRegister(rz, result)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SubInstruction : InstructionStrategy {
    override val opcode = 0x2
    override val mnemonic = "SUB"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SUB
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rz = instructionSet.parseRz(byte2)
        val valX = Registers.getGeneralPurposeRegister(rx)
        val valY = Registers.getGeneralPurposeRegister(ry)
        val result = (valX - valY).toUByte()
        Registers.setGeneralPurposeRegister(rz, result)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ReadInstruction : InstructionStrategy {
    override val opcode = 0x3
    override val mnemonic = "READ"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.READ
        val rx = instructionSet.parseRx(byte1)
        val result = MemoryController.readByte(Registers.A)
        Registers.setGeneralPurposeRegister(rx, result)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class WriteInstruction : InstructionStrategy { // TODO: Figure out memory switching in terms of memory addresses. Put each device in its own memory array? rather than having it all sequential?
    override val opcode = 0x4
    override val mnemonic = "WRITE"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.WRITE
        val rx = instructionSet.parseRx(byte1)
        val data = Registers.getGeneralPurposeRegister(rx)
        MemoryController.writeByte(Registers.A, data)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class JumpInstruction : InstructionStrategy {
    override val opcode = 0x5
    override val mnemonic = "JUMP"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.JUMP
        val aaa = instructionSet.parseAaa(byte1, byte2)
        if (aaa % 2u != 0u) throw JumpToInvalidInstructionException("Attempted to jump to odd-numbered address 0x${aaa.toString(16).uppercase().padStart(4, '0')}")
        Registers.P = aaa
    }
}

class ReadKeyboardInstruction : InstructionStrategy {
    override val opcode = 0x6
    override val mnemonic = "READ_KEYBOARD"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.READ_KEYBOARD
        val rx = instructionSet.parseRx(byte1)
        val result = MemoryController.readByte(Constants.KEYBOARD_START_ADDRESS)
        Registers.setGeneralPurposeRegister(rx, result)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SwitchMemoryInstruction : InstructionStrategy {
    override val opcode = 0x7
    override val mnemonic = "SWITCH_MEMORY"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SWITCH_MEMORY
        Registers.M = !Registers.M
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SkipEqualInstruction : InstructionStrategy {
    override val opcode = 0x8
    override val mnemonic = "SKIP_EQUAL"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SKIP_EQUAL
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rxData = Registers.getGeneralPurposeRegister(rx)
        val ryData = Registers.getGeneralPurposeRegister(ry)
        if  (rxData == ryData) {
            Registers.P = (Registers.P + (2 * instructionSet.pcIncrement).toUShort()).toUShort()
        } else {
            Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
        }
    }
}

class SkipNotEqualInstruction : InstructionStrategy {
    override val opcode = 0x9
    override val mnemonic = "SKIP_NOT_EQUAL"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SKIP_NOT_EQUAL
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rxData = Registers.getGeneralPurposeRegister(rx)
        val ryData = Registers.getGeneralPurposeRegister(ry)
        if  (rxData != ryData) {
            Registers.P = (Registers.P + (2 * instructionSet.pcIncrement).toUShort()).toUShort()
        } else {
            Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
        }
    }
}

class SetAInstruction : InstructionStrategy {
    override val opcode = 0xA
    override val mnemonic = "SET_A"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SET_A
        val aaa = instructionSet.parseAaa(byte1, byte2)
        Registers.A = aaa
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class SetTInstruction : InstructionStrategy {
    override val opcode = 0xB
    override val mnemonic = "SET_T"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.SET_T
        val bb = instructionSet.parseBb(byte2)
        cpu.timerUnit.setTimerValue(bb)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ReadTInstruction : InstructionStrategy {
    override val opcode = 0xC
    override val mnemonic = "READ_T"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.READ_T
        val rx = instructionSet.parseRx(byte1)
        Registers.setGeneralPurposeRegister(rx, Registers.T)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ConvertToBase10Instruction : InstructionStrategy {
    override val opcode = 0xD
    override val mnemonic = "CONVERT_TO_BASE_10"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.CONVERT_TO_BASE_10
        val rx = instructionSet.parseRx(byte1)
        val data = Registers.getGeneralPurposeRegister(rx)
        val hundreds = (data / 100u).toUByte()
        val tens = ((data % 100u) / 10u).toUByte()
        val ones = (data % 10u).toUByte()
        MemoryController.writeByte(Registers.A, hundreds)
        MemoryController.writeByte((Registers.A + 1u).toUShort(), tens)
        MemoryController.writeByte((Registers.A + 2u).toUShort(), ones)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class ConvertByteToAsciiInstruction : InstructionStrategy {
    override val opcode = 0xE
    override val mnemonic = "CONVERT_BYTE_TO_ASCII"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.CONVERT_BYTE_TO_ASCII
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val data = Registers.getGeneralPurposeRegister(rx)
        val result = when (data) {
            in 0x0u..0x9u -> (0x30u + data).toUByte()
            in 0xAu..0xFu -> (0x37u + data).toUByte()
            else -> throw ConvertInvalidByteToAsciiException("Attempted to convert byte greater than 0xF to ASCII (0x${rx.toString(16).uppercase()}).")
        }
        Registers.setGeneralPurposeRegister(ry, result)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class DrawInstruction : InstructionStrategy {
    override val opcode = 0xF
    override val mnemonic = "DRAW"
    override fun execute(cpu: CPU, byte1: UByte, byte2: UByte) {
        val instructionSet = InstructionSet.DRAW
        val rx = instructionSet.parseRx(byte1)
        val ry = instructionSet.parseRy(byte2)
        val rz = instructionSet.parseRz(byte2)
        val data = Registers.getGeneralPurposeRegister(rx)
        val row = Registers.getGeneralPurposeRegister(ry)
        val column = Registers.getGeneralPurposeRegister(rz)
        if (data > 0x7Fu) throw DrawInvalidByteToScreenException("Attempted to draw an invalid byte to the screen (0x${rx.toString(16).uppercase()}).")
        if (column >= Constants.ASCII_DISPLAY_WIDTH_CHARS.toUShort() || row >= Constants.ASCII_DISPLAY_HEIGHT_CHARS.toUShort()) throw DrawToInvalidScreenLocationException("Attempted to draw to an invalid screen location: (${row}, ${column})")
        val index = (row * Constants.ASCII_DISPLAY_WIDTH_CHARS.toUShort()) + column
        MemoryController.writeByte((Constants.ASCII_DISPLAY_START_ADDRESS + index.toUShort()).toUShort(), data)
        val screenContent = MemoryController.getRenderedScreen()
        println(screenContent)
        Registers.P = (Registers.P + instructionSet.pcIncrement.toUShort()).toUShort()
    }
}

class JumpToInvalidInstructionException(message: String) : RuntimeException(message)
class ConvertInvalidByteToAsciiException(message: String) : RuntimeException(message)
class DrawInvalidByteToScreenException(message: String) : RuntimeException(message)
class DrawToInvalidScreenLocationException(message: String) : RuntimeException(message)