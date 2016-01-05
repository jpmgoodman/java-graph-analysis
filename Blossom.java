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

    // run Edmond's blossom algorithm for maximum matchings in a general graph
    public Blossom(Graph g) {
        this.graph = g;
        this.maxMatching = new HashSet<Edge>();
        this.numContractions = 0;
        updateMatchedVertices();

        // find and set maxMatching
        HashSet<Edge> augPath = new HashSet<Edge>();
        do {
            this.maxMatching = Graph.symDiff(this.maxMatching, augPath);
            updateMatchedVertices();
            augPath = getAugPath(this.graph, this.maxMatching);
            System.out.println("augPath:");
            System.out.println(augPath);
        } while (augPath.size() != 0);

        // check here to make sure we have a valid matching stored in
        // maxMatching after setting it in constructor
        if (!isValidMatching(this.maxMatching)) {
            throw new IllegalStateException("programmer error;" +
            " somehow created invalid matching:\n" + this.maxMatching);
        }
    }

    // most important method; finds an augmenting path in G, given a matching M
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
                System.out.println("CONTRACTING BLOSSOM");
                this.numContractions++;
                /* BEGIN BLOSSOM CONTRACTION */
                // shrink blossom and restart find aug path on shrunken graph
                // get stem
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

                System.out.println("stem: " + stem);

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

                System.out.println(blossomVs);

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

                    if (i == stem) {
                        vertices.add(stemList);
                    }
                    else {
                        vertices.add(vertex);
                    }
                }

                Graph _g = new Graph(Graph.adjListsToAdjMatrix(vertices));
                /* END BLOSSOM CONTRACTION */

                HashSet<Edge> sAugPath = getAugPath(_g, _m);
                System.out.println("GOT PATH FROM BELOW");
                System.out.println("sAugPath: " + sAugPath);
                // if (sAugPath.contains(new Edge(7,8,1)));
                // System.out.println("from graph");
                // System.out.println(_g);
                // System.out.println();


                /* LIFT AUG PATH */
                HashSet<Edge> liftedPath = new HashSet<Edge>();
                // edges in and out of stem.
                Edge in = null;
                Edge out = null;


                for (Edge ape : sAugPath) {
                    if (ape.v2() == stem) {
                        in = ape;
                    }
                    else if (ape.v1() == stem) {
                        out = ape;
                    }
                    else {
                        liftedPath.add(ape);
                        System.out.println("add: " + ape);
                    }
                }

                System.out.println("in: " + in);
                System.out.println("out: " + out);


                HashSet<Edge> origEdges = g.getEdges();
                // System.out.println(origEdges);

                if (in == null && out == null) {
                    return sAugPath;
                }


                if ( in != null && out != null &&
                (origEdges.contains(in) || origEdges.contains(in.rev())) &&
                (origEdges.contains(out) || origEdges.contains(out.rev())) )
                {
                    return sAugPath; // recursive aug path doesn't pass thru blossom
                }

                // either in or out is null at this point

                boolean wantMatched = false; // do we want the next edge in our aug path to be matched?

                // blossom is at one end of aug path.
                if (in == null) {
                    if (origEdges.contains(out) || origEdges.contains(out.rev())) {
                        wantMatched = !(m.contains(out) || m.contains(out.rev()));
                        liftedPath.add(out);
                        int currV = stem;
                        // continue until we reach a free vertex
                        while (matches[currV] != -1) {
                            LinkedList<Integer> nextVs = blossomMap.get(currV);
                            Edge nbr1 = new Edge(currV,nextVs.get(0),1);
                            Edge nbr2 = new Edge(currV, nextVs.get(1),1);

                            if (wantMatched) {
                                if (!m.contains(nbr1) && !m.contains(nbr1.rev())) {
                                    // continue to nbr2, assumedly along matched edge
                                    liftedPath.add(nbr2);
                                    currV = nbr2.v2();
                                }
                                else {
                                    // continue to nbr1, assumedly along matched edge
                                    liftedPath.add(nbr1);
                                    currV = nbr1.v2();
                                }

                            }
                            else {
                                if (m.contains(nbr1) || m.contains(nbr1.rev())) {
                                    // containue to nbr2, assumedly along unmatched edge
                                    liftedPath.add(nbr2);
                                    currV = nbr2.v2();
                                }
                                else {
                                    // continue to nbr1, assumedly along unmatched edge
                                    liftedPath.add(nbr1);
                                    currV = nbr1.v2();
                                }
                            }

                            wantMatched = !wantMatched; // alternate path
                        }

                    }
                    else {
                        // System.out.println("blossomMap");
                        // System.out.println(blossomMap);

                        int lastKnown = out.v2();

                        // System.out.println("last known");
                        // System.out.println(lastKnown);
                        // out: stem ---- v2 not in original graph
                        for (Edge sE : sAugPath) {
                            Edge target = null;
                            if (sE.v1() == lastKnown || sE.v2() == lastKnown) {
                                wantMatched = !(m.contains(sE) || m.contains(sE.rev()));
                            }
                        }

                        LinkedList<Integer> maybes = new LinkedList<Integer>();
                        // find vertex that stem was actually representing
                        for (int bv : blossomVs) {
                            Edge testE = new Edge(bv,lastKnown,1);
                            if (origEdges.contains(testE) || origEdges.contains(testE.rev())) {
                                if (wantMatched != (m.contains(testE) || m.contains(testE.rev()))) {
                                    maybes.add(bv);
                                }
                            }
                        }

                        // System.out.println("maybes:");
                        // System.out.println(maybes);

                        for (int mv : maybes) {
                            System.out.println("howdy");
                            System.out.println("MAYBES");
                            System.out.println(maybes);
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
                                    // System.out.println("about to return lifted path...");
                                    // System.out.println(liftedPath);
                                    return liftedPath;
                                }
                                else if (wantMatched == (m.contains(mvNbr1) || m.contains(mvNbr1.rev()))) {
                                    currV = mvNbr1.v2();
                                    augMaybe.add(mvNbr1);
                                    // System.out.println("adding this edge1: " + mvNbr1);
                                }
                                else if (wantMatched == (m.contains(mvNbr2) || m.contains(mvNbr2.rev()))) {
                                    currV = mvNbr2.v2();
                                    augMaybe.add(mvNbr2);
                                    // System.out.println("adding this edge2: " + mvNbr2);

                                }
                                else {
                                    System.out.println("NO DIRECTION HUH???");
                                    break; // could not find pleasing direction to travel
                                }
                                // starting to revisit vertices
                                if (currV == mv) {
                                    // System.out.println("REVISIT");
                                    break;
                                }
                                wantMatched = !wantMatched;
                            }
                            // throw new RuntimeException("This line of code should never be reached");
                        }
                        // System.out.println("here");
                        // System.out.println("curr lifted path");
                        // System.out.println(liftedPath);
                    }
                }
                // blossom is at other end of aug path
                else if (out == null) {
                    // System.out.println("AHHHHHHHHHHHH");
                    if (origEdges.contains(in) || origEdges.contains(in.rev())) {
                        wantMatched = !(m.contains(in) || m.contains(in.rev()));
                        liftedPath.add(in);
                        int currV = stem;
                        // continue until we reach a free vertex
                        while (matches[currV] != -1) {
                            LinkedList<Integer> nextVs = blossomMap.get(currV);
                            Edge nbr1 = new Edge(currV,nextVs.get(0),1);
                            Edge nbr2 = new Edge(currV, nextVs.get(1),1);

                            if (wantMatched) {
                                if (!m.contains(nbr1) && !m.contains(nbr1.rev())) {
                                    // continue to nbr2, assumedly along matched edge
                                    liftedPath.add(nbr2);
                                    currV = nbr2.v2();
                                }
                                else {
                                    // continue to nbr1, assumedly along matched edge
                                    liftedPath.add(nbr1);
                                    currV = nbr1.v2();
                                }

                            }
                            else {
                                if (m.contains(nbr1) || m.contains(nbr1.rev())) {
                                    // containue to nbr2, assumedly along unmatched edge
                                    liftedPath.add(nbr2);
                                    currV = nbr2.v2();
                                }
                                else {
                                    // continue to nbr1, assumedly along unmatched edge
                                    liftedPath.add(nbr1);
                                    currV = nbr1.v2();
                                }
                            }

                            wantMatched = !wantMatched; // alternate path
                        }
                    }
                    else {
                        int lastKnown = in.v1();
                        // out: stem ---- v2 not in original graph
                        for (Edge sE : sAugPath) {
                            Edge target = null;
                            if (sE.v1() == lastKnown || sE.v2() == lastKnown) {
                                wantMatched = !(m.contains(sE) || m.contains(sE.rev()));
                            }
                        }

                        LinkedList<Integer> maybes = new LinkedList<Integer>();
                        // find vertex that stem was actually representing
                        for (int bv : blossomVs) {
                            Edge testE = new Edge(bv,lastKnown,1);
                            if (origEdges.contains(testE) || origEdges.contains(testE.rev())) {
                                if (wantMatched == (m.contains(testE) || m.contains(testE.rev()))) {
                                    maybes.add(bv);
                                }
                            }
                        }

                        wantMatched = !wantMatched;

                        for (int mv : maybes) {
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
                                    return liftedPath;
                                }
                                else if (wantMatched == (m.contains(mvNbr1) || m.contains(mvNbr1.rev()))) {
                                    currV = mvNbr1.v2();
                                    augMaybe.add(mvNbr1);
                                }
                                else if (wantMatched == (m.contains(mvNbr2) || m.contains(mvNbr2.rev()))) {
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
                                wantMatched = !wantMatched;
                            }
                            throw new RuntimeException("This line of code should never be reached");
                        }

                    }
                }
                // blossom is in middle of aug path
                else {
                    System.out.println("MIDDDDDDDLEE????");
                    int left = in.v1();
                    System.out.println("left: " + left);
                    int right = out.v2();
                    System.out.println("right: " + right);
                    boolean inMatched = _m.contains(in) || _m.contains(in.rev());

                    // aug path in blossom starts at some starter, ands at some ender
                    HashSet<Integer> starters = new HashSet<Integer>();
                    HashSet<Integer> enders = new HashSet<Integer>();

                    wantMatched = !inMatched;

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

                    System.out.println("starters");
                    System.out.println(starters);

                    System.out.println("enders");
                    System.out.println(enders);

                    for (int starter : starters) {
                        HashSet<Edge> augMaybe = new HashSet<Edge>();
                        int currV = starter;

                        for (int startNbr : blossomMap.get(starter)) {
                            Edge startEdge = new Edge(starter, startNbr,1);
                            if (wantMatched != (m.contains(startEdge) || m.contains(startEdge.rev()))) {
                                continue;
                            }

                            wantMatched = !wantMatched;
                            currV = startNbr;
                            System.out.println("ADDING EDGE");
                            System.out.println(startEdge);
                            augMaybe.add(startEdge);

                            while (true) {

                                // found aug path thru blossom!
                                if (enders.contains(currV) && (wantMatched != inMatched)) {
                                    for (Edge eam : augMaybe) {
                                        liftedPath.add(eam);
                                        // return liftedPath;
                                    }
                                    liftedPath.add(new Edge(left, starter, 1));
                                    liftedPath.add(new Edge(currV, right, 1));
                                    return liftedPath;
                                }

                                if (currV == starter) {
                                    break; // cycled
                                }

                                LinkedList<Integer> currVNbrs = blossomMap.get(currV);
                                System.out.println("currVNbrs" + currVNbrs);
                                Edge vNbr1 = new Edge(currV, currVNbrs.get(0),1);
                                Edge vNbr2 = new Edge(currV, currVNbrs.get(1),1);

                                if (wantMatched == (m.contains(vNbr1) || m.contains(vNbr1.rev()))) {
                                    currV = vNbr1.v2();
                                    augMaybe.add(vNbr1);
                                    System.out.println("adding" + vNbr1);
                                }
                                else if (wantMatched == (m.contains(vNbr2) || m.contains(vNbr2.rev()))) {
                                    currV = vNbr2.v2();
                                    augMaybe.add(vNbr2);
                                    System.out.println("adding" + vNbr2);
                                }
                                else {
                                    // dead end
                                    break;
                                }
                                // dead end
                                wantMatched = !wantMatched;
                            }
                        }

                        wantMatched = !wantMatched;
                    }
                }
                // System.out.println("RETURNING!!");
                return liftedPath;
            }
        }
        // System.out.println("RETURNING THIS: " + augPath);
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

    public static void main(String[] args) {
        Graph g;

        if (args.length == 0) {
            g = new Graph(Graph.loadMatrixFromStdIn());
        }
        else if (args.length == 2) {
            int n = Integer.parseInt(args[0]);
            int m = Integer.parseInt(args[1]);
            g = RandomGraph.getPerfectNonbipartite(n, m);
            // System.out.println(g);
        }
        else {
            System.out.println("Please pipe in graph or enter number of " +
            "vertices and probability of including edges.");
            return;
        }

        // System.out.println("testing on the following graph: " + g);
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
