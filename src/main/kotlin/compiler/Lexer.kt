package compiler.lexer

import compiler.symbol.*
import compiler.symbolTable.*
import compiler.token.*
import kotlin.io.println

fun run(text: String, table: SymbolTable): List<Token> {

    val element: Symbol? =
            createSybol(
                    "-{fadasdasd sadasdas asdsadasd  sadasdas \n asdsdasd 334234;;;;;~\\.x.c,c,zçççççáaaaaáááéréãss-}"
            )

    if (element != null) {
        println(element.value)
        println(element.type)
    }

    table.add(EntrySymbol("Hello", ValueType.VALUE_STR))

    val t0 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    table.add(EntrySymbol('h', ValueType.VALUE_CHAR))
    val t1 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    table.add(EntrySymbol("Hello", ValueType.VALUE_STR))
    return listOf(t0, t1)
}
