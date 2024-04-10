package compiler

import java.io.File
import kotlin.Result
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isNullOrEmpty()) exitErro("Nenhum arquivo informado!", -1)

    var resGetFile = getFile(args[0])

    if (resGetFile.isSuccess) {
        val program = resGetFile.getOrThrow()
        println("\nArquivo do Programa:")
        println(program)
    } else {
        val excecao = resGetFile.exceptionOrNull()
        println("Erro ao ler o programa:\n$excecao\n")
        exitProcess(-1)
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

fun exitErro(message: String, valueErro: Int) {
    println(message)
    exitProcess(valueErro)
}
