/*******************************************************************************
* Hopcroft-Karp algorithm for finding maximum matchings in bipartite graphs.
* Time complexity: O(n^(5/2)), where n is the number of vertices in the graph.
*
* Author: Jesse Goodman
******************************************************************************/
import java.util.*;

public class hopcroftKarp {

    private ArrayList<Edge> maxMatching;
    private boolean[] matchedVertices;
    private boolean[] freeBoys; // free vertices in first partition
    private boolean[] partitions;
    private Graph g;

    // representation of gHat graph. arraylist of buckets (levels) of vertices
    private ArrayList<HashMap<Integer, ArrayList<Edge>>> gHat;

    // run hopcroft karp algorithm for maximum matchings in a bipartite graph
    public hopcroftKarp(Graph g) {
        this.g = g;
        // is this vertex a girl?
        // thus, false == boys
        this.partitions = g.getBipartitions();
        this.maxMatching = new ArrayList<Edge>();
        this.matchedVertices = new boolean[this.g.getNumVertices()];
        this.gHat = new ArrayList<HashMap<Integer, ArrayList<Edge>>>();

        for ( Edge e : this.minAugPathFromGHat())
            System.out.println(e);

        // put max matching into global variable
        int result = 1;
        // no more than sqrt(|V(G)|) iterations
        while (result > 0) {
            System.out.println(result);
            if (extractAugGraph() == null) break;
            result = augmentMatching();
        }
    }


    // returns the edge set of the max cardinality matching of this graph
    public ArrayList<Edge> getMaxMatching() {
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
    private ArrayList<HashMap<Integer, ArrayList<Edge>>> extractAugGraph() {

        ArrayList<HashMap<Integer, ArrayList<Edge>>> levels = new ArrayList<HashMap<Integer, ArrayList<Edge>>>();
        boolean[] visited = new boolean[this.g.getNumVertices()];
        boolean foundFreeGirl = false;

        // populate first level -- free boys
        HashMap<ArrayList<Edge>> level_0 = new HashMap<ArrayList<Edge>>();
        for (int i = 0; i < partitions.length; i++) {
            if (partitions[i]) continue; // vertex is a girl
            if (matchedVertices[i]) continue; //vertex already matched

            // found free boy
            ArrayList<Edge> freeBoy = new ArrayList<Edge>();
            visited[i] = true;
            prevLevelVertices[i] = true;

            // decide which edges to include
            for (Edge e : this.g.getVertices().get(i)) {
                // only add edges that are not in matching
                // (looking for alternate path)
                if (!maxMatching.contains(e)) freeBoy.add(e);
            }
            level_0.put(i, freeBoy);
            prevLevelVertices[i] = true;
        }
        // if no free boys left
        if (level_0.size() == 0) return null;

        levels.add(level_0);

        // create levels
        for (int i = 1; !foundFreeGirl; i++) {

            HashMap<Integer, ArrayList<Edge>> level = new HashMap<Integer, ArrayList<Edge>>();
            // get all vertices to put into new level
            // aka, vertices adjacent to vertices from previous level
            for (ArrayList<Edge> v : levels.get(i - 1)) {
                // iterate over neighbors of some vertex in the previous levels
                for (Edge e : v) {
                    int vi = e.v2(); // label of current vertex being examined
                    // found free girl
                    if (!matchedVertices[vi] && (i % 2 == 1)) {
                        foundFreeGirl = true;
                    }

                    // once we've found a free girl, no need for any more forward edges
                    if (foundFreeGirl) continue;

                    ArrayList<Edge> newVertex = new ArrayList<Edge>();
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
                    level.add(vi, newVertex);
                }
            }
            if (level.size == 0) return null;

            levels.add(level);
        }
        return levels;
    }

    // get a min augmenting path from g hat
    private ArrayList<Edge> minAugPathFromGHat() {
        ArrayList<Edge> path = new ArrayList<Edge>();
        //
        // int numLvls = this.gHat.size();
        // int currLvl = numLvls - 1;
        // int currVertex = this.gHat.get(currLvl).get(0);
        // Edge currE;
        //
        // if (numLvls <= 1) {
        //     return null;
        // }
        //
        // // continue until we arrive at free boy
        // while (matchedVertices[currVertex] || partitions[currVertex]) {
        //     currE = this.gHat.get(currLvl).get(0).get(0);
        //     path.add(currE);
        //     currVertex = currE.v2();
        // }
        //
        // for (int i = numLvls - 1; i >= 0; i--) {
        //     sizeLvl = this.gHat.get(i).size();
        // }
        int numFreeBoys = this.gHat.get(0).size();

        // num of augmenting paths is at most the number of free boys
        for (int i = 0; i < numFreeBoys; i++) {
            if (this.hasPathToGirl(0,))
        }

        HashSet<Integer, ArrayList<Edge>> freeBoys = this.gHat.get(0);
        // iterate over free boys
        // freeboys will need to be updated after every iteration
        for (Entry<Integer, ArrayList<Edge>> freeBoy : freeBoys) {
            path = findAugPath(new HashSet<Edge>());

        }

        return path;
    }

    // is there a path from some vertex in some level to a free girl
    // (i.e., free vertex in 2nd partition) in g hat?
    private ArrayList<Edge> findAugPath(ArrayList<Edge> acc) {
        ArrayList<Edge> path = acc;
        int lvl = path.size() - 1;
        // iterate over vertices
        for (Entry<Integer,ArrayList<Edge>> : this.gHat.get(lvl)) {

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
        ArrayList<Edge> augPath = minAugPathFromGHat();

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
    private static ArrayList<Edge> symDiff(ArrayList<Edge> edges1,
    ArrayList<Edge> edges2) {

        ArrayList<Edge> symDiff = new ArrayList<Edge>();
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

        ArrayList<Edge> ls = new ArrayList<Edge>();
        ls.get(0);
    }

}