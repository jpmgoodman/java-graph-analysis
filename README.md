# java-graph-analysis
Library for determining various characteristics about graphs / networks.
Given the adjacency matrix of a simple graph as input, this library has the following functions:

**Undirected, unweighted graphs**
* Determine number of vertices
* Determine number of edges
* Determine the total degree of the graph
* Determine the max degree of the graph
* Determine if the graph is connected
* Determine the number of connected components
* Determine if there is a path between vertices u and v
* Determine if the graph has a cycle
  * Determine if the graph is a tree or forest
* Determine if the graph is bipartite

**Bipartite graphs**
* Determine size of max cardinality matching (Hopcroft-Karp implementation)
* Determine edge set of max cardinality matching (Hopcroft-Karp implementation)

The library contains the following classes:
* Graph (representation of a graph)
* Edge (representation of an edge in the graph)
* DFS (depth first search)
* HopcroftKarp (custom implementation of the Hopcroft-Karp algorithm)
* RandomGraph (random graph generator)

Currently, the graph has two instance variables that represent it: (1) an
adjacency matrix, and (2) an ArrayList of ArrayLists of Edge objects. At this
point, all functions utilize the second implementation, as it makes the code
much easier to understand, and because the latter implementation facilitates
 traversal along edges.

Note that several test input adjacency matrices are located in the test_input_graphs
directory. There, you will find several popular graphs.
