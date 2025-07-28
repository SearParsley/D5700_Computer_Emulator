import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

class TestTest {

    @Test
    fun `Dummy test should succeed`() {
        val num1 = "123"
        val num2 = 123.toString()

        assertEquals(num1, num2)
        assertNull(null)
        assertTrue { true }
    }
}