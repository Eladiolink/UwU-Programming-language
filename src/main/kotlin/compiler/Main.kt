package compiler

import java.io.File
import kotlin.Result

fun main(args: Array<String>) {
    var resGetFile = getFile(args[0])
    var program: String = ""

    if (resGetFile.isSuccess) {
        program = resGetFile.getOrThrow()
        println("\nArquivo do Programa:")
        println(program)
    } else {
        val excecao = resGetFile.exceptionOrNull()
        println("Erro ao ler o programa:\n$excecao\n")
    }

    val table: compiler.symbolTable.SymbolTable = mutableListOf()

    println("Tokens: ")
    println(compiler.lexer.run("test", table))
    println("Table: ")
    println(table)
}

fun getFile(path: String): Result<String> {
    return runCatching {
        val arquivo = File(path)

        if (!arquivo.exists()) {
            throw IllegalArgumentException("Arquivo não encontrado: $path")
        }
        arquivo.readText()
    }
}
