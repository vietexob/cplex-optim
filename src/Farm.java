import ilog.concert.*;
import ilog.cplex.*;

/**
 * Farm LP example.
 * 
 * @author trucvietle
 *
 */
public class Farm {
	public static void main(String[] args) {
		int n = 2; // number of decision variables
		int m = 3; // number of constraints
		double[] c = {10, 12}; // cost vector
		double[][] A = {{1, 1}, {5, 3}, {3, 5}}; // constraint coefficient matrix
		double[] b = {20, 10, 12}; // capacity constraints
		solveModel(n, m, c, A, b);
	}
	
	/**
	 * Defines the solves the LP.
	 * 
	 * @param n Number of variables
	 * @param m Number of constraints
	 * @param c Cost vector
	 * @param A Constraint coefficient matrix
	 * @param b Capacity constraint vector
	 */
	public static void solveModel(int n, int m, double[] c,
			double[][] A, double[] b) {
		try {
			// Instantiate an empty model
			IloCplex model = new IloCplex();
			
			// Define an array of decision variables
			IloNumVar[] x = new IloNumVar[n];
			for(int i = 0; i < n; i++) {
				// Define each variable's range from 0 to +Infinity
				x[i] = model.numVar(0, Double.MAX_VALUE);
			}
			
			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			// Add expressions to the objective function
			for(int i = 0; i < n; i++) {
				obj.addTerm(c[i], x[i]);
			}
			// Define a maximization problem
			model.addMaximize(obj);
			
			// Define the constraints
			for(int i = 0; i < m; i++) { // for each constraint
				IloLinearNumExpr constraint = model.linearNumExpr();
				for(int j = 0; j < n; j++) { // for each variable
					constraint.addTerm(A[i][j], x[j]);
				}
				// Define the RHS of the constraint
				model.addLe(constraint, b[i]);
			}
			
			// Suppress the auxiliary output printout
			model.setParam(IloCplex.IntParam.SimDisplay, 0);
			
			// Solve the model and print the output
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
		} catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
