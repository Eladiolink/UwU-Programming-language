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

typealias FunArgs = MutableList<ValueType>

data class EntrySymbol(val tokenValue: Any, var valueType: ValueType, var identificador: IdentificadorType? = null,var  isMountPoint: Boolean = false, val args: FunArgs = mutableListOf())

typealias SymbolTable = MutableList<EntrySymbol>
