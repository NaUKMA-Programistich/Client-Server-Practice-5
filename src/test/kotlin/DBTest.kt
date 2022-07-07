import db.DB
import db.ProductFilter
import db.mockProducts
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DBTest {

    private lateinit var db: DB

    @BeforeAll
    fun setup() {
        db = DB()
        db.dropTable()
        db.createTable()
    }

    @Test
    fun `Insert six product`() {
        mockProducts.forEachIndexed { index, product ->
            val id = db.create(product)
            assertTrue(id.isSuccess)
            assertDoesNotThrow {
                assertEquals((index + 1), id.getOrThrow())
            }
        }
    }

    @Test
    fun `Select All and equals with mock`() {
        val products = db.findAll()
        assertTrue(products.isSuccess)
        assertDoesNotThrow {
            assertEquals(products.getOrThrow().size, mockProducts.size)
            repeat(mockProducts.size) {
                assertEquals(products.getOrThrow()[it], mockProducts[it])
            }
        }
    }

    @Test
    fun `Find by exist id`() {
        assertDoesNotThrow {
            repeat(mockProducts.size) {
                val product = db.findById(it + 1)
                assertTrue(product.isSuccess)
            }
        }
    }

    @Test
    fun `Find by not exist id`() {
        val product = db.findById(-1)
        assertTrue(product.isFailure)
    }

    @Test
    fun `Criteria with no elements`() {
        val filter = ProductFilter(
            priceFrom = 800.0,
            priceTo = 10000.0,
            countFrom = 3,
            countTo = 20,
            nameStart = "I",
            nameEnd = "ad",
            groupStart = "as",
            groupEnd = "da",
        )
        val products = db.filter(filter)
        assertTrue(products.isSuccess)
        assertDoesNotThrow {
            assertTrue(products.getOrThrow().isEmpty())
        }
    }

    @Test
    fun `Criteria with all elements`() {
        val filter = ProductFilter(countFrom = -1)
        val products = db.filter(filter)
        assertTrue(products.isSuccess)
        assertDoesNotThrow {
            assertEquals(products.getOrThrow().size, mockProducts.size)
        }
    }

    @Test
    fun `Complex criteria`() {
        val filter = ProductFilter(
            priceFrom = 800.0,
            priceTo = 10000.0,
            countFrom = 3,
            countTo = 20,
            nameStart = "I",
            groupEnd = "t",
        )
        val products = db.filter(filter)
        assertTrue(products.isSuccess)
        assertDoesNotThrow {
            assertEquals(products.getOrThrow().size, 1)
        }
    }

    @Test
    fun `Null criteria`() {
        val products = db.filter(null)
        assertTrue(products.isSuccess)
        assertDoesNotThrow {
            assertEquals(products.getOrThrow().size, mockProducts.size)
        }
    }

    @Test
    fun `Delete by id`() {
        val products = db.findAll()
        assertTrue(products.isSuccess)
        assertDoesNotThrow {
            repeat(mockProducts.size) {
                db.delete(it + 1)
            }
        }
    }

    @AfterAll
    fun done() {
        db.close()
    }
}
