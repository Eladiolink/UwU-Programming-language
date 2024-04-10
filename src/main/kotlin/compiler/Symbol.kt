package compiler.symbol

import compiler.token.TokenType

class Symbol(t: Any, type: TokenType) {
  val value = t
  val type: TokenType = type
}

fun createSybol(element: String): Symbol? {

  var typeElement = getTypeSymbol(element)

  var symbol: Symbol? =
      when (typeElement) {
        TokenType.COMENTARIO -> Symbol(element, TokenType.COMENTARIO)
        TokenType.RESERVADAS -> Symbol(element, TokenType.RESERVADAS)
        else -> null
      }

  return symbol
}

fun getTypeSymbol(element: String): TokenType? {

  val regexReservadas =
      Regex("\\b(char|string|int|float|rational|program|if|else|while|input|print|return)\\b")

  val regexComemtarioLinha = Regex("--[\\w\\s]+")

  val regexComentarioBlock = Regex("-\\{[^-}]*-\\}")

  val regexIdentificadores = Regex("")

  if (element.matches(regexReservadas)) return TokenType.RESERVADAS

  if (element.matches(regexComemtarioLinha)) return TokenType.COMENTARIO

  if (element.matches(regexComentarioBlock)) return TokenType.COMENTARIO

  return null
}
