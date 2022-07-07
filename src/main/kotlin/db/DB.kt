package db

import java.sql.DriverManager

class DB(private val name: String = "db.db") {

    private val connection = DriverManager.getConnection("jdbc:sqlite:$name")

    fun close() = connection.close()

    fun createTable() {
        val statement = connection.createStatement()
        val query = """
            CREATE TABLE IF NOT EXISTS product (
                `id` INTEGER PRIMARY KEY,
                `count` INTEGER,
                `name` INTEGER,
                `price` DOUBLE,
                `group` TEXT
            )
        """.trimIndent()
        statement.executeUpdate(query)
    }

    fun dropTable() {
        val statement = connection.createStatement()
        val query = "DROP TABLE IF EXISTS product"
        statement.executeUpdate(query)
    }

    fun create(product: Product): Result<Int> {
        return runCatching {
            val query = """
            INSERT INTO product (
                `count`,
                `name`,
                `price`,
                `group`
            )
            values (?,?,?,?)
            """.trimIndent()
            val statement = connection.prepareStatement(query)
            statement.apply {
                setInt(1, product.count)
                setString(2, product.name)
                setDouble(3, product.price)
                setString(4, product.group)
                executeUpdate()
            }
            val result = statement.generatedKeys
            result.getInt(1)
        }
    }

    fun findAll(): Result<List<Product>> {
        return runCatching {
            val products = arrayListOf<Product>()
            val query = "SELECT * FROM product"
            val statement = connection.prepareStatement(query)
            val result = statement.executeQuery()
            while (result.next()) {
                products.add(result.toProduct())
            }
            products
        }
    }

    fun findById(id: Int): Result<Product> {
        return runCatching {
            val query = "SELECT * FROM product item where item.id = ?"
            val statement = connection.prepareStatement(query)
            statement.setInt(1, id)
            val result = statement.executeQuery()
            result.toProduct()
        }
    }

    fun update(id: Int, product: Product): Result<Unit> {
        return runCatching {
            val query = """
            UPDATE product set
                `count` = ?,
                `name` = ?,
                `price` = ?,    
                `group` = ?
            WHERE id = ?
            """.trimIndent()
            val statement = connection.prepareStatement(query)
            statement.apply {
                setInt(1, product.count)
                setString(2, product.name)
                setDouble(3, product.price)
                setString(4, product.group)
                setInt(5, id)
                executeUpdate()
            }
            println(statement)
        }
    }

    fun delete(id: Int): Result<Int> {
        return runCatching {
            val query = "DELETE FROM product where id = ?"
            val statement = connection.prepareStatement(query)
            statement.setInt(1, id)
            statement.execute()
            id
        }
    }

    fun filter(productFilter: ProductFilter?): Result<List<Product>> {
        return runCatching {
            val products = arrayListOf<Product>()
            var query = "SELECT * FROM product"

            if (productFilter != null) {

                val filters = setOf(
                    SqlBuilder.less(productFilter.priceFrom, "price"),
                    SqlBuilder.more(productFilter.priceTo, "price"),

                    SqlBuilder.less(productFilter.countFrom?.toDouble(), "count"),
                    SqlBuilder.more(productFilter.countTo?.toDouble(), "count"),

                    SqlBuilder.startWith(productFilter.nameStart, "name"),
                    SqlBuilder.endWith(productFilter.nameEnd, "name"),

                    SqlBuilder.startWith(productFilter.groupStart, "group"),
                    SqlBuilder.endWith(productFilter.groupEnd, "group")
                )

                if (query.isNotEmpty()) {
                    query += " WHERE " + filters.filterNotNull().joinToString(" AND ")
                }
            }
            val statement = connection.prepareStatement(query)
            val result = statement.executeQuery()
            while (result.next()) {
                products.add(result.toProduct())
            }
            products
        }
    }
}
