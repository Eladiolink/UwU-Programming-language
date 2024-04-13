package compiler

import java.io.File
import kotlin.Result
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    if (args.isNullOrEmpty()) exitErro("Nenhum arquivo informado!", -1)

    var resGetFile = getFile(args[0])

    if (resGetFile.isSuccess) {
        val program = resGetFile.getOrThrow()

        val table: compiler.symbolTable.SymbolTable = mutableListOf()
        val tokens = compiler.lexer.run(program, table)

        println("Tokens: ")
        if (tokens.isSuccess) {
            for (token in tokens.getOrThrow()) {
                println(
                        "type: ${token.type}, value: '${token.tokenStr}, reference: ${token.value}, line: ${token.line}'"
                )
            }
        } else {
            exitErro(tokens.exceptionOrNull()?.message ?: "", -1)
        }
        println("Table: ")
        for (i in 0 until table.size) {
            println(table[i])
        }
    } else {
        val excecao = resGetFile.exceptionOrNull()
        exitErro("Erro ao ler o programa:\n$excecao\n", -1)
    }
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
