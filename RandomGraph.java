import java.util.*;

/*
 * RandomGraph.java
 *
 * Class to generate random bipartite and general (simple) graphs.
 * Author: Jesse Goodman
 */

public class RandomGraph {

    // get random general graph with n vertices
    // each pair of vertices has a probability p of being adjacent
    // assume probability is significant to one digit;
    public static Graph getBipartite(int n, float p) {
        if (p > 1 || p < 0) return null; // cannot have invalid probability
        int pAdj = (int) (p*10); // probability will be a number btw 0 and 10

        Random random = new Random();
        int randVal;

        int sp1 = 1 + random.nextInt(n-1); // size of partition 1, btw [1,n-1]

        ArrayList<HashSet<Edge>> graph = new ArrayList<HashSet<Edge>>();

        HashSet<Edge> v;
        HashSet<Edge> u;

        for (int remVertices = n; remVertices > 0; remVertices--) {
            graph.add(new HashSet<Edge>());
        }

        // iterate over all possible edges from p1 to p2
        for (int i = 0; i < sp1; i++) {
            v = graph.get(i);

            // partition 2 starts at index sp1
            for (int j = sp1; j < n; j++) {
                u = graph.get(j);

                // add this with probability p
                randVal = random.nextInt(10);
                if (randVal < pAdj) {
                    v.add(new Edge(i, j));
                    u.add(new Edge(j, i));
                }
            }
        }

        return new Graph(Graph.adjListsToAdjMatrix(graph));
    }


    // get random general graph with n vertices and m edges
    public static Graph getGeneral(int n, int m) {
        Random random = new Random();
        // graph must be simple; m > (n(n-1))/2 is more edges than in a
        // complete graph on n vertices
        if (m > (n*(n-1))/2) return null;

        ArrayList<HashSet<Edge>> graph = new ArrayList<HashSet<Edge>>();

        // add vertices
        for (int i = n; i > 0; i--) {
            graph.add(new HashSet<Edge>());
        }

        while (m > 0) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if ((u == v) || hasNbr(graph.get(u), v)) continue;

            graph.get(u).add(new Edge(u, v));
            graph.get(v).add(new Edge(v, u));
            m--;
        }

        return new Graph(Graph.adjListsToAdjMatrix(graph));
    }

    // does vertex v have vertex u as a neighbor?
    private static boolean hasNbr(HashSet<Edge> v, int u) {
        for (Edge e : v) {
            if (e.v2() == u) return true;
        }

        return false;
    }

    // unit tests
    public static void main(String[] args) {
        RandomGraph rg = new RandomGraph();
    }

}
