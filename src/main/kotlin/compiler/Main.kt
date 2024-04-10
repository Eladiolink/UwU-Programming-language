package compiler 


fun main() {
    val table: compiler.symbolTable.SymbolTable = mutableListOf()
    println("Tokens: ")
    println(compiler.lexer.run("teste", table))
    println("Table: ")
    println(table)
}
