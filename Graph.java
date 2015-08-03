import java.io.IOException;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Graph {
    private int[][] adjMatrix;  // adj matrix; rep1 of graph
    private ArrayList<ArrayList<Edge>> buckets; // vertex array of edge lists; rep2 of graph

    public Graph(int[][] adjMatrix) {
        this.adjMatrix = adjMatrix;
        this.buckets = new ArrayList<ArrayList<Edge>>();

        Edge currEdge;
        ArrayList<Edge> allEdges;

        // iterate over all vertices
        for (int i = 0; i < adjMatrix.length; i++) {
            allEdges = new ArrayList<Edge>();
            // iterate over possible edges from current vertex
            for (int j = 0; j < adjMatrix[i].length; j++) {
                if (adjMatrix[i][j] == 0) {
                    continue;
                }
                else if (adjMatrix[i][j] == 1) {
                    currEdge = new Edge(i, j);
                    allEdges.add(currEdge);
                }
                else {
                    currEdge = new Edge(i, j, adjMatrix[i][j]);
                    allEdges.add(currEdge);
                }
            }
            buckets.add(allEdges);
        }
    }

    // returns number of vertices in graph.
    public int getNumVertices() {
        return adjMatrix.length;
    }

    // returns number of edges in graph.
    public int getNumEdges() {
        return this.getSumDegrees() / 2;
    }

    // returns total sum of degrees in graph.
    public int getSumDegrees() {
        int totalDegree = 0;

        for (List<Edge> edgeList : buckets)
            totalDegree += edgeList.size();

        return totalDegree;
    }

    // returns max degree of the graph.
    public int getMaxDegree() {
        int maxDegree = 0;

        for (List<Edge> edgeList : buckets)
            if (edgeList.size() > maxDegree)
                maxDegree = edgeList.size();

        return maxDegree;
    }
    // is the graph connected?
    public boolean isConnected() {
        int numVertices = this.getNumVertices();
        if (numVertices <= 1)
            return true;

        DFS dfs = new DFS(this, 0);
        return (dfs.numVisited() == numVertices) ? true : false;
    }

    public ArrayList<ArrayList<Edge>> getBuckets() {
        return this.buckets;
    }

    // loads and returns 2d adjacency matrix from standard in
    private static int[][] loadMatrixFromStdIn() {
        int[][] adjMatrix;
        String stdinLine;
        String[] stdinArray;
        int dim;
        Scanner stdin = new Scanner(System.in);

        // need this to get dimension of adj matrix that we're going to fill
        stdinLine = stdin.nextLine();
        stdinArray = stdinLine.split("\\s+");
        dim = stdinArray.length;

        if (dim <= 0)
            return null;

        adjMatrix = new int[dim][dim];

        // load adjMatrix
        for (int i = 0; i < dim; i++) {

            for (int j = 0; j < dim; j++)
                adjMatrix[i][j] = Integer.parseInt(stdinArray[j]);

            if (stdin.hasNextLine()){
                stdinLine = stdin.nextLine();
                stdinArray = stdinLine.split("\\s+");
            }
        }

        return adjMatrix;
    }

    public static void main(String[] args) throws java.io.IOException {
        int[][] adjMatrix = loadMatrixFromStdIn();
        // unit tests
        Graph g = new Graph(adjMatrix);
        System.out.println("This graph has " + g.getNumVertices() + " vertices.");
        System.out.println("This graph has total degree " + g.getSumDegrees());
        System.out.println("This graph has max degree " + g.getMaxDegree());
        System.out.println("This graph has " + g.getNumEdges() + " edges.");
        System.out.println("Is this graph connected? " + g.isConnected());
    }
}
