package compiler.parserTools

import compiler.token.*

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

