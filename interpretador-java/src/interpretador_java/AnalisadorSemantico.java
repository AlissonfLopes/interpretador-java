package interpretador_java;

import java.util.HashMap;

public class AnalisadorSemantico {
	private HashMap <String, Atributos> tabelaSimbolos;
	private int tipo_expressao;
	
	
	public AnalisadorSemantico()
	{
		tabelaSimbolos = new HashMap<>();
	}
	
	public void inicializaAvaliacaoTipo()
	{
		this.tipo_expressao = 0;
	}
	
	public void atualizaTipo(Token t)
	{
		if(t.getTipo() == TipoToken.CONSTANTE_INTEIRA)
		{
			this.tipo_expressao = Math.max(tipo_expressao, 1);
		}
		else if(t.getTipo() == TipoToken.IDENTIFICADOR)
		{
			// Procurar pelo tipo da variável
			this.tipo_expressao = Math.max(this.tipo_expressao, converteTipo(t));
		}
	}
	
	private int converteTipo(Token t)
	{
		String tipo = this.getTipo(t.getLexema());
		int tipoId = 0;		
		if(tipo.equals("double"))
			tipoId = 3;
		else if(tipo.equals("float"))
			tipoId = 2;
		else if(tipo.equals("int"))
			tipoId = 1;
		return tipoId;
	}
	
	
	public boolean checarPerdaPrecisao(Token t)
	{
		return converteTipo(t) < this.tipo_expressao;
	}
	
	public boolean checarPromossao(Token t)
	{
		return converteTipo(t) > this.tipo_expressao;
	}
	
	public boolean verificaVariavelDuplicada(Token token)
	{
		// Verificar se o lexema do token existe na tabela.
		if(tabelaSimbolos.get(token.getLexema()) != null)
			return true;
		
		return false;
	}
	
	public boolean verificaNaoVariavelDeclarada(Token token)
	{
		// Verifica se o lexema NÃO existe na tabela.
		if(tabelaSimbolos.get(token.getLexema()) == null)
			return true;
		
		return false;
	}
	
	public void insereVariavel(Token token, String tipo)
	{
		// Se o token for de identificador.
		if(token.getTipo() == TipoToken.IDENTIFICADOR)
		{
			// Insere a nova variável na tabela.
			Atributos atributo = new Atributos(tipo, "0");
			tabelaSimbolos.put(token.getLexema(), atributo);
		}
	}
	
	public String getTipo(String lexema)
	{
		return tabelaSimbolos.get(lexema).getTipo_dado();
	}
	
	public String getValor(String lexema)
	{
		return tabelaSimbolos.get(lexema).getValor();
	}
	
	public void atualizaValor(String lexema, String novoValor)
	{
		tabelaSimbolos.get(lexema).setValor(novoValor);
	}
	
	public String toString()
	{
		String msg = "";
		// Iterar ao longo de todas as linhas da tabela.
		for(String lexema : tabelaSimbolos.keySet())
		{
			Atributos atrib = tabelaSimbolos.get(lexema);
			msg += lexema+"\t"+atrib.getTipo_dado()+"\t"+atrib.getValor() + "\n";
		}
		return msg;	
	}

}
