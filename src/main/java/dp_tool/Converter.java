package dp_tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;


/***
 *	Conversion between ARFF and CSV files
 * @author Jin, Young In. 
 * Please report any issues to jinyi1187@gmail.com. Thank you!
 */
public class Converter {
	
	String csv_path, arff_path;
	String input_directory;
	
	/**
	 * Converting CSV file to ARFF file
	 * @param csv_path
	 * @param arff_path
	 * @return returns ARFF file
	 */
	public String csvToArff(String csv_path, String arff_path){
		try{
						
				CSVLoader loader = new CSVLoader();
				loader.setSource(new File(csv_path));
				String [] options = new String[1];
				options[0] = "-H";
				loader.setOptions(options);
				
				Instances data = loader.getDataSet();

				//save as an arff (output file)
				ArffSaver saver = new ArffSaver();
				saver.setInstances(data);
				saver.setFile(new File(arff_path));
				
				saver.writeBatch();
				
			} catch(Exception e){
				e.printStackTrace();
			}
		
		return arff_path;
	}
	
	public String csvToArff(String csv_path){ //flag = 0 : no header, 1 : exists header
		try{
			
			
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(csv_path));
			
			Instances data = loader.getDataSet();

			//save as an arff (output file)
			ArffSaver saver = new ArffSaver();
			saver.setInstances(data);
			saver.setFile(new File(csv_path.replace(".csv", ".arff")));
			
			saver.writeBatch();

			} catch(Exception e){
				e.printStackTrace();
			}
		
		return arff_path;
	}
	
	/***
	 * Converting ARFF file to CSV file
	 * @param arff_path
	 * @param new_csv_path
	 * @return returns CSV file
	 */
	public String arffToCSV(String arff_path, String csv_name){
		
		int index = arff_path.indexOf(".arff");
		
		ArrayList<String> directories = new ArrayList<String>();
		
		for(String tok : arff_path.split("/")){
			directories.add(tok);
		}

		input_directory = directories.get(0) + "/";
		for(int i = 1; i < directories.size()-1; i++){
			input_directory = input_directory + directories.get(i) + "/";
		}
		
//		System.out.println(input_directory);
		
		String new_csv_name = input_directory + csv_name + ".csv";
		
		//load ARFF
		ArffLoader loader = new ArffLoader();
		try {
			loader.setSource(new File(arff_path));
			Instances data = loader.getDataSet(); 
			
			//save CSV
			CSVSaver saver = new CSVSaver();
			saver.setInstances(data);
			//and save as CSV
			saver.setFile(new File(new_csv_name));
			
			saver.writeBatch();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new_csv_name;
	}
	
	public String normalizeData(String input_path) throws Exception{
		
		System.out.println("Start normalizing");
	
		if(input_path.contains(".csv")){
			Converter conv = new Converter();
			conv.csvToArff(input_path);
			input_path = input_path.replace(".csv", ".arff");
		}
		
		DataSource source = new DataSource(input_path);
	    Instances dataset = source.getDataSet();
	    dataset.setClassIndex(dataset.numAttributes()-1);
	    /*
		 normalize all the attribute values between 0 and 1
		*/
	    Normalize normalize = new Normalize();
	    normalize.setInputFormat(dataset);
	    Instances newdata = Filter.useFilter(dataset, normalize);
	    
	    String norm_input_path = input_path.replace(".arff", "_normalized.arff");
	    
	    ArffSaver saver = new ArffSaver();
	    saver.setInstances(newdata);
	    saver.setFile(new File(norm_input_path));
	    saver.writeBatch();
	    
	    System.out.println("Normalization Completed");
	    
	    return norm_input_path;
	}
	
	public String arffToCSV(String arff_path){
		
//		int index = arff_path.indexOf(".arff");
		
		String default_name = arff_path.replace(".arff", ".csv"); //arff_path.substring(0, index) + ".csv";
		
		//load ARFF
				ArffLoader loader = new ArffLoader();
				try {
					loader.setSource(new File(arff_path));
					Instances data = loader.getDataSet(); 
					
					//save CSV
					CSVSaver saver = new CSVSaver();
					saver.setInstances(data);
					//and save as CSV
					saver.setFile(new File(default_name));
					
					saver.writeBatch();
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				return default_name;
	}
	
	
}

