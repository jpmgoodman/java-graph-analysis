/*******************************************************************************
* Edmond's Blossom algorithm for finding maximum matchings in general graphs.
* Time complexity: ~O(mn), where n = |V(G)|, m = |E(G)|
*
* Author: Jesse Goodman
*******************************************************************************/
import java.util.*;

public class Blossom {

    private HashSet<Edge> maxMatching;
    private Graph graph;
    private boolean[] matchedVertices;

    // run Edmond's blossom algorithm for maximum matchings in a general graph
    public Blossom(Graph g) {
        this.graph = g;
        this.maxMatching = new HashSet<Edge>();
        this.matchedVertices = new boolean[this.graph.getNumVertices()];

        // find and set maxMatching
        HashSet<Edge> augPath = new HashSet<Edge>();
        while (augPath != null) {
            this.maxMatching = Graph.symDiff(this.maxMatching, augPath);
            augPath = getAugPath(this.graph, this.maxMatching);
        }

        // check here to make sure we have a valid matching stored in
        // maxMatching after setting it in constructor
        if (!isValidMatching(this.maxMatching)) {
            throw new IllegalStateException("programmer error;" +
            " somehow created invalid matching.");
        }
    }

    // most important method; finds an augmenting path in G, given a matching M
    private HashSet<Edge> getAugPath(Graph g, HashSet<Edge> m) {
        return null;
    }

    /* VALIDATION METHODS */
    // checks if a set of edges is a valid matching (i.e., no repeated vertices)
    public static boolean isValidMatching(Set<Edge> m) {
        Set<Integer> vertices = new HashSet<Integer>(); // visited vertices
        int u;
        int v;

        for (Edge e : m) {
            u = e.v1();
            v = e.v2();

            if (vertices.contains(u) || vertices.contains(v)) {
                return false;
            }
            else {
                vertices.add(u);
                vertices.add(v);
            }
        }

        return true; // didn't find any shared vertices
    }

    public static void main(String[] args) {
        System.out.println("yo");
    }
}
