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

interface MatchParser {
    fun match(state: ParserState): ParserState
}

class MatchString(val str: String): MatchParser {
    override fun match(state: ParserState): ParserState {
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
}

class MatchStrings(val strs: List<String>): MatchParser {
    override fun match(state: ParserState): ParserState {
        var match_state = state
        for (str in strs) {
            val match_str = MatchString(str)
            match_state = match_str.match(state)
            if (match_state.success()) {
                return match_state
            }
        }
        return match_state
    }
}

class MatchType(val type: TokenType): MatchParser {
    override fun match(state: ParserState): ParserState {
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
}

class MatchFun(val func: (ParserState) -> ParserState): MatchParser {
    override fun match(state: ParserState): ParserState {
        return func(state)
    }
}

fun checkMatches(matches: List<MatchParser>, state: ParserState): ParserState {
    var s = state
    for (match in matches) {
        s = match.match(s)
        if (!s.success()) {
            return s
        }
    }
    return s
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
    val only_prog = listOf(MatchString("program"), MatchType(TokenType.IDENTIFICADORES), MatchString(";"), MatchFun(::endOfFile))
    val only_prog_match = checkMatches(only_prog, state)
    if (only_prog_match.success()) {
        return only_prog_match
    }
    val complete_prog = listOf(MatchString("program"), MatchType(TokenType.IDENTIFICADORES), MatchString(";"), MatchFun(::args))
    val complete_prog_match = checkMatches(complete_prog, state)
    if (complete_prog_match.success()) {
        if (complete_prog_match.lookahead < complete_prog_match.tokens.size) {
            return state.errorNew(Throwable())
        } else {
            return complete_prog_match
        }
    } else {
        return complete_prog_match
    }
}

// DEC -> TYPE IDENT = PALAVRAS
fun dec(state: ParserState): ParserState {
    val matches = listOf(
        MatchFun(::type),
        MatchType(TokenType.IDENTIFICADORES),
        MatchString("="),
        MatchType(TokenType.PALAVRAS),
    )
    return checkMatches(matches, state)
}

// ARGS -> TYPE IDENT, ARGS | TYPE IDENT | LAMBDA
fun args(state: ParserState): ParserState {
    val list_args = listOf(MatchFun(::type), MatchType(TokenType.IDENTIFICADORES), MatchString(","), MatchFun(::args))
    val list_args_match = checkMatches(list_args, state)
    if (list_args_match.success()) {
        return list_args_match
    }
    val one_arg = listOf(MatchFun(::type), MatchType(TokenType.IDENTIFICADORES))
    val one_arg_match = checkMatches(one_arg, state)
    if (one_arg_match.success()) {
        return one_arg_match
    }
    return state
}

fun type(state: ParserState): ParserState {
    val match = MatchStrings(listOf("char", "int", "string", "rational", "float"))
    val match_state = checkMatches(listOf(match), state)
    if (match_state.success()) {
        return match_state
    }
    if (!state.hasToken()) {
        return state.errorNew(Throwable("É esperado o nome do tipo, no entanto, o arquivo finalizou"))
    }
    val token = state.next()
    return state.errorNew(Throwable("Na linha ${token.getLineNumber()} é esperado o nome do tipo, ao invés de ${token.tokenStr}."))
}

fun endOfFile(state: ParserState): ParserState {
    if (state.tokens.size <= state.lookahead) {
        return state
    } else {
        return state.errorNew(Throwable())
    }
}

