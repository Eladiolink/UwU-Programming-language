package compiler.token

enum class TokenType {
    COMENTARIO,
    ESPACO,
    RESERVADAS,
    SIMBOLOS,
    RELACIONAIS,
    IDENTIFICADORES,
    PALAVRAS
}

data class Token(
        val type: TokenType,
        val value: Any,
        val tokenStr: String,
        val filepath: String,
        val line: UInt
) {
    fun getLineNumber(): UInt {
        return this.line
    }
}
