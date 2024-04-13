package compiler.lexer

import compiler.symbolTable.*
import compiler.token.*

var running: Boolean = false

fun run(text: String, table: SymbolTable): Result<List<Token>> {
    running = true
    var tokens: MutableList<Token> = mutableListOf()
    var rem = text
    var lineCount = 1

    while (rem != "") {
        val result = matchToken(rem)
        if (result.isSuccess) {
            val (type, tokenStr, res) = result.getOrThrow()
            var linesToAdd = 0
            var index: UInt? = null
            rem = res

            if (type in listOf(TokenType.IDENTIFICADORES, TokenType.PALAVRAS)) {
                val entryType = getEntryType(tokenStr)
                val lineTable = EntrySymbol(tokenStr, entryType)
                if (lineTable !in table) {
                    table.add(lineTable)
                }

                index = table.indexOf(lineTable).toUInt()
                if (type == TokenType.PALAVRAS && entryType == ValueType.VALUE_STR) {
                    linesToAdd = tokenStr.count({ c: Char -> c == '\n'})
                }

            }
            if (type in listOf(TokenType.ESPACO, TokenType.COMENTARIO)) {
                linesToAdd = tokenStr.count({ c: Char -> c == '\n'})
            }
            // TODO: Trocar RESERVADA_CHAR pelo nome do token
            tokens.add(Token(type, TokenName.RESERVADA_CHAR, index, tokenStr, lineCount.toUInt()))
            lineCount += linesToAdd


        } else {
            return Result.failure(result.exceptionOrNull() ?: Throwable())
        }
    }
    running = false
    return Result.success(tokens)
}

fun isRunning(): Boolean {
    return running
}

fun getEntryType(token: String): ValueType {
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

