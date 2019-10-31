package dp_tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.opencsv.CSVWriter;

public class RatioUtil {
   ArrayList <String> fileList = new ArrayList<String>();
   HashMap <String, Integer> label_count = new HashMap <String, Integer>();
   String [] tok;
   String label1, label2;
   String buggy_label, clean_label;
   int buggy_count, clean_count = 0;
   
   ArrayList<String> clean_data = new ArrayList<String>();
   ArrayList<String> bug_data = new ArrayList<String>();
   
   
   
   public void getLabelNames(String input_path) throws Exception{
      
      if(input_path.contains(".csv")){
         
         BufferedReader br = new BufferedReader(new FileReader(input_path));
         
         String line = br.readLine();
         
         tok = line.split(",");
         
         int att_num = tok.length;////
         
         System.out.println("att_num : " + att_num);/////
         
         line = br.readLine();
         
         while(line != null){
            
            tok = line.split(",");
            
            Integer count = label_count.get(tok[tok.length-1]);
            
            if(count == null){
               label_count.put(tok[tok.length-1], 1);
            }
            
            else{
               label_count.put(tok[tok.length-1], count+1);
            }
            
            line = br.readLine();
         }
         
         
      }
      
      else if(input_path.contains(".arff")){
         
         BufferedReader br1 = new BufferedReader(new FileReader(input_path));
         
         String line = br1.readLine();
         
         System.out.println(line);
         
         
         while(line != null){
            
            if(line.contains("{") || line.contains("}")){
               tok = line.split(" ");
               System.out.println(line);
               
               
               
               int idx = tok[tok.length - 1].indexOf(",");
               
               label1 = tok[tok.length - 1].substring(1, idx);
               label2 = tok[tok.length - 1].substring(idx+1, tok[tok.length - 1].length()-1);;
            }
            
            line = br1.readLine();
         }
         
         br1.close();
         
         BufferedReader br2 = new BufferedReader(new FileReader(input_path));
         
         line = br2.readLine();
         while(line != null){
            
            Integer count1 = label_count.get(label1);
            
            if(count1 == null){
               label_count.put(label1, 1);
            }
            
            else{
               label_count.put(label1, count1+1);
            }
            
            Integer count2 = label_count.get(label2);
            
            if(count2 == null){
               label_count.put(label2, 1);
            }
            
            else{
               label_count.put(label2, count1+1);
            }
            
            line = br2.readLine();
         }
      }
      
      Set <Entry <String, Integer>> set = label_count.entrySet();
      Iterator<Entry<String, Integer>> itr = set.iterator();
      
      while(itr.hasNext()){
         Map.Entry<String, Integer> e = (Map.Entry<String, Integer>)itr.next();

         if(e.getValue() > clean_count){
            clean_count = e.getValue();
            CLI.clean_label = e.getKey();
         }
         
         else{
            buggy_count = e.getValue();
            CLI.buggy_label = e.getKey();
         }
      }
      
      System.out.println(CLI.buggy_label + ", " + buggy_count);
      System.out.println(CLI.clean_label + ", " + clean_count);
      
   }
   
   public void bugDetector(int repeat, int folds) throws IOException, InterruptedException{
         
         System.out.println("Reading files....");
         Converter conv = new Converter();
         
         for(int i = 0; i < repeat; i++){
            for(int j = 0; j < folds; j++){
               BufferedReader br = new BufferedReader(new FileReader(CLI.savingDir + CLI.train + i + "_" + j +".arff")); 
               String line = br.readLine();
               line = br.readLine();
               
               conv.arffToCSV(CLI.savingDir + CLI.train + i + "_" + j +".arff");
               
               while(line != null){
                  if(line.contains(CLI.clean_label) && !line.contains("@")){
                     clean_data.add(line);
                  }
                  
                  else if(line.contains(CLI.buggy_label) && !line.contains("@")){
                     bug_data.add(line);
                  }
                  
                  if(line.contains("{") && line.contains("}")){
//                	System.out.println(line);
                	int idx = line.indexOf("{");
                	CLI.label_order = line.substring(idx, line.length());
                	System.out.println(CLI.label_order);
                  }
                  
                  
                     
                     line = br.readLine();
               }
               
               System.out.println("Buggy data : " + bug_data.size());
               System.out.println("Clean data : " + clean_data.size());
               
               Clone c = new Clone();
               
               
               c.cloneInstance(bug_data, clean_data, i, j);
               BufferedWriter bw = new BufferedWriter(new FileWriter(CLI.savingDir + CLI.train + CLI.buggy_label + "_" + i + "_" + j + ".csv"));
               StringBuilder sb = new StringBuilder();
               
               for(String instance : bug_data){
            	   
            	   String [] temp = instance.split(",");
            	   
            	   double [] values = new double [temp.length-1];
            	   
            	   for(int k = 0; k < temp.length-1; k++){
            		   values[k] = Double.parseDouble(temp[k]);
            		   
            		   sb.append(values[k]+"");
            		   
            		   if(k < values.length - 1)
            			   sb.append(",");
            	   }
            	   sb.append("\n");
               }
               bw.write(sb.toString());
               System.out.println("Buggy File Written");
               bw.close();
             
               clean_data.removeAll(clean_data);
               bug_data.removeAll(bug_data);
               System.out.println("Finished " + CLI.savingDir + CLI.train + CLI.buggy_label + "_" + i + "_" + j + ".csv extraction...");

            }   
         }
      }
   
   
   public void getRatio(String input_path) throws IOException{
      
      File folder = new File(input_path);
      CSVWriter cw = new CSVWriter(new FileWriter(input_path + File.separator + "FileRatios.csv"));
      String [] header = {"File Name", "buggy","clean", "total", "ratio"};
      
      String fileName = null;
      double bug = 0.0, clean = 0.0;
      double total = 0, ratio;
      
      String [] temp;
      cw.writeNext(header);
      
      
      Converter conv = new Converter();
      //read filenames in folder
      for (final File fileEntry : folder.listFiles()){
         
         if(fileEntry.toString().endsWith(".csv")){
            System.out.println("Ratio Util csv files : " + fileEntry);
            fileList.add(fileEntry.toString());
         }
      }
      
      for(String str : fileList){
         System.out.println();
         temp = str.split(File.separator);
         
         fileName = temp[temp.length-1].replace(".csv", "");
         fileName = fileName.replace(input_path, "");
         
         BufferedReader br = new BufferedReader(new FileReader(str));
         
         String line = br.readLine();
         
         line = br.readLine();
         
         while(line != null){
            if(line.contains("buggy"))
               bug++;
            if(line.contains("clean"))
               clean++;
            
            line = br.readLine();
         }
         
         total= bug + clean;
         ratio = bug / total;
         String [] body = {fileName, Double.toString(bug), Double.toString(clean), Double.toString(total), Double.toString(ratio)};
         cw.writeNext(body, true);
         System.out.println(body);
         
         bug = 0;
         clean = 0;
         total = 0;
         ratio = 0;
      }
      
      cw.close();

   }
}
