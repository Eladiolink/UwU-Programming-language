package compiler.lexer

import compiler.symbolTable.*
import compiler.token.*

fun run(text: String, table: SymbolTable): Result<List<Token>> {
    var tokens: MutableList<Token> = mutableListOf()
    var rem = text
    var lineTotal = contarCaracteresEspecificos(text, '\n')
    var lineCount: Int

    while (rem != "") {
        val result = matchToken(rem)
        if (result.isSuccess) {
            val (type, tokenStr, res) = result.getOrThrow()
            rem = res
            lineCount = (lineTotal - contarCaracteresEspecificos(rem, '\n')) + 1

            if (lineCount > lineTotal) break

            print("Token: $tokenStr")
            // TODO: Trocar RESERVADA_CHAR pelo nome do token, trocar 0u pela posição na tabela de
            // simbolos
            // TODO: Popular tabela de simbolo
            tokens.add(Token(type, TokenName.RESERVADA_CHAR, 0u, tokenStr, lineCount.toUInt()))
        } else {
            return Result.failure(result.exceptionOrNull() ?: Throwable())
        }
    }
    return Result.success(tokens)
}

fun matchToken(text: String): Result<Triple<TokenType, String, String>> {
    val reservadaReg =
            Regex("char|string|int|float|rational|program|if|else|while|input|print|return")
    val regexes =
            listOf(
                    TokenType.IDENTIFICADORES to Regex("[a-zA-Z]\\w+"),
                    // TODO: Suportar mais palavras. A regex abaixo suporta inteiro e char
                    TokenType.PALAVRAS to Regex("-?\\d+|'[^']'"),
                    TokenType.SIMBOLOS to Regex("[,;()\\[\\]{}=+\\-*/%<>&|~!]"),
                    TokenType.ESPACO to Regex("\\s+")
            )
    for ((type, reg) in regexes) {
        val match = reg.matchAt(text, 0)
        if (match != null) {
            val rem = text.substring(match.range.endInclusive + 1)
            if (type == TokenType.IDENTIFICADORES && reservadaReg.matchAt(text, 0) != null) {
                return Result.success(Triple(TokenType.RESERVADAS, match.value, rem))
            }
            return Result.success(Triple(type, match.value, rem))
        }
    }
    val maxToken = 10
    val length = if (text.count() < maxToken) text.count() else maxToken
    val msg = "O token não foi reconhecido: ${text.substring(0, length)}"
    return Result.failure(Throwable(msg))
}

fun contarCaracteresEspecificos(texto: String, caracterEspecifico: Char): Int {
    var contador = 0
    for (caracter in texto) {
        if (caracter == caracterEspecifico) {
            contador++
        }
    }
    return contador
}
