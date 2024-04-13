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

            // TODO: Trocar RESERVADA_CHAR pelo nome do token, trocar 0u pela posição na tabela de

            if (type in listOf(TokenType.IDENTIFICADORES, TokenType.PALAVRAS)) {
                val lineTable = EntrySymbol(tokenStr, entryType(tokenStr))
                if (lineTable !in table) {
                    table.add(lineTable)
                }

                var index = table.indexOf(lineTable)

                tokens.add(
                        Token(
                                type,
                                TokenName.RESERVADA_CHAR,
                                index.toUInt(),
                                tokenStr,
                                lineCount.toUInt()
                        )
                )
                continue
            }

            tokens.add(Token(type, TokenName.RESERVADA_CHAR, null, tokenStr, lineCount.toUInt()))
        } else {
            return Result.failure(result.exceptionOrNull() ?: Throwable())
        }
    }
    return Result.success(tokens)
}

fun entryType(token: String): ValueType {
    if (token.matches(Regex("[a-zA-Z]\\w+"))) return ValueType.VALUE_IDENTIFICADOR

    if (token.matches(Regex("-?(\\d+)(\\.)(\\d+)"))) return ValueType.VALUE_FLOAT
    if (token.matches(Regex("-?(\\d+)(\\|)(\\d+)"))) return ValueType.VALUE_RACIONAL
    if (token.matches(Regex("-?\\d+"))) return ValueType.VALUE_INT
    if (token.matches(Regex("'[^']'"))) return ValueType.VALUE_CHAR

    return ValueType.VALUE_STR
}

fun matchToken(text: String): Result<Triple<TokenType, String, String>> {
    val reservadaReg =
            Regex("char|string|int|float|rational|program|if|else|while|input|print|return")
    val regexes =
            listOf(
                    TokenType.COMENTARIO to Regex("--.*\\n|-\\{(.*|\\n)*-\\}"),
                    TokenType.IDENTIFICADORES to Regex("[a-zA-Z]\\w+"),
                    TokenType.PALAVRAS to Regex("-?(\\d+)(\\.|\\|)(\\d+)|-?\\d+|'[^']'|\"[^\"]*\""),
                    TokenType.RELACIONAIS to Regex("<=|>=|<|>|!=|!!"),
                    TokenType.SIMBOLOS to Regex("[,;()\\[\\]{}=+\\-*/%<>&|~!]"),
                    TokenType.ESPACO to Regex("\\s+"),
            )
    for ((type, reg) in regexes) {
        val match = reg.matchAt(text, 0)
        if (match != null) {
            val rem = text.substring(match.range.endInclusive + 1)
            if (type == TokenType.IDENTIFICADORES && reservadaReg.matches(match.value)) {
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
