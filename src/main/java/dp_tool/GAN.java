package dp_tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class passes values to python, and executes GAN to generate new buggy instances.
 * @author Jin, Young In
 * Please report any issues to jinyi1187@gmail.com. Thank you!
 *
 */
public class GAN {
	static Process p;
	static int exitVal = 1;
	String gan_file_path;
	static String commandline;
	static String cmd [];
	private String python_exe_path;
	private String py_path;
	
	
	GAN(String python_exe_path, String py_path){
		this.python_exe_path = python_exe_path;
		this.py_path = py_path;
	}
	
	public printOutput getStreamWrapper(InputStream is, String type) {
	      return new printOutput(is, type);
	   }
	    
	public void runCommands(String python_input_file_name, String python_output_file_name, String generating_num) throws InterruptedException{
	    	
	    Runtime rt = Runtime.getRuntime();

	    printOutput errorReported, outputMsg;
	    
	    gan_file_path = python_exe_path + " " + py_path; //py_path;
	        
	    try{
	     	   
	     	   System.out.println("start process...");	     	   
	     	   
	     	   commandline = gan_file_path + " " + python_input_file_name + " " + python_output_file_name + " " + " " + generating_num;
	     	   System.out.println("commandline : " + commandline);
	     	   p = rt.exec(commandline);
	     	   errorReported = getStreamWrapper(p.getErrorStream(), "ERROR");
	     	   outputMsg = getStreamWrapper(p.getInputStream(), "OUTPUT");
	     	   errorReported.start();
	     	   outputMsg.start();
	     	   
	     	   exitVal = p.waitFor(); //waits for processors to finish works
	     	   commandline = null; //initializing commandline
	        } catch(IOException e){
	           e.printStackTrace();
	        }
	    
	    	
	    	
	    }
	
	public void run_clone_GAN(String python_input_file_name, String python_output_file_name, String generating_num) throws InterruptedException{
    	
	    Runtime rt = Runtime.getRuntime();

	    CLI cli = new CLI();
	    printOutput errorReported, outputMsg;
	    
	    gan_file_path = python_exe_path + " " + py_path;
	        
	    try{
	     	   
	     	   System.out.println("start process...");	     	   
	     	   
	     	   commandline = gan_file_path + " " + python_input_file_name + " " + python_output_file_name + " " + " " + generating_num;
	     	   System.out.println("commandline : " + commandline);
	     	   p = rt.exec(commandline);
	     	   errorReported = getStreamWrapper(p.getErrorStream(), "ERROR");
	     	   outputMsg = getStreamWrapper(p.getInputStream(), "OUTPUT");
	     	   errorReported.start();
	     	   outputMsg.start();
	     	   
	     	   exitVal = p.waitFor(); //waits for processors to finish works
	     	   commandline = null; //initializing commandline
	        } catch(IOException e){
	           e.printStackTrace();
	        }
	    
	    	
	    	
	    }
	    
	    private class printOutput extends Thread{
	       InputStream is = null;
	       
	       printOutput(InputStream is, String type){
	          this.is = is;
	       }
	       
	       public void run(){
	          String str = null;
	          
	          try{
	             BufferedReader br = new BufferedReader(new InputStreamReader(is));
	             
	             while((str = br.readLine()) != null){
	                System.out.println(str);
	             }
	          } catch(IOException ioe){
	             ioe.printStackTrace(); 
	          }
	       }
	    }
}

