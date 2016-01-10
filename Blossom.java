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
    private int numContractions;
    private int infloop;

    // run Edmond's blossom algorithm for maximum matchings in a general graph
    public Blossom(Graph g) {
        this.graph = g;
        this.maxMatching = new HashSet<Edge>();
        this.numContractions = 0;
        this.infloop = 0;
        updateMatchedVertices();

        // System.out.println("Blossom called on this graph: ");
        // System.out.println(g);

        /* FIND AND SET MAX MATCHING */
        HashSet<Edge> augPath = new HashSet<Edge>();
        do {
            this.maxMatching = Graph.symDiff(this.maxMatching, augPath);
            updateMatchedVertices();
            augPath = getAugPath(this.graph, this.maxMatching);
            // System.out.println("augPath:");
            // System.out.println(augPath);
        } while (augPath.size() != 0);

        /* VALIDATE MAX MATCHING */
        if (!isValidMatching(this.maxMatching)) {
            throw new IllegalStateException("programmer error;" +
            " somehow created invalid matching:\n" + this.maxMatching);
        }
    }

    /* Finds an augmenting path in graph g, given a matching m. */
    private HashSet<Edge> getAugPath(Graph g, HashSet<Edge> m) {
        // NOTE: labeling something means it goes in the alternating forest.
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

        // keep looping until we have no more unexamined edges from even vertices
        while (freshEvens.size() > 0) {
            // choose an unexamined e(v,w) with v:[r, even]
            int v = freshEvens.peek();
            HashSet<Edge> edges = g.getVertices().get(v);

            if (edges.size() == 0) {
                freshEvens.poll();
                continue;
            }

            // find unexamined incident edge to v
            Edge e = null;
            for (Edge edge : edges) {
                if (!examined.contains(edge) && !examined.contains(edge.rev())) {
                    e = edge;
                    break;
                }
            }
            if (e == null) { // not fresh! -- all nbrs examined already
                freshEvens.poll();
                continue;
            }

            // if we made it this far, we've found an unexamined
            // edge e(v,w) with v labeled [r, even]
            examined.add(e);
            int w = e.v2();

            // if w is unlabeled and matched to x,
            // label w[r, odd] and x[r, even]
            if (!labeled[w] && matches[w] != -1) {
                int x = matches[w];

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
                int vRoot = roots[v];
                int wRoot = roots[w];

                while (v != vRoot) {
                    int parent = parents[v];
                    augPath.add(new Edge(v,parent,1));
                    v = parent; // proceed up tree.
                }
                while (w != wRoot) {
                    int parent = parents[w];
                    augPath.add(new Edge(w,parent,1));
                    w = parent; // proceed up tree.
                }
                augPath.add(e); // original e(v,w)
                break;
            }

            // if w is labeled [r, even], we found a BLOSSOM
            if (roots[w] == roots[v] && evenLvl[w]) {
                /* BEGIN BLOSSOM CONTRACTION */
                this.numContractions++;

                // find stem
                int stem = roots[v]; // should always get overridden
                if (roots[w] != w && roots[v] != v) {
                    // find most recent common ancestor between v, w: stem
                    HashSet<Integer> vLineage = new HashSet<Integer>();
                    int vCopy = v;
                    int wCopy = w;

                    while (vCopy != roots[v]) {
                        vLineage.add(vCopy);
                        vCopy = parents[vCopy];
                    }
                    while (wCopy != roots[w]) {
                        if (vLineage.contains(wCopy)) {
                            stem = wCopy;
                            break; // replace stem by lca != root
                        }
                        wCopy = parents[wCopy];
                    }
                }
                // stem is now set

                HashSet<Integer> blossomVs = new HashSet<Integer>();
                HashSet<Edge> blossomEs = new HashSet<Edge>();
                HashMap<Integer,LinkedList<Integer>> blossomMap = new HashMap<Integer,LinkedList<Integer>>();
                int vCopy = v;
                int wCopy = w;
                int parent;

                while (vCopy != stem) {
                    blossomVs.add(vCopy);
                    parent = parents[vCopy];
                    blossomEs.add(new Edge(vCopy,parent,1));
                    vCopy = parent;
                }
                while (wCopy != stem) {
                    blossomVs.add(wCopy);
                    parent = parents[wCopy];
                    blossomEs.add(new Edge(wCopy,parent,1));
                    wCopy = parent;
                }
                blossomVs.add(stem);
                blossomEs.add(e); // v----w

                // make map that will store blossom vs mapped to their nbrs in the blossom
                for (Edge be : blossomEs) {
                    int v1 = be.v1();
                    int v2 = be.v2();
                    if (blossomMap.containsKey(v1)) {
                        LinkedList<Integer> nbrs = blossomMap.get(v1);
                        if (!nbrs.contains(v2)) {
                            nbrs.add(v2);
                        }
                    }
                    else {
                        LinkedList<Integer> nbrs = new LinkedList<Integer>();
                        nbrs.add(v2);
                        blossomMap.put(v1,nbrs);
                    }

                    if (blossomMap.containsKey(v2)) {
                        LinkedList<Integer> nbrs = blossomMap.get(v2);
                        if (!nbrs.contains(v1)) {
                            nbrs.add(v1);
                        }
                    }
                    else {
                        LinkedList<Integer> nbrs = new LinkedList<Integer>();
                        nbrs.add(v1);
                        blossomMap.put(v2,nbrs);
                    }
                }

                // make copy of G, M
                // go thru every vertex in blossom Vs. if vertex is
                // stem should have all edges connected to vertices in blossom, where the edges themselves are not in the blossom.
                // every other vertex is a component.
                LinkedList<HashSet<Edge>> vertices = new LinkedList<HashSet<Edge>>();
                HashSet<Edge> stemList = new HashSet<Edge>();
                HashSet<Edge> _m = new HashSet<Edge>();

                // load _m, _g (to be obtained from vertices)
                for (int i = 0; i < g.getNumVertices(); i++) {
                    HashSet<Edge> vertex = new HashSet<Edge>(); // copy of vertex

                    for (Edge edge : g.getVertices().get(i)) {
                        int nbr = edge.v2();

                        // this vertex is in the blossom
                        if (blossomVs.contains(i)) {
                            if (!blossomVs.contains(nbr)) {
                                stemList.add(new Edge(stem,nbr,1));
                            }
                            // delete edges within blossom
                        }
                        // only nbr is in blossom
                        else if (blossomVs.contains(nbr)) {
                            vertex.add(new Edge(i,stem,1));
                            if (m.contains(edge) || m.contains(edge.rev())) {
                                _m.add(new Edge(i,stem,1));
                            }
                        }
                        // neither vertex is in blossom
                        else {
                            vertex.add(new Edge(i,nbr,1));
                            if (m.contains(edge) || m.contains(edge.rev())) {
                                _m.add(edge);
                            }
                        }
                    }

                    if (i == stem) { vertices.add(stemList); }
                    else { vertices.add(vertex); }
                }

                Graph _g = new Graph(Graph.adjListsToAdjMatrix(vertices));

                // System.out.println("contracted graph, _g: ");
                // System.out.println(_g);
                // System.out.println("_m: " + _m);
                /* END BLOSSOM CONTRACTION */

                HashSet<Edge> sAugPath = getAugPath(_g, _m);
                // System.out.println("received this aug path: ");
                // System.out.println(sAugPath);

                /* LIFT AUG PATH */
                HashSet<Edge> liftedPath = new HashSet<Edge>();
                LinkedList<Edge> stemEdges = new LinkedList<Edge>();

                for (Edge ape : sAugPath) {
                    if (ape.v1() == stem || ape.v2() == stem) {
                        stemEdges.add(ape);
                    }
                    else {
                        liftedPath.add(ape);
                    }
                }

                HashSet<Edge> origEdges = g.getEdges();

                if (stemEdges.size() == 0) {
                    return sAugPath;
                }

                if (stemEdges.size() == 2) {
                    Edge stem1 = stemEdges.get(0);
                    Edge stem2 = stemEdges.get(1);

                    if (origEdges.contains(stem1) || origEdges.contains(stem1.rev())) {
                        if (origEdges.contains(stem2) || origEdges.contains(stem2.rev())) {
                            return sAugPath;
                        }
                    }
                }

                // do we want the next edge in our path to be matched?
                boolean matchNext = false;

                // blossom is at one end of aug path.
                if (stemEdges.size() == 1) {
                    Edge stemEdge = stemEdges.get(0);

                    if (origEdges.contains(stemEdge) || origEdges.contains(stemEdge.rev())) {

                        matchNext = !(m.contains(stemEdge) || m.contains(stemEdge.rev()));

                        liftedPath.add(stemEdge);
                        int currV = stem;
                        // continue until we reach a free vertex
                        // should never reach this, because this means that an aug path ends with a matched vertex
                        while (matches[currV] != -1) {
                            throw new IllegalStateException("Recursive call returned an invalid aug path");
                        }
                    }
                    else {
                        int lastKnown = stem == stemEdge.v2() ? stemEdge.v1() : stemEdge.v2();
                        // System.out.println("lastKnown: " + lastKnown);

                        // find if last edge leading to blossom was in matching
                        for (Edge sE : sAugPath) {

                            // if (sE.v1() == lastKnown || sE.v2() == lastKnown) {
                            //     System.out.println("WOOOO");
                            //     System.out.println(sE);
                            //     matchNext = !(m.contains(sE) || m.contains(sE.rev()));
                            //     System.out.println(matchNext);
                            // }
                            if (sE.v1() == lastKnown && blossomVs.contains(sE.v2()) ||
                            sE.v2() == lastKnown && blossomVs.contains(sE.v1())) {
                                matchNext = !(_m.contains(sE) || _m.contains(sE));
                            }

                        }
                        // System.out.println("WANT MATCHED? " + matchNext);

                        LinkedList<Integer> maybes = new LinkedList<Integer>();
                        // find vertex that stem was actually representing (store in maybes)
                        // System.out.println("blossomVs: " + blossomVs);
                        // System.out.println("origEdges: " + origEdges);
                        for (int bv : blossomVs) {
                            Edge testE = new Edge(bv,lastKnown,1);
                            if (origEdges.contains(testE) || origEdges.contains(testE.rev())) {
                                // System.out.println("CONTAINS IT: " + testE);
                                if (matchNext != (m.contains(testE) || m.contains(testE.rev()))) {
                                    maybes.add(bv);
                                }
                            }
                        }

                        // System.out.println("MAYBES LENGTH : " + maybes.size());
                        for (int mv : maybes) {
                            // System.out.println("mv: " + mv);
                            HashSet<Edge> augMaybe = new HashSet<Edge>();
                            int currV = mv;

                            while (true) {
                                LinkedList<Integer> mvNbrs = blossomMap.get(currV);
                                Edge mvNbr1 = new Edge(currV, mvNbrs.get(0), 1);
                                Edge mvNbr2 = new Edge(currV, mvNbrs.get(1), 1);
                                // success!
                                if (matches[currV] == -1) {
                                    for (Edge e_star : augMaybe) {
                                        liftedPath.add(e_star);
                                    }
                                    liftedPath.add(new Edge(lastKnown, mv, 1));
                                    // System.out.println("returning lifted 1: " + liftedPath);
                                    return liftedPath;
                                }
                                else if (matchNext == (m.contains(mvNbr1) || m.contains(mvNbr1.rev()))) {
                                    currV = mvNbr1.v2();
                                    augMaybe.add(mvNbr1);
                                    // System.out.println("adding this edge1: " + mvNbr1);
                                }
                                else if (matchNext == (m.contains(mvNbr2) || m.contains(mvNbr2.rev()))) {
                                    currV = mvNbr2.v2();
                                    augMaybe.add(mvNbr2);
                                }
                                else {
                                    break; // could not find pleasing direction to travel
                                }
                                // starting to revisit vertices
                                if (currV == mv) {
                                    break;
                                }
                                matchNext = !matchNext;
                            }
                            throw new RuntimeException("This line of code should never be reached");
                        }
                    }
                }
                // blossom is in middle of aug path
                else {
                    Edge stemEdge1 = stemEdges.get(0);
                    Edge stemEdge2 = stemEdges.get(1);
                    // System.out.println("blossom in middle of aug path");

                    int left = stem == stemEdge1.v1() ? stemEdge1.v2() : stemEdge1.v1();

                    // System.out.println("left: " + left);
                    int right = stem == stemEdge2.v1() ? stemEdge2.v2() : stemEdge2.v1();
                    // System.out.println("right: " + right);

                    boolean inMatched = _m.contains(stemEdge1) || _m.contains(stemEdge1.rev());
                    // System.out.println(stemEdge1);

                    // aug path in blossom starts at some starter, ands at some ender
                    HashSet<Integer> starters = new HashSet<Integer>();
                    HashSet<Integer> enders = new HashSet<Integer>();

                    // potential starter (ps) criteria:
                    // matching from left to ps
                    for (int ps : blossomVs) {
                        Edge pse = new Edge(left, ps, 1);
                        if (origEdges.contains(pse) || origEdges.contains(pse.rev())) {
                            if (inMatched == (m.contains(pse) || m.contains(pse.rev()))) {
                                starters.add(ps);
                            }
                        }
                    }
                    // potential ender (ps) criteria:
                    // non-matching from pe to right
                    for (int pe : blossomVs) {
                        Edge pee = new Edge(pe, right, 1); // potential ender edge
                        if (origEdges.contains(pee) || origEdges.contains(pee.rev())) {
                            if (inMatched != (m.contains(pee) || m.contains(pee.rev()))) {
                                enders.add(pe);
                            }
                        }
                    }

                    // System.out.println("starters");
                    // System.out.println(starters);
                    //
                    // System.out.println("enders");
                    // System.out.println(enders);

                    for (int starter : starters) {

                        int currV = starter;

                        for (int startNbr : blossomMap.get(starter)) {
                            matchNext = !inMatched;
                            // System.out.println("startNbr: " + startNbr);
                            // System.out.println("matchNext: " + matchNext);
                            HashSet<Edge> augMaybe = new HashSet<Edge>();
                            Edge startEdge = new Edge(starter, startNbr,1);
                            if (matchNext != (m.contains(startEdge) || m.contains(startEdge.rev()))) {
                                // System.out.println("uh oh");
                                continue;
                            }

                            matchNext = !matchNext;
                            currV = startNbr;
                            // System.out.println("ADDING EDGE");
                            // System.out.println(startEdge);
                            augMaybe.add(startEdge);

                            while (true) {

                                // found aug path thru blossom!
                                if (enders.contains(currV) && (matchNext != inMatched)) {
                                    for (Edge eam : augMaybe) {
                                        liftedPath.add(eam);
                                    }
                                    liftedPath.add(new Edge(left, starter, 1));
                                    liftedPath.add(new Edge(currV, right, 1));
                                    // System.out.println("returning lifted 2: " + liftedPath);
                                    return liftedPath;
                                }
                                if (currV == starter) {
                                    // System.out.println("CYCLE");
                                    break; // cycled
                                }

                                LinkedList<Integer> currVNbrs = blossomMap.get(currV);
                                // System.out.println("currVNbrs" + currVNbrs);
                                Edge vNbr1 = new Edge(currV, currVNbrs.get(0),1);
                                Edge vNbr2 = new Edge(currV, currVNbrs.get(1),1);

                                if (matchNext == (m.contains(vNbr1) || m.contains(vNbr1.rev()))) {
                                    currV = vNbr1.v2();
                                    augMaybe.add(vNbr1);
                                    // System.out.println("adding" + vNbr1);
                                }
                                else if (matchNext == (m.contains(vNbr2) || m.contains(vNbr2.rev()))) {
                                    currV = vNbr2.v2();
                                    augMaybe.add(vNbr2);
                                    // System.out.println("adding" + vNbr2);
                                }
                                else {
                                    // dead end
                                    break;
                                }
                                // dead end
                                matchNext = !matchNext;
                            }
                        }
                        matchNext = !matchNext;
                    }
                }
                // System.out.println("returning lifted 3: " + liftedPath);
                return liftedPath;
            }
        }
        // System.out.println("returning: " + augPath);
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

    // String representation of result
    public String toString() {
        StringBuilder edges = new StringBuilder();
        for (Edge e : this.getMaxMatching()) {
            edges.append(e).append("\n");
        }
        String rtnStr = "--------------------------------------------------\n" +
        "EDMONDS BLOSSOM RESULTS:\n" +
        "--------------------------------------------------\n" +
        "Num contractions:\n" + this.numContractions + "\n\n" +
        "Max matching size:\n" + this.getMaxMatchingSize() + "\n\n" +
        "Illustration:\n" +
        edges +
        "--------------------------------------------------";

        int numVs = this.graph.getNumVertices();
        if (numVs % 2 == 0 && this.getMaxMatchingSize() == numVs/2) {
            rtnStr += "\nPERFECT MATCHING FOUND\n" +
            "--------------------------------------------------";
        }

        return rtnStr;
    }


    // run time trials on Blossom alg
    public static void runTimeTrials() {
        // average degree of a vertex is n/2
        long start, end, runtime;

        for (int n = 0; n <= 500; n += 10) {
            int m = (n*n) / 4; // set avg degree of vertex to n/2
            runtime = 0;
            for (int i = 0; i < 100; i++) {
                System.out.println("n: " + n + ", i: " + i);

                Graph g;
                if (n < 4 || m < 4) {
                    g = RandomGraph.getPerfectGeneral(n, m);
                }
                else {
                    g = RandomGraph.getPerfectNonbipartite(n, m);
                }

                start = System.nanoTime();
                Blossom b = new Blossom(g);
                end = System.nanoTime();
                runtime += (end - start)/1000000;

            }
            runtime /= 100;
            System.out.println("avg runtime for n = " + n + ", m = " + m + " is: " + runtime + "ms");
        }
    }

    public static void main(String[] args) {
        Graph g;

        if (args.length == 0) {
            g = new Graph(Graph.loadMatrixFromStdIn());
        }
        else if (args.length == 1) {
            // TESTING MODE!
            int k = Integer.parseInt(args[0]); // how many graphs to test blossom on
            for (int i = 0; i < k; i++) {
                System.out.print("Test " + (i+1) + ": ");
                int n = 500;
                int m = 62500;
                g = RandomGraph.getPerfectNonbipartite(n, m);
                System.out.println("done creating graph");
                Blossom b = new Blossom(g);
                if (b.getMaxMatchingSize() != n/2) {
                    throw new IllegalStateException("Blossom alg failed - did not find perfect matching.");
                }
                System.out.println("passed!");
            }
            System.out.println("All tests passed!");
            return;
        }
        else if (args.length == 2) {
            int n = Integer.parseInt(args[0]);
            int m = Integer.parseInt(args[1]);
            g = RandomGraph.getPerfectNonbipartite(n, m);
        }
        else {
            System.out.println("Please pipe in graph or enter number of " +
            "vertices and probability of including edges.");
            return;
        }

        // runTimeTrials();

        // System.out.println("testing on the following graph: \n" + g);
        Blossom blossom = new Blossom(g);
        System.out.println(blossom);
        // HopcroftKarp hk = new HopcroftKarp(g);
        // HashSet<Edge> bMatching = blossom.getMaxMatching();
        // System.out.println("matching:");
        // System.out.println(bMatching);
        // System.out.println("matching size: " + bMatching.size());
        // HashSet<Edge> hkMatching = hk.getMaxMatching();

        // System.out.println(Graph.equivMatchings(hkMatching, bMatching));
        // int hkSize = hkMatching.size();
        // int bSize = bMatching.size();
        // System.out.println("HK and Blossom found same size matching?");
        // System.out.println(hkSize == bSize);

    }
}
