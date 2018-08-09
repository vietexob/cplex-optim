import ilog.concert.*;
import ilog.cplex.*;

public class MaxSelmair {

	public static void main(String[] args) {
		// Define the cost matrix
		// Number of rows: number of vehicles
		// Number of columns: number of jobs
		double[][] cost = {
				{150, 160, 90, 120, 150},
				{90, 90, 120, 90, 140},
				{120, 120, 150, 120, 90},
				{100, 90, 150, 150, 120},
				{140, 120, 90, 140, 90},
				{180, 150, 180, 120, 120},
				{160, 120, 160, 100, 150},
				{110, 100, 130, 80, 110}};
		
		int numVehicles = cost.length;
		int numJobs = cost[0].length;
		solveModel(numVehicles, numJobs, cost);
	}
	
	/***
	 * Formulates and solves the LP model.
	 * 
	 * @param n Number of vehicles
	 * @param m Number of jobs
	 * @param cost Cost matrix
	 */
	public static void solveModel(int n, int m, double[][] cost) {
		try {
			// Define an empty model
			IloCplex model = new IloCplex();
			
			// Define the binary (decision) variables
			IloNumVar[][] x = new IloNumVar[n][m];
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < m; j++) {
					x[i][j] = model.boolVar();
				}
			}
			
			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < m; j++) {
					obj.addTerm(cost[i][j], x[i][j]);
				}
			}
			model.addMinimize(obj);
			
			// Define the constraints
			// (1) Every job must be scheduled
			for(int j = 0; j < m; j++) {
				IloLinearNumExpr nonStarvation = model.linearNumExpr();
				for(int i = 0; i < n; i++) {
					nonStarvation.addTerm(1, x[i][j]);
				}
				model.addEq(nonStarvation, 1);
			}
			// (2) Each vehicle performs no more than one job
			for(int i = 0; i < n; i++) {
				IloLinearNumExpr nonBurnout = model.linearNumExpr();
				for(int j = 0; j < m; j++) {
					nonBurnout.addTerm(1, x[i][j]);
				}
				model.addLe(nonBurnout, 1);
			}
			
			// Print out the optimal objective value and decision variables
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				System.out.println("Objective value = " + objValue);
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < m; j++) {
						int decisionVar = (int) (model.getValue(x[i][j]));
						System.out.print(decisionVar + "\t");
					}
					System.out.println();
				}
			} else {
				System.out.println("Model not solved :(");
			}
			
			// Free the memory
			model.end();
		} catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
