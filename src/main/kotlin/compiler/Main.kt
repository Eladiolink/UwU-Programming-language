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
                    "type: ${token.type}, value: '${token.tokenStr}', reference: ${token.value}, line: ${token.getLineNumber()}"
                )
            }
        } else {
            exitErro(tokens.exceptionOrNull()?.message ?: "", -1)
        }
        println("Table: ")
        for (entry in table) {
            println(entry)
        }
        println("Is running: ${compiler.lexer.isRunning()}")
    } else {
        exitErro(resGetFile.exceptionOrNull()?.message ?: "", -1)
    }
}

fun getFile(path: String): Result<String> {
    val arquivo = File(path)
    if (!arquivo.exists()) {
        return Result.failure(IllegalArgumentException("Arquivo não encontrado: $path"))
    }
    return Result.success(arquivo.readText())

}

fun exitErro(message: String, valueErro: Int) {
    println(message)
    exitProcess(valueErro)
}
