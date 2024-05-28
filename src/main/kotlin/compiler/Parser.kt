package compiler.parser

import compiler.parserTools.*
import compiler.symbolTable.*
import compiler.token.*
import kotlin.io.println

fun run(tokens: List<Token>, table: SymbolTable): Result<AstNode> {
    val useful_tokens =
            tokens.filter { it.type != TokenType.ESPACO && it.type != TokenType.COMENTARIO }
    var state = prog(ParserState(useful_tokens, 0, null, null))
    if (state.success() && state.node != null) {
        println("Parser realizado com sucesso")
        return Result.success(state.node!!)
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
    val only_prog_match = checkMatches(only_prog, state, NodeType.PROG)
    if (only_prog_match.success()) {
        return only_prog_match
    }
    val complete_prog =
            listOf(
                    MatchString("program"),
                    MatchType(TokenType.IDENTIFICADORES),
                    MatchString(";"),
                    MatchFun(::listf)
            )
    val complete_prog_match = checkMatches(complete_prog, state, NodeType.PROG)
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
// LISTF -> <FUN> | <FUN><LISTF>
fun listf(state: ParserState): ParserState {
    val funs = listOf(MatchFun(::fun_stmt), MatchFun(::listf))
    val funs_match = checkMatches(funs, state, NodeType.LISTF)
    if (funs_match.success()) {
        return funs_match
    }
    return fun_stmt(state)
}

// DEFINIÇÃO DE PRODUÇÃO
// FUN -> <TYPE> <ID> (input!!<ARGS>) {<LISTC>}
fun fun_stmt(state: ParserState): ParserState {
    return checkMatches(listOf(
        MatchFun(::type),
        MatchType(TokenType.IDENTIFICADORES),
        MatchString("("),
        MatchString("input"),
        MatchString("!!"),
        MatchFun(::args),
        MatchString(")"),
        MatchString("{"),
        MatchFun(::listc),
        MatchString("}")
    ), state, NodeType.FUN)
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
    val dec_word_match = checkMatches(dec_word, state, NodeType.DEC)
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
    val dec_ari_match = checkMatches(dec_ari, state, NodeType.DEC)
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
    val list_args_match = checkMatches(list_args, state, NodeType.ARGS)
    if (list_args_match.success()) {
        return list_args_match
    }
    val one_arg = listOf(MatchFun(::type), MatchType(TokenType.IDENTIFICADORES))
    val one_arg_match = checkMatches(one_arg, state, NodeType.ARGS)
    if (one_arg_match.success()) {
        return one_arg_match
    }
    // Retorna o status anterior, indicando que não fez parser de nada(lambda produção)
    // Define o nó como null pois não há nada para adicionar na árvore
    return state.nodeNew(null)
}

// DEFINIÇÃO DE PRODUÇÃO
// TYPE -> CHAR | INT | STRING | RATIONAL | FLOAT
fun type(state: ParserState): ParserState {
    val match = MatchStrings(listOf("char", "int", "string", "rational", "float"))
    val match_state = checkMatches(listOf(match), state, NodeType.TYPE)
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

// DEFINIÇÃO DE PRODUÇÃO
// <LISTC> -> <CMD> | <CMD><LISTC>
fun listc(state: ParserState): ParserState {
    val cmds = listOf(MatchFun(::cmd), MatchFun(::listc))
    val cmds_match = checkMatches(cmds, state, NodeType.LISTC)
    if (cmds_match.success()) {
        return cmds_match
    }
    return cmd(state)
}

// DEFINIÇÃO DE PRODUÇÃO
// <CMD> -> <DEC>; | <IF> | <LOOP> | <CALL>; | <RET>;
fun cmd(state: ParserState): ParserState {
    val match = MatchOr(listOf(MatchFun(::if_stmt), MatchFun(::loop)))
    val match_state = match.match(state)
    if (match_state.success()) {
        return match_state
    }
    val match_pv = listOf(MatchOr(listOf(MatchFun(::dec), MatchFun(::call), MatchFun(::ret))), MatchString(";"))
    val match_pv_state = checkMatches(match_pv, state, NodeType.CMD)
    return match_pv_state
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
    val match_state = checkMatches(listOf(match), state, NodeType.ARI)
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
    val match_state = checkMatches(listOf(match), state, NodeType.OP)
    if (match_state.success()) {
        return match_state
    }

    return returnByState(state)
}

// DEFINIÇÃO DE PRODUÇÃO
// <REL> -> <WORD> | <ID> | (<REL>) | <WORD><REL>' | <ID><REL>' | (<REL>)<REL>'
fun rel(state: ParserState): ParserState {
    // CASO RECURSIVO
    val list_line_idw = listOf(
        MatchOr(listOf(MatchType(TokenType.IDENTIFICADORES), MatchType(TokenType.PALAVRAS))),
        MatchFun(::rel_line)
    )
    val list_line_idw_match = checkMatches(list_line_idw, state, NodeType.REL)
    if (list_line_idw_match.success()) {
        return list_line_idw_match
    }

    val list_rel_parentese_recursive =
            listOf(MatchString("("), MatchFun(::rel), MatchString(")"), MatchFun(::rel_line))
    val list_rel_parentese_recursive_match = checkMatches(list_rel_parentese_recursive, state, NodeType.REL)

    if (list_rel_parentese_recursive_match.success()) {
        return list_rel_parentese_recursive_match
    }
    // NÃO RECURSIVO

    val list_idw = MatchOr(listOf(MatchType(TokenType.PALAVRAS), MatchType(TokenType.IDENTIFICADORES)))
    val list_idw_match = list_idw.match(state)

    if (list_idw_match.success()) {
        return list_idw_match
    }

    val list_rel_parentese = listOf(MatchString("("), MatchFun(::rel), MatchString(")"))
    val list_rel_parentese_match = checkMatches(list_rel_parentese, state, NodeType.REL)

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
    val list_op_recursive_match = checkMatches(list_op_recursive, state, NodeType.REL_LINE)

    if (list_op_recursive_match.success()) {
        return list_op_recursive_match
    }

    val list_op = listOf(MatchFun(::op), MatchFun(::rel))
    val list_op_match = checkMatches(list_op, state, NodeType.REL_LINE)

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
    val no_arg_match = checkMatches(no_arg_call, state, NodeType.CALL)
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
    return checkMatches(args_call, state, NodeType.CALL)
}

// DEFINIÇÃO DE PRODUÇÃO
// LID -> IDENT | IDENT,LID
fun lid(state: ParserState): ParserState {
    val idents = listOf(MatchType(TokenType.IDENTIFICADORES), MatchString(","), MatchFun(::lid))
    val idents_match = checkMatches(idents, state, NodeType.LID)
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
                    MatchOr(listOf(MatchType(TokenType.IDENTIFICADORES), MatchType(TokenType.PALAVRAS)))
            ),
            state,
            NodeType.RET
    )
}

// DEFINIÇÃO DE PRODUÇÃO
// <LOOP> -> while(<REL>){<LISTC>}
fun loop(state: ParserState): ParserState {

    val list_while =
            listOf(
                    MatchString("while"),
                    MatchString("("),
                    MatchFun(::rel),
                    MatchString(")"),
                    MatchString("{"),
                    MatchFun(::listc),
                    MatchString("}")
            )
    val list_while_match = checkMatches(list_while, state, NodeType.LOOP)
    if (list_while_match.success()) {
        return list_while_match
    }

    return returnByState(state)
}

// DEFINIÇÃO DE PRODUÇÃO
// IF -> if(<REL>){<LISTC>} | if(<REL>){<LISTC>}else{<LISTC>}
fun if_stmt(state: ParserState): ParserState {
    val list_if_else =
            listOf(
                    MatchString("if"),
                    MatchString("("),
                    MatchFun(::rel),
                    MatchString(")"),
                    MatchString("{"),
                    MatchFun(::listc),
                    MatchString("}"),
                    MatchString("else"),
                    MatchString("{"),
                    MatchFun(::listc),
                    MatchString("}")
            )
    val list_if_else_match = checkMatches(list_if_else, state, NodeType.IF)
    if (list_if_else_match.success()) {
        return list_if_else_match
    }

    val list_if =
            listOf(
                    MatchString("if"),
                    MatchString("("),
                    MatchFun(::rel),
                    MatchString(")"),
                    MatchString("{"),
                    MatchFun(::listc),
                    MatchString("}")
            )
    val list_if_match = checkMatches(list_if, state, NodeType.IF)
    if (list_if_match.success()) {
        return list_if_match
    }

    return returnByState(state)
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
