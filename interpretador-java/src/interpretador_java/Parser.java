package interpretador_java;

import java.io.FileReader;
import java.io.IOException;

public class Parser {
	
	private Token lookAhead;
	private Lexer lexer;
	private AnalisadorSemantico as;
	private MaquinaVirtual mv;
	
	public Parser(String arquivoFonte) throws IOException
	{
		// Instaciar um Lexer
		lexer = new Lexer(new FileReader (arquivoFonte));
		// Inicializar o lookAhead. lookAhead obtém o próximo Token
		this.lookAhead = (Token) lexer.yylex();
		
		// Criar uma instancia do Analisador Semantico
		as = new AnalisadorSemantico();
		
		// Criar uma instância da Máquina Virtual
		mv = new MaquinaVirtual(as);
	}
	
	public void match(TipoToken esperado)
	{
		// Se o tipo do token esperado pela gramatica foi o mesmo que foi lido
		if(esperado == this.lookAhead.getTipo())
		{
			try
			{
				System.out.print(lookAhead.getLexema());
				if(lookAhead.getTipo() == TipoToken.FIM_COMANDO || lookAhead.getTipo() == TipoToken.FIM_ESCOPO || lookAhead.getTipo() == TipoToken.INICIO_ESCOPO)
					System.out.println();
				else
					System.out.print(" ");
				this.lookAhead = (Token) lexer.yylex();
			} catch(IOException ex)
			{
				System.out.println("Erro ao ler o arquivo!");
			}
		}
		else 
		{
			erro("esperado: " + String.valueOf(esperado) + " lido: " + String.valueOf(this.lookAhead.getTipo()));
		}
	}
	
	private void erro(String msg)
	{
		System.out.println("Erro sintático na linha " + lexer.linha +": " + msg);
		System.exit(0);
	}
	
	// Não terminal Programa
	public void programa()
	{	
		if(!this.lookAhead.getLexema().equals("int"))
			erro("main deve ser do tipo int.");
		match(TipoToken.TIPO_DADO);
		if(!this.lookAhead.getLexema().equals("main"))
			erro("não foi encontrada a função \"main\"");
		match(TipoToken.IDENTIFICADOR);
		
		match(TipoToken.ABRE_PARENTESES);
		match(TipoToken.FECHA_PARENTESES);
		
		match(TipoToken.INICIO_ESCOPO);
		corpo();
		System.out.println(as);
		match(TipoToken.FIM_ESCOPO);
	}
	
	private void corpo()
	{
		declaracao();
		corpoCMD();
		
		// Avaliar o return "0"
		if(!this.lookAhead.getLexema().equals("return"))
			erro("Está faltando a palavra reservada \"return\"");
		match(TipoToken.PALAVRA_RESERVADA);
		match(TipoToken.CONSTANTE_INTEIRA);
		match(TipoToken.FIM_COMANDO);
	}
	
	private void declaracao()
	{
		if(this.lookAhead.getTipo() == TipoToken.TIPO_DADO)
		{
			String tipo = this.lookAhead.getLexema();
			match(TipoToken.TIPO_DADO);
			// Avaliar se a variável não foi previamente declarada.
			if(!as.verificaVariavelDuplicada(this.lookAhead))
				// Se não existir previamente na tabela de símbolos, adicioná-la.
				as.insereVariavel(this.lookAhead, tipo);
			else
				erro("VARIAVEL " + this.lookAhead.getLexema() + " previamente declarada!");
			match(TipoToken.IDENTIFICADOR);
			listaVar(tipo);
		}
	}
	
	private void listaVar(String tipo)
	{
		if(this.lookAhead.getTipo() == TipoToken.FIM_COMANDO)
		{
			match(TipoToken.FIM_COMANDO);
			declaracao();
		}
		else if(this.lookAhead.getTipo() == TipoToken.SEPARADOR_ARGUMENTO)
		{
			match(TipoToken.SEPARADOR_ARGUMENTO);
			if(!as.verificaVariavelDuplicada(lookAhead))
				as.insereVariavel(this.lookAhead, tipo);
			else 
				erro("VARIAVEL " + this.lookAhead.getLexema() + " previamente declarada!");
			match(TipoToken.IDENTIFICADOR);
			listaVar(tipo);
		}
	}
	
	private void corpoCMD()
	{
		if(this.lookAhead.getTipo() == TipoToken.IDENTIFICADOR)
		{
			as.verificaNaoVariavelDeclarada(lookAhead);
			
			if(as.verificaNaoVariavelDeclarada(lookAhead))
				erro("Erro: Variável " + this.lookAhead.getLexema() + " não declarada");
			
			Token ladoEsquerdo = this.lookAhead;
			match(TipoToken.IDENTIFICADOR);
			
			if(this.lookAhead.getTipo() == TipoToken.INCREMENTADOR_DECREMENTADOR)
			{
				match(TipoToken.INCREMENTADOR_DECREMENTADOR);
			}
			else
			{
				match(TipoToken.OPERADOR_ATRIBUICAO);
				as.inicializaAvaliacaoTipo();
				expressao();
				
				// Aqui retorna o valor final da expressão aritmética. Atualizar a variável do lado esquerdo
				String valorFinal = mv.getResultadoFinal();
				as.atualizaValor(ladoEsquerdo.getLexema(), valorFinal);
				
				if(as.checarPerdaPrecisao(ladoEsquerdo))
					System.out.println("ALERTA: Perda de precisão na linha " + lexer.linha);
				else if(as.checarPromossao(ladoEsquerdo))
					System.out.println("ALERTA: Promoção na linha " + lexer.linha);
				
			}
			
			match(TipoToken.FIM_COMANDO);
			corpoCMD();
		}
		else if(this.lookAhead.getTipo() == TipoToken.INCREMENTADOR_DECREMENTADOR)
		{
			//incremento();
			match(TipoToken.FIM_COMANDO);
			corpoCMD();
		}
		
		else if(this.lookAhead.getTipo() == TipoToken.PALAVRA_RESERVADA)
		{
			if(!this.lookAhead.getLexema().equals("return") ^ this.lookAhead.getLexema().equals("else"))
			{
				comandoBloco();
				corpoCMD();
			}
		}

	}
	
	private void expressao()
	{
		termo();
		while(this.lookAhead.getLexema().equals("+") || this.lookAhead.getLexema().equals("-"))
		{
			// Empilha o operador + ou -
			mv.empilha(lookAhead);
			match(TipoToken.OPERADOR_ARITMETICO);
			termo();
			// Ação semântica de calcular a expressão
			mv.calculaExpressao();
		}
	}
	
	private void termo()
	{
		fator();
		while(this.lookAhead.getLexema().equals("*") || this.lookAhead.getLexema().equals("/"))
		{
			// Empilha o operador * ou /
			mv.empilha(lookAhead);
			match(TipoToken.OPERADOR_ARITMETICO);
			fator();
			// Ação semântica de calcular a expressão
			mv.calculaExpressao();
		}
	}
	
	private void fator()
	{
		if(this.lookAhead.getTipo() == TipoToken.IDENTIFICADOR)
		{
			if(as.verificaNaoVariavelDeclarada(lookAhead))
				erro("Erro: Variável " + this.lookAhead.getLexema() + " não declarada");
			
			as.atualizaTipo(this.lookAhead);
			mv.empilha(lookAhead);
			match(TipoToken.IDENTIFICADOR);
		}
		else if(this.lookAhead.getTipo() == TipoToken.CONSTANTE_INTEIRA) 
		{
			as.atualizaTipo(this.lookAhead);
			mv.empilha(lookAhead);
			match(TipoToken.CONSTANTE_INTEIRA);
		}
		else if(this.lookAhead.getTipo() == TipoToken.CONSTANTE_FLOAT)
		{
			as.atualizaTipo(this.lookAhead);
			mv.empilha(lookAhead);
			match(TipoToken.CONSTANTE_FLOAT);
		}
		else if(this.lookAhead.getTipo() == TipoToken.CONSTANTE_DOUBLE)
		{
			as.atualizaTipo(this.lookAhead);
			mv.empilha(lookAhead);
			match(TipoToken.ABRE_PARENTESES);
		}
		else
		{
			match(TipoToken.ABRE_PARENTESES);
			expressao();
			match(TipoToken.FECHA_PARENTESES);
		}
	}
	
	private void comandoBloco()
	{

		
		if(this.lookAhead.getLexema().equals("if"))
		{
			match(TipoToken.PALAVRA_RESERVADA);
			match(TipoToken.ABRE_PARENTESES);
			condicao();
			
			// Verificar se a condição é falsa
			String valor = mv.getResultadoCondicao();
			
			System.out.println(valor);
			if(valor != null)
				if(valor.equals("false") && mv.isHabilitada())
					mv.desabilitaMaquina();
			
			match(TipoToken.FECHA_PARENTESES);
			match(TipoToken.INICIO_ESCOPO);
			corpoCMD();
			match(TipoToken.FIM_ESCOPO);
			
			
			if(this.lookAhead.getLexema().equals("else"))
			{				
				if(valor != null)
				{
					if(valor.equals("false"))
						mv.habilitaMaquina();
					else if(valor.equals("true"))
						mv.desabilitaMaquina();
				}
				
				match(TipoToken.PALAVRA_RESERVADA);
				match(TipoToken.INICIO_ESCOPO);
				corpoCMD();
				match(TipoToken.FIM_ESCOPO);
			}
			
			if(!mv.isHabilitada() && valor != null)
				mv.habilitaMaquina();
			
		}
		
		else if(this.lookAhead.getLexema().equals("do"))
		{
			// Ação semântica - Antes de ler o próximo token, vamos marcar esta posição no buffer
			int posicao = this.lexer.getPosicaoAtual();
			
			match(TipoToken.PALAVRA_RESERVADA);
			match(TipoToken.INICIO_ESCOPO);
			corpoCMD();
			match(TipoToken.FIM_ESCOPO);
		}
		
		else if(this.lookAhead.getLexema().equals("while"))
		{
			// Ação semântica - Antes de ler o próximo token, vamos marcar esta posição no buffer
			int posicao = this.lexer.getPosicaoAtual();
			
			match(TipoToken.PALAVRA_RESERVADA);
			match(TipoToken.ABRE_PARENTESES);
			condicao();
			
			// Verificar se a condição é falsa
			String valor = mv.getResultadoCondicao();
			System.out.println(valor);
			
			if(valor.equals("false") && mv.isHabilitada())
				mv.desabilitaMaquina();
			
			match(TipoToken.FECHA_PARENTESES);
			match(TipoToken.INICIO_ESCOPO);
			corpoCMD();
			
			// Se a condição é falsa, passa reto
			if(!mv.isHabilitada() && valor != null)
				mv.habilitaMaquina();
			
			else
				// Se a condição é verdadeira, retorna para a posição do buffer demarcada anteriormente
				lexer.setPosicaoAtual(posicao);
			
			match(TipoToken.FIM_ESCOPO);
		}
		
		else if(this.lookAhead.getLexema().equals("for"))
		{
			// Ação semântica - Antes de ler o próximo token, vamos marcar esta posição no buffer
			int posicao = this.lexer.getPosicaoAtual();
			
			match(TipoToken.PALAVRA_RESERVADA);
			match(TipoToken.ABRE_PARENTESES);
			
			String tipo = this.lookAhead.getLexema();
			
			match(TipoToken.TIPO_DADO);
			
			// Verificação de varíaveis duplicadas - Retirar comentário quando adicionar a MV ao laço for
			if(!as.verificaVariavelDuplicada(lookAhead))
				as.insereVariavel(this.lookAhead, tipo); // Insere caso esteja duplicada
			/*else
				erro("Variável " + this.lookAhead.getLexema() + " previamente declarada.");	*/	
		
		
			Token ladoEsquerdo = this.lookAhead; // Confere perda de precisão
			
			match(TipoToken.IDENTIFICADOR);
			match(TipoToken.OPERADOR_ATRIBUICAO);
			match(TipoToken.CONSTANTE_INTEIRA);
			match(TipoToken.FIM_COMANDO);
			condicao();
			
			// Verificar se a condição é falsa
			String valor = mv.getResultadoCondicao();
			System.out.println(valor);
			if(valor != null)
				if(valor.equals("false") && mv.isHabilitada())
					mv.desabilitaMaquina();
			
			match(TipoToken.FIM_COMANDO);
			incremento();
			match(TipoToken.FECHA_PARENTESES);
			match(TipoToken.INICIO_ESCOPO);
			corpoCMD();
			
			
			if(!mv.isHabilitada() && valor != null)
				mv.habilitaMaquina();
			else
			{
				// Se a condição é verdadeira, retorna para a posição do buffer demarcada anteriormente
				lexer.setPosicaoAtual(posicao);
			}
				
			
			match(TipoToken.FIM_ESCOPO);
			
		}
		

	}
	

	private void condicao()
	{
		expressaoRelacional();
		while(lookAhead.getTipo() == TipoToken.OPERADOR_LOGICO)
		{
			// Empilha o operador Lógico
			mv.empilhaBooleana(lookAhead);
			match(TipoToken.OPERADOR_LOGICO);
			expressaoRelacional();
			// Calcular a expressão.
			mv.avaliarCondicao();
		}
	}
	
	private void expressaoRelacional()
	{
		expressao();
		// Empilhar o topo da pilha de expressões aritméticas.
		mv.empilhaBooleana();
		while(lookAhead.getTipo() == TipoToken.OPERADOR_RELACIONAL)
		{
			// Empilha o operador Relacional.
			mv.empilhaBooleana(lookAhead);
			match(TipoToken.OPERADOR_RELACIONAL);
			expressao();
			// Empilhar o topo da pilha de expressões aritméticas.
			mv.empilhaBooleana();
			// Calcular a expressão.
			mv.avaliarCondicao();
		}
	}
	
	private void incremento()
	{
		if(this.lookAhead.getTipo() == TipoToken.IDENTIFICADOR)
		{
			// Verificar se a variável foi declarada
			as.verificaNaoVariavelDeclarada(lookAhead);
			if(as.verificaNaoVariavelDeclarada(lookAhead))
				erro("Variável " + this.lookAhead.getLexema() + "não declarada");
			
			match(TipoToken.IDENTIFICADOR);
			match(TipoToken.INCREMENTADOR_DECREMENTADOR);
		}
		
		else if(this.lookAhead.getTipo() == TipoToken.INCREMENTADOR_DECREMENTADOR)
		{
			match(TipoToken.INCREMENTADOR_DECREMENTADOR);
			
			// Verificar se a variável foi declarada
			as.verificaNaoVariavelDeclarada(lookAhead);
			if(as.verificaNaoVariavelDeclarada(lookAhead))
				erro("Variável " + this.lookAhead.getLexema() + " não declarada.");
			
			match(TipoToken.IDENTIFICADOR);
		}
	}
}

