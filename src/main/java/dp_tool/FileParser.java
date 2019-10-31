package dp_tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import weka.core.Instances;

public class FileParser {
	
	ArrayList <String> fileList = new ArrayList<String>();
	ArrayList <String> fileNames = new ArrayList<String>();
	ArrayList <String> no_dummy = new ArrayList<String>();
	
	
	String arff_path;
	
	FileParser(){
		
	}
	
	FileParser(String arff_path){
		this.arff_path = arff_path;
	}
	
	

	
	/**
	 * Generate independent arff files for each fold of n-fold cross validation
	 * @param dataPath directory where an arff file is
	 * @param dataFile arff file name
	 * @param savingDir directory name for the arff for each fold
	 * @param repeat number of repetition
	 * @param folds n-fold
	 * @throws IOException 
	 */
	void saveCrossValidationFold(String dataPath,  String dataFile, String savingDir, int repeat,int folds) throws IOException{

		Instances instances = loadArff(arff_path);

		File createdDir = new File(savingDir);
		if(!createdDir.exists()){
			if(!(new File(savingDir).mkdirs())){
				System.err.println(savingDir +" is not created");
				System.exit(0);
			}
		}

		for(int i=0;i<repeat;i++){

			instances.randomize(new Random(i)); 
			instances.stratify(folds);
			
			for(int n=0;n<folds;n++){
				Instances testInstances = instances.testCV(folds, n);
				writeAFile(testInstances + "", savingDir +  dataFile.replace(".arff", "") + "_" + i + "_" + n + ".arff");
//				Converter con = new Converter();
//				con.arffToCSV(savingDir + dataFile.replace(".arff", "") + "_" + i + "_" + n + ".arff");	
			}
		}
		
		String dataFileName = dataFile.replace(".arff", "");
		
//		makeDir(savingDir + "Train/");
		copyFiles(savingDir, savingDir + "Train" + File.separator, dataFileName);
//		makeDir(savingDir + "Test/");
		copyFiles(savingDir, savingDir + "Test" + File.separator, dataFileName);	

	}
	
	public void copyFiles(String savingDir , String train_test_dir, String dataFile){
		
		File folder = new File(savingDir);
		
		File new_file;
		//read filenames in folder
		for (final File fileEntry : folder.listFiles()){
			
			if(fileEntry.toString().endsWith(".arff")){
				fileList.add(fileEntry.toString());

			}
		}
		
		
		for (String str : fileList){
			
			int num = str.indexOf(dataFile);

			String temp = str.substring(num, str.length()); 
			num = temp.indexOf(File.separator);
			
			fileNames.add(temp.substring(num+1, temp.length()));
		}
		
		
//		for(String str : fileNames)
//			System.out.println("File names : " + str);
		
		System.out.println("File Entries ended....");
		
		for(String file_name : fileNames){
			InputStream is = null;
			OutputStream os = null;
				
			try {
				
			int idx = file_name.indexOf(File.separator);
			file_name = file_name.substring(idx+1, file_name.length());
				
			File origin_file = new File(savingDir + File.separator + file_name); // <-- 이부분에 AndResGuard가 두번 겹침 해결해야함!!
			System.out.println("saving Dir : " + savingDir);
			System.out.println("file_name : " + file_name);
			System.out.println("train_test_dir : " + train_test_dir);
			System.out.println("input_file_name : " + CLI.input_file_name);
			
			
			
			if(train_test_dir.contains("Train"))
				new_file = new File(train_test_dir + "train" + file_name.replace(CLI.input_file_name, ""));
			else
				new_file = new File(train_test_dir + "test" + file_name.replace(CLI.input_file_name, ""));
			
			is = new FileInputStream(origin_file);
			os = new FileOutputStream(new_file);
			
			byte[] buffer = new byte[1024];
			
			int length;
			//copy the file content in bytes
			while((length = is.read(buffer)) > 0){
				os.write(buffer, 0, length);
			}
			
			is.close();
			os.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void makeDir(String directoryName){
			
		File createdDir = new File(directoryName);
		if(!createdDir.exists()){
			if(!(new File(directoryName).mkdirs())){
				System.err.println(directoryName +" is not created");
				System.exit(0);
			}
		}
		
	}
	
	
	/**
	 * Load Instances from arff file. Last attribute will be set as class attribute
	 * @param path arff file path
	 * @return Instances
	 */
	public static Instances loadArff(String path){
		Instances instances=null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			instances = new Instances(reader);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		instances.setClassIndex(instances.numAttributes()-1);

		return instances;
	}
	
	public void getHeader(String csv_file) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(csv_file));
		
		CLI.header = br.readLine();
		
		br.close();
		
	}
	
	
	public static void writeAFile(String lines, String targetFileName){
		try {
			File file= new File(targetFileName);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
			
			dos.writeBytes(lines);
				
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void deleteArffDummy(String input_path) throws IOException{
		
		String [] tok;
		int dummy_cnt = 0;
		File folder = new File(input_path);
		
		Converter conv = new Converter();
		//read filenames in folder
		for (final File fileEntry : folder.listFiles()){
			
			if(fileEntry.toString().endsWith(".arff")){
//				System.out.println("file Entry : " + fileEntry);
				fileList.add(fileEntry.toString());
			}
		}
		
		for(String str : fileList){
			BufferedReader br = new BufferedReader(new FileReader(str));
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(str));
			String line = br.readLine();
			
			while(line != null){
				
				tok = line.split(",");
				
				for(int i = 0; i < tok.length; i++){
					if(tok[i].equals("0"))
						dummy_cnt++;
				}
				
				if(dummy_cnt != 8){
					no_dummy.add(line);
//					System.out.println(line);
				}
				
				line = br.readLine();
				dummy_cnt = 0;
			}
			
			for(String instances : no_dummy){
				bw.write(instances + "\n");
//				System.out.println(instances);
			}
			
			bw.flush();
			bw.close();
			
		}
	}
	
	public void deleteCSVDummy(String input_path) throws IOException{
		
		File folder = new File(input_path);
		
		Converter conv = new Converter();
		//read filenames in folder
		for (final File fileEntry : folder.listFiles()){
			
			if(fileEntry.toString().endsWith(".csv")){
//				System.out.println("file Entry : " + fileEntry);
				fileList.add(fileEntry.toString());
			}
		}
		
		for(String str : fileList){
			
			BufferedReader br = new BufferedReader(new FileReader(str));
			
			System.out.println(str);
			String line = br.readLine();
			
			CLI.header = line;
	        System.out.println("Header : " + CLI.header);
			
			String [] temp;
			int dummy_cnt = 0;
			
			while(line != null){
				temp = line.split(",");
				
				for(int i = 0; i < temp.length; i++){
					if(temp[i].equals("0")){
						dummy_cnt++;
					}
				}
//				System.out.println(dummy_cnt);
				
				if(dummy_cnt != 8){
//					System.out.println(line);
					no_dummy.add(line+"\n");
				}
				
				line = br.readLine();
				dummy_cnt = 0;
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(str));
			
			for(String inst : no_dummy){
				bw.write(inst);
			}
			
			System.out.println("File completed");
			bw.flush();
			bw.close();
			conv.csvToArff(str);
			no_dummy.clear();
		}
	}
	
	public int get_Generated_Num(String input_path) throws IOException{
		int buggy_cnt = 0;
		int clean_cnt = 0;
		
		
		BufferedReader br = new BufferedReader(new FileReader(input_path));
		String line = br.readLine();
//		System.out.println(line);
		line = br.readLine();
		while(line != null){

			if(line.contains(CLI.buggy_label) && !line.contains("@"))
				buggy_cnt++;
			if(line.contains(CLI.clean_label) && !line.contains("@"))
				clean_cnt++;
			
			line = br.readLine();
		}
		
		System.out.println("Buggy number : " + buggy_cnt);
		System.out.println("Clean number : " + clean_cnt);
		
		CLI.buggy_cnt = buggy_cnt;
		CLI.clean_cnt = clean_cnt;
		
		return (clean_cnt - buggy_cnt); //buggy_cnt;//
	}
	
	public double get_Generated_Ratio(String input_path) throws IOException{
		double buggy_cnt = 0.0;
		double clean_cnt = 0.0;
		
		
		BufferedReader br = new BufferedReader(new FileReader(input_path));
		String line = br.readLine();
		CLI.header = line;
//		System.out.println(line);
		line = br.readLine();
		while(line != null){

			if(line.contains(CLI.buggy_label) && !line.contains("@"))
				buggy_cnt++;
			if(line.contains(CLI.clean_label) && !line.contains("@"))
				clean_cnt++;
			
			line = br.readLine();
		}
		
		System.out.println("Buggy number : " + buggy_cnt);
		System.out.println("Clean number : " + clean_cnt);
		
		CLI.buggy_cnt = buggy_cnt;
		
		return (clean_cnt - buggy_cnt);
	}
	
	public void parseBuggyLabel(String buggy_path) throws IOException{
		
		ArrayList <String> temp = new ArrayList<String>();
		
		File file = new File(buggy_path);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

		String line = null;
		
		while((line = br.readLine()) != null){
			String label = "," + CLI.buggy_label;
			temp.add(line + label + System.lineSeparator());
		}
		
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
		
		for(String s : temp)
			bw.write(s);
		bw.close();
		
	}
	
	public void mergeFiles(String original_path, String generated_path, String merged_path) throws IOException{
		
		ArrayList<String> buggy_data = new ArrayList <String>();
		ArrayList<String> clean_data = new ArrayList <String>();
		
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(merged_path, true));
		PrintWriter pw = new PrintWriter(bw, true);
	
		BufferedReader br1 = new BufferedReader(new FileReader(original_path));
		BufferedReader br2 = new BufferedReader(new FileReader(generated_path));
		
		String line = br1.readLine();
		CLI.header = line;
		line = br1.readLine();
		
		while(line != null && !line.contains("@")){
			
			if(line.contains("buggy"))
				buggy_data.add(line);
			else if(line.contains("clean"))
				clean_data.add(line);
			
//			pw.println(line);
		
			line = br1.readLine();
		}
		
		br1.close();
		
		line = br2.readLine();
		
		while(line != null){
			
			if(line.contains(CLI.buggy_label) && !line.contains("@"))
				buggy_data.add(line);
			else if(line.contains(CLI.clean_label) && !line.contains("@"))
				clean_data.add(line);
//			pw.println(line);
		
			line = br2.readLine();
		}
		
		br2.close();
		
		//to solve an incompatible problem caused in weka
		
		pw.println(CLI.header);
		
		int buggy_idx = CLI.label_order.indexOf(CLI.buggy_label);
		int clean_idx = CLI.label_order.indexOf(CLI.clean_label);
		
		if(buggy_idx < clean_idx){
			for(String inst : buggy_data)
				pw.println(inst);
			
			for(String inst : clean_data)
				pw.println(inst);
		}
		
		else{
			for(String inst : clean_data)
				pw.println(inst);
			
			for(String inst : buggy_data)
				pw.println(inst);
		}

		pw.flush();
		pw.close();
		
		Converter conv = new Converter();
		conv.csvToArff(merged_path);
		
	}
}
