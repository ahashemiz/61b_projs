public class Edge implements Comparable<Edge> {

    private Vertex src;
    private Vertex dest;
    private double weight;

    /* Creates an Edge (SRC, DEST) with edge weight WEIGHT. */
    Edge(Vertex src, Vertex dest, double weight) {
        this.src = src;
        this.dest = dest;
        this.weight = weight;
    }

    /* Returns the edge's source node. */
    public Vertex getSource() {
        return src;
    }

    /* Returns the edge's destination node. */
    public Vertex getDest() {
        return dest;
    }

    /* Returns the weight of the edge. */
    public double getWeight() {
        return weight;
    }

    public int compareTo(Edge other) {
        double cmp =  weight - other.weight;
        if (cmp > 0) {
            return 1;
        } else if (cmp == 0) {
            return 0;
        } else {
            return -1;
        }
    }



    /* Returns the string representation of an edge. */
    public String toString() {
        return "{" + src + ", " + dest + "} -> " + weight;
    }
}
