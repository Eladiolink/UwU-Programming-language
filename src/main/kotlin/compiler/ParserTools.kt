package compiler.parserTools

import compiler.token.*
import compiler.naryTree.*


enum class NodeType {
    TERMINAL,
    // -- NO TERMINAL SYMBOLS
    PROG,
    LISTF,
    FUN,
    TYPE,
    ARGS,
    LISTC,
    CMD,
    DEC,
    IF,
    LOOP,
    CALL,
    RET,
    ARI,
    REL,
    REL_LINE,
    LID,
    OP
}

data class ParserNode(val type: NodeType, val token: Token?)

typealias AstNode = NaryTreeNode<ParserNode>

data class ParserState(
    val tokens: List<Token>,
    val lookahead: Int,
    val error: Throwable?,
    val node: AstNode?
) {
    fun next(): Token {
        return tokens[lookahead]
    }

    fun incNew(): ParserState {
        return ParserState(tokens, lookahead + 1, error, node?.copy())
    }

    fun errorNew(error_new: Throwable): ParserState {
        return ParserState(tokens, lookahead, error_new, null)
    }

    fun nodeNew(n: AstNode?): ParserState {
        return ParserState(tokens, lookahead, error, n)
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
            val node_v = ParserNode(NodeType.TERMINAL, token)
            return ParserState(state.tokens, state.lookahead + 1, null, AstNode(node_v, listOf()))
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
            val node_v = ParserNode(NodeType.TERMINAL, token)
            return ParserState(state.tokens, state.lookahead + 1, null, AstNode(node_v, listOf()))
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

class MatchOr(val ms: List<MatchParser>): MatchParser {
    override fun match(state: ParserState): ParserState {
        var result = state
        for (m in ms) {
            result = m.match(state)
            if (result.success()) {
                return result
            }
        }
        return result
    }
}

fun checkMatches(matches: List<MatchParser>, state: ParserState, type: NodeType): ParserState {
    var s = state
    val nodes: MutableList<AstNode> = mutableListOf()
    for (match in matches) {
        s = match.match(s)
        if (!s.success()) {
            return s
        }
        if (s.node != null) {
            nodes.add(s.node!!)
        }
    }
    return s.nodeNew(AstNode(ParserNode(type, null), nodes))
}

fun printTree(t: AstNode?) {
    println("=== Tree ===")
    printTreeRec(t, 1)
}

fun printTreeRec(t: AstNode?, level: Int) {
    if (t?.value?.type == NodeType.TERMINAL) {
        println("    ".repeat(level) + "| " + t.value.type + " - " + t.value.token?.tokenStr)
    } else {
        println("    ".repeat(level) + "| NON-TERMINAL " + t?.value?.type)
        if (t != null) {
            for (n in t.children) {
                printTreeRec(n, level + 1)
            }
        }
    }
}

