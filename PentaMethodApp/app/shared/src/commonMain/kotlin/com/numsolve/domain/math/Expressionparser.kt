package com.numsolve.domain.math

import kotlin.math.*

/**
 * Resultado de intentar compilar una expresión matemática ingresada por el usuario.
 */
sealed class ParseResult {
    data class Success(val expression: MathExpression) : ParseResult()
    data class Failure(val message: String) : ParseResult()
}

/**
 * Una expresión matemática compilada y evaluable en un valor de x.
 * Se evalúa cuantas veces sea necesario sin volver a parsear el texto,
 * lo cual es clave para el rendimiento en métodos iterativos.
 */
fun interface MathExpression {
    fun evaluate(x: Double): Double
}

/**
 * Excepción lanzada durante la evaluación numérica (p.ej. división por cero,
 * logaritmo de negativo, dominio inválido, etc.)
 */
class MathEvaluationException(message: String) : Exception(message)

/**
 * Parser recursivo-descendente para expresiones de una variable (x).
 *
 * Gramática soportada:
 *   expr    := term (('+' | '-') term)*
 *   term    := power (('*' | '/' | '%') power)*
 *   power   := unary ('^' power)?          // ^ es asociativo por la derecha
 *   unary   := ('-' | '+') unary | primary
 *   primary := NUMBER | 'x' | CONST | FUNC '(' expr ')' | '(' expr ')'
 *
 * Funciones soportadas: sin, cos, tan, asin, acos, atan, sinh, cosh, tanh,
 * exp, ln, log (base 10), log2, sqrt, abs, cbrt
 * Constantes: pi, e
 *
 * Ejemplos válidos:
 *   "x^3 + 2*x^2 + 10*x - 20"
 *   "exp(-x^3) - 2*x + 1"
 *   "sqrt(x^2 + 1) - tan(x)"
 *   "(20 - 2*x^2 - x^3) / 10"   (típico g(x) de punto fijo)
 */
object ExpressionParser {

    fun parse(input: String): ParseResult {
        val cleaned = input.trim()
        if (cleaned.isEmpty()) {
            return ParseResult.Failure("La expresión no puede estar vacía.")
        }
        return try {
            val tokens = tokenize(cleaned)
            val parser = RecursiveDescentParser(tokens, cleaned)
            val node = parser.parseExpression()
            parser.expectEnd()
            // Validamos que la expresión sea evaluable en al menos un punto de prueba.
            node.evaluate(1.0)
            ParseResult.Success(MathExpression { x -> node.evaluate(x) })
        } catch (e: MathEvaluationException) {
            ParseResult.Failure(e.message ?: "Error evaluando la expresión.")
        } catch (e: IllegalArgumentException) {
            ParseResult.Failure(e.message ?: "Expresión inválida.")
        } catch (e: Exception) {
            ParseResult.Failure("No se pudo interpretar la expresión: ${e.message ?: "sintaxis inválida"}")
        }
    }

    // ---------- Tokenización ----------

    private sealed class Token {
        data class Num(val value: Double) : Token()
        data class Ident(val name: String) : Token()
        data class Op(val symbol: Char) : Token()
        object LParen : Token()
        object RParen : Token()
        object Comma : Token()
    }

    private fun tokenize(input: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            when {
                c.isWhitespace() -> i++
                c.isDigit() || (c == '.' && i + 1 < input.length && input[i + 1].isDigit()) -> {
                    val start = i
                    var sawDot = false
                    while (i < input.length && (input[i].isDigit() || (input[i] == '.' && !sawDot))) {
                        if (input[i] == '.') sawDot = true
                        i++
                    }
                    // notación científica: 1e-3, 2.5E10
                    if (i < input.length && (input[i] == 'e' || input[i] == 'E') &&
                        i + 1 < input.length && (input[i + 1].isDigit() || input[i + 1] == '+' || input[i + 1] == '-')
                    ) {
                        i++
                        if (input[i] == '+' || input[i] == '-') i++
                        while (i < input.length && input[i].isDigit()) i++
                    }
                    tokens += Token.Num(input.substring(start, i).toDouble())
                }
                c.isLetter() -> {
                    val start = i
                    while (i < input.length && (input[i].isLetterOrDigit() || input[i] == '_')) i++
                    tokens += Token.Ident(input.substring(start, i))
                }
                c == '(' -> {
                    tokens += Token.LParen; i++
                }
                c == ')' -> {
                    tokens += Token.RParen; i++
                }
                c == ',' -> {
                    tokens += Token.Comma; i++
                }
                c in "+-*/^%" -> {
                    tokens += Token.Op(c); i++
                }
                else -> throw IllegalArgumentException("Carácter no reconocido: '$c'")
            }
        }
        return tokens
    }

    // ---------- Nodo AST evaluable ----------

    private fun interface Node {
        fun evaluate(x: Double): Double
    }

    // ---------- Parser ----------

    private class RecursiveDescentParser(private val tokens: List<Token>, private val source: String) {
        private var pos = 0

        private fun peek(): Token? = tokens.getOrNull(pos)
        private fun advance(): Token = tokens[pos++]

        fun expectEnd() {
            if (pos != tokens.size) {
                throw IllegalArgumentException("Sintaxis inválida cerca de: '${source}'. Revisa paréntesis y operadores.")
            }
        }

        fun parseExpression(): Node {
            var left = parseTerm()
            while (true) {
                val t = peek()
                if (t is Token.Op && (t.symbol == '+' || t.symbol == '-')) {
                    advance()
                    val right = parseTerm()
                    val op = t.symbol
                    val prev = left
                    left = Node { x -> if (op == '+') prev.evaluate(x) + right.evaluate(x) else prev.evaluate(x) - right.evaluate(x) }
                } else break
            }
            return left
        }

        private fun parseTerm(): Node {
            var left = parsePower()
            while (true) {
                val t = peek()
                if (t is Token.Op && (t.symbol == '*' || t.symbol == '/' || t.symbol == '%')) {
                    advance()
                    val right = parsePower()
                    val op = t.symbol
                    val prev = left
                    left = Node { x ->
                        val r = right.evaluate(x)
                        when (op) {
                            '*' -> prev.evaluate(x) * r
                            '%' -> prev.evaluate(x).rem(r)
                            else -> {
                                if (r == 0.0) throw MathEvaluationException("División por cero al evaluar f(x).")
                                prev.evaluate(x) / r
                            }
                        }
                    }
                } else break
            }
            return left
        }

        private fun parsePower(): Node {
            val base = parseUnary()
            val t = peek()
            return if (t is Token.Op && t.symbol == '^') {
                advance()
                val exponent = parsePower() // asociativo por la derecha
                Node { x -> base.evaluate(x).pow(exponent.evaluate(x)) }
            } else base
        }

        private fun parseUnary(): Node {
            val t = peek()
            if (t is Token.Op && (t.symbol == '-' || t.symbol == '+')) {
                advance()
                val operand = parseUnary()
                return if (t.symbol == '-') Node { x -> -operand.evaluate(x) } else operand
            }
            return parsePrimary()
        }

        private fun parsePrimary(): Node {
            val t = peek() ?: throw IllegalArgumentException("Se esperaba un valor u operador y la expresión terminó antes.")
            return when (t) {
                is Token.Num -> {
                    advance(); Node { t.value }
                }
                is Token.Ident -> parseIdentifier(t)
                Token.LParen -> {
                    advance()
                    val inner = parseExpression()
                    if (peek() != Token.RParen) throw IllegalArgumentException("Falta un paréntesis de cierre ')'.")
                    advance()
                    inner
                }
                else -> throw IllegalArgumentException("Token inesperado en la expresión.")
            }
        }

        private fun parseIdentifier(t: Token.Ident): Node {
            advance()
            val name = t.name.lowercase()
            // variable x
            if (name == "x") return Node { x -> x }
            // constantes
            when (name) {
                "pi" -> return Node { PI }
                "e" -> return Node { E }
            }
            // función: nombre '(' args ')'
            if (peek() == Token.LParen) {
                advance()
                val args = mutableListOf<Node>()
                if (peek() != Token.RParen) {
                    args += parseExpression()
                    while (peek() == Token.Comma) {
                        advance()
                        args += parseExpression()
                    }
                }
                if (peek() != Token.RParen) throw IllegalArgumentException("Falta ')' al cerrar la función '$name'.")
                advance()
                return buildFunctionNode(name, args)
            }
            throw IllegalArgumentException("Identificador no reconocido: '$name'. Usa 'x' como variable.")
        }

        private fun buildFunctionNode(name: String, args: List<Node>): Node {
            fun arg1(): Node = args.getOrNull(0)
                ?: throw IllegalArgumentException("La función '$name' requiere un argumento.")

            return when (name) {
                "sin" -> Node { x -> sin(arg1().evaluate(x)) }
                "cos" -> Node { x -> cos(arg1().evaluate(x)) }
                "tan" -> Node { x -> tan(arg1().evaluate(x)) }
                "asin" -> Node { x -> asin(arg1().evaluate(x)) }
                "acos" -> Node { x -> acos(arg1().evaluate(x)) }
                "atan" -> Node { x -> atan(arg1().evaluate(x)) }
                "sinh" -> Node { x -> sinh(arg1().evaluate(x)) }
                "cosh" -> Node { x -> cosh(arg1().evaluate(x)) }
                "tanh" -> Node { x -> tanh(arg1().evaluate(x)) }
                "exp" -> Node { x -> exp(arg1().evaluate(x)) }
                "ln" -> Node { x ->
                    val v = arg1().evaluate(x)
                    if (v <= 0.0) throw MathEvaluationException("ln(x) requiere x > 0.")
                    ln(v)
                }
                "log", "log10" -> Node { x ->
                    val v = arg1().evaluate(x)
                    if (v <= 0.0) throw MathEvaluationException("log(x) requiere x > 0.")
                    log10(v)
                }
                "log2" -> Node { x ->
                    val v = arg1().evaluate(x)
                    if (v <= 0.0) throw MathEvaluationException("log2(x) requiere x > 0.")
                    ln(v) / ln(2.0)
                }
                "sqrt" -> Node { x ->
                    val v = arg1().evaluate(x)
                    if (v < 0.0) throw MathEvaluationException("sqrt(x) requiere x >= 0.")
                    sqrt(v)
                }
                "cbrt" -> Node { x -> cbrt(arg1().evaluate(x)) }
                "abs" -> Node { x -> abs(arg1().evaluate(x)) }
                else -> throw IllegalArgumentException("Función no soportada: '$name'.")
            }
        }
    }
}