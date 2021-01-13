package interpretador_java;

import java.util.Stack;

public class MaquinaVirtual {
	
	private Stack <Token> aritmetica;
	private Stack <Token> booleana;
	private AnalisadorSemantico as;
	private boolean habilitada = true;
	
	public MaquinaVirtual(AnalisadorSemantico as)
	{
		aritmetica = new Stack<>();
		booleana = new Stack<>();
		this.as = as;
	}
	
	public void empilha(Token t)
	{
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return;
		
		// Empilhar lexema de um token.
		aritmetica.push(t);
	}
	
	public void empilhaBooleana()
	{	
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return;
		
		Token t = new Token(getResultadoFinal(), TipoToken.CONSTANTE_INTEIRA);
		
		//Empilhar novo token
		booleana.push(t);
	}
	
	public void empilhaBooleana(Token t)
	{
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return;
		
		// Empilha um novo token
		booleana.push(t);
	}
	
	private String getValor(Token t)
	{
		String valor = "";
		if(t.getTipo() == TipoToken.IDENTIFICADOR)
		{
			// Obter o valor da tabela de símbolos
			valor = as.getValor(t.getLexema());
		}
		
		else if(t.getTipo() == TipoToken.CONSTANTE_INTEIRA)
			valor = t.getLexema();	
		else if(t.getTipo() == TipoToken.CONSTANTE_BOOLEANA)
			valor = t.getLexema();
		return valor;
	}
	
	private String checarTipo(Token t)
	{
		if(t.getTipo() == TipoToken.IDENTIFICADOR)
			return getValor(t);
		
		return t.getLexema();
	}
	
	public void avaliarCondicao()
	{
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return;
		
		Token tokenOperando2 = booleana.pop();
		Token tokenOperador = booleana.pop();
		Token tokenOperando1 = booleana.pop();
		
		// Desempilhando
		String operando1 = getValor(tokenOperando1);
		String operando2 = getValor(tokenOperando2);	
		String operador = tokenOperador.getLexema();
		
		boolean resultado = true;
		
		
		// Verifico o lexema do operador, converter os operandos e avaliar (String -> int) (String -> booleano)
		if(operador.equals("&&"))
			resultado = Boolean.parseBoolean(operando1) && Boolean.parseBoolean(operando2);
		else if(operador.equals("||"))
			resultado = Boolean.parseBoolean(operando1) || Boolean.parseBoolean(operando2);
		else if(operador.equals("=="))
			resultado = Integer.parseInt(operando1) == Integer.parseInt(operando2);
		else if(operador.equals("!="))
			resultado = Integer.parseInt(operando1) != Integer.parseInt(operando2);
		else if(operador.equals("<="))
			resultado = Integer.parseInt(operando1) <= Integer.parseInt(operando2);
		else if(operador.equals(">="))
			resultado = Integer.parseInt(operando1) >= Integer.parseInt(operando2);
		else if(operador.equals(">"))
			resultado = Integer.parseInt(operando1) > Integer.parseInt(operando2);
		else if(operador.equals("<"))
			resultado = Integer.parseInt(operando1) < Integer.parseInt(operando2);
		
		// Converter resultado em String
		String resultadoString = String.valueOf(resultado);
		
		// Montar novo token que será empilhado
		Token resultadoToken = new Token(resultadoString, TipoToken.CONSTANTE_BOOLEANA);
		// Empilhar
		booleana.push(resultadoToken);
	}
	
	
	public void calculaExpressao()
	{
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return;
		
		Token tokenOperando2 = aritmetica.pop();
		Token tokenOperador = aritmetica.pop();
		Token tokenOperando1 = aritmetica.pop();
		
		// Desempilhando
		String operando1 = getValor(tokenOperando1);
		String operando2 = getValor(tokenOperando2);	
		String operador = tokenOperador.getLexema();
	
		
		// Revisar e fazer tydi daqui pra baixo
		/*
		double numOperando2 = 0;
		double numResultado = 0;
		
		// Checagem, conversão e cálculo - Revisar e refazer, mantive igual do renatim para testes
		if(as.getTipo(operando2).equals("int"))
		{
			numOperando1 = Integer.parseInt(operando1);
			numOperando2 = Integer.parseInt(operando2);
			
		}
		
		else if(as.getTipo(operando2).equals("float"))
		{
			numOperando2 = Float.parseFloat(operando1);
			numOperando1 = Float.parseFloat(operando2);

		}
		
		else if(as.getTipo(operando2).equals("double"))
		{
			numOperando1 = Double.parseDouble(operando1);
			numOperando2 = Double.parseDouble(operando2);
			
		}
		*/
		
		int numOperando1 = Integer.parseInt(operando1);
		int numOperando2 = Integer.parseInt(operando2);
		int numResultado = 0;
		
		if(operador.equals("+"))
			numResultado = numOperando1 + numOperando2;
		else if(operador.equals("-"))
			numResultado = numOperando1 - numOperando2;
		else if(operador.equals("*"))
			numResultado = numOperando1 * numOperando2;
		else if(operador.equals("/"))
			numResultado = numOperando1 / numOperando2;
		
		
		// Converter em String novamente e reempilhar
		String resultado = String.valueOf(numResultado);
		Token tokenResultado = new Token(resultado, TipoToken.CONSTANTE_INTEIRA);
		
		// Empilha o resultado
		aritmetica.push(tokenResultado);
	}
	
	public String getResultadoFinal()
	{
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return null;
		Token topo = aritmetica.pop();
		return getValor(topo);
	}
	
	public String getResultadoCondicao()
	{
		// Podo, ou seja, desabilito ela de fazer as expressões 
		if(!habilitada)
			return null;
		
		return booleana.pop().getLexema();
	}
	
	public void habilitaMaquina()
	{
		this.habilitada = true;
	}
	
	public void desabilitaMaquina()
	{
		this.habilitada = false;
	}
	
	public boolean isHabilitada()
	{
		return this.habilitada;
	}
	
}
