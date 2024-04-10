
package compiler.symbolTable

enum class ValueType {
    VALUE_CHAR,
    VALUE_STR,
    VALUE_INT,
    VALUE_FLOAT
}

data class EntrySymbol(val tokenValue: Any, val valueType: ValueType)

typealias SymbolTable = MutableList<EntrySymbol>

