package server

import com.sun.net.httpserver.HttpExchange

data class EndpointHandler(
    val pattern: String,
    val method: String,
    val handler: (HttpExchange) -> Unit
) {
    fun isMatch(exchange: HttpExchange): Boolean {
        if (!exchange.requestMethod.equals(method)) return false
        val path: String = exchange.requestURI.path
        return path.matches(pattern.toRegex())
    }

    fun handle(exchange: HttpExchange) {
        exchange.responseHeaders["jwt"] = listOf("application/json")
        handler.invoke(exchange)
    }
}
