package pruebaAudio;

import pruebaAudio.Controlador;;

public class Test {
	public static void main(String[] args) {	
		Controlador controlador = Controlador.getControlador();
		
		System.out.println("Invocando grabarAudio...");
		
		System.out.println("Se graban "+controlador.RECORD_TIME/1000+" segundos");
		
		controlador.grabarAudio();
	}
}