
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.List;
import java.util.Objects;
import java.util.Collections;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     * @param g <code>GraphDB</code> data source.
     * @param stlon The longitude of the starting coordinate.
     * @param stlat The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */
    


    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {





        HashMap<Long, Long> parents = new HashMap<>();
        HashSet<Long> visited = new HashSet<>();
        HashMap<Long, Double> sdistancePATH = new HashMap<>();
        HashMap<Long, Double> prioritymap = new HashMap<>();
        HashMap<Long, Double> heuristic = new HashMap<>();



        Long s = g.closest(stlon, stlat);


        Long t = g.closest(destlon, destlat);



        ArrayList<Long> rtnlist = new ArrayList<>();

        PriorityQueue<Long> pq = new PriorityQueue<>((a, b) ->
                (Double.compare(sdistancePATH.get(a) + heuristic.get(a),
                        sdistancePATH.get(b) + heuristic.get(b))));

        //add source to fringe. initialize "BEST"
        pq.add(s);
        heuristic.put(s, 0.0);
        sdistancePATH.put(s, 0.0);
        parents.put(s, null);

        while (!pq.isEmpty()) {
            Long popd = pq.poll();
            visited.add(popd);

            //visited.add(popd);
            if (popd == t) {
                break;
            }

            for (Long neigh : g.adjacent(popd)) {
                if (visited.contains(neigh)) {
                    continue;
                }

                double newdist = g.distance(popd, neigh);
                double olddist = sdistancePATH.get(popd);

                if (!sdistancePATH.containsKey(neigh)
                        || newdist + olddist < sdistancePATH.get(neigh)) {

                    sdistancePATH.put(neigh, newdist + olddist);
                    heuristic.put(neigh, g.distance(neigh, t));
                    parents.put(neigh, popd);
                    pq.add(neigh);
                }
            }
        }

        long last = t;
        while (parents.get(last) != null) {
            rtnlist.add(last);
            last = parents.get(last);
        }

        rtnlist.add(s);

        Collections.reverse(rtnlist);

//        rtnlist.add(0, s);
//        rtnlist.add(t);

        return rtnlist;
    }

    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     * @param g <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        // TODO
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /** Integer constants representing directions. */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /** Number of directions supported. */
        public static final int NUM_DIRECTIONS = 8;

        /** A mapping of integer values to directions.*/
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /** The direction represented.*/
        int direction;
        /** The name of this way. */
        String way;
        /** The distance along this way. */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
