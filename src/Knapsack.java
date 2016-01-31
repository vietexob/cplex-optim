import ilog.concert.*;
import ilog.cplex.*;

public class Knapsack {
	public static void main(String[] args) {
		// Run the Eilon model
		int n = 12; // number of items
		double[] value = {4113, 2890, 577, 1780, 2096, 2184,
				1170, 780, 739, 147, 136, 211};
		double[] weight = {131, 119, 37, 117, 140, 148,
				93, 64, 78, 16, 22, 58};
		double capacity = 620;
		solveModel(n, value, weight, capacity);
	}
	
	public static void solveModel(int n, double[] value,
			double[] weight, double capacity) {
		try {
			// Define an empty model
			IloCplex model = new IloCplex();
			
			// Define the binary variables
			IloNumVar[] x = new IloNumVar[n];
			for(int i = 0; i < n; i++) {
				x[i] = model.boolVar();
			}
			
			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			for(int i = 0; i < n; i++) {
				obj.addTerm(value[i], x[i]);
			}
			model.addMaximize(obj);
			
			// Define the constraints
			IloLinearNumExpr constraint = model.linearNumExpr();
			for(int i = 0; i < n; i++) {
				constraint.addTerm(weight[i], x[i]);
			}
			model.addLe(constraint, capacity);
			
			boolean isSolved = model.solve();
			if(isSolved) {
				double objValue = model.getObjValue();
				System.out.println("obj_val = " + objValue);
				for(int i = 0; i < n; i++) {
					System.out.println("x[" + (i+1) + "] = " + model.getValue(x[i]));
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
