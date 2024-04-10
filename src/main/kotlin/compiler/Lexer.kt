package compiler.lexer

import compiler.symbol.*
import compiler.token.*
import kotlin.io.println

fun run(text: String): List<Token> {

    val t0 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    val t1 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)

    val element: Symbol? =
            createSybol(
                    "-{fadasdasd sadasdas asdsadasd  sadasdas \n asdsdasd 334234;;;;;~\\.x.c,c,zçççççáaaaaáááéréãss-}"
            )

    if (element != null) {
        println(element.value)
        println(element.type)
    }

    return listOf(t0, t1)
}
