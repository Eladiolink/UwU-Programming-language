package compiler.symbolTable

enum class ValueType {
    VALUE_IDENTIFICADOR,
    VALUE_CHAR,
    VALUE_STR,
    VALUE_INT,
    VALUE_FLOAT,
    VALUE_RACIONAL
}

enum class IdentificadorType {
    FUNC,
    ARG,
    VARIABLE,
    PROGRAM
}

data class EntrySymbol(val tokenValue: Any, var valueType: ValueType, var identificador: IdentificadorType? = null)

typealias SymbolTable = MutableList<EntrySymbol>
