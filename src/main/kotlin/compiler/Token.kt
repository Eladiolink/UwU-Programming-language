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
        val value: UInt,
        val tokenStr: String,
        val filepath: String,
        val line: UInt
) {
    fun getLineNumber(): UInt {
        return this.line
    }
}

enum class TokenName {
    RESERVADA_CHAR,
    RESERVADA_INT,
    RESERVADA_STRING,
    RESERVADA_FLOAT,
    RESERVADA_ROTIONAL,
    RESERVADA_PROGRAM,
    RESERVADA_IF,
    RESERVADA_ELSE,
    RESERVADA_WHILE,
    RESERVADA_INPUT,
    RESERVADA_PRINT,
    RESERVADA_RETURN,
    //
    ESPECIAL_VIRGULA,
    ESPECIAL_PONTO_E_VIRGULA,
    ESPECIAL_PARENTESE_ESQUERDO,
    ESPECIAL_PARENTESE_DIREITO,
    ESPECIAL_COLCHETE_ESQUERDO,
    ESPECIAL_COLCHETE_DIREITO,
    ESPECIAL_CHAVE_ESQUERDO,
    ESPECIAL_CHAVE_DIREITO,
    ESPECIAL_IGUAL,
    ESPECIAL_MAIS,
    ESPECIAL_MENOS,
    ESPECIAL_ASTERISCO,
    ESPECIAL_BARRA,
    ESPECIAL_PORCENTAGEM,
    ESPECIAL_MAIOR_QUE,
    ESPECIAL_MENOR_QUE,
    ESPECIAL_E_COMECIAL,
    ESPECIAL_PIPE,
    ESPECIAL_TIL,
    ESPECIAL_EXCLAMACAO,
    //
    RELACAO_MENOR_IGUAL,
    RELACAO_MAIOR_IGUAL,
    RELACAO_MAIOR_QUE,
    RELACAO_MENOR_QUE,
    RELACAO_EXCLAMACAO_IGUAL,
    RELACAO_DUPLA_EXCLAMACAO,
    //
    CARACTER_EM_BRANCO,
    COMENTARIO,
    PALAVRA,
    IDENTIFICADOR
}
