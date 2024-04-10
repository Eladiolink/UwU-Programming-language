package compiler.lexer

import compiler.symbol.*
import compiler.token.*
<<<<<<< HEAD
import kotlin.io.println

fun run(text: String): List<Token> {

=======
import compiler.symbolTable.*

fun run(text: String, table: SymbolTable): List<Token> {
>>>>>>> 9fe63f472e1308fdccd309eeacf54cc7ed26cc27
    val t0 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    table.add(EntrySymbol('h', ValueType.VALUE_CHAR))
    val t1 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
<<<<<<< HEAD

    val element: Symbol? =
            createSybol(
                    "-{fadasdasd sadasdas asdsadasd  sadasdas \n asdsdasd 334234;;;;;~\\.x.c,c,zçççççáaaaaáááéréãss-}"
            )

    if (element != null) {
        println(element.value)
        println(element.type)
    }

=======
    table.add(EntrySymbol("Hello", ValueType.VALUE_STR))
>>>>>>> 9fe63f472e1308fdccd309eeacf54cc7ed26cc27
    return listOf(t0, t1)
}
