package dp_tool;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Clone {
	
	
	public void cloneInstance(ArrayList <String> bug_data, ArrayList <String> clean_data, int repeat, int fold) throws IOException{
		
		Converter conv = new Converter();
		int generating_num = clean_data.size() - bug_data.size();	
		int loop_num = generating_num / bug_data.size();
		int remainder = generating_num % bug_data.size();
		
		
		int buggy_idx = CLI.label_order.indexOf(CLI.buggy_label);
		int clean_idx = CLI.label_order.indexOf(CLI.clean_label);
		
		if(buggy_idx < clean_idx) { // if the order of label_order is buggy, clean
			PrintWriter pw = new PrintWriter(new File(CLI.savingDir + CLI.train + "clone_" + repeat + "_" + fold + ".csv"));
			
			pw.write(CLI.header + "\n");
			
			for(String buggy : bug_data){
				pw.write(buggy + "\n");
			}
			
			for(int i = 0; i < loop_num; i++){
				for(String buggy : bug_data){
					pw.write(buggy + "\n");
				}
			}
			
			for(int i = 0; i <= remainder; i++){
				pw.write(bug_data.get(i) + "\n");
			}
			
			for(String clean : clean_data){
				pw.write(clean + "\n");
			}
			

			pw.flush();
			pw.close();
			
			conv.csvToArff(CLI.savingDir + CLI.train + "clone_" + repeat + "_" + fold + ".csv");
		}
		
		else{
			PrintWriter pw = new PrintWriter(new File(CLI.savingDir + CLI.train + "clone_" + repeat + "_" + fold + ".csv"));
			
			pw.write(CLI.header + "\n");
			
			for(String clean : clean_data){
				pw.write(clean + "\n");
			}
			
			for(String buggy : bug_data){
				pw.write(buggy + "\n");
			}
			
			for(int i = 0; i < loop_num; i++){
				for(String buggy : bug_data){
					pw.write(buggy + "\n");
				}
			}
			
			for(int i = 0; i <= remainder; i++){
				pw.write(bug_data.get(i) + "\n");
			}

			pw.flush();
			pw.close();
			
			conv.csvToArff(CLI.savingDir + CLI.train + "clone_" + repeat + "_" + fold + ".csv");
		}
		
		
//		OverSample os = new OverSample();
//		os.make_compatible(CLI.savingDir + CLI.train + "clone_" + repeat + "_" + fold + ".csv", repeat, fold); // make compatible 필요없음. 헤더 문제라 converting할때 해결해주면 됌.
	
	}
	
	public void correctLabelOrder(){
		
	}

}
