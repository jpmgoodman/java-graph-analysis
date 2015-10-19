public class Edge implements Comparable<Edge> {

    private boolean hasWeight;      // is this a weighted edge?
    private boolean hasDirection;   // does this edge have direction?
    private int weight;             // weight of edge
    private int v1;              // first vertex of edge
    private int v2;              // second vertex of edge
    private boolean v1Tov2;         // directed edge from v1 to v2

    // unweighted, undirected edge
    public Edge(int u, int v) {
        this.hasWeight = false;
        this.hasDirection = false;
        this.v1 = u;
        this.v2 = v;
    }

    // weighted, undirected edge
    public Edge(int u, int v, int weight) {
        this.hasWeight = true;
        this.hasDirection = false;
        this.weight = weight;
        this.v1 = u;
        this.v2 = v;
    }

    // unweighted, directed edge
    public Edge(int u, int v, boolean v1Tov2) {
        this.hasWeight = false;
        this.hasDirection = true;
        this.v1Tov2 = v1Tov2;
        this.v1 = u;
        this.v2 = v;
    }

    // weighted, directed edge
    public Edge(int u, int v, int weight, boolean v1Tov2) {
        this.hasWeight = true;
        this.hasDirection = true;
        this.weight = weight;
        this.v1Tov2 = v1Tov2;
        this.v1 = u;
        this.v2 = v;
    }

    // return first vertex of edge
    public int v1() {
        return this.v1;
    }

    // return second vertex of edge
    public int v2() {
        return this.v2;
    }

    // is this edge weighted?
    public boolean hasWeight() {
        return this.hasWeight;
    }

    // does this edge have direction?
    public boolean hasDirection() {
        return this.hasDirection;
    }

    // what is the weight of this edge?
    public int getWeight() {
        return this.hasWeight ? this.weight : null;
    }

    // Override
    public int compareTo(Edge e) {
        if (!(this.hasWeight() && e.hasWeight()))
            throw new IllegalArgumentException("Each edge must have a weight.");

        int w1 = this.getWeight();
        int w2 = e.getWeight();

        if (w1 == w2)
            return 0;
        else if (w1 > w2)
            return 1;
        else
            return -1;
    }

    // String representation of an edge
    public String toString() {
        if (this.hasWeight()) {
            if (this.hasDirection())
                return "(" + this.v1() + ")" + "-----" + this.getWeight() + "---->" + "(" + this.v2() + ")";
            else
                return "(" + this.v1() + ")" + "-----" + this.getWeight() + "-----" + "(" + this.v2() + ")";
        }
        else {
            if (this.hasDirection())
                return "(" + this.v1() + ")" + "--------->" + "(" + this.v2() + ")";
            else
                return "(" + this.v1() + ")" + "----------" + "(" + this.v2() + ")";
        }
    }

    // is the direction of this edge from v1 to v2?
    private boolean v1Tov2() {
        return this.hasDirection ? this.v1Tov2 : null;
    }
}
