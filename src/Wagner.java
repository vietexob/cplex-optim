import java.util.ArrayList;
import java.util.List;

import ilog.concert.*;
import ilog.cplex.*;

/**
 * The Wagner diet problem (Kimbrough & Lau, 2016, p. 44).
 * 
 * @author trucvietle
 *
 */
public class Wagner {
	// Video tutorial: https://www.youtube.com/watch?v=oA86HCkCg5k
	
	public static void main(String[] args) {
		int n = 3; // number of decision variables (types of grain)
		int m = 4; // number of constraints (the ingredients)
		
		double[] c = {41, 35, 96}; // cost of each type of grain
		
		// Define the coefficient matrix for the constraint
		double[][] A = {{2, 3, 7}, {1, 1, 0}, {5, 3, 0}, {0.60, 0.25, 1}};
		
		double[] b = {1250, 250, 900, 232.50}; // RHS of the constraints
		
		// Specify and solve the optimization problem
		solveModel(n, m, c, A, b);
	}
	
	/**
	 * Defines and solves the LP.
	 * 
	 * @param n Number of decision variables
	 * @param m Number of constraints
	 * @param c Cost vector
	 * @param A Constraint coefficient matrix
	 * @param b Demand vector (RHS of the constraints)
	 */
	public static void solveModel(int n, int m, double[] c, double[][] A,
			double[] b) {
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
			// Define and add expressions for objective function
			for(int i = 0; i < n; i++) {
				obj.addTerm(c[i], x[i]);
			}
			// Define a minimization problem
			model.addMinimize(obj);
			
			// Define the constraints
			// Create a list of constraints
			List<IloRange> constraints = new ArrayList<IloRange>();
			
			for(int i = 0; i < m; i++) { // for each constraint
				IloLinearNumExpr constraint = model.linearNumExpr();
				for(int j = 0; j < n; j++) { // for each variable
					constraint.addTerm(A[i][j], x[j]);
				}
				constraints.add(model.addGe(constraint, b[i]));
			}
			
			// Suppress the auxiliary output printout
			model.setParam(IloCplex.IntParam.SimDisplay, 0);
			// Solve the model
			boolean isSolved = model.solve();
			if(isSolved) {
				// Print out the objective function
				System.out.println("obj_val = " + model.getObjValue());
				System.out.println();
				
				// Print out the decision variables
				for(int i = 0; i < n; i++) { // for each variable
					System.out.println("x[" + (i+1) + "] = " + model.getValue(x[i]));
					System.out.println("Reduced cost = " + model.getReducedCost(x[i]));
					System.out.println();
				}
				// Check for binding/non-binding constraints
				for(int i = 0; i < constraints.size(); i++) { // for each constraint
					double slack = model.getSlack(constraints.get(i));
					double dual = model.getDual(constraints.get(i));
					if(slack != 0) {
						System.out.println("Constraint " + (i+1) + " is non-binding.");
					} else {
						System.out.println("Constraint " + (i+1) + " is binding.");
					}
					System.out.println("Shadow price = " + dual);
					System.out.println();
				}
			} else {
				System.out.println("Model not solved :(");
			}
		} catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
