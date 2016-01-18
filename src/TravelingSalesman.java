import ilog.concert.*;
import ilog.cplex.*;

public class TravelingSalesman {
	// The problem: https://www.youtube.com/watch?v=QzOLL2tUXKE
	
	public static void main(String[] args) {
		model(28);
	}
	
	public static void model(int n) {
		// Generate n random cities
		double[] xPos = new double[n];
		double[] yPos = new double[n];
		for(int i = 0; i < n; i++) {
			xPos[i] = Math.random() * 100;
			yPos[i] = Math.random() * 100;
		}
		
		// Generate the Euclidean distance matrix between the cities
		double[][] c = new double[n][n];
		for(int i = 0; i < n; i++) {
			for(int j = 0; j < n; j++) {
				c[i][j] = Math.sqrt(Math.pow(xPos[i] - xPos[j], 2) + Math.pow(yPos[i] - yPos[j], 2));
			}
		}
		
		// Define the model
		try {
			IloCplex cplex = new IloCplex();
			// Variables
			IloNumVar[][] x = new IloNumVar[n][];
			for(int i = 0; i < n; i++) {
				x[i] = cplex.boolVarArray(n);
			}
			
			IloNumVar[] u = cplex.numVarArray(n, 0, Double.MAX_VALUE);
			
			// Objective
			IloLinearNumExpr obj = cplex.linearNumExpr();
			for(int i = 0; i < n; i++) {
				for(int j = 0; j < n; j++) {
					if(j != i) {
						obj.addTerm(c[i][j], x[i][j]);
					}
				}
			}
			cplex.addMinimize(obj);
			
			// Constraints
			for(int j = 0; j < n; j++) {
				IloLinearNumExpr expr = cplex.linearNumExpr();
				for(int i = 0; i < n; i++) {
					if(i != j) {
						expr.addTerm(1.0, x[i][j]);
					}
				}
				// The constraint
				cplex.addEq(expr, 1);
			}
			
			// The second constraint
			for(int i = 0; i < n; i++) {
				IloLinearNumExpr expr2 = cplex.linearNumExpr();
				for(int j = 0; j < n; j++) {
					if(j != i) {
						expr2.addTerm(1.0, x[i][j]);
					}
				}
				cplex.addEq(expr2, 1);
			}
			
			// The third constraint
			for(int i = 1; i < n; i++) {
				for(int j = 1; j < n; j++) {
					if(j != i) {
						IloLinearNumExpr expr3 = cplex.linearNumExpr();
						expr3.addTerm(1.0, u[i]);
						expr3.addTerm(-1.0, u[j]);
						expr3.addTerm(n-1, x[i][j]);
						cplex.addLe(expr3, n-2);
					}
				}
			}
			
			// Solve the model
			cplex.solve();
			System.out.println("Obj fun = " + cplex.getObjValue());
			
			cplex.end();
		} catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
