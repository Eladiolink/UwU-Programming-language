package compiler.symbolTable

enum class ValueType {
    VALUE_IDENTIFICADOR,
    VALUE_CHAR,
    VALUE_STR,
    VALUE_INT,
    VALUE_FLOAT,
    VALUE_RACIONAL
}

data class EntrySymbol(val tokenValue: Any, val valueType: ValueType)

typealias SymbolTable = MutableList<EntrySymbol>
