package compiler.semantic

import compiler.exceptions.*
import compiler.naryTree.*
import compiler.parser.type
import compiler.parserTools.*
import compiler.symbolTable.*
import compiler.token.*

fun run(ast: AstNode, table: SymbolTable, tokens: List<Token>): Result<AstNode> {
    var err = astLoop(ast, table, tokens, ::tipaIdentificadores)

    if (err != null) {
        return Result.failure(err)
    }
    err = astLoop(ast, table, tokens, ::checkSemantica)
    if (err != null) {
        return Result.failure(err)
    }

    return Result.success(ast)
}

fun astLoop(
        ast: AstNode,
        table: SymbolTable,
        tokens: List<Token>,
        func: (AstNode, SymbolTable, List<Token>) -> Throwable?
): Throwable? {
    if (ast.value.type == NodeType.TERMINAL) {
        return null
    }
    for (node in ast.children) {
        val err = astLoop(node, table, tokens, func)
        if (err != null) {
            return err
        }
    }
    return func(ast, table, tokens)
}

fun callCheck(ast: AstNode, table: SymbolTable, tokens: List<Token>): Throwable? {
    val ident = ast.children[0].value.token!!
    val entry = ident.getEntry(table)!!

    var argsIndice = 0

    for(key in tokens.indices){
        if(tokens[key].tokenStr == entry.tokenValue){
            if(tokens[key+2].tokenStr == "input"){
                argsIndice = key+2
            }
        }
    }


    var current = tokens[argsIndice]
    val parametsTypes = mutableListOf<ValueType>()
    val parametsName = mutableListOf<String>()
    while(current.tokenStr != ")"){
        when(current.tokenStr){
            "int"-> parametsTypes.add(ValueType.VALUE_INT)
            "float" -> parametsTypes.add(ValueType.VALUE_FLOAT)
            "char" -> parametsTypes.add(ValueType.VALUE_CHAR)
            "string"-> parametsTypes.add(ValueType.VALUE_STR)
        }

        when(current.tokenStr){
            "int"-> parametsName.add(tokens[argsIndice+2].tokenStr)
            "float" -> parametsName.add(tokens[argsIndice+2].tokenStr)
            "char" -> parametsName.add(tokens[argsIndice+2].tokenStr)
            "string"-> parametsName.add(tokens[argsIndice+2].tokenStr)
        }
        
        current = tokens[argsIndice+1]
        argsIndice++
    }
    val inTableOfSymbols = findAllInOrder(ast,table)
    val paramentsCall = mutableListOf<EntrySymbol>()

    for(key in inTableOfSymbols.indices){
        if(key == 0) continue
        var token = inTableOfSymbols[key].value.token!!
        var a = table.find { it.tokenValue == token.tokenStr }!!
        paramentsCall.add(a)
    }

    if(parametsTypes.size != paramentsCall.size) 
        return Throwable("A função ${entry.tokenValue} é esperado ${parametsTypes.size} argumentos, no entanto foram informados ${paramentsCall.size} argumentos.")
    
    for(key in paramentsCall.indices){
        val element = paramentsCall[key].tokenValue
        if(paramentsCall[key].valueType != parametsTypes[key])
            return Throwable("Na função ${entry.tokenValue} é esperado ${parametsTypes[key]} em ${parametsName[key]}, no entanto ${element} é ${paramentsCall[key].valueType}.")
    }
    return null
}
   
fun funCheck(ast: AstNode, table: SymbolTable, tokens: List<Token>): Throwable? {
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

fun tipaIdentificadores(ast: AstNode, table: SymbolTable, tokens: List<Token>): Throwable? {
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
    }
    if (ast.value.type == NodeType.PROG) {
        val ident = ast.children[1].value.token!!
        ident.getEntry(table)!!.identificador = IdentificadorType.PROGRAM
    }
    return null
}

fun checkSemantica(ast: AstNode, table: SymbolTable, tokens: List<Token>): Throwable? {
    return when (ast.value.type) {
        NodeType.DEC -> decCheck(ast, table)
        NodeType.FUN -> funCheck(ast, table, tokens)
        NodeType.CALL -> callCheck(ast, table, tokens)
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
    if (dec_type != op1_type) {
        return Throwable(
                "Na linha ${op1.getLineNumber()} é esperado token do tipo $dec_type, ao invés de ${op1.tokenStr}."
        )
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

fun findAllInOrder(node: AstNode?, table: SymbolTable, result: MutableList<AstNode> = mutableListOf()): List<AstNode> {
    if (node == null) return result

    val mid = node.children.size / 2

    // Processa os primeiros n/2 filhos
    for (i in 0 until mid) {
        findAllInOrder(node.children[i], table, result)
    }

    // Processa o nó atual
    val token = node.value.token
    if (token != null) {
        val foundSymbol = table.find { it.tokenValue == token.tokenStr }
        if (foundSymbol != null) {
            result.add(node)
        }
    }

    // Processa os restantes n/2 filhos
    for (i in mid until node.children.size) {
        findAllInOrder(node.children[i], table, result)
    }

    return result
}

