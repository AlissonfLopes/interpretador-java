package interpretador_java;

import java.io.FileReader;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		Parser parser = new Parser("main.cpp");
		
		// Dispara a análise sintática
		parser.programa();
	}

}
