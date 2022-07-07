package server

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sun.net.httpserver.*
import db.DB
import db.Product
import server.JWT.createJWT
import java.io.IOException
import java.net.InetSocketAddress

class Server {
    private val mapper = jacksonObjectMapper()
    private val database = DB()

    private val jwts = hashMapOf<String, String>()

    fun instance(): HttpServer {
        val server: HttpServer = HttpServer.create()
        server.apply {
            bind(InetSocketAddress(8081), 0)

            val context = createContext("/")
            context.handler = HttpHandler { exchange ->
                val handler = handlers.firstOrNull { it.isMatch(exchange) }
                if (handler == null) processUnknown(exchange)
                else handler.handle(exchange)
            }
            context.authenticator = object : Authenticator() {
                override fun authenticate(exchange: HttpExchange): Result {
                    val path = exchange.requestURI.path
                    if (path == "/login") return Success(HttpPrincipal("c0nst", "realm"))

                    val jwt: String = exchange.requestHeaders.getFirst("jwt") ?: return Failure(403)

                    val username = JWT.extractUserName(jwt)
                    return if (jwts.map { it.key }.filter { it == username }.isEmpty()) Failure(403)
                    else Success(HttpPrincipal(username, ""))
                }
            }
            executor = null
        }
        return server
    }

    private val handlers = listOf(
        EndpointHandler("/api/good/?", "GET", this::processGetAll),
        EndpointHandler("/api/good/?", "PUT", this::processAddProduct),
        EndpointHandler("/api/good/(\\d+)", "GET", this::processGetById),
        EndpointHandler("/api/good/(\\d+)", "DELETE", this::processDeleteById),
        EndpointHandler("/api/good/(\\d+)", "POST", this::processUpdateById),
        EndpointHandler("/login", "POST", this::processLogin)
    )

    private fun process(exchange: HttpExchange, content: Result<Any>, code: Int) {
        if (content.isFailure) process(exchange, "Something wrong", 422)
        if (content.isSuccess) process(exchange, content.getOrThrow(), code)
    }

    @JvmName("processResult")
    private fun process(exchange: HttpExchange, content: Any?, code: Int) {
        try {
            val data = mapper.writeValueAsBytes(content)
            exchange.sendResponseHeaders(code, data.size.toLong())
            if (content == null) { return }
            exchange.responseBody.apply {
                write(data)
                close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun processUnknown(exchange: HttpExchange) {
        process(exchange, "Error", 404)
    }

    private fun processGetAll(exchange: HttpExchange) {
        val products = database.findAll()
        process(exchange, products, 200)
    }

    private fun processAddProduct(exchange: HttpExchange) {
        val product = mapper.readValue(exchange.requestBody, Product::class.java)
        if (!product.valid()) process(exchange, "Some data broke", 409)
        else {
            val id = database.create(product)
            process(exchange, id, 201)
        }
    }

    private fun processGetById(exchange: HttpExchange) {
        val id = exchange.requestURI.path.replace("/api/good/", "").toInt()
        val product = database.findById(id)
        process(exchange, product, 200)
    }

    private fun processDeleteById(exchange: HttpExchange) {
        val id = exchange.requestURI.path.replace("/api/good/", "").toInt()
        val result = database.delete(id)
        process(exchange, result, 204)
    }

    private fun processUpdateById(exchange: HttpExchange) {
        val id = exchange.requestURI.path.replace("/api/good/", "").toInt()
        val product = mapper.readValue(exchange.requestBody, Product::class.java)
        val result = database.update(id, product)
        process(exchange, result, 204)
    }

    private fun processLogin(exchange: HttpExchange) {
        try {
            val user = mapper.readValue(exchange.requestBody, User::class.java)
            if (jwts[user.username] == null) {
                val jwt = createJWT(user.username)
                jwts[user.username] = jwt
                process(exchange, mapOf("token" to jwt), 200)
            } else {
                process(exchange, mapOf("token" to jwts[user.username]), 200)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}

fun main() {
    val server = Server()
    server.instance().start()
}
