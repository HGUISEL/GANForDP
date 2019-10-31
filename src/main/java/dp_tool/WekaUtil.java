package dp_tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opencsv.CSVWriter;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;

public class WekaUtil {
	
	String buggy_label, clean_label;
	ArrayList <String> temp1 = new ArrayList<String>();
	ArrayList <String> temp2 = new ArrayList<String>();
	static List<Integer> numbers = new ArrayList<Integer>();
	static List<Integer> correct_numbers = new ArrayList<Integer>();
	static List<Integer> incorrect_numbers = new ArrayList<Integer>();
	String model;
	String cor_matched_num, inc_matched_num;
	String cor_accuracy, inc_accuracy;
	int header_flag;
	
	WekaUtil(){
		
	}
	
	WekaUtil(String model){
	      this.model = model;
	      header_flag = 0;
	}
	
	
	public void result_in_csv(Instances trainingIns, CSVWriter cw, PrintWriter pw, PrintWriter m_pw, Evaluation eval, int train_num, int test_num, int repeat_num, String strClassifier) throws IOException{
		   String [] csv_header = {model, "Correctly matched", "Correct Accuracy", "Incorrectly matched", "Incorrect Accuracy", "TP Rate", "FP Rate", "Precision", "Recall", "F-Measure", "ROC-Area", strClassifier};
	       cw.writeNext(csv_header);
	       
	       int buggy_label_index = trainingIns.attribute(trainingIns.classIndex()).indexOfValue("buggy");
	       
	       String [] csv_body = {"train_" + train_num + "_" + "test_" + test_num + "_" + repeat_num + "\t", 
	             cor_matched_num,
	             cor_accuracy,
	             inc_matched_num,
	             inc_accuracy,
	             String.valueOf(eval.truePositiveRate(buggy_label_index)),
	             String.valueOf(eval.falsePositiveRate(buggy_label_index)), 
	             String.valueOf(eval.precision(buggy_label_index)),
	             String.valueOf(eval.recall(buggy_label_index)),
	             String.valueOf(eval.fMeasure(buggy_label_index)),
	             String.valueOf(eval.areaUnderROC(buggy_label_index))};	
	       		 
	       
	       cw.writeNext(csv_body);
	        
	       if(header_flag == 0){
	    	   for(String str : csv_header){
	        	   m_pw.write(str + " ,");
	           }
	           m_pw.write("\n");
	       }

	       for(String str : csv_body){
	    	   m_pw.write(str + " ,");
	       }
	       m_pw.write("\n");
	       
	       m_pw.flush();
	       m_pw.close();
	       
	       pw.close();
	       cw.close();
	}
	
	public void evaluateClassifier(String train_path, String test_path, String result_path, String csv_path, String final_result_path, int train_num, int test_num, int repeat_num,
			   String strClassifier) throws Exception{
		
		  FileParser fp = new FileParser();
	        
		  Instances trainingIns = fp.loadArff(train_path);
		  Instances testingIns = fp.loadArff(test_path);
		  
		  System.out.println(trainingIns.equalHeaders(testingIns));
		  
		  Classifier classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null); //strClassier
		  System.out.println("Set Classifier");
		  trainingIns.setClassIndex(trainingIns.numAttributes() - 1); //Make the last index to be the class
		  
		  testingIns.setClassIndex(testingIns.numAttributes() - 1);
		  
		  System.out.println("Building Classifier");
		  classifier.buildClassifier(trainingIns);
		  
		  System.out.println("Evaluating");
		  Evaluation eval = new Evaluation(trainingIns);
		  
		  System.out.println("Evaluating model");
		  eval.evaluateModel(classifier, testingIns);
		  
		  int buggy_label_index = trainingIns.attribute(trainingIns.classIndex()).indexOfValue(CLI.buggy_label);
	      
	      System.out.println("**" + CLI.classifier + "Evaluation with Datasets**");
	      System.out.println(eval.toSummaryString());
	      System.out.print("The Expression for the input data as per algorithm is ");
	      System.out.println(classifier);
	      System.out.println("--------------------------------Evaluation Results--------------------------------");
	      System.out.println("TP : " + eval.truePositiveRate(buggy_label_index));
	      System.out.println("FP : " + eval.falsePositiveRate(buggy_label_index));
	      System.out.println("Precision : " + eval.precision(buggy_label_index));
	      System.out.println("Recall : " + eval.recall(buggy_label_index));
	      System.out.println("F-Measure : " + eval.fMeasure(buggy_label_index));
	      System.out.println("ROC-Area : " + eval.areaUnderROC(buggy_label_index));
	      
	      PrintWriter pw = new PrintWriter(result_path);
	      FileWriter fw = new FileWriter(csv_path, true);

	      pw.write("**" + CLI.classifier + " Evaluation with Datasets**" + System.getProperty("line.separator"));
	      pw.write(eval.toSummaryString() + System.getProperty("line.separator"));
	      pw.write("The Expression for the input data as per algorithm is" + System.getProperty("line.separator"));
	      pw.write(classifier.toString() + System.getProperty("line.separator"));
	      pw.println("--------------------------------Evaluation Results--------------------------------");
	      pw.println("TP : " + eval.truePositiveRate(buggy_label_index));
	      pw.println("FP : " + eval.falsePositiveRate(buggy_label_index));
	      pw.println("Precision : " + eval.precision(buggy_label_index));
	      pw.println("Recall : " + eval.recall(buggy_label_index));
	      pw.println("F-Measure : " + eval.fMeasure(buggy_label_index));
	      pw.println("ROC-Area : " +eval.areaUnderROC(buggy_label_index));
	      
	      int acc_idx = 0;
	      int star_idx = 0;
	      
	      star_idx = eval.toSummaryString().indexOf("Datasets**");
	      acc_idx = eval.toSummaryString().indexOf("Kappa");
	      String correct_incorrect = eval.toSummaryString().substring(star_idx + 1, acc_idx);
	      
	      int idx = correct_incorrect.indexOf("%");
	      String correct = correct_incorrect.substring(1, idx);
	      String incorrect = correct_incorrect.substring(idx+2, correct_incorrect.length());
	      
	      correct_numbers = extractNumbers(correct);
	      incorrect_numbers = extractNumbers(incorrect);
	      
	      for(int cor : correct_numbers)
	    	  System.out.println("Correct : " + cor);
	      
	      System.out.println("Correct line : " + correct);
	      
	      for(int incor : incorrect_numbers)
	    	  System.out.println("Incorrect : " + incor);
	      
	      System.out.println("Incorrect line : " + incorrect);
	      
	      if(correct.contains(".")){
	    	  cor_matched_num = String.valueOf(correct_numbers.get(0));
	          cor_accuracy = String.valueOf(correct_numbers.get(1)) + "." + String.valueOf(correct_numbers.get(2));
	          
	          if(incorrect.contains(".")){
	        	  inc_matched_num = String.valueOf(incorrect_numbers.get(3));
	        	  inc_accuracy = String.valueOf(incorrect_numbers.get(4)) + "." + String.valueOf(incorrect_numbers.get(5));
	          }
	          
	          else{
	        	  inc_matched_num = String.valueOf(incorrect_numbers.get(3));
	        	  inc_accuracy = String.valueOf(incorrect_numbers.get(4));
	          }
	      }
	      
	      else{
	    	  cor_matched_num = String.valueOf(correct_numbers.get(0));
	          cor_accuracy = String.valueOf(correct_numbers.get(1));
	          
	          if(incorrect.contains(".")){
	        	  inc_matched_num = String.valueOf(incorrect_numbers.get(3));
	        	  inc_accuracy = String.valueOf(incorrect_numbers.get(4)) + "." + String.valueOf(incorrect_numbers.get(5));
	          }
	          
	          else{
	        	  inc_matched_num = String.valueOf(incorrect_numbers.get(2));
	        	  inc_accuracy = String.valueOf(incorrect_numbers.get(3));
	          }
	      }
	      
	      System.out.println(cor_matched_num);
	      System.out.println(cor_accuracy);
	      
	      System.out.println(inc_matched_num);
	      System.out.println(inc_accuracy);
	      
	      
	      correct_numbers.clear();
	      incorrect_numbers.clear();
	      
	      CSVWriter cw = new CSVWriter(fw);
	      
	      BufferedWriter m_bw = new BufferedWriter(new FileWriter(final_result_path, true));
	      PrintWriter m_pw = new PrintWriter(m_bw, true);   
      
	      result_in_csv(trainingIns, cw, pw, m_pw, eval, train_num, test_num, repeat_num, strClassifier);
	      header_flag = 1;
	      
	      System.out.println(strClassifier + " Results Finished...");
	   }
	
	
		public static List<Integer> extractNumbers(String s){       
	
		     Pattern p = Pattern.compile("\\d+");
		     Matcher m = p.matcher(s);
	
		     while(m.find()){
		         numbers.add(Integer.parseInt(m.group()));
		     }       
		     return numbers;     
		 }

}

