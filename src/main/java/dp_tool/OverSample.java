package dp_tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

public class OverSample {
	
	ArrayList <String> buggy_data = new ArrayList<String>();
	ArrayList <String> clean_data = new ArrayList<String>();
	
	
	public void run_SMOTE(String train_path, int i, int n, double generating_ratio) throws Exception{
	    
	      try {
	    	  
	         Instances data = DataSource.read(train_path);
	
	         int numAttr = data.numAttributes();
	         data.setClassIndex(numAttr - 1);
	         
	         SMOTE smote = new SMOTE();
	         smote.setInputFormat(data);
	         smote.setNearestNeighbors(1);
	         smote.setPercentage((generating_ratio/CLI.buggy_cnt) * 100);//((generating_ratio + 0.5)*100);
	         Instances smote_ins = Filter.useFilter(data, smote);
	         
	         ArffSaver arffSaver = new ArffSaver();
	         arffSaver.setInstances(smote_ins);
	         arffSaver.setFile(new File(CLI.savingDir + File.separator + "Train" + File.separator + "SMOTE_" + i + "_" + n + ".arff"));
	         arffSaver.writeBatch();
	         
	         setLabelOrder(CLI.savingDir + File.separator + "Train" + File.separator + "SMOTE_" + i + "_" + n + ".arff");
	         
	         
	         Converter conv = new Converter();
	         conv.csvToArff(CLI.savingDir + File.separator + "Train" + File.separator + "SMOTE_" + i + "_" + n + ".csv");
	         
	         clean_data.removeAll(clean_data);
	         buggy_data.removeAll(buggy_data);
	        
	         
	         
	      } catch (IOException e) {
	         e.printStackTrace();
	      }  
      }
	
	public void setLabelOrder(String arff_path) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(arff_path));
        String line = br.readLine();
        
        line = br.readLine();
        
        while(line != null){
            if(line.contains(CLI.clean_label) && !line.contains("@")){
               clean_data.add(line);
            }
            
            else if(line.contains(CLI.buggy_label) && !line.contains("@")){
               buggy_data.add(line);
            }
            
            if(line.contains("{") && line.contains("}")){
//           	 System.out.println(line);
              	int idx = line.indexOf("{");
              	CLI.label_order = line.substring(idx, line.length());
              	System.out.println(CLI.label_order);
           }
            
               line = br.readLine();
         }
        
       int buggy_idx = CLI.label_order.indexOf(CLI.buggy_label);
		int clean_idx = CLI.label_order.indexOf(CLI.clean_label);
		
		if(buggy_idx < clean_idx) { // if the order of label_order is buggy, clean
			PrintWriter pw = new PrintWriter(new File(arff_path.replace(".arff", ".csv")));//
			
			pw.write(CLI.header + "\n");
			
			for(String buggy : buggy_data){
				pw.write(buggy + "\n");
			}
			
			
			for(String clean : clean_data){
				pw.write(clean + "\n");
			}

			pw.flush();
			pw.close();
		}
		
		else{
			PrintWriter pw = new PrintWriter(new File(arff_path.replace(".arff", ".csv")));
			
			pw.write(CLI.header + "\n");
			
			for(String clean : clean_data){
				pw.write(clean + "\n");
			}
			
			for(String buggy : buggy_data){
				pw.write(buggy + "\n");
			}

			pw.flush();
			pw.close();
		} 
	}
	
	public void make_compatible(String csv_path, int i, int n) throws IOException{
		
		System.out.println("make_compat csv path : " + csv_path);
		
		 /////to resolve an incompatible problem occurred in weka
        BufferedReader br = new BufferedReader(new FileReader(csv_path));
        
        String line = br.readLine();
//        String header = line;
        System.out.println("make_compatible header : " + line);
        line = br.readLine();
        
        while(line != null){
       	 
       	 if(line.contains(CLI.buggy_label)){
       		 System.out.println("buggy labeled : " + line);
       		 buggy_data.add(line);
       	 }
       	 
       	 else if (line.contains(CLI.clean_label)){
       		clean_data.add(line); 
       	 }
       	 
       	 line = br.readLine();
        }
        br.close();
        BufferedWriter bw = new BufferedWriter(new FileWriter(csv_path));
        PrintWriter pw = new PrintWriter(bw);
        
        pw.println(CLI.header);
        
        for(String inst : clean_data)
       	 pw.println(inst);
        
        for(String inst : buggy_data)
       	 pw.println(inst);
        
        pw.flush();
        pw.close();
        bw.close();
        clean_data.clear();
        buggy_data.clear();
        
        Converter conv = new Converter();
        conv.csvToArff(csv_path);
        //////
	}

}
