/*******************************************************************************
* Hopcroft-Karp algorithm for finding maximum matchings in bipartite graphs.
* Time complexity: O(n^(5/2)), where n is the number of vertices in the graph.
*
* Author: Jesse Goodman
*
* Prove max matching by seeing if there are no aug paths left.
******************************************************************************/
import java.util.*;

public class HopcroftKarp {

    private HashSet<Edge> maxMatching;
    private boolean[] matchedVertices;
    private boolean[] freeBoys; // free vertices in first partition
    private boolean[] partitions;
    private Graph g;
    private int numGHatsMade;
    private HashSet<Edge> augAcc; // augmenting path accumulator
    private static final boolean DEBUG = false; // debug flag

    // representation of gHat graph. arraylist of buckets (levels) of vertices
    // each hashmap represents a level. each vertex has a label
    private ArrayList<HashMap<Integer, HashSet<Edge>>> gHat;

    // run hopcroft karp algorithm for maximum matchings in a bipartite graph
    public HopcroftKarp(Graph g) {
        this.g = g;
        this.numGHatsMade = 0;
        // is this vertex a girl?
        // thus, false == boys
        this.partitions = g.getBipartitions();
        if (this.partitions == null) {
            throw new IllegalArgumentException("Input must be the" +
            " adjacency matrix of a bipartite graph.");
        }
        this.maxMatching = new HashSet<Edge>();
        this.matchedVertices = new boolean[this.g.getNumVertices()];
        this.gHat = new ArrayList<HashMap<Integer, HashSet<Edge>>>();

        // put max matching into global variable
        int result = 1;
        // no more than sqrt(|V(G)|) iterations
        while (result > 0) {
            // get another augmenting graph, and then symdiff all new matchings
            // from that grpah into our current matching
            if (setNewGHat() == null) break;
            result = augmentMatching();
        }

        // check here to make sure we have a valid matching stored in
        // maxMatching after setting it in constructor
        if (!isValidMatching(this.maxMatching)) {
            throw new IllegalStateException("programmer error;" +
            " somehow created invalid matching.");
        }

        // check here to make sure we cannot find any more augmenting
        // paths; that is, make sure our matching is maximum
        for (int i = 0; i < partitions.length; i++) {
            if (!partitions[i]) continue; // vertex is a girl
            // found free boy
            if (!matchedVertices[i]) {
                if (existsAugPath(new boolean[partitions.length], i, 0)) {
                    throw new IllegalStateException("programmer error;" +
                    " found augmenting path; matching not maximum.");
                }
            }
        }
    }

    // does there exist an augmenting path starting from vertex v?
    private boolean existsAugPath(boolean[] visited, int v, int lenPath) {
        boolean boy = lenPath % 2 == 0;

        if (visited[v]) return false; // avoid cycles

        if (!boy && !matchedVertices[v] && lenPath > 0) {
            return true;
        }

        visited[v] = true;

        if (boy) {
            for (Edge e : g.getVertices().get(v)) {
                if (!maxMatching.contains(e) && existsAugPath(visited, e.v2(), lenPath + 1)) {
                    return true;
                }
            }
        }
        else {
            for (Edge e : g.getVertices().get(v)) {
                if (maxMatching.contains(e) && existsAugPath(visited, e.v2(), lenPath + 1)) {
                    return true;
                }
            }
        }

        return false;
    }



    // returns the edge set of the max cardinality matching of this graph
    public HashSet<Edge> getMaxMatching() {
        return this.maxMatching;
    }

    // returns the size of the max cardinality matching of this graph
    public int getMaxMatchingSize() {
        return this.maxMatching.size();
    }

    // checks if a set of edges is a valid matching (i.e., no repeated vertices)
    public static boolean isValidMatching(Collection<Edge> m) {
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

    // checks if a set of edges is a perfect matching (check validity and size)
    public static boolean isPerfectMatching(Collection<Edge> m, Graph g) {
        return (isValidMatching(m) && m.size() == ((double) g.getNumVertices())/2);
    }

    // build graph G^hat of G with least
    // G^hat represented as an array list of levels (pools of vertices)
    // similar to a BFS
    // levels are built by filtering neighbors of vertices in original graph
    // levels will only have edges going forward
    private ArrayList<HashMap<Integer, HashSet<Edge>>> setNewGHat() {


        if (DEBUG) {
            System.out.println("-----------------------CURR MATCHING--------------------------");
            System.out.println(maxMatching);
            System.out.println("-----------------------CURR MATCHING--------------------------");
        }

        ArrayList<HashMap<Integer, HashSet<Edge>>> levels =
        new ArrayList<HashMap<Integer, HashSet<Edge>>>();

        boolean[] visited = new boolean[this.g.getNumVertices()];
        boolean foundFreeGirl = false;

        // populate first level -- free boys
        HashMap<Integer, HashSet<Edge>> level_0 =
        new HashMap<Integer, HashSet<Edge>>();

        for (int i = 0; i < partitions.length; i++) {
            if (!partitions[i]) continue; // vertex is a girl
            if (matchedVertices[i]) continue; //vertex already matched

            // found free boy
            HashSet<Edge> freeBoy = new HashSet<Edge>();
            visited[i] = true;

            // decide which edges to include
            for (Edge e : this.g.getVertices().get(i)) {
                // only add edges that are not in matching
                // (looking for alternate path)
                if (!maxMatching.contains(e)) freeBoy.add(e);
            }
            level_0.put(i, freeBoy);
        }
        // if no free boys left
        if (level_0.size() == 0) return null;

        levels.add(level_0);
        // create levels
        for (int i = 1; !foundFreeGirl; i++) {
            HashMap<Integer, HashSet<Edge>> level = new HashMap<Integer, HashSet<Edge>>();
            // get all vertices to put into new level
            // aka, vertices adjacent to vertices from previous level
            for (HashSet<Edge> nbrs : levels.get(i-1).values()) {
                // iterate over neighbors of some vertex in the previous levels
                for (Edge e : nbrs) {
                    int vi = e.v2(); // label of current vertex being examined

                    // found free girl
                    if (!matchedVertices[vi] && (i % 2 == 1)) {
                        foundFreeGirl = true;
                    }

                    // once we've found a free girl, no need for fwd edges from this lvl
                    if (foundFreeGirl) break;

                    HashSet<Edge> newVertex = new HashSet<Edge>();
                    visited[vi] = true;

                    // filter neighbors of vertex in original graph
                    // which neighbors to add to new vertex?
                    for (Edge j : this.g.getVertices().get(vi)) {

                        if (DEBUG)
                            System.out.println(j.v1() + " == to == " + j.v2());

                        int vj = j.v2();
                        if (visited[vj] || foundFreeGirl) continue;

                        // do we want a matching edge for our alternating path?
                        // yes -- odd level
                        Edge mirror_j = new Edge(j.v2(), j.v1(), 1);
                        if (i % 2 == 1) {
                            if (maxMatching.contains(j) || maxMatching.contains(mirror_j)) {
                                newVertex.add(j);
                            }
                        }
                        // no
                        else {
                            if (!maxMatching.contains(j) && !maxMatching.contains(mirror_j)) {
                                newVertex.add(j);
                            }
                        }
                    }
                    level.put(vi, newVertex);
                }
            }

            if (level.size() == 0 && !foundFreeGirl) {
                return null;
            }

            // if found girl, remove all edges pointing to a taken girl in
            // this level; then, reset level
            if (foundFreeGirl) {
                for (Map.Entry<Integer, HashSet<Edge>> vertex : levels.get(i-1).entrySet()) {
                    int v = vertex.getKey();
                    HashSet<Edge> nbrs = vertex.getValue();
                    Iterator<Edge> iter = nbrs.iterator();
                    while (iter.hasNext()) {
                        Edge e = iter.next();
                        if (matchedVertices[e.v2()]) {
                            iter.remove();
                        }
                    }
                }
                levels.add(new HashMap<Integer, HashSet<Edge>>());
            }
            else {
                levels.add(level);
            }
        }
        this.gHat = levels;
        numGHatsMade++;

        if (DEBUG) {
            System.out.println("-----------------------LEVELS--------------------------");
            System.out.println(levels);
            System.out.println("-----------------------LEVELS--------------------------");
        }

        return levels;
    }

    // get a min augmenting path from g hat
    private HashSet<Edge> minAugPathFromGHat() {

        if (DEBUG) {
            System.out.println("in min aug path from ghat");
            System.out.println(this.gHat);
        }

        this.augAcc = new HashSet<Edge>(); // reset accumulator global

        HashMap<Integer, HashSet<Edge>> freeBoys = gHat.get(0);

        for (int freeBoy : freeBoys.keySet()) {
            // also updates augAcc
            if (hasPathToGirl(freeBoy, 0)) {
                removeAugPathFromGHat(augAcc);
                return augAcc;
            }
        }

        // g hat is exhausted of augmenting paths
        return null;
    }

    // is there a path from vertex v to a free girl? use DFS
    private boolean hasPathToGirl(int v, int lvl) {

        if (DEBUG)
            System.out.println("--hasPathToGirl--(" + v + "," + lvl + ")");

        if (lvl == gHat.size() - 1) {
            return true;
        }

        // each neighbor of given vertex
        for (Edge e : gHat.get(lvl).get(v)) {
            if (hasPathToGirl(e.v2(), lvl+1)) {
                this.augAcc.add(e);
                return true;
            }
        }

        return false;
    }

    // remove augPath from ghat (delete edges)
    private void removeAugPathFromGHat(HashSet<Edge> augPath) {
        boolean inMatching;
        HashSet<Integer> augPathVs = new HashSet<Integer>();

        // initialize vertices along augmented path
        for (Edge e : augPath) {
            augPathVs.add(e.v1());
            augPathVs.add(e.v2());
        }

        if (DEBUG) {
            System.out.println("===================================================");
            System.out.println(augPathVs);
            System.out.println("===================================================");
        }

        for (HashMap<Integer, HashSet<Edge>> level : gHat) {

            Iterator<Map.Entry<Integer, HashSet<Edge>>> iter = level.entrySet().iterator();
            while (iter.hasNext()) {
                inMatching = false;

                Map.Entry<Integer, HashSet<Edge>> vertex = iter.next();
                int v = vertex.getKey();

                // delete all vertices along the aug path
                HashSet<Edge> nbrs = vertex.getValue();
                if (augPathVs.contains(v)) {
                    iter.remove();
                    continue;
                }

                // delete all edges with one vertex along the aug path
                Iterator<Edge> iterE = nbrs.iterator();
                while (iterE.hasNext()) {
                    Edge e = iterE.next();
                    if (augPathVs.contains(e.v2())) {
                        iterE.remove();
                    }
                }
            }
        }
    }

    // M' = M (+) A1 (+) A2 (+) ... (+) An, where Ai is a min augmenting path
    // in a maximal set (with n elements) of min augmenting paths. Note that
    // (+) represents symmetric difference, M represents the old matching,
    // and M' represents the augmented matching.
    // One iteration of augmenting matching thru maximal set of min
    // augmenting paths
    // returns 0 if cannot augment matching
    private int augmentMatching() {
        int timesAugmented = 0;

        HashSet<Edge> augPath = minAugPathFromGHat();

        // continue until there are no aug paths left in maximal set of
        // minimum augmenting paths
        while (augPath != null) {
            timesAugmented++;
            this.maxMatching = Graph.symDiff(this.maxMatching, augPath);
            updateMatchedVertices();
            augPath = minAugPathFromGHat();
        }

        return timesAugmented;
    }

    private void updateMatchedVertices() {
        int v1;
        int v2;
        this.matchedVertices = new boolean[this.g.getNumVertices()];
        for (Edge e : this.maxMatching) {
            v1 = e.v1();
            v2 = e.v2();
            this.matchedVertices[v1] = true;
            this.matchedVertices[v2] = true;
        }
    }

    public int getNumGHatsMade() {
        return this.numGHatsMade;
    }

    // String representation of result
    public String toString() {
        StringBuilder edges = new StringBuilder();
        for (Edge e : this.getMaxMatching()) {
            edges.append(e).append("\n");
        }
        return "--------------------------------------------------\n" +
        "HOPCROFT-KARP RESULTS:\n" +
        "--------------------------------------------------\n" +
        "Max matching size:\n" + this.getMaxMatchingSize() + "\n\n" +
        "Illustration:\n" +
        edges +
        "--------------------------------------------------";
    }

    // unit testing
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

        long start = System.nanoTime();
        HopcroftKarp hk = new HopcroftKarp(g);
        long end = System.nanoTime();
        long time = (end - start)/1000000;

        System.out.println(hk);
        // System.out.println("ghats made: " + hk.getNumGHatsMade());
        // System.out.println("time: " + time + " ms");
    }

}
