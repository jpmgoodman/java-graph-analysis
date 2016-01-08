/*******************************************************************************
* RandomGraph.java
* Class to generate random bipartite and general (simple) graphs.
*
* Author: Jesse Goodman
******************************************************************************/
import java.util.*;

public class RandomGraph {

    // get random general graph with n vertices
    // each pair of vertices has a probability p of being adjacent
    // assume probability is significant to one digit;
    public static Graph getBipartite(int n, double p) {
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

    // get bipartite graph with a perfect matching guaranteed
    // so, partition cardinality equal
    // o/w, works the same as getBipartite (n vertices, edges
    // included with probability p)
    public static Graph getPerfectBipartite(int n, double p) {
        if (p > 1 || p < 0) {
            throw new IllegalArgumentException("Not a valid probability");
        }
        if (n % 2 == 1) {
            throw new IllegalArgumentException("Cannot have a PM on odd num of vertices");
        }
        int pAdj = (int) (p*10); // probability will be a number btw 0 and 10

        Random random = new Random();
        int randVal;
        ArrayList<HashSet<Edge>> graph = new ArrayList<HashSet<Edge>>();
        HashSet<Edge> v;
        HashSet<Edge> u;

        for (int remVertices = n; remVertices > 0; remVertices--) {
            graph.add(new HashSet<Edge>());
        }

        // iterate over all possible edges from p1 to p2
        for (int i = 0; i < n/2; i++) {
            v = graph.get(i);

            // partition 2 starts at index sp1
            for (int j = n/2; j < n; j++) {
                u = graph.get(j);

                // override randval with -1 when looking at edge
                // btw mirrored vertices (i.e., same index vertex
                // in each partition) to guarantee edge and, thus, plant a PM
                randVal = (j - n/2 == i) ? -1 : random.nextInt(10);

                // add this edge with probability p
                // will always be true (-1 < all nonnegs)
                // when special edge in planted PM
                if (randVal < pAdj) {
                    v.add(new Edge(i, j));
                    u.add(new Edge(j, i));
                }
            }
        }

        // System.out.println("Finished creating graph.");
        return new Graph(Graph.adjListsToAdjMatrix(graph));
    }

    // add method for creating balanced bipartite graph

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

    // get random general graph with n vertices and m edges,
    // and a perfect matching planted
    public static Graph getPerfectGeneral(int n, int m) {
        if (n % 2 == 1) {
            throw new IllegalArgumentException("Cannot have a PM on odd num of vertices");
        }
        if (m < n/2) {
            throw new IllegalArgumentException("Cannot have a PM with less than n/2 edges");
        }

        ArrayList<HashSet<Edge>> graph = new ArrayList<HashSet<Edge>>();
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            graph.add(new HashSet<Edge>());
        }

        boolean[] matched = new boolean[n];
        int numMatched = 0;

        // continue to match 2 random vertices until perfect matching
        while (numMatched < n) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if ((u == v) || matched[u] || matched[v]) continue;

            matched[u] = true;
            matched[v] = true;
            graph.get(u).add(new Edge(u,v,1));
            graph.get(v).add(new Edge(v,u,1));
            numMatched += 2;
        }

        // obscure perfect matching by adding m - (n/2) edges
        for (int remEdges = m - (n/2); remEdges > 0; remEdges--) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if ((u == v) || graph.get(u).contains(v)) continue;

            graph.get(u).add(new Edge(u,v,1));
            graph.get(v).add(new Edge(v,u,1));
        }

        return new Graph(Graph.adjListsToAdjMatrix(graph));
    }

    // get perfect general that has a guaranteed odd cycle
    // that is, get a perfect general graph that is nonbipartite
    public static Graph getPerfectNonbipartite(int n, int m) {
        Graph g = getPerfectGeneral(n,m);
        while (g.getBipartitions() != null) {
            g = getPerfectGeneral(n,m);
        }

        return g;
    }

    // generate complete graph on n vertices
    public static Graph getComplete(int n) {
        if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }

        ArrayList<HashSet<Edge>> graph = new ArrayList<HashSet<Edge>>();

        for (int i = 0; i < n; i++) {
            HashSet<Edge> vertex = new HashSet<Edge>();
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                vertex.add(new Edge(i,j));
            }
            graph.add(vertex);
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
