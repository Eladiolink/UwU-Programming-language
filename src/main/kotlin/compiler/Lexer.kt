package compiler.lexer

import compiler.token.*

fun run(text: String): List<Token> {
    val t0 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    val t1 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    return listOf(t0, t1)
}
