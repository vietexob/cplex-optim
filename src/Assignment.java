import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ilog.concert.*;
import ilog.cplex.*;

/**
 * The classic assignment problem on weighted bipartite graph using the Singapore
 * road network, where the two sides are the taxis and passenger origin nodes.
 * 
 * @author trucvietle
 *
 */
public class Assignment {

	public static void main(String[] args) {
		Integer[] num_taxis = {5, 10, 20, 25};
		for(int a_num_taxi : num_taxis) {
			String input_filename = "./data/sin/dist_mat_" + a_num_taxi + "_" + a_num_taxi + ".csv";
			Map<Integer, Integer> rowNodeIdx = new HashMap<Integer, Integer>();
			Map<Integer, Integer> colNodeIdx = new HashMap<Integer, Integer>();
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(input_filename));
				String line = null;
				int rowCount = 0;
				int taxiLocNode = 0;
				
				while((line = br.readLine()) != null) { // for each row
					int colCount = 0;
					int originNode = 0;
					rowCount++;
					String[] strTokens = line.split(",");
					
					for(String token : strTokens) { // for each column
						if(rowCount == 1 && !token.isEmpty()) {
							originNode = Integer.parseInt(token);
						}
						
						colCount++;
						if(colCount == 1 && rowCount > 1) {
							taxiLocNode = Integer.parseInt(token);
						}
						
						if(originNode > 0) {
							System.out.println((colCount-2) + " : " + originNode);
						}
					}
					if(taxiLocNode > 0) {
						System.out.println((rowCount-2) + " : " + taxiLocNode);
					}
				}
				System.out.println("---");
				br.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
}
