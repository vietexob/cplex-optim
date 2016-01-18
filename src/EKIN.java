import ilog.concert.*;
import ilog.cplex.*;

/**
 * The EKIN company production planning problem in Lecture 2.
 * Template for using LP to solve the production planning problem.
 * 
 * @author trucvietle
 *
 */

public class EKIN {
	public static void main(String[] args) throws IloException {
		// Define the problem parameters (data)
		int n = 2; // number of products (A and B)
		int m = 2; // choices of production (internally or outsourcing)
		
		// Production capacity for each product if produced inhouse
		int[] capacity = {1000, 2000};
		
		// Unit cost matrix per product per mode of production
		double[][] unitCost = {{1, 3}, {2, 2}};
		
		// Unit cost matrix per product per choice of production
		double[][] prodCost = {{10, 13}, {15, 18}};
				
		// Demand for each product
		int[] demand = {1200, 800};
		
		// Specify the model and optimize for cost
		solveModel(n, m, prodCost, unitCost, capacity, demand);
	}
	
	/**
	 * Defines and solves the model: decision variables, objective function, and constraints.
	 * 
	 * @param n Number of products
	 * @param m Choices of production 
	 * @param prodCost Unit cost per product per production choice (inhouse/outsourcing)
	 * @param unitCost Unit cost per product per production mode (inhouse)
	 * @param capacity Capacity constraints for inhouse production
	 * @param demand Demand constraints
	 */
	public static void solveModel(int n, int m, double[][] prodCost,
			double[][] unitCost, int[] capacity, int[] demand) {
		try {
			// Instantiate an empty model
			IloCplex model = new IloCplex();
			
			// Define the decision variables
			// Production quantity per production per production choice
			IloNumVar[][] x = new IloNumVar[n][];
			for(int i = 0; i < n; i++) {
				x[i] = model.numVarArray(m, 0, Double.MAX_VALUE);
			}
			
			// Define the objective function
			IloLinearNumExpr obj = model.linearNumExpr();
			// Add terms to the objective function
			for(int i = 0; i < n; i++) { // for each product
				for(int j = 0; j < m; j++) { // for each choice of production
					obj.addTerm(prodCost[i][j], x[i][j]);
				}
			}
			model.addMinimize(obj);
			
			// Define the constraints
			// (1) How much manpower/machine power would be required if produced inhouse
			IloLinearNumExpr[] inhouseCapacity = new IloLinearNumExpr[2];
			// Define the expressions for capacity constraints
			for(int i = 0; i < 2; i++) { // for each production mode
				inhouseCapacity[i] = model.linearNumExpr();
				for(int j = 0; j < n; j++) { // for each product
					inhouseCapacity[i].addTerm(unitCost[j][i], x[j][0]);
				}
				model.addLe(inhouseCapacity[i], capacity[i]);
			}
			// (2) Define the demand constraints
			IloLinearNumExpr[] demandConstraint = new IloLinearNumExpr[n];
			for(int i = 0; i < n; i++) { // for each product
				demandConstraint[i] = model.linearNumExpr();
				for(int j = 0; j < m; j++) { // for each mode of production
					demandConstraint[i].addTerm(1.0, x[i][j]);
				}
				model.addGe(demandConstraint[i], demand[i]);
			}
			
			// Suppress the output printout
			model.setParam(IloCplex.IntParam.SimDisplay, 0);
			// Solve the model
			boolean isSolved = model.solve();
			if(isSolved) {
				System.out.println("obj_fun = " + model.getObjValue());
				for(int i = 0; i < n; i++) {
					for(int j = 0; j < m; j++) {
						System.out.println("x[" + i + "," + j + "] = " + model.getValue(x[i][j]));
					}
				}
			} else {
				System.out.print("Model not solved.");
			}
						
		} catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
