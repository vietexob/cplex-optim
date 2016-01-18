import java.util.ArrayList;
import java.util.List;

import ilog.concert.*; // for model specification
import ilog.cplex.*; // for solving model

public class Example01 {
	// The problem: https://www.youtube.com/watch?v=sf59_7r8QSY
	
	public static void main(String[] args) {
		model01();
	}
	
	public static void model01() {
		try {
			IloCplex cplex = new IloCplex();
			
			// Decision variables
			IloNumVar x = cplex.numVar(0, Double.MAX_VALUE, "x");
			IloNumVar y = cplex.numVar(0, Double.MAX_VALUE, "y");
			
			// Expressions
			IloLinearNumExpr objective = cplex.linearNumExpr();
			objective.addTerm(0.12, x);
			objective.addTerm(0.15, y);
			
			// Define objective function
			cplex.addMinimize(objective);
			
			// Define constraints
			// Create a list of constraints
			List<IloRange> constraints = new ArrayList<IloRange>();
			
			constraints.add(cplex.addGe(cplex.sum(cplex.prod(60, x), cplex.prod(60, y)), 300));
			constraints.add(cplex.addGe(cplex.sum(cplex.prod(12, x), cplex.prod(6, y)), 36));
			constraints.add(cplex.addGe(cplex.sum(cplex.prod(10, x), cplex.prod(30, y)), 90));
			
			// Additional constraint (1)
			IloLinearNumExpr numExpr = cplex.linearNumExpr();
			numExpr.addTerm(2, x);
			numExpr.addTerm(-1, y);
			constraints.add(cplex.addEq(numExpr, 0));
			
			// Additional constraint (2)
			IloLinearNumExpr numExpr2 = cplex.linearNumExpr();
			numExpr2.addTerm(-1, x);
			numExpr2.addTerm(1, y);
			constraints.add(cplex.addLe(numExpr2, 8));
			
			// Suppress the output printout
			cplex.setParam(IloCplex.IntParam.SimDisplay, 0);
			
			// Solve the model
			if(cplex.solve()) {
				System.out.println("obj = " + cplex.getObjValue());
				System.out.println("x = " + cplex.getValue(x));
				System.out.println("y = " + cplex.getValue(y));
				
				// Get the dual values
				for(int i = 0; i < constraints.size(); i++) {
					System.out.println("Dual constraint " + (i+1) + " = " + cplex.getDual(constraints.get(i)));
					System.out.println("Slack constraint " + (i+1) + " = " + cplex.getSlack(constraints.get(i)));
					
				}
			} else {
				System.out.println("Model not solved!");
			}
			
			// Free the memory
			cplex.end();
		}
		catch(IloException ex) {
			ex.printStackTrace();
		}
	}
}
