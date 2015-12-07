/*******************************************************************************
* Disjoint Set (aka Union Find) data structure, containing elements of type T.
* Supports operations to make a set, find the root of a set containing some
* element x, and combine/link two sets.
*
* Author: Jesse Goodman, implementation influenced by COS 528 slides by Tarjan
******************************************************************************/
import java.util.*;

public class DisjointSet<T> {

    // Node to represent each element in our disjoint set
    private class Node {
        private T data; // data stored in node
        private Node parent; // parent of node
        private int rank; // rank of node to assist with linking

        private Node() {
            this.rank = 0;
        }
    }

    // store map from some data element to node in some tree of disjoint set
    HashMap<T, Node> dataToRefs;

    // construct a disjoint set object
    public DisjointSet() {
        dataToRefs = new HashMap<T, Node>();
    }

    // create singleton tree set, with x the root
    // return created node
    // O(1) time
    public Node makeSet(T x) {
        if (dataToRefs.containsKey(x)) {
            throw new IllegalArgumentException("Cannot make a set using an " +
            "element already in disjoint set.");
        }

        Node n = new Node();
        n.data = x;
        n.parent = n;
        dataToRefs.put(x, n);

        return n;
    }

    // link sets that have roots x and y.
    // return root of new combined set.
    // O(1) time; uses rank to reduce node depths and
    // thus decrease amortized "find" time
    public Node linkSets(T x, T y) {
        if (!(isRoot(x) && isRoot(y))) {
            throw new IllegalArgumentException("Only roots already existing " +
            " in the disjoint set can be linked.");
        }

        Node nx = dataToRefs.get(x);
        Node ny = dataToRefs.get(y);

        if (nx.rank == ny.rank) ny.rank++;

        // attach smaller tree as child of larger tree
        if (nx.rank < ny.rank) {
            nx.parent = ny;
            return ny;
        }
        else {
            ny.parent = nx;
            return nx;
        }
    }

    // find and return the root of the tree containing the node associated
    // with data x. uses path compression, so amortized O(1) time
    public Node find(T x) {
        if (!dataToRefs.containsKey(x)) {
            throw new IllegalArgumentException("input does not exist in set.");
        }

        Node n = dataToRefs.get(x);
        // node is not root or child of root
        if (n.parent.parent != n.parent) {
            n.parent = find(n.parent.data);
        }
        return n.parent;
    }

    // is data "x" in our disjoint set as a root?
    private boolean isRoot(T x) {
        if (!dataToRefs.containsKey(x)) return false;

        Node n = dataToRefs.get(x);
        return n.parent == n;
    }

    // unit tests
    public static void main(String[] args) {
        DisjointSet<Integer> ds = new DisjointSet<>();
        ds.makeSet(3);
        System.out.println(ds.isRoot(2));
    }
}
