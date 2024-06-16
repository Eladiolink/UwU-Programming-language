package compiler.exceptions

open class CompilerException(line: UInt, code: Int, message: String): Throwable("[$code] - Na linha $line, houve o erro: $message")

class SemPontoVirgula(line: UInt): CompilerException(line, 501, "Sem ponto e virgula")

class SemParenteses(line: UInt): CompilerException(line, 502, "Sem pareteses")

class SemChaves(line: UInt): CompilerException(line, 503, "Sem chaves")

class SemTipo(line: UInt): CompilerException(line,504, "Sem definição de tipo")

class SemCondicao(line: UInt): CompilerException(line, 505, "Sem codição")

class SemComandos(line: UInt): CompilerException(line, 506, "Sem comandos")

class RelacaoIncompleta(line: UInt): CompilerException(line, 507, "Relação Incompleta")

class InicioInvalido(line: UInt): CompilerException(line, 508, "Programas devem iniciar com a palavra reservada 'program'")

class Redeclaracao(line: UInt, str: String): CompilerException(line, 509, "O identificador $str está sendo redeclarado")
