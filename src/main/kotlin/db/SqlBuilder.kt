package db

object SqlBuilder {
    fun less(value: Double?, column: String): String? {
        return if (value == null) null
        else "`$column` >= $value"
    }

    fun more(value: Double?, column: String): String? {
        return if (value == null) null
        else "`$column` <= $value"
    }

    fun startWith(value: String?, column: String): String? {
        return if (value == null || value == "") null
        else "`$column` like '$value%'"
    }

    fun endWith(value: String?, column: String): String? {
        return if (value == null || value == "") null
        else "`$column` like '%$value'"
    }
}
