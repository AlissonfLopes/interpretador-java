package interpretador_java;
%% 
%class Lexer
%{

	int linha = 1;     
 
%}
%eof{
	System.out.println("Arquivo interpretado com sucesso.");
	System.exit(0);
%eof}

// Especifica os alfabetos e linguagens.
LETRA  = [a-zA-Z]
DIGITO = [0-9]
DIGITOSZERO = [1-9]
TIPO   = "int" | "float" | "char" | "double"
OPERADOR_ARITMETICO = \+ | \- | \* | \/ 
PALAVRA_RESERVADA = "if" | "while" | "do" | "switch" | "else" | "return" | "for" | "return"
FIM_COMANDO = ;
SEPARADOR_ARGUMENTO = ,
OPERADOR_ATRIBUICAO = \= | \+\=| \-\= | \*\= 
OPERADOR_RELACIONAL = "<" | ">" | "!=" | "==" | "<=" | ">="
OPERADOR_LOGICO = "||" | "&&"
INCREMENTADOR_DECREMENTADOR = \+\+ | \-\-

%%
// Linguagens e expressões regulares.

\}                                { return new Token(yytext(), TipoToken.FIM_ESCOPO); }
\{								  { return new Token(yytext(), TipoToken.INICIO_ESCOPO);}

\)                                { return new Token(yytext(), TipoToken.FECHA_PARENTESES);}
\(                                { return new Token(yytext(), TipoToken.ABRE_PARENTESES);}

// Quando uma palavra reservada for encontrada.
{PALAVRA_RESERVADA}               { return new Token(yytext(), TipoToken.PALAVRA_RESERVADA); }

// Quando um operador aritmético for reconhecido.
{OPERADOR_ARITMETICO}			  { return new Token(yytext(), TipoToken.OPERADOR_ARITMETICO); }

// Quando uma das palavras da linguagem for encontrada.
{TIPO}                            {return new Token(yytext(), TipoToken.TIPO_DADO); }

// Ao ler um identificador, classifica-lo pelo TOKEN
{LETRA}({LETRA} | {DIGITO})*      { return new Token(yytext(), TipoToken.IDENTIFICADOR);}

// Ao ler um número, classificá-lo pelo TOKEN
({DIGITOSZERO}{DIGITO} | {DIGITO})						  { return new Token(yytext(), TipoToken.CONSTANTE_INTEIRA);}

// Ao ler um número do tipo float, classificá-lo pelo TOKEN
{DIGITO}+(.{DIGITO})			  { return new Token(yytext(), TipoToken.CONSTANTE_FLOAT);}

// Ao ler um número do tipo double, classificá-lo pelo TOKEN
{DIGITO}+(.{DIGITO}+)			  { return new Token(yytext(), TipoToken.CONSTANTE_DOUBLE);}

// Ao ler um fim de comando, classificá-lo pelo TOKEN
;								  { return new Token(yytext(), TipoToken.FIM_COMANDO);}

// Ao ler um separado de argumento (,), classificá-lo pelo TOKEN
,								  { return new Token(yytext(), TipoToken.SEPARADOR_ARGUMENTO);}

// Ao ler um operador de atribuição (=), classificá-lo pelo TOKEN
{OPERADOR_ATRIBUICAO}			  { return new Token(yytext(), TipoToken.OPERADOR_ATRIBUICAO);}

// Ao ler um operador relacional, classificá-lo pelo TOKEN
{OPERADOR_RELACIONAL}			  { return new Token(yytext(), TipoToken.OPERADOR_RELACIONAL);}

// Ao ler um operador lógico, classificá-lo pelo TOKEN
{OPERADOR_LOGICO}                 { return new token(yytext(), TipoToken.OPERADOR_LOGICO);}

// Ao ler um incrementador, classificá-lo pelo TOKEN
{INCREMENTADOR_DECREMENTADOR}        { return new Token(yytext(), TipoToken.INCREMENTADOR_DECREMENTADOR);}

// Quando \n for lido, incrementar o contador a classe.
\n                                {linha++;}

// Quando encontrar o \r.
\r                                {}

// Qualquer outro caractere lido, não faça nada.
. 					{}