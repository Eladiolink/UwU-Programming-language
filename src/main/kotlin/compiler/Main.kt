package compiler

fun main(args: Array<String>) {
    val table: compiler.symbolTable.SymbolTable = mutableListOf()
    println("Tokens: ")
    println(compiler.lexer.run("teste", table))
    println("Table: ")
    println(table)
    println(args[0])
}
