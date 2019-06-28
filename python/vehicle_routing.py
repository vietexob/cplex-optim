import sys
import numpy as np

from docplex.mp.model import Model

def generate_input(filename='vrp_input.txt'):
	# Number of customers: Random integer in [10, 20]
	n = np.random.randint(10, 21)
	# Number of vehicles: Random integer in [5, 10]
	K = np.random.randint(5, 11)
	# Vehicle capacity: Random integer in [10, 20]
	Q = np.random.randint(10, 21)
	# Set of customers
	C = [i for i in range(1, n+1)]

	# Generate random demand for each customer using a dict: cust_id --> demand
	# Each customer has a demand from 1 to 10 (upper bound)
	q = {i: np.random.randint(1, 11) for i in C}
	
	# Generate random x, y coordinates for each customer
	coord_x = np.random.rand(len(C)) * 200
	coord_y = np.random.rand(len(C)) * 200

	# Write the generated input file
	first_line = str(n) + ' ' + str(K) + ' ' + str(Q) + '\n'
	file = open(filename, 'w')
	file.write(first_line)
	for k in q.keys():
		demand = q[k]
		loc_x = coord_x[k-1]
		loc_y = coord_y[k-1]
		next_line = str(demand) + ' ' + str(loc_x) + ' ' + str(loc_y) + '\n'
		file.write(next_line)
	file.close()

def read_input(filename='vrp_input.txt'):
	file = open(filename, 'r')
	counter = 0
	n = 0
	K = 0
	Q = 0
	list_demands = []
	list_coord_x = []
	list_coord_y = []

	for line in file:
		tokens = line.split()
		if counter == 0:
			n = int(tokens[0])
			K = int(tokens[1])
			Q = int(tokens[2])
		else:
			q = int(tokens[0])
			coord_x = float(tokens[1])
			coord_y = float(tokens[2])
			list_demands.append(q)
			list_coord_x.append(coord_x)
			list_coord_y.append(coord_y)

		counter += 1

	file.close()
	return n, K, Q, list_demands, list_coord_x, list_coord_y

def get_routes(active_edges):
	"""
	Returns the list of routes given the solved active edges (==1)
	"""

	start_edges = [e for e in active_edges if e[0] == 0]
	list_routes = []
	for start in start_edges:
		route = []
		route.append(start)
		while True:
			last_edge = route[-1]
			next_edge = None
			for edge in active_edges:
				if edge[0] == last_edge[1]:
					next_edge = edge
					break
			if next_edge is not None:
				route.append(next_edge)
				if next_edge[1] == 0:
					list_routes.append(route)
					break
			else:
				break

	return list_routes

if __name__ == '__main__':
	# generate_input()
	if len(sys.argv) > 1:
		filename = sys.argv[1]
	else:
		filename = 'vrp_input.txt'

	n, K, Q, list_demands, list_coord_x, list_coord_y = read_input()
	C = [i for i in range(1, n+1)] # set of customers
	V = [0] + C # set of vertices, including the depot at node 0

	# Demand of each customer is a dict: cust_id --> demand
	q = {i: list_demands[i-1] for i in C}

	# Get x, y coordinates of each customer
	coord_x = [0] * len(V)
	coord_y = [0] * len(V)

	# The depot is fixed at location (100, 100)
	coord_x[0] = 100
	coord_y[0] = 100
	
	# The rest are customer locations
	coord_x[1:] = list_coord_x
	coord_y[1:] = list_coord_y

	# Set of all points (coordinates)
	coords = [np.array((x, y)) for (x, y) in zip(coord_x, coord_y)]

	# Set of edges: Assume a complete graph
	E = [(i, j) for i in V for j in V if i != j]
	
	# Compute the Euclidean distance (cost) between each pair of nodes in the graph
	# Cost is represented as a dict: (node tuple) --> float
	c = {(i, j): np.linalg.norm(coords[i] - coords[j]) for i, j in E}
	

	# Define an empty model
	model = Model('CVRP')

	# Defined the decision variables
	# The binary variable for each edge in the graph
	x = model.binary_var_dict(E, name = 'x')
	# The cumulative demand served by each vehicle upper bounded by the vehicle's capacity
	# Default lower bound is 0
	u = model.continuous_var_dict(C, ub = Q, name = 'u')
	
	# Define the objective function
	model.minimize(model.sum(c[i, j] * x[i, j] for i, j in E))

	# Define the constraints:
	# Each customer is visited once
	model.add_constraints(model.sum(x[i, j] for j in V if j != i) == 1 for i in C)
	# Any vehicle arriving at a node must also depart from that node
	model.add_constraints(model.sum(x[i, j] for i in V if i != j) == 1 for j in C)
	# There cannot be more routes than the number of vehicles
	model.add_constraints(model.sum(x[i, j] for j in C if i == 0) <= K for i in V)
	# If edge (i, j) belongs to a route then the cumulative demand of the route is the sum of the cumulative demand up to i
	# and the demand at j
	model.add_indicator_constraints(model.indicator_constraint(x[i, j], u[i] + q[j] == u[j]) for i, j in E if i != 0 and j != 0)
	# Cumulative demand at any node i has to be at least its demand
	model.add_constraints(u[i] >= q[i] for i in C)

	# Add a time limit (seconds) on the solver
	model.parameters.timelimit = 60

	# Solve and obtain the solution
	solution = model.solve(log_output=True)
	if solution is not None:
		objective_value = solution.objective_value
		solve_status_str = str(solution.solve_status)
		# print(solve_status_str)
		is_optimal = solve_status_str == 'JobSolveStatus.OPTIMAL_SOLUTION'
		
		# Write to the output file
		first_line = str(objective_value) + ' ' + str(int(is_optimal)) + '\n'
		file = open('vrp_output.txt', 'w')
		file.write(first_line)

		active_edges = [e for e in E if x[e].solution_value > 0.9]
		# print(active_edges)
		list_routes = get_routes(active_edges)
		for route in list_routes:
			next_line = ''
			for edge in route:
				next_line += str(edge[0]) + ' '
			next_line += '0\n'
			file.write(next_line)
		file.close()
	else:
		print('No solution reached within time limit!')
