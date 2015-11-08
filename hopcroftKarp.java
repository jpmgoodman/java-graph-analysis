/*******************************************************************************
* Hopcroft-Karp algorithm for finding maximum matchings in bipartite graphs.
* Time complexity: O(n^(5/2)), where n is the number of vertices in the graph.
*
* Author: Jesse Goodman
******************************************************************************/
import java.util.*;

public class hopcroftKarp {

    private HashSet<Edge> maxMatching;
    private boolean[] matchedVertices;
    private boolean[] freeBoys; // free vertices in first partition
    private boolean[] partitions;
    private Graph g;
    private static HashSet<Edge> augAcc; // augmenting path accumulator

    // representation of gHat graph. arraylist of buckets (levels) of vertices
    // each hashmap represents a level. each vertex has a label
    private static ArrayList<HashMap<Integer, HashSet<Edge>>> gHat;

    // run hopcroft karp algorithm for maximum matchings in a bipartite graph
    public hopcroftKarp(Graph g) {
        this.g = g;
        // is this vertex a girl?
        // thus, false == boys
        this.partitions = g.getBipartitions();
        this.maxMatching = new HashSet<Edge>();
        this.matchedVertices = new boolean[this.g.getNumVertices()];
        this.gHat = new ArrayList<HashMap<Integer, HashSet<Edge>>>();

        for (Edge e : minAugPathFromGHat())
        System.out.println(e);

        // put max matching into global variable
        int result = 1;
        // no more than sqrt(|V(G)|) iterations
        while (result > 0) {
            System.out.println(result);
            // get another augmenting graph, and then symdiff all new matchings
            // from that grpah into our current matching
            if (setNewGHat() == null) break;
            result = augmentMatching();
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

    // build graph G^hat of G with least
    // G^hat represented as an array list of levels (pools of vertices)
    // similar to a BFS
    // levels are built by filtering neighbrs of vertices in original graph
    // levels will only have edges going forward
    private ArrayList<HashMap<Integer, HashSet<Edge>>> setNewGHat() {

        ArrayList<HashMap<Integer, HashSet<Edge>>> levels = new ArrayList<HashMap<Integer, HashSet<Edge>>>();
        boolean[] visited = new boolean[this.g.getNumVertices()];
        boolean foundFreeGirl = false;

        // populate first level -- free boys
        HashMap<Integer, HashSet<Edge>> level_0 = new HashMap<Integer, HashSet<Edge>>();
        for (int i = 0; i < partitions.length; i++) {
            if (partitions[i]) continue; // vertex is a girl
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

                    // once we've found a free girl, no need for any more forward edges
                    if (foundFreeGirl) continue;

                    HashSet<Edge> newVertex = new HashSet<Edge>();
                    visited[vi] = true;

                    // filter neighbors of vertex in original graph
                    // which neighbors to add to new vertex?
                    for (Edge j : this.g.getVertices().get(vi)) {
                        int vj = j.v2();
                        if (visited[vj] || foundFreeGirl) continue;

                        // do we want a matching edge for our alternating path?
                        // yes -- odd level
                        if (i % 2 == 1) {
                            if (maxMatching.contains(e)) {
                                newVertex.add(e);
                            }
                        }
                        // no
                        else {
                            if (!maxMatching.contains(e)) {
                                newVertex.add(e);
                            }
                        }
                    }
                    level.put(vi, newVertex);
                }
            }
            if (level.size() == 0) return null;

            levels.add(level);
        }
        return levels;
    }

    // get a min augmenting path from g hat
    private static HashSet<Edge> minAugPathFromGHat() {
        augAcc = new HashSet<Edge>(); // reset accumulator global

        HashMap<Integer, HashSet<Edge>> freeBoys = gHat.get(0);

        for (int freeBoy : freeBoys.keySet()) {
            if (hasPathToGirl(freeBoy, 0)) {
                removeAugPathFromGHat(augAcc);
                return augAcc;
            }
        }

        // g hat is exhausted of augmenting paths
        return null;
    }

    // is there a path from vertex v to a free girl? use DFS
    private static boolean hasPathToGirl(int v, int lvl) {
        if (lvl == gHat.size()) {
            return true;
        }

        // each neighbor of given vertex
        for (Edge e : gHat.get(lvl).get(v)) {
            if (hasPathToGirl(e.v2(), lvl+1)) {
                augAcc.add(e);
                return true;
            }
        }

        return false;
    }

    // remove augPath from ghat (delete edges)
    private static void removeAugPathFromGHat(HashSet<Edge> augPath) {

        for (HashMap<Integer, HashSet<Edge>> level : gHat) {
            for (HashSet<Edge> nbrs : level.values()) {
                for (Edge e : nbrs) {
                    if (augAcc.contains(e)) nbrs.remove(e);
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
            this.maxMatching = symDiff(this.maxMatching, augPath);
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
            this.matchedVertices[v2] = false;
        }
    }

    // get symmetric difference of two sets of edges
    public static HashSet<Edge> symDiff(HashSet<Edge> edges1,
    HashSet<Edge> edges2) {

        HashSet<Edge> symDiff = new HashSet<Edge>();
        for (Edge e : edges1) {
            if (edges2.contains(e)) continue;
            symDiff.add(e);
        }
        for (Edge e : edges2) {
            if (edges1.contains(e)) continue;
            symDiff.add(e);
        }
        return symDiff;
    }

    // unit testing
    public static void main(String[] args) {
        // int[][] adjMatrix = Graph.loadMatrixFromStdIn();
        // Graph g = new Graph(adjMatrix);
        // hopcroftKarp hk = new hopcroftKarp(g);
    }

}
