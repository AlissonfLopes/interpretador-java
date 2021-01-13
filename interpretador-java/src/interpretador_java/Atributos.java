package interpretador_java;

public class Atributos {
	private String tipo_dado;
	private String valor;
		
	public Atributos(String tipo_dado, String valor)
	{
		this.tipo_dado = tipo_dado;
		this.valor = valor;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getTipo_dado() {
		return tipo_dado;
	}
		
}


