import java.util.*;

public class Test {
    public static void main(String[] args) {
        Graph g = new Graph(Graph.loadMatrixFromStdIn());
        HashSet<Edge> m = new HashSet<Edge>();
        m.add(new Edge(1,2,1));
        m.add(new Edge(5,6,1));
        m.add(new Edge(3,4,1));

        HashSet<Integer> blossomVs = new HashSet<Integer>();
        HashSet<Edge> blossomEs = new HashSet<Edge>();
        blossomVs.add(2);
        blossomVs.add(3);
        blossomVs.add(4);
        blossomVs.add(5);
        blossomVs.add(6);
        int stem = 2;


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
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        System.out.println("GRAPH: " + g);
        System.out.println("matching: " + m);
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        System.out.println("CONTRACTED: " + _g);
        System.out.println("matching: " + _m);
    }
}
