package dp_tool;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

//multi-GAN codes

public class CLI {
	
	boolean verbose;
	boolean help;
	String ans;
	String input_path, new_csv_name;
	static String input_file_name;
	static String input_file;
	static String dataPath, savingDir;
	static String buggy_label;
	static String clean_label;
	static String train = "Train" + File.separator + "train_";
	static String test = "Test" + File.separator + "test_";
	static String header;
	static String label_order;
	static double buggy_cnt;
	static double clean_cnt;
	String input_extension;
	static String python_exe_path, py_path, strClassifier;
	String resultFileName;
	static String classifier;
	ArrayList <String> fileList = new ArrayList<String>();
	int repeat = 10, folds = 2;
	ArrayList<String> tokens = new ArrayList<String>();
	
	
	public void parsePath(){
		
		//get input file name
		for(String str :  input_path.split(File.separator)){
		  if(str.contains("."))
	    	input_file_name = str;
		  
		  tokens.add(str);
		}
		
		int index = input_file_name.indexOf(".");
	    input_file_name = input_file_name.substring(0, index);
	    input_extension = input_file_name.substring(index, input_file_name.length());
		
		//get input working directory
		dataPath = tokens.get(0) + File.separator;
		for(int i = 1; i < tokens.size() - 1; i++){
			dataPath += tokens.get(i) + File.separator;
		}
		
		savingDir = dataPath + input_file_name + File.separator;
		input_file = input_file_name + input_extension;
		
		
		resultFileName = input_file_name + "_FINAL_RESULTS.csv";
		
		tokens.clear();

		String [] tok = strClassifier.split(File.separator + "."); // //.

		classifier = tok[tok.length-1];
		
	}
	
	
	public void run(String [] args) throws Exception{
	      Options options = createOptions();
	      Scanner kb = new Scanner(System.in);
	      Converter conv = new Converter();
	      if(parseOptions(options, args)){
	         
	         if(help){
	            printHelp(options);
	            return;
	         }
	      }
	      FileParser fp = new FileParser();
//	      fp.deleteCSVDummy(input_path);
	      
	     RatioUtil ru = new RatioUtil();
//	     ru.getRatio(input_path);
	       
	      
	      
	      File folder = new File(input_path);
	      
	      for (final File fileEntry : folder.listFiles()){
	          
	          if(fileEntry.toString().endsWith(".csv")){
//	             System.out.println("file Entry : " + fileEntry);
	             fileList.add(fileEntry.toString());
	          }
	       }
	      
	      
	      for(String file : fileList){
	    	  
	    	//get label names from input file
		      ru.getLabelNames(file);
		      
		      fp.getHeader(file);
	    	  
		      System.out.println("input_path : " + file);
	    	  input_path = file;
	    	  savingDir = null;
	    	  dataPath = null;
	    	  input_file = null;
	    	  
	    	  parsePath();
		      System.out.println("saving dir : " + savingDir); //savingDir
		      System.out.println("dataPath : " + dataPath); //dataPaths
		      System.out.println("data file : " + input_file); //dataFileName
		
	    	  input_path = conv.normalizeData(input_path);
	    	  
	    	  
	    	  FileParser file_parser = new FileParser(input_path);
	    	  
	    	  file_parser.makeDir(savingDir + "Train" + File.separator);
	    	  file_parser.makeDir(savingDir + "Test" + File.separator);
	    	  
	    	  System.out.println();
	          System.out.println("CROSS VALIDATION EXECUTING......");
	          System.out.println("PLEASE WAIT.....");
	          
	          //dividing dataset into training set and test set by cross validation
	          
	          file_parser.saveCrossValidationFold(dataPath, input_file, savingDir, repeat, folds);
	          System.out.println("CROSS VALIDATION FILE PARSING FINISHED........");
	          System.out.println();
	          
	          System.out.println("clean_label : " + clean_label);
	          System.out.println("buggy_label : " + buggy_label);
	          
	          ru.bugDetector(repeat, folds);
	          
	          System.out.println("GETTING BUGGY RATIOS FINISHED.......");
	          System.out.println();
	          
	          System.out.println("Getting GENERAL CASE evaluation.....");
	          WekaUtil wu_general = new WekaUtil("GENERAL");
	          for(int i = 0; i < folds; i++){
	             for(int j = 0; j < repeat; j++){
	                for(int k = 0; k < folds; k++){
	                   
	                   if(i == k){
	                	   wu_general.evaluateClassifier(savingDir + train + j + "_" + i + ".arff", 
		                     		 savingDir + test + j + "_"  + k + ".arff",
		                            savingDir + train  + i + "_test_" + k + "_GENERAL" + j + ".txt", 
		                            savingDir + train  + i + "_test_" + k + "_GENERAL" + j + ".csv",
		                            savingDir + resultFileName, i, k, j,
		                            strClassifier);
	                      
	                   }
	                }
	             }
	          }
	          
	          WekaUtil wu_clone = new WekaUtil("CLONE");
	          System.out.println("Getting CLONE evaluation......");
	          for(int i = 0; i < folds; i++){
		        for(int j = 0; j < repeat; j++){
		           for(int k = 0; k < folds; k++){
		        	   if(i == k){
	                	   wu_clone.evaluateClassifier(savingDir + train + "clone_" + j + "_" + i + ".arff", 
		                     		 savingDir + test + j + "_"  + k + ".arff",
		                            savingDir + train  + i + "_test_" + k + "_CLONE" + j + ".txt", 
		                            savingDir + train  + i + "_test_" + k + "_CLONE" + j + ".csv",
		                            savingDir + resultFileName, i, k, j,
		                            strClassifier);
	                   }
		           }
		        }
		     }
	          
	         
	          int cnt = 0;
		        //Apply GAN
		            System.out.println("GAN EXECUTING......");
		            System.out.println("PLEASE WAIT.....");
		            for(int i = 0; i < repeat; i++){
		               for(int j = 0; j < folds; j++){
		                  GAN gan = new GAN(python_exe_path, py_path);
		                  
		                  int generating_num = file_parser.get_Generated_Num(savingDir + train  + i + "_" + j + ".csv");
		                  
		                  System.out.println("Generating number : " + generating_num);
		                  gan.runCommands(savingDir + train + "buggy_" + i + "_" + j + ".csv", 
		                        savingDir + "Train" + File.separator + "Generated_" + i + "_" + j + ".csv",
		                        Integer.toString(generating_num));
		                  
		                /*  int loop_counter = 1;
		                  for(int k = 0; k < (CLI.clean_cnt - CLI.buggy_cnt) / generating_num; k++){
		                	  System.out.println("Generating number : " + generating_num);
			                  gan.runCommands(savingDir + train + "buggy_" + i + "_" + j + ".csv", 
			                        savingDir + "Train" + File.separator + "Generated_" + i + "_" + j + ".csv",
			                        Integer.toString(generating_num)); //
			                  
			                  cnt++;
			                  loop_counter++;
			                  System.out.println(cnt + "th execution....");  
		                  }
		                  
		                  if((CLI.clean_cnt - CLI.buggy_cnt) - (generating_num * loop_counter) != 0
		                		  && (CLI.clean_cnt - CLI.buggy_cnt) - (generating_num * loop_counter) > 0){
		                	  System.out.println((CLI.clean_cnt - CLI.buggy_cnt) - (generating_num * loop_counter) + "buggy data needed more");
		                	  System.out.println("generating remainder needed buggy data");
		                	  
		                	  gan.runCommands(savingDir + train + "buggy_" + i + "_" + j + ".csv", 
				                        savingDir + "Train" + File.separator + "Generated_" + i + "_" + j + ".csv",
				                        Double.toString((CLI.clean_cnt - CLI.buggy_cnt) - (generating_num * loop_counter))); 
		                	  
		                	  cnt++;
			                  System.out.println(cnt + "th execution...."); 
		                  }*/
		               }
		            }
	          
	          GAN.exitVal = 0;
		            
		            
		            if(GAN.exitVal == 0){
		            	//add labels to dataset
			            for(int i = 0; i < repeat; i++){ 
			               for(int j = 0; j < folds; j++){
			                  file_parser.parseBuggyLabel(savingDir + "Train" + File.separator + "Generated_" + i + "_" + j + ".csv");
			                  
			                  file_parser.mergeFiles(
			                		  savingDir + train + i + "_" + j + ".csv",
			                		  savingDir + "Train" + File.separator + "Generated_" + i + "_" + j + ".csv",
			                		  savingDir + train + "MERGED_" + i + "_" + j + ".csv"
			                		  );
			               }
			            }
			            
			            WekaUtil wu_gan = new WekaUtil("GAN");
			            
			          //Get Classifier evaluation
			            for(int i = 0; i < folds; i++){
			                  for(int j = 0; j < repeat; j++){
			                     for(int k = 0; k < folds; k++){
			                        
			                        if(i == k){
			                           
			                           wu_gan.evaluateClassifier(savingDir + train + "MERGED_" + j + "_" + i + ".arff", 
			                                 savingDir + test + j + "_"  + k + ".arff",
			                                 savingDir + train  + i + "_test_" + k + "_GAN" + j + ".txt", 
			                                 savingDir + train  + i + "_test_" + k + "_GAN" + ".csv",
//			                                 savingDir + train  + i + "_test_" + k + "_GAN" + j + ".csv", 너무 파일이 많아져서 그냥 csv 하나에 모두 다 저장하기
			                                 savingDir + resultFileName, i, k, j,
			                                 strClassifier);
			                        }
			                     }
			                  }
			               }
			       /////////
			         //SMOTE
			         System.out.println("SMOTE EXECUTING...");

			        
			         OverSample os = new OverSample();
			         
			         for(int i=0; i< repeat; i++) {
			            for(int n=0;n<folds;n++) {
			                     
			         //Get the number of buggy data that must be generated through the SMOTE
			            	double generating_ratio = file_parser.get_Generated_Ratio(savingDir + train + i + "_" + n + ".csv"); 
			                System.out.println("generating_ratio : " + generating_ratio);
			                     
			                os.run_SMOTE(savingDir + train + i + "_" + n + ".arff", i,n,generating_ratio);
			            }
			        }     
//		        }    
			       //Get Classifier evaluation for SMOTE
		            WekaUtil wu_smote = new WekaUtil("SMOTE");   
			         System.out.println("Getting SMOTE evaluation.....");
			            for(int i = 0; i < folds; i++){
			               for(int j = 0; j < repeat; j++){
			                  for(int k = 0; k < folds; k++){
			                     
			                     if(i == k){
			                        wu_smote.evaluateClassifier(savingDir + File.separator + "Train" + File.separator + "SMOTE_" + j + "_" + i + ".arff", 
			                              savingDir + test + j + "_"  + k + ".arff",
			                              savingDir + train + i + "_test_" + k + "_SMOTE" + j + ".txt", 
			                              savingDir + train + i + "_test_" + "_" + k + "_SMOTE.csv",
			                              savingDir + resultFileName, i, k, j,
			                              strClassifier);
			                     }
			                     
			                  }
			               }
			           }
		            }
	      }
	          
	      	
		  
//	      } ./DebugServer -i "./data/" -e "./python.exe" -p "/GAN.py" -c "weka.classifiers.functions.Logistic"
	      
	}
	
	 /**
	    * This is a method that parse options
	    * @param options : options; v,h,i,e,p,c
	    * @param args : getting argument
	    * @return : returning true when right option was chosen
	    */
	   private boolean parseOptions(Options options, String [] args){
	      CommandLineParser parser = new DefaultParser();
	      
	      try{
	         CommandLine cmd = parser.parse(options, args);
	         
	         verbose = cmd.hasOption("v");
	         help = cmd.hasOption("h");
	         input_path = cmd.getOptionValue("i");
	         python_exe_path = cmd.getOptionValue("e");
	         py_path = cmd.getOptionValue("p");
	         strClassifier = cmd.getOptionValue("c");

	         
	      } catch (Exception e) {
	         printHelp(options);
	         return false;
	      }
	      
	      return true;
	   }
	   
	   /**
	    * This is a method that pops up when user asks for help
	    * @param options : getting option value
	    */
	   private void printHelp(Options options){
	      HelpFormatter formatter = new HelpFormatter();
	      String header = "GAN WEKA JAVA TOOL";
	      String footer = "\nPlease report any issues to jinyi1187@gmail.com";
	      formatter.printHelp("GAN_Tool", header, options, footer, true);
	   }
	   
	
	/**
	    * This method is creating options 
	    * @return : returning the created option value
	    */
	   private Options createOptions(){
	      Options options = new Options();
	      
	      options.addOption(Option.builder("h").longOpt("help")
	            .desc("Help")
	            .build());
	      
	      options.addOption(Option.builder("i").longOpt("input")
	            .desc("Set an input file directory\n")
	            .hasArg()
	            .argName("Input File Path")
	            .required()
	            .build());
	     
	      options.addOption(Option.builder("e").longOpt("py_exe_path")
	            .desc("Set a directory path that executes python")
	            .hasArg()
	            .argName("python.exe directory path")
	            .required()
	            .build());
	      
	      options.addOption(Option.builder("p").longOpt("py_path")
	            .desc("Set a .py directory path")
	            .hasArg()
	            .argName(".py directory path")
	            .required()
	            .build());
	      
	      options.addOption(Option.builder("c").longOpt("classifier")
	            .desc("Enter the name of Evaluation Classifier correctly. Proper capitalization is needed. Please write down package name of the classifier\n")
	            .hasArg()
	            .argName("Classifier Name")
	            .required()
	            .build());
	      
	      return options;
	   }
}
