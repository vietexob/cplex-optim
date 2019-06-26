import numpy as np
# Set a fixed seed for random number generator
np.random.seed(42)

from docplex.mp.model import Model

if __name__ == '__main__':
	
	n  = 10 # number of customers
	Q = 20 # fixed capacity of each vehicle
	C = [i for i in range(1, n+1)] # set of customers
	V = [0] + C # set of vertices, including the depot at node 0

	# Generate random demand for each customer using a dict: cust_id --> demand
	# Each customer has a demand from 1 to 10 (upper bound)
	q = {i: np.random.randint(1, 11) for i in C}
	
	# Generate random x, y coordinates for each customer
	coord_x = np.random.rand(len(V)) * 200
	coord_y = np.random.rand(len(V)) * 200

	# The depot is fixed at location (100, 100)
	coord_x[0] = 100
	coord_y[0] = 100
	
	# Set of all points (coordinates)
	coords = [np.array((x, y)) for (x, y) in zip(coord_x, coord_y)]

	# Set of edges: Assume a complete graph
	E = [(i, j) for i in V for j in V if i != j]
	
	# Compute the Euclidean distance (cost) between each pair of nodes in the graph
	# Cost is represented as a dict: (node tuple) --> float
	c = {(i, j): np.linalg.norm(coords[i] - coords[j]) for i, j in E}
	

	# Define the model
	model = Model('CVRP')
	# Defined the decision variables
	# The binary variable for each edge in the graph
	x = model.binary_var_dict(E, name = 'x')
	print(x)
	# The cumulative demand served by each vehicle upper bounded by the vehicle's capacity
	u = model.continuous_var_dict(C, ub = Q, name = 'u')
	print(u)
