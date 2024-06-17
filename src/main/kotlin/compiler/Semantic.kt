package compiler.semantic

import compiler.parserTools.*
import compiler.symbolTable.*
import compiler.token.*
import compiler.exceptions.*


fun run(ast: AstNode, table: SymbolTable): Result<AstNode> {
    var err = astLoop(ast, table, ::tipaIdentificadores)
    if (err != null) {
        return Result.failure(err)
    }
    err = astLoop(ast, table, ::checkSemantica)
    if (err != null) {
        return Result.failure(err)
    }
    return Result.success(ast)
}

fun astLoop(ast: AstNode, table: SymbolTable, func: (AstNode, SymbolTable) -> Throwable?): Throwable? {
    if (ast.value.type == NodeType.TERMINAL) {
        return null
    }
    for (node in ast.children) {
        val err = astLoop(node, table, func)
        if (err != null) {
            return err
        }
    }
    return func(ast, table)
}

fun tipaIdentificadores(ast: AstNode, table: SymbolTable): Throwable? {
    if (ast.value.type == NodeType.DEC || ast.value.type == NodeType.ARGS) {
        val type = getType(ast.children[0].children[0].value.token)
        val ident = ast.children[1].value.token!!
        val entry = ident.getEntry(table)!!
        entry.valueType = type!!
        if (entry.identificador != null) {
            return Redeclaracao(ident.getLineNumber(), ident.tokenStr)
        }
        if (ast.value.type == NodeType.DEC) {
            entry.identificador = IdentificadorType.VARIABLE
        } else {
            entry.identificador = IdentificadorType.ARG
        }
    }
    if (ast.value.type == NodeType.FUN) {
        val ident = ast.children[1].value.token!!
        val entry = ident.getEntry(table)!!
        if (entry.identificador != null) {
            return Redeclaracao(ident.getLineNumber(), ident.tokenStr)
        }
        entry.identificador = IdentificadorType.FUNC
    }
    if (ast.value.type == NodeType.PROG) {
        val ident = ast.children[1].value.token!!
        ident.getEntry(table)!!.identificador = IdentificadorType.PROGRAM
    }
    return null
}

fun checkSemantica(ast: AstNode, table: SymbolTable): Throwable? {
    return when (ast.value.type) {
        NodeType.DEC -> decCheck(ast, table)
        else -> null
    }
}

fun decCheck(ast: AstNode, table: SymbolTable): Throwable? {
    val ident = ast.children[1].value.token!!
    val dec_type = ident.getEntry(table)!!.valueType
    if (ast.children.count() == 4) {
        return decSimpleCheck(ast, table, dec_type)
    } else {
        return decAriCheck(ast, table, dec_type)
    }
}

fun decSimpleCheck(ast: AstNode, table: SymbolTable, dec_type: ValueType): Throwable? {
    val value = ast.children[3].value.token!!
    val value_type = value.getEntry(table)!!.valueType
    if (dec_type == value_type) {
        return null
    } else {
        return Throwable("Na linha ${value.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${value.tokenStr}.")
    }
}

fun decAriCheck(ast: AstNode, table: SymbolTable, dec_type: ValueType): Throwable? {
    val op1 = ast.children[3].value.token!!
    val op1_type = op1.getEntry(table)!!.valueType
    val op2 = ast.children[5].value.token!!
    val op2_type = op2.getEntry(table)!!.valueType
    if (dec_type != op1_type) {
        return Throwable("Na linha ${op1.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${op1.tokenStr}.")
    }
    if (dec_type != op2_type) {
        return Throwable("Na linha ${op2.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${op2.tokenStr}.")
    }
    return null
}

fun getType(token: Token?): ValueType? {
    if (token == null) {
        return null
    }
    return when (token.tokenStr) {
        "char" -> ValueType.VALUE_CHAR
        "string" -> ValueType.VALUE_STR
        "int" -> ValueType.VALUE_INT
        "float" -> ValueType.VALUE_FLOAT
        else -> null
    }
}

