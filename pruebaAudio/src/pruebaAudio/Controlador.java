package pruebaAudio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Controlador {
	private static Controlador cont;
	
	private Controlador(){}
	
	public static Controlador getControlador(){
		if(cont==null)
			cont = new Controlador();
		return cont;
	}
	
	AudioFormat format;
	TargetDataLine line;
	final long RECORD_TIME = 60000/6;  // 10 seg
	
	public void grabarAudio(){
		format = getAudioFormat();
		
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
			format); // format is an AudioFormat object
		
		if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line not supported");
            System.exit(0);
        }
		
		// Obtain and open the line.
		try {
		    line = (TargetDataLine) AudioSystem.getLine(info);
		    //line.open(format);
		    //new StopperThread().start();
		    new CaptureThread().start();		
		} catch (LineUnavailableException ex) {
		    // Handle the error ... 
			ex.printStackTrace();
		    System.exit(0);	
		}
	}
		
	private AudioFormat getAudioFormat() {
		float sampleRate = 44100.0F;
	    //8000,11025,16000,22050,44100
	    int sampleSizeInBits = 16;
	    //8,16
	    int channels = 2;
	    //1,2
	    boolean signed = true;
	    //true,false
	    boolean bigEndian = false;
	    //true,false
	    return new AudioFormat(sampleRate,
	                           sampleSizeInBits,
	                           channels,
	                           signed,
	                           bigEndian);
	  }//end getAudioFormat
	
	
	//Inner class to capture data from microphone
	//and write it to an OPC1: array / OPC2: output audio file.
	class CaptureThread extends Thread{
	  public void run(){
	    AudioFileFormat.Type fileType = null;
	    File audioFile = null;

	    //Set the file type and the file extension
	    
	      fileType = AudioFileFormat.Type.WAVE;
	      audioFile = new File("ambient.wav");

	    try{
	      line.open(format);
	      
	      //OPC1: Guardar salida del mic a un array
	      ByteArrayOutputStream out = new ByteArrayOutputStream();
	        int numBytesRead;
	        int CHUNK_SIZE = 1024;
	        byte[] data = new byte[line.getBufferSize() / 5];
	      
	      line.start();
	      
	      int bytesRead = 0;
	      
	      while (bytesRead < 1843200) { //1800000 es maso durante 10 segundos parece
	            numBytesRead = line.read(data, 0, CHUNK_SIZE);
	            bytesRead += numBytesRead;
	            // write the mic data to a stream for use later
	            out.write(data, 0, numBytesRead);
	      }
	      
	      line.stop();
          line.close();
          System.out.println("Terminado...");
	      
	      System.out.println("Bytes leidos del mic: "+bytesRead);
	      
	      //OPC3: Guardar salida del mic a un array y de ese array a un archivo .wav
	      //va en combinacion con OPC1
	      
	      byte[] audioData = out.toByteArray(); //El tan buscado ByteArray "raw"
	      										//probablemente necesite usar este
	      										//con chromaprint
          
          ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
          AudioInputStream outputAIS = new AudioInputStream(bais, format,
                  audioData.length / format.getFrameSize());
          
          AudioSystem.write(
  	            outputAIS,
  	            fileType,
  	            audioFile);
	      
	      //OPC2: Guardar salida del mic a un archivo .wav sin hacer OPC1 ni OPC3
	      /*
	      AudioSystem.write(
	            new AudioInputStream(line),
	            fileType,
	            audioFile);
	      */
	      
	    }catch (Exception ioe) {
            ioe.printStackTrace();
        /* 
        } catch (LineUnavailableException ex) {
			// TODO Auto-generated catch block
        	ex.printStackTrace();
		    System.exit(0);	
		}
		*/
	    }
	  }//end run
	}//end inner class CaptureThread
	
	//Inner class to stop capturing data from microphone
	class StopperThread extends Thread{
        public void run() {
            try {
                Thread.sleep(RECORD_TIME);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            line.stop();
            line.close();
            System.out.println("Terminado...");
        }
    }//end inner class StopperThread
}
