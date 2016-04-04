import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
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
//		Integer[] num_taxis = {5, 10, 20, 50, 100};
		Integer[] num_taxis = {10, 13, 16};
		for(int a_num_taxi : num_taxis) {
//			String dirStr = "./data/sin/training/dist_mat_";
			String dirStr = "./data/sin/test/dist_mat_";
			String inputFilename = dirStr + a_num_taxi + "_" + a_num_taxi + ".csv";
			Map<Integer, Integer> rowNodeIdx = new HashMap<Integer, Integer>();
			Map<Integer, Integer> colNodeIdx = new HashMap<Integer, Integer>();
			double[][] distMatrix = new double[a_num_taxi][a_num_taxi];
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(inputFilename));
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
						} else {
							taxiLocNode = 0;
						}
						
						if(taxiLocNode > 0) {
							rowNodeIdx.put(rowCount-2, taxiLocNode);
						}
						
						if(originNode > 0) {
							colNodeIdx.put(colCount-2, originNode);
						} else {
							if(!token.isEmpty() && taxiLocNode == 0) {
								double travelTime = Double.parseDouble(token);
								distMatrix[rowCount-2][colCount-2] = travelTime;
							}
						}
					}
				}
				br.close();
				
				int[][] assignment = solveModel(distMatrix);
				Map<Integer, Integer> taxiLocOrigin = new HashMap<Integer, Integer>();
				for(int i = 0; i < a_num_taxi; i++) {
					for(int j = 0; j < a_num_taxi; j++) {
						if(assignment[i][j] == 1) {
							int thisTaxiLocNode = rowNodeIdx.get(i);
							int thisOriginNode = colNodeIdx.get(j);
							taxiLocOrigin.put(thisTaxiLocNode, thisOriginNode);
						}
					}
				}
				
				// Write the matching as text file
//				dirStr = "./data/sin/training/assign_";
				dirStr = "./data/sin/test/assign_";
				String outputFilename = dirStr + a_num_taxi + "_" + a_num_taxi + ".txt";
				FileWriter fw = new FileWriter(outputFilename);
				for(int taxiNode : taxiLocOrigin.keySet()) {
					int originNode = taxiLocOrigin.get(taxiNode);
					String strLine = taxiNode + ", " + originNode + "\n";
					fw.write(strLine);
				}
				System.out.println("Written to file: " + outputFilename);
				fw.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static int[][] solveModel(double[][] distMatrix) {
		int n = distMatrix[0].length;
		int[][] assignment = new int[n][n];
		
		try {
			// Define an empty model
			IloCplex model = new IloCplex();
			
			// Define the binary variables
			IloNumVar[][] x = new IloNumVar[n][n];
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < n; j++) {
					x[i][j] = model.boolVar();
				}
			}
			
			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < n; j++) {
					obj.addTerm(distMatrix[i][j], x[i][j]);
				}
			}
			model.addMinimize(obj);
			
			// Add the constraints
			// Each row sums up to 1
			for(int i = 0; i < n; i++) {
				IloLinearNumExpr rowSum = model.linearNumExpr();
				for(int j = 0; j < n; j++) {
					rowSum.addTerm(1, x[i][j]);
				}
				model.addEq(rowSum, 1);
			}
			// Each column sums up to 1
			for(int i = 0; i < n; i++) {
				IloLinearNumExpr colSum = model.linearNumExpr();
				for(int j = 0; j < n; j++) {
					colSum.addTerm(1, x[j][i]);
				}
				model.addEq(colSum, 1);
			}
			// The assignment is symmetrical
			// NOTE: This is incorrect!
//			for(int i = 0; i < n; i++) {
//				for(int j = 0; j < n; j++) {
//					if(i != j) {
//						IloLinearNumExpr numExpr = model.linearNumExpr();
//						numExpr.addTerm(1, x[i][j]);
//						model.addEq(numExpr, x[j][i]);
//					}
//				}
//			}
			
			// Suppress the output printout
			model.setParam(IloCplex.IntParam.SimDisplay, 0);
			// Solve the model
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				System.out.println("obj_val = " + objValue);
				
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < n; j++) {
						assignment[i][j] = (int) model.getValue(x[i][j]);
					}
				}
			} else {
				System.out.println("Model not solved :(");
			}
		} catch(IloException ex) {
			ex.printStackTrace();
		}
		
		return assignment;
	}
}
