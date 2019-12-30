import org.xml.sax.SAXException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Graph for storing all of the intersection (Vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 */
public class GraphDB {
    KDTree kdtree;
    ArrayList<Double> xproj = new ArrayList<>();
    ArrayList<Double> yproj = new ArrayList<>();
    TreeMap<Long, KDPoint> pointmap = new TreeMap<>();
    TreeMap<Double, Long> xid = new TreeMap<>();
    TreeMap<Double, Long> yid = new TreeMap<>();


    HashMap<Vertex, ArrayList<Edge>> circuit = new HashMap();
    HashMap<Vertex, ArrayList<Vertex>> neighbors = new HashMap();
    HashMap<Long, Vertex> vertexids = new HashMap<>();
    HashMap<Double, Long> distances = new HashMap<>();


    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing.
     * Modify this constructor to initialize your data structures.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        File inputFile = new File(dbPath);
        //dbpath = dbPath;
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
        kdtree = new KDTree(vertices());
        kdtree.add();
    }

    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */

    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (Vertex v : circuit.keySet()) {
            if (circuit.get(v).isEmpty()) {
                vertexids.remove(v.id);
            }
        }
    }



    /**
     * Returns the longitude of Vertex <code>v</code>.
     * @param v The ID of a Vertex in the graph.
     * @return The longitude of that Vertex, or 0.0 if the Vertex is not in the graph.
     */
    double lon(Long v) {
        return vertexids.get(v).lon;
    }

    /**
     * Returns the latitude of Vertex <code>v</code>.
     * @param v The ID of a Vertex in the graph.
     * @return The latitude of that Vertex, or 0.0 if the Vertex is not in the graph.
     */
    double lat(Long v) {
        return vertexids.get(v).lat;
    }
    /**
     * Returns an iterable of all Vertex IDs in the graph.
     * @return An iterable of all Vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        return vertexids.keySet();
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     * @param v The ID for any Vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the Vertex is not in the graph.
     */

    //need an iterable, so a list of all the vertices adjacent. neighbors

    Iterable<Long> adjacent(long v) {
        ArrayList<Long> adj = new ArrayList<>();
        Vertex vert = vertexids.get(v);
        if (circuit.get(vert).size() != 0) {
            for (Edge e : circuit.get(vert)) {
                adj.add(e.getDest().id);
            }
        }
        return adj;
    }
//might need to come back to this. Check if weight is a string.
    public void addedge(Vertex v1, Vertex v2, double weight) {


        Edge e = new Edge(v1, v2, weight);
        circuit.get(v1).add(e);
//        for (Vertex v : circuit.keySet()) {
//            if (e.getSource().id == v.id) {
//                circuit.get(v).add(e);
//            }
//        }
    }

    public void addvertex(long id, double lat, double lon) {
        Vertex toadd = new Vertex(id, lat, lon);
        circuit.put(toadd, new ArrayList<>());
        vertexids.put(id, toadd);
    }

    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first Vertex.
     * @param w The ID for the second Vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    static double euclidean(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Returns the ID of the Vertex closest to the given longitude and latitude.
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the Vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    public long closest(double lon, double lat) {


        Double qpx = projectToX(lon, lat);
        Double qpy = projectToY(lon, lat);

        Double rootx = kdtree.root.coords.x;
        Double rooty = kdtree.root.coords.y;
        kdtree.champdist = euclidean(qpx, rootx, qpy, rooty);
        kdtree.champ = xid.get(rootx);

        kdtree.nearestneighbor(qpx, qpy, kdtree.root);
        return kdtree.champ;


    }

    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {
        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     * @param v The ID for the first Vertex.
     * @param w The ID for the second Vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /** Radius of the Earth in miles. */
    private static final int R = 3963;
    /** Latitude centered on Berkeley. */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /** Longitude centered on Berkeley. */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;

    public int getMedianIndex(ArrayList<Double> projList) {
        return ((projList.size() - 1) / 2);
    }

    public double getMedianValue(ArrayList<Double> projList) {
        return projList.get(getMedianIndex(projList));
    }

    public class KDTree extends BinaryTree {

        Long champ;
        Double champdist;
        TreeNode root;
        Iterable<Long> allverts;


        public KDTree(Iterable<Long> allverts) {
            this.allverts = allverts;
            for (Long v : allverts) {
                //double xprj = projectToX(lon(v), lat(v)); why no work
                //double yprj = projectToY(lon(v), lat(v));

                double xprj = projectToX(vertexids.get(v).lon, vertexids.get(v).lat);
                double yprj = projectToY(vertexids.get(v).lon, vertexids.get(v).lat);
                pointmap.put(v, new KDPoint(xprj, yprj, v));
                xid.put(xprj, v);
                yid.put(yprj, v);

                xproj.add(xprj);
                yproj.add(yprj);


            }
            Collections.sort(xproj);
            Collections.sort(yproj);
        }

        public void add() {
            int medindex = getMedianIndex(xproj);
            double axisval = getMedianValue(xproj);

            List<Double> xsublL = xproj.subList(0, medindex);
            List<Double> xsublR = xproj.subList(medindex + 1, xproj.size());

            Long id = xid.get(axisval);
            KDPoint p = pointmap.get(id);

            TreeNode firstnode = new TreeNode(p, null, null, "x");
            root = firstnode;
            root.left = addhelper(root.axis, xsublL);
            root.right = addhelper(root.axis, xsublR);
        }

        public TreeNode addhelper(String s, List<Double> subl) {


            if (subl.size() == 0) {
                return null;
            }

            ArrayList<Double> convsubl = convertList(subl, s);
            double median = getMedianValue(convsubl);
            int medianindex = getMedianIndex(convsubl);

            List<Double> sublL = convsubl.subList(0, medianindex);
            List<Double> sublR = convsubl.subList(medianindex + 1, convsubl.size());


            if (s.equals("x")) {
                KDPoint point = pointmap.get(yid.get(median));
                TreeNode left = addhelper("y", sublL);
                TreeNode right = addhelper("y", sublR);
                return new TreeNode(point, left, right, "y");

            } else {
                KDPoint point = pointmap.get(xid.get(median));
                TreeNode left = addhelper("x", sublL);
                TreeNode right = addhelper("x", sublR);
                return new TreeNode(point, left, right, "x");
            }
        }

        ArrayList<Double> convertList(List<Double> inList, String axis) {
            ArrayList<Double> outList = new ArrayList<>();
            if (axis.equals("x")) {
                for (Double value : inList) {
                    Long id = xid.get(value);
                    KDPoint point = pointmap.get(id);
                    outList.add(point.y);
                    Collections.sort(outList);
                }
            } else {
                for (Double value : inList) {
                    Long id = yid.get(value);
                    KDPoint point = pointmap.get(id);
                    outList.add(point.x);
                    Collections.sort(outList);
                }
            }
            return outList;
        }

        public void nearestneighbor(Double qpx, Double qpy, TreeNode node) {

            if (node == null) {
                return;
            }

            KDPoint currPoint = node.coords;
            double currDistance = euclidean(qpx, currPoint.x, qpy, currPoint.y);

            if (currDistance < champdist) {
                champ = xid.get(node.coords.x);
                champdist = currDistance;
            }

            if (node.axis.equals("x")) {

                Double straightdist = Math.abs(qpx - node.coords.x);


                if (qpx < node.coords.x) {
                    nearestneighbor(qpx, qpy, node.left);
                    if (straightdist > champdist) {
                        return;
                    } else {
                        nearestneighbor(qpx, qpy, node.right);
                    }

                } else {
                    nearestneighbor(qpx, qpy, node.right);
                    if (straightdist > champdist) {
                        return;
                    } else {
                        nearestneighbor(qpx, qpy, node.left);
                    }
                }

            } else {

                Double straightdist = Math.abs(qpy - node.coords.y);

                if (qpy < node.coords.y) {
                    nearestneighbor(qpx, qpy, node.left);
                    if (straightdist > champdist) {
                        return;
                    } else {
                        nearestneighbor(qpx, qpy, node.right);
                    }
                } else {
                    nearestneighbor(qpx, qpy, node.right);
                    if (straightdist > champdist) {
                        return;
                    } else {
                        nearestneighbor(qpx, qpy, node.left);
                    }
                }
            }
            return;
        }

    }



}









