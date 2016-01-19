import ilog.concert.*;
import ilog.cplex.*;

public class Factory {
	public static void main(String[] args) {
		int n = 3; // number of products
		int k = 2; // number of resources
		// Cost of production for each product for each resource type
		double[][] cost = {{0.6, 0.8}, {0.8, 0.9}, {0.3, 0.4}};
		// Capacity coefficient matrix (per resource type per product)
		double[][] capacityMatrix = {{0.5, 0.4, 0.3}, {0.2, 0.4, 0.6}};
		// Capacity constraints (for each resource type)
		double[] capacity = {20, 40};
		// Demand constraints (for each product)
		double[] demand = {100, 200, 300};
		
		// Solve the model
		solveModel(n, k, cost, capacityMatrix, capacity, demand);
	}
	
	public static void solveModel(int n, int k, double[][] cost,
			double[][] capacityMatrix, double[] capacity, double[] demand) {
		try {
			// Instantiate an empty model
			IloCplex model = new IloCplex();
			
			// Define multidimensional array of decision variables
			IloNumVar[][] x = new IloNumVar[n][];
			for(int i = 0; i < n; i++) { // for each product
				x[i] = model.numVarArray(k, 0, Double.MAX_VALUE);
			}
			
			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			// Add expressions to the objective function
			for(int i = 0; i < n; i++) { // for each product
				for(int j = 0; j < k; j++) { // for each resource
					obj.addTerm(cost[i][j], x[i][j]);
				}
			}
			// Define a minimization problem
			model.addMinimize(obj);
			
			// Define the constraints
			// (1) Capacity constraints
			for(int i = 0; i < k; i++) { // for each type of resource
				IloLinearNumExpr constraint = model.linearNumExpr();
				for(int j = 0; j < n; j++) { // for each product
					constraint.addTerm(capacityMatrix[i][j], x[j][0]);
				}
				model.addLe(constraint, capacity[i]);
			}
			// (2) Demand constraints
			for(int i = 0; i < n; i++) { // for each product
				IloLinearNumExpr constraint = model.linearNumExpr();
				for(int j = 0; j < k; j++) { // for each type of resource
					constraint.addTerm(1.0, x[i][j]);
				}
				model.addGe(constraint, demand[i]);
			}
			
			// Suppress the auxiliary output printout
			model.setParam(IloCplex.IntParam.SimDisplay, 0);
			
			// Solve the model and print the output
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				System.out.println("obj_val = " + objValue);
				
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < k; j++) {
						System.out.println("x[" + (i+1) + "," + (j+1) + "] = " + model.getValue(x[i][j]));
					}
				}
			} else {
				System.out.println("Model not solved :(");
			}
		} catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
