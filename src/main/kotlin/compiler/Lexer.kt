package compiler.lexer

import compiler.token.*
import compiler.symbolTable.*

fun run(text: String, table: SymbolTable): List<Token> {
    val t0 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    table.add(EntrySymbol('h', ValueType.VALUE_CHAR))
    val t1 = Token(TokenType.PALAVRAS, 0u, "0", "example/UwU.u", 1u)
    table.add(EntrySymbol("Hello", ValueType.VALUE_STR))
    return listOf(t0, t1)
}
