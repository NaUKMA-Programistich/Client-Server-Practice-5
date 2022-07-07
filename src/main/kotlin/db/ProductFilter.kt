package db

data class ProductFilter(
    val priceFrom: Double? = null,
    val priceTo: Double? = null,
    val countFrom: Int? = null,
    val countTo: Int? = null,
    val nameStart: String? = null,
    val nameEnd: String? = null,
    val groupStart: String? = null,
    val groupEnd: String? = null,
)
