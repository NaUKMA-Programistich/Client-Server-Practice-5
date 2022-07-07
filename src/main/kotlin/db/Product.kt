package db

import java.sql.ResultSet

data class Product(
    val count: Int,
    val price: Double,
    val name: String,
    val group: String,
    val id: Int = 0
) {
    fun valid(): Boolean {
        if (count < 0) return false
        if (price < 0) return false
        if (name.isBlank()) return false
        if (group.isBlank()) return false
        return true
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (count != other.count) return false
        if (price != other.price) return false
        if (name != other.name) return false
        if (group != other.group) return false

        return true
    }

    override fun hashCode(): Int {
        var result = count
        result = 31 * result + price.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + group.hashCode()
        return result
    }
}

fun ResultSet.toProduct(): Product {
    return Product(
        id = this.getInt("id"),
        count = this.getInt("count"),
        price = this.getDouble("price"),
        name = this.getString("name"),
        group = this.getString("group")
    )
}
