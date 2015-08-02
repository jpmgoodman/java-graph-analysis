import java.util.List;

public class Vertex {

    private List<Edge> edges;
    private int degree;

    public Vertex(List<Edge> edges) {
        this.edges = edges;
        this.degree = edges.size();
    }

    public int degree() {
        return this.degree;
    }
}
