# java-graph-analysis
Library for determining various characteristics about graphs / networks.
Given the adjacency matrix for a graph as input, this library has the following functions:

**Undirected, unweighted graphs**
* Determine number of vertices
* Determine number of edges
* Determine the total degree of the graph
* Determine the max degree of the graph
* Determine if the graph is connected
* Determine the number of connected components

Currently, the graph has two instance variables that represent it: (1) an
adjacency matrix, and (2) an ArrayList of ArrayLists of Edge objects. At this
point, all functions utilize the second implementation, as it makes the code
much easier to understand, and because the latter implementation facilitates
 traversal along edges.

Note that several test input adjacency matrices are located in the test_input_graphs
directory. There, you will find several popular graphs.
