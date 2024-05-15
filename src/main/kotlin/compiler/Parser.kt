package compiler.parser

import compiler.parserTools.*
import compiler.symbolTable.*
import compiler.token.*
import kotlin.io.println

fun run(tokens: List<Token>, table: SymbolTable): Result<Int> {
    val useful_tokens =
            tokens.filter { it.type != TokenType.ESPACO && it.type != TokenType.COMENTARIO }
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
    val only_prog =
            listOf(
                    MatchString("program"),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString(";"),
                    MatchFun(::endOfFile)
            )
    val only_prog_match = checkMatches(only_prog, state)
    if (only_prog_match.success()) {
        return only_prog_match
    }
    val complete_prog =
            listOf(
                    MatchString("program"),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString(";"),
                    MatchFun(::ret)
            )
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
// DEC -> TYPE IDENT = PALAVRAS | TYPE IDEN = IDENT ARI IDENT
fun dec(state: ParserState): ParserState {
    val dec_word =
            listOf(
                    MatchFun(::type),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString("="),
                    MatchType(TokenType.PALAVRAS),
            )
    val dec_word_match = checkMatches(dec_word, state)
    if (dec_word_match.success()) {
        return dec_word_match
    }
    val dec_ari =
            listOf(
                    MatchFun(::type),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString("="),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchFun(::ari),
                    MatchType(TokenType.IDENTIFICADORES),
            )
    val dec_ari_match = checkMatches(dec_ari, state)
    return dec_ari_match
}

// DEFINIÇÃO DE PRODUÇÃO
// ARGS -> TYPE IDENT, ARGS | TYPE IDENT | LAMBDA
fun args(state: ParserState): ParserState {
    val list_args =
            listOf(
                    MatchFun(::type),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString(","),
                    MatchFun(::args)
            )
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
        return state.errorNew(
                Throwable("É esperado o nome do tipo, no entanto, o arquivo finalizou")
        )
    }
    val token = state.next()
    return state.errorNew(
            Throwable(
                    "Na linha ${token.getLineNumber()} é esperado o nome do tipo, ao invés de ${token.tokenStr}."
            )
    )
}

fun endOfFile(state: ParserState): ParserState {
    if (state.tokens.size <= state.lookahead) {
        return state
    } else {
        return state.errorNew(Throwable())
    }
}

// DEFINIÇÃO DE PRODUÇÃO
// ARI -> + | - | * | / | | | %
fun ari(state: ParserState): ParserState {
    val match = MatchStrings(listOf("+", "-", "*", "/", "|", "%"))
    val match_state = checkMatches(listOf(match), state)
    if (match_state.success()) {
        return match_state
    }
    if (!state.hasToken()) {
        return state.errorNew(
                Throwable("É esperado operador aritmético, no entanto, o arquivo finalizou")
        )
    }
    val token = state.next()
    return state.errorNew(
            Throwable(
                    "Na linha ${token.getLineNumber()} é esperado operador aritmético, ao invés de ${token.tokenStr}."
            )
    )
}

// DEFINIÇÃO DE PRODUÇÃO
// <OP> -> <= | < | > | >= |!= | ==

fun op(state: ParserState): ParserState {
    val match = MatchStrings(listOf("<=", "<", ">", ">=", "!=", "=="))
    val match_state = checkMatches(listOf(match), state)
    if (match_state.success()) {
        return match_state
    }

    return returnByState(state)
}

// DEFINIÇÃO DE PRODUÇÃO
// <REL> -> <WORD> | <ID> | (<REL>) | <WORD><REL>' | <ID><REL>' | (<REL>)<REL>'
fun rel(state: ParserState): ParserState {
    // CASO RECURSIVO

    val list_word_recursive = listOf(MatchType(TokenType.PALAVRAS), MatchFun(::rel_line))
    val list_word_recursive_match = checkMatches(list_word_recursive, state)

    if (list_word_recursive_match.success()) {
        return list_word_recursive_match
    }
    val list_line_id = listOf(MatchType(TokenType.IDENTIFICADORES), MatchFun(::rel_line))
    val list_line_id_match = checkMatches(list_line_id, state)

    if (list_line_id_match.success()) {
        return list_line_id_match
    }

    val list_rel_parentese_recursive =
            listOf(MatchString("("), MatchFun(::rel), MatchString(")"), MatchFun(::rel_line))
    val list_rel_parentese_recursive_match = checkMatches(list_rel_parentese_recursive, state)

    if (list_rel_parentese_recursive_match.success()) {
        return list_rel_parentese_recursive_match
    }
    // NÃO RECURSIVO

    val list_word = listOf(MatchType(TokenType.PALAVRAS))
    val list_word_match = checkMatches(list_word, state)

    if (list_word_match.success()) {
        return list_word_match
    }

    val list_id = listOf(MatchType(TokenType.IDENTIFICADORES))
    val list_id_match = checkMatches(list_id, state)

    if (list_id_match.success()) {
        return list_id_match
    }

    val list_rel_parentese = listOf(MatchString("("), MatchFun(::rel), MatchString(")"))
    val list_rel_parentese_match = checkMatches(list_rel_parentese, state)

    if (list_rel_parentese_match.success()) {
        return list_rel_parentese_match
    }

    if (!state.hasToken()) {
        return state.errorNew(
                Throwable(
                        "É esperado uma operação de relação válida, no entanto, o arquivo finalizou"
                )
        )
    }
    val token = state.next()
    return state.errorNew(
            Throwable(
                    "Na linha ${token.getLineNumber()} é esperado operador aritmético, ao invés de ${token.tokenStr}."
            )
    )
}

// DEFINIÇÃO DE PRODUÇÃO
// <REL>' -> <OP><REL> | <OP><REL><REL>'
fun rel_line(state: ParserState): ParserState {

    val list_op_recursive = listOf(MatchFun(::op), MatchFun(::rel), MatchFun(::rel_line))
    val list_op_recursive_match = checkMatches(list_op_recursive, state)

    if (list_op_recursive_match.success()) {
        return list_op_recursive_match
    }

    val list_op = listOf(MatchFun(::op), MatchFun(::rel))
    val list_op_match = checkMatches(list_op, state)

    if (list_op_match.success()) {
        return list_op_match
    }
    return returnByState(state)
}

// DEFINIÇÃO DE PRODUÇÃO
// CALL -> IDENT() | IDENT(LID)
fun call(state: ParserState): ParserState {
    val no_arg_call =
            listOf(MatchType(TokenType.IDENTIFICADORES), MatchString("("), MatchString(")"))
    val no_arg_match = checkMatches(no_arg_call, state)
    if (no_arg_match.success()) {
        return no_arg_match
    }
    val args_call =
            listOf(
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString("("),
                    MatchFun(::lid),
                    MatchString(")")
            )
    return checkMatches(args_call, state)
}

// DEFINIÇÃO DE PRODUÇÃO
// LID -> IDENT | IDENT,LID
fun lid(state: ParserState): ParserState {
    val idents = listOf(MatchType(TokenType.IDENTIFICADORES), MatchString(","), MatchFun(::lid))
    val idents_match = checkMatches(idents, state)
    if (idents_match.success()) {
        return idents_match
    }
    val one_ident = MatchType(TokenType.IDENTIFICADORES).match(state)
    return one_ident
}

// DEFINIÇÃO DE PRODUÇÃO
// RET -> RETURN IDENT | RETURN WORD
fun ret(state: ParserState): ParserState {
    return checkMatches(
            listOf(
                    MatchString("return"),
                    MatchOr(MatchType(TokenType.IDENTIFICADORES), MatchType(TokenType.PALAVRAS))
            ),
            state
    )
}

fun returnByState(state: ParserState): ParserState {
    if (!state.hasToken()) {
        return state.errorNew(
                Throwable("É esperado operador aritmético, no entanto, o arquivo finalizou")
        )
    }
    val token = state.next()
    return state.errorNew(
            Throwable(
                    "Na linha ${token.getLineNumber()} é esperado operador aritmético, ao invés de ${token.tokenStr}."
            )
    )
}
