package compiler.parser

import compiler.symbolTable.*
import compiler.token.*
import compiler.tokenMatch.*


data class ParserState(
    val tokens: List<Token>,
    var lookahead: Int,
    var error: Throwable?,
) {
    fun next(): Token {
        return tokens[lookahead]
    }

    fun incNew(): ParserState {
        return ParserState(tokens, lookahead + 1, error)
    }

    fun errorNew(error_new: Throwable): ParserState {
        return ParserState(tokens, lookahead, error_new)
    }

    fun hasToken(): Boolean {
        return lookahead < tokens.size 
    }

    fun success(): Boolean {
        return error == null
    }
}


fun run(tokens: List<Token>, table: SymbolTable): Result<Int> {
    val useful_tokens = tokens.filter { it.type != TokenType.ESPACO && it.type != TokenType.COMENTARIO }
    var state = prog(ParserState(useful_tokens, 0, null))
    if (state.success()) {
        println("Parser realizado com sucesso")
        println(state)
        return Result.success(0)
    } else {
        println("Erro ao realizar parser")
        return Result.failure(state.error ?: Throwable())
    }
}

fun prog(state: ParserState): ParserState {
    val only_prog = chainMatch(matchString(state, "program"), {
        chainMatch(matchType(it, TokenType.IDENTIFICADORES), {
            chainMatch(matchString(it, ";"), { endOfFile(it) })
        })
    })
    if (only_prog.success()) {
        return only_prog
    }
    val complete_prog = chainMatch(matchString(state, "program"), {
        chainMatch(matchType(it, TokenType.IDENTIFICADORES), {
            chainMatch(matchString(it, ";"), { args(it) })
        })
    })
    if (complete_prog.success()) {
        if (complete_prog.lookahead < complete_prog.tokens.size) {
            return state.errorNew(Throwable())
        } else {
            return complete_prog
        }
    } else {
        return complete_prog
    }
}

// DEC -> TYPE IDENT = PALAVRAS
fun dec(state: ParserState): ParserState {
    return chainMatch(type(state), {
        chainMatch(matchType(it, TokenType.IDENTIFICADORES), {
            chainMatch(matchString(it, "="), { matchType(it, TokenType.PALAVRAS) })
        })
    })
}

// ARGS -> TYPE IDENT, ARGS | TYPE IDENT | LAMBDA
fun args(state: ParserState): ParserState {
    val list_args = chainMatch(type(state), {
        chainMatch(matchType(it, TokenType.IDENTIFICADORES), {
            chainMatch(matchString(it, ","),  { args(it) })
        })
    })
    if (list_args.success()) {
        return list_args
    }
    val one_arg = chainMatch(type(state), { matchType(it, TokenType.IDENTIFICADORES) })
    if (one_arg.success()) {
        return one_arg
    }
    return state
}

fun type(state: ParserState): ParserState {
    val types = listOf("char", "int", "string", "rational", "float")
    var type_match = state
    for (type_str in types) {
        type_match = matchString(state, type_str)
        if (type_match.success()) {
            return type_match
        }
    }
    return type_match
}

fun endOfFile(state: ParserState): ParserState {
    if (state.tokens.size <= state.lookahead) {
        return state
    } else {
        return state.errorNew(Throwable())
    }
}

fun matchString(state: ParserState, str: String): ParserState {
    if (!state.hasToken()) {
        return state.errorNew(Throwable("É esperado o token $str, no entanto, o arquivo finalizou"))
    }
    val token = state.next()
    if (token.tokenStr == str) {
        return state.incNew()
    } else {
        return state.errorNew(Throwable("Na linha ${token.getLineNumber()} é esperado o token $str, ao invés de ${token.tokenStr}."))
    }
}

fun matchType(state: ParserState, type: TokenType): ParserState {
    if (!state.hasToken()) {
        return state.errorNew(Throwable("É esperado token do tipo $type, no entanto, o arquivo finalizou"))
    }
    val token = state.next()
    if (token.type == type) {
        return state.incNew()
    } else {
        return state.errorNew(Throwable("Na linha ${token.getLineNumber()} é esperado token do tipo $type, ao invés de ${token.tokenStr}."))
    }
}

fun chainMatch(state: ParserState, success: (ParserState) -> ParserState): ParserState {
    if (state.success()) {
        return success(state)
    } else {
        return state
    }
}

