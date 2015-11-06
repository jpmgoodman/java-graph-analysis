public class DFS {
    private boolean[] visited; // represents vertices that have been visited
    private int numVisited; // number of vertices that have been visited

    // constructs DFS object, which calls private dfs fn.
    public DFS(Graph graph, int vertex) {
        this.visited = new boolean[graph.getNumVertices()];
        this.numVisited = 0;
        dfs(graph, vertex);
    }

    // perform depth first search on given vertex
    private void dfs(Graph graph, int vertex) {
        this.visited[vertex] = true;
        this.numVisited++;

        for (Edge e : graph.getVertices().get(vertex)) {
            int nextVertex = e.v2();
            if (!this.visited[nextVertex])
                dfs(graph, nextVertex);
        }
    }

    // returns boolean array of verticies visited in this DFS
    public boolean[] getVisited() {
        return visited;
    }

    // how many vertices were visited in this DFS?
    public int getNumVisited() {
        return this.numVisited;
    }
}
