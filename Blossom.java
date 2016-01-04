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
    private int[] vertexMatches;

    // run Edmond's blossom algorithm for maximum matchings in a general graph
    public Blossom(Graph g) {
        this.graph = g;
        this.maxMatching = new HashSet<Edge>();
        updateMatchedVertices();

        // find and set maxMatching
        HashSet<Edge> augPath = new HashSet<Edge>();
        do {
            this.maxMatching = Graph.symDiff(this.maxMatching, augPath);
            updateMatchedVertices();
            augPath = getAugPath(this.graph, this.maxMatching);
            System.out.println(augPath);
            // System.out.println(augPath);
        } while (augPath.size() != 0);

        // check here to make sure we have a valid matching stored in
        // maxMatching after setting it in constructor
        if (!isValidMatching(this.maxMatching)) {
            throw new IllegalStateException("programmer error;" +
            " somehow created invalid matching.");
        }
    }

    // most important method; finds an augmenting path in G, given a matching M
    private HashSet<Edge> getAugPath(Graph g, HashSet<Edge> m) {
        // NOTE: labeling something means it goes in the alternating forest.
        System.out.println("****************************************");
        System.out.println("in get aug path");
        // roots[v] contains root of tree containing vertex v
        boolean[] labeled = new boolean[g.getNumVertices()];
        int[] roots = new int[g.getNumVertices()];
        boolean[] evenLvl = new boolean[g.getNumVertices()]; // level of tree
        int[] parents = new int[g.getNumVertices()];
        Arrays.fill(parents, -1); // easily track errors with -1 indexing
        // even labeled vertices that have some unexamined adjacent edge
        LinkedList<Integer> freshEvens = new LinkedList<Integer>();
        HashSet<Edge> examined = new HashSet<Edge>();
        HashSet<Edge> augPath = new HashSet<Edge>();

        int[] matches = new int[g.getNumVertices()];
        Arrays.fill(matches,-1);
        for (Edge e : m) {
            int v1 = e.v1();
            int v2 = e.v2();
            matches[v1] = v2;
            matches[v2] = v1;
            examined.add(e);
        }

        // start by labeling all free vertices
        for (int i = 0; i < matches.length; i++) {
            // free vertex
            if (matches[i] == -1) {
                // label it
                labeled[i] = true;
                roots[i] = i;
                evenLvl[i] = true;
                freshEvens.add(i);
            }
        }
        System.out.println(freshEvens.size());

        // keep looping until we have no more unexamined edges from even vertices
        while (freshEvens.size() > 0) {
            System.out.println("freshEvens");
            System.out.println(freshEvens);
            // choose an unexamined e(v,w) with v:[r, even]
            int v = freshEvens.peek();
            System.out.println("v: " + v);
            HashSet<Edge> edges = g.getVertices().get(v);

            if (edges.size() == 0) {
                freshEvens.poll();
                continue;
            }

            // find unexamined incident edge to v
            Edge e = null;
            for (Edge edge : edges) {
                if (!examined.contains(edge)) {
                    e = edge;
                    break;
                }
            }
            if (e == null) { // not fresh! -- all nbrs examined already
                System.out.println("NOT FRESH");
                freshEvens.poll();
                continue;
            }

            // if we made it this far, we've found an unexamined
            // edge e(v,w) with v labeled [r, even]
            examined.add(e);
            int w = e.v2();
            System.out.println("w: " + w);

            // if w is unlabeled and matched to x,
            // label w[r, odd] and x[r, even]
            if (!labeled[w] && matches[w] != -1) {
                int x = matches[w];
                System.out.println("x: " + x);

                labeled[w] = true;
                roots[w] = roots[v];
                evenLvl[w] = false;
                parents[w] = v;

                labeled[x] = true;
                roots[x] = roots[v];
                evenLvl[x] = true;
                parents[x] = w;
                freshEvens.add(x);
                continue;
            }

            // if w is labeled [s, even] with r != s, we found AUG PATH
            // aug path: roots[v] --> v --> w --> roots[w]
            if (roots[w] != roots[v] && evenLvl[w]) {
                System.out.println("FOUND AUG PATH");
                int vRoot = roots[v];
                int wRoot = roots[w];

                while (v != vRoot) {
                    // System.out.println("v: " + v);
                    // System.out.println("vRoot: " + vRoot);

                    // if (evenLvl[v]) {
                    //     // parent edge must be adjacent matching edge
                    //     lineage = new Edge(v, matches[v],1);
                    // }
                    // else {
                    //     // parent edge must be adjacent edge that is
                    //     // examined and not a matching edge
                    //     for (Edge adj : g.getVertices().get(v)) {
                    //         if ((examined.contains(adj) || examined.contains(adj.rev())) && !(m.contains(adj) || m.contains(adj.rev()))) {
                    //             // corner case where two roots have path to a single node
                    //             // make sure that we are not adding edge to free vertex that isn't vRoot
                    //             if (!(matches[adj.v2()] == -1 && adj.v2() != vRoot)) {
                    //                 lineage = adj;
                    //             }
                    //         }
                    //     }
                    // }

                    int parent = parents[v];
                    augPath.add(new Edge(v,parent,1));
                    v = parent; // proceed up tree.
                }
                while (w != wRoot) {
                    // // Edge lineage = null; // edge to parent in tree
                    //
                    // if (evenLvl[w]) {
                    //     // parent edge must be adjacent matching edge
                    //     lineage = new Edge(w, matches[w],1);
                    // }
                    // else {
                    //     // parent edge must be adjacent edge that is
                    //     // examined and not a matching edge
                    //     for (Edge adj : g.getVertices().get(w)) {
                    //         if ((examined.contains(adj) || examined.contains(adj.rev())) && !(m.contains(adj) || m.contains(adj.rev()))) {
                    //             // corner case where two roots have path to a single node
                    //             // make sure that we are not adding edge to free vertex that isn't wRoot
                    //             if (!(matches[adj.v2()] == -1 && adj.v2() != wRoot)) {
                    //                 lineage = adj;
                    //             }
                    //         }
                    //     }
                    // }

                    int parent = parents[w];
                    augPath.add(new Edge(w,parent,1));
                    w = parent; // proceed up tree.
                }
                augPath.add(e); // original e(v,w)
                System.out.println("WILL HIT BREAK");
                break;
            }

            // if w is labeled [r, even], we found a BLOSSOM
            if (roots[w] == roots[v] && evenLvl[w]) {
                // shrink blossom and restart find aug path on shrunken graph
            }
            // freshEvens.poll();
        }


        return augPath;
    }

    // updates matched vertices, keeps track of mate
    private void updateMatchedVertices() {
        int v1;
        int v2;
        this.vertexMatches = new int[this.graph.getNumVertices()];
        Arrays.fill(this.vertexMatches, -1);

        for (Edge e : this.maxMatching) {
            v1 = e.v1();
            v2 = e.v2();
            this.vertexMatches[v1] = v2;
            this.vertexMatches[v2] = v1;
        }
    }

    // returns the edge set of the max cardinality matching of this graph
    public HashSet<Edge> getMaxMatching() {
        return this.maxMatching;
    }

    // returns the size of the max cardinality matching of this graph
    public int getMaxMatchingSize() {
        return this.maxMatching.size();
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
        Graph g;

        if (args.length == 0) {
            g = new Graph(Graph.loadMatrixFromStdIn());
        }
        else if (args.length == 2) {
            int n = Integer.parseInt(args[0]);
            double p = Double.parseDouble(args[1]);
            g = RandomGraph.getPerfectBipartite(n, p);
        }
        else {
            System.out.println("Please pipe in graph or enter number of " +
            "vertices and probability of including edges.");
            return;
        }

        Blossom blossom = new Blossom(g);
        HopcroftKarp hk = new HopcroftKarp(g);
        HashSet<Edge> bMatching = blossom.getMaxMatching();
        HashSet<Edge> hkMatching = hk.getMaxMatching();

        // System.out.println(Graph.equivMatchings(hkMatching, bMatching));
        int hkSize = hkMatching.size();
        int bSize = bMatching.size();
        System.out.println("HK and Blossom found same size matching?");
        System.out.println(hkSize == bSize);

        // long start = System.nanoTime();
        // HopcroftKarp hk = new HopcroftKarp(g);
        // long end = System.nanoTime();
        // long time = (end - start)/1000000;
        //
        // System.out.println(hk);
        // System.out.println("ghats made: " + hk.getNumGHatsMade());
        // System.out.println("time: " + time + " ms");
    }
}
