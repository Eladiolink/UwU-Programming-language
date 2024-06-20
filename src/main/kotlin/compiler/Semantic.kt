package compiler.semantic

import compiler.exceptions.*
import compiler.naryTree.*
import compiler.parser.type
import compiler.parserTools.*
import compiler.symbolTable.*
import compiler.token.*

var countReturn = 0

fun run(ast: AstNode, table: SymbolTable): Result<AstNode> {

    // definir ponto de momtagem
    var err = defineMountPoint(ast, table)

    if (err != null) {
        return Result.failure(err)
    }

    err = astLoop(ast, table, ::tipaIdentificadores)

    if (err != null) {
        return Result.failure(err)
    }
    err = astLoop(ast, table, ::checkSemantica)
    if (err != null) {
        return Result.failure(err)
    }

    if (countReturn == 0) {
        return Result.failure(Throwable("O programa deve ter pelo menos 1 return"))
    }

    return Result.success(ast)
}

fun defineMountPoint(ast: AstNode, table: SymbolTable): Throwable? {
    var t = findLastInserted(ast, NodeType.FUN)

    if (t != null) {

        val ident = t.children[1].value.token!!
        val entry = ident.getEntry(table)!!
        entry.isMountPoint = true
        return null
    }

    return Throwable("Não há definição de ponto de montagem")
}

fun astLoop(
        ast: AstNode,
        table: SymbolTable,
        func: (AstNode, SymbolTable) -> Throwable?
): Throwable? {
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

fun retCheck(ast: AstNode, table: SymbolTable): Throwable? {
    countReturn++
    val token = ast.children[1].value.token!!
    if (token.type == TokenType.IDENTIFICADORES) {
        val entry = token.getEntry(table)!!
        if (!isVar(entry)) {
            return Throwable("Na linha ${token.getLineNumber()}, o identificador retornado não é uma variável ou argumento")
        }
    }
    return null
}

fun callCheck(ast: AstNode, table: SymbolTable): Throwable? {
    val ident = ast.children[0].value.token!!
    val entry = ident.getEntry(table)!!
    if (entry.identificador != IdentificadorType.FUNC) {
        return Throwable("Na linha ${ident.getLineNumber()}, o identificador ${entry.tokenValue} não é uma função.")
    }
    val call_idents = findTokensAst(ast.children[2], TokenType.IDENTIFICADORES)
    val call_types = call_idents.map { it.getEntry(table)!!.valueType }
    if (entry.args.size != call_types.size) {
        return Throwable(
                "A função ${entry.tokenValue} é esperado ${entry.args.size} argumentos, no entanto foram informados ${call_types.size} argumentos."
        )
    }
    for (key in entry.args.indices) {
        if (!isVar(call_idents[key].getEntry(table)!!)) {
            return Throwable(
                "Na chamada da função ${entry.tokenValue} o argumento ${call_idents[key].tokenStr} não é uma variável ou argumento"
            )
        }
        if (entry.args[key] != call_types[key]) {
            return Throwable(
                    "Na chamada da função ${entry.tokenValue} o argumento ${call_idents[key].tokenStr} não é do tipo esperado ${entry.args[key]}"
            )
        }
    }
    return null
}

fun funCheck(ast: AstNode, table: SymbolTable): Throwable? {
    val ident = ast.children[1].value.token!!
    val entry = ident.getEntry(table)!!

    if (entry.identificador == IdentificadorType.FUNC) {
        val typeFunc: ValueType = entry.valueType
        val tkn = ast.children[ast.children.size - 2]

        var t = findLastInserted(tkn, NodeType.DEC)

        if (t != null) {
            val element = t.children[t.children.size - 3].value.token!!
            var lineOfTable = table.find { it.tokenValue == element.tokenStr }!!
            if (lineOfTable.valueType != typeFunc) {
                return Throwable(
                        "A função ${entry.tokenValue} é esperado token do tipo $typeFunc, ao invés de ${lineOfTable.valueType}."
                )
            }
        }
    }

    return null
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
        val type = getType(ast.children[0].children[0].value.token)
        val entry = ident.getEntry(table)!!
        entry.valueType = type!!

        if (entry.identificador != null) {
            return Redeclaracao(ident.getLineNumber(), ident.tokenStr)
        }
        entry.identificador = IdentificadorType.FUNC
        if (ast.children[5].value.type == NodeType.ARGS) {
            val types = findTokensAst(ast.children[5], TokenType.RESERVADAS)
            entry.args.addAll(types.map { getType(it)!! })
        }
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
        NodeType.FUN -> funCheck(ast, table)
        NodeType.CALL -> callCheck(ast, table)
        NodeType.LOOP -> relCheck(ast, table)
        NodeType.IF -> relCheck(ast, table)
        NodeType.RET -> retCheck(ast, table)
        else -> null
    }
}

fun relCheck(ast: AstNode, table: SymbolTable): Throwable? {
    val rel_ast = ast.children[2]
    val ident = findTokensAst(rel_ast, TokenType.IDENTIFICADORES)
    var ident_type: ValueType? = null
    if (!ident.isEmpty()) {
        ident_type = ident[0].getEntry(table)!!.valueType
        if (!isVar(ident[0].getEntry(table)!!)) {
            return Throwable("Na linha ${ident[0].getLineNumber()}, o identificador ${ident[0].tokenStr} não é uma variável ou argumento")
        }
        for (i in 1..ident.count() - 1) {
            if (!isVar(ident[i].getEntry(table)!!)) {
                return Throwable("Na linha ${ident[i].getLineNumber()}, o identificador ${ident[i].tokenStr} não é uma variável ou argumento")
            }
            if (ident[i].getEntry(table)!!.valueType != ident_type) {
                return Throwable(
                        "Na linha ${ident[i].getLineNumber()}, o identificador ${ident[i].tokenStr} deve ser do tipo $ident_type"
                )
            }
        }
    }
    val palavras = findTokensAst(rel_ast, TokenType.PALAVRAS)
    if (!palavras.isEmpty()) {
        val palavras_type = palavras[0].getEntry(table)!!.valueType
        if (palavras_type != ident_type) {
            return Throwable(
                    "Na linha ${palavras[0].getLineNumber()}, a palavra ${palavras[0].tokenStr} deve ser do mesmo tipo do identificador ${ident[0].tokenStr}"
            )
        }
        for (i in 1..palavras.count() - 1) {
            if (palavras[i].getEntry(table)!!.valueType != palavras_type) {
                return Throwable(
                        "Na linha ${palavras[i].getLineNumber()}, a palavra ${palavras[i].tokenStr} deve ser do tipo $palavras_type"
                )
            }
        }
    }
    return null
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
        return Throwable(
                "Na linha ${value.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${value.tokenStr}."
        )
    }
}

fun decAriCheck(ast: AstNode, table: SymbolTable, dec_type: ValueType): Throwable? {
    val op1 = ast.children[3].value.token!!
    val op1_type = op1.getEntry(table)!!.valueType
    val op2 = ast.children[5].value.token!!
    val op2_type = op2.getEntry(table)!!.valueType
    if (!isVar(op1.getEntry(table)!!)) {
        return Throwable("Na linha ${op1.getLineNumber()}, o identificador ${op1.tokenStr} não é uma variável ou argumento")
    }
    if (dec_type != op1_type) {
        return Throwable(
                "Na linha ${op1.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${op1.tokenStr}."
        )
    }
    if (!isVar(op2.getEntry(table)!!)) {
        return Throwable("Na linha ${op2.getLineNumber()}, o identificador ${op2.tokenStr} não é uma variável ou argumento")
    }
    if (dec_type != op2_type) {
        return Throwable(
                "Na linha ${op2.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${op2.tokenStr}."
        )
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

fun findLastInserted(node: AstNode?, type: NodeType): AstNode? {
    if (node == null) return null

    var lastInserted: AstNode? = null

    // Primeiro, processa todos os filhos
    for (child in node.children) {
        val result = findLastInserted(child, type)
        if (result != null) {
            lastInserted = result
        }
    }

    // Depois, verifica o nó atual
    if (node.value.type == type) {
        lastInserted = node
    }

    return lastInserted
}
