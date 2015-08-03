public class DFS {
    private boolean[] visited;
    private int numVisited;

    public DFS(Graph graph, int vertex) {
        this.visited = new int[graph.getNumVertices()];
        this.numVisited = 0;
        dfs(graph, vertex);
    }

    // perform depth first search on given vertex
    private void dfs(Graph graph, int vertex) {
        this.visited[vertex] = true;
        this.numVisited++;

        for (Edge e : graph.get(vertex)) {
            int nextVertex = e.v2();
            if (!this.visited[nextVertex])
                dfs(nextVertex);
        }
    }

    // returns boolean array of verticies visited in this DFS
    public boolean[] visited() {
        return visited;
    }

    // how many vertices were visited in this DFS?
    public int numVisited() {
        return this.numVisited;
    }
}
