package interpretador_java;

import interpretador_java.TipoToken;
import interpretador_java.Yytoken;

public class Token implements Yytoken{

	private String lexema;
	private TipoToken tipo;
	
	public Token(String lexema, TipoToken tipo)
	{
		this.lexema = lexema;
		this.tipo   = tipo;
	}

	public String getLexema() {
		return lexema;
	}

	public TipoToken getTipo() {
		return tipo;
	}
	
	public String toString()
	{
		return "("+lexema+", "+tipo.toString()+")";
	}
}
