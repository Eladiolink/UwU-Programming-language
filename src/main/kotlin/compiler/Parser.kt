package compiler.parser

import compiler.symbolTable.*
import compiler.token.*
import compiler.parserTools.*


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

// DEFINIÇÃO DE PRODUÇÃO
// PROG -> program IDENTIFICADORES; | program IDENT; LISTF
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

// DEFINIÇÃO DE PRODUÇÃO
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

// DEFINIÇÃO DE PRODUÇÃO
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

// DEFINIÇÃO DE PRODUÇÃO
// TYPE -> CHAR | INT | STRING | RATIONAL | FLOAT
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

