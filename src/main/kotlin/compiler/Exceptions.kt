package compiler.exceptions

open class CompilerException(line: Int, code: Int, message: String): Throwable("[$code] - Na linha $line, houve o erro: $message")

class SemPontoVirgula(line: Int): CompilerException(line, 501, "Sem ponto e virgula")

class SemParenteses(line: Int): CompilerException(line, 502, "Sem pareteses")

class SemChaves(line: Int): CompilerException(line, 503, "Sem chaves")

class SemTipo(line: Int): CompilerException(line,504, "Sem definição de tipo")

class SemCondicao(line: Int): CompilerException(line, 505, "Sem codição")

class SemComandos(line: Int): CompilerException(line, 506, "Sem comandos")

class RelacaoIncompleta(line: Int): CompilerException(line, 507, "Relação Incompleta")

class InicioInvalido(line: Int): CompilerException(line, 508, "Programas devem iniciar com a palavra reservada 'program'")
