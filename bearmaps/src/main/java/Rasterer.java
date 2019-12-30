import java.util.ArrayList;


/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /**
     * The max image depth level.
     */
    public static final int MAX_DEPTH = 7;
    private ArrayList<Double> depthdict = new ArrayList<>();

    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     * possible, while still covering less than or equal to the amount of longitudinal distance
     * per pixel in the query box for the user viewport size.</li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the above
     * condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */
    //RasterResultParams//
    public RasterResultParams getMapRaster(RasterRequestParams params) {
        System.out.println("Since you haven't implemented getMapRaster, "
                + "nothing is displayed in the browser.");
        boolean querrysuccess = true;


        /* 
         * Hint: Define additional classes to make it easier to pass around multiple values, and
         * define additional methods to make it easier to test and reason about code. */
        if (params.lrlat > MapServer.ROOT_ULLAT 
            || params.ullat < MapServer.ROOT_LRLAT 
            || params.lrlon < MapServer.ROOT_ULLON 
            || params.ullon > MapServer.ROOT_LRLON) {


            return RasterResultParams.queryFailed();
        }
        double querryDPP = lonDPP(params.lrlon, params.ullon, params.w);
        int depth = depthChooser(querryDPP);

        String[][] rendergrid = stringarray(coords(params, depth), depth);
        double[] latlon = buildercoords(params, depth);
        double bullat = latlon[0];
        double bullon = latlon[1];
        double blrlat = latlon[2];
        double blrlon = latlon[3];

        RasterResultParams.Builder build = new RasterResultParams.Builder();
        build.setRenderGrid(rendergrid);
        build.setRasterUlLat(bullat);
        build.setRasterUlLon(bullon);
        build.setRasterLrLat(blrlat);
        build.setRasterLrLon(blrlon);
        build.setQuerySuccess(querrysuccess);
        build.setDepth(depth);
        RasterResultParams rasta = build.create();
        return rasta;
    }

    public String[][] stringarray(int[] coords, int depth) {
        int xstart = coords[2];
        int xend = coords[3];
        int ystart = coords[0];
        int yend = coords[1];


        String[][] rtrnArray = new String[yend - ystart + 1][xend - xstart + 1];

        for (int y = ystart, i = 0; y <= yend; y++, i++) {
            for (int x = xstart, k = 0; x <= xend; x++, k++) {
                rtrnArray[i][k] = "d" + Integer.toString(depth) + "_x" + x + "_y" + y
                        + ".png";
            }
        }
        return rtrnArray;
    }

    public int[] coords(RasterRequestParams params, int depth) {

        int cpydepth = depth;
        if (depth > 7) {
            depth = 7;
        }
        double boxes = Math.pow(2, depth);
        double ullon = MapServer.ROOT_ULLON;
        double lrlon = MapServer.ROOT_LRLON;
        double ullat = MapServer.ROOT_ULLAT;
        double lrlat = MapServer.ROOT_LRLAT;
        double pullon = params.ullon;
        double plrlon = params.lrlon;
        double pullot = params.ullat;
        double plrlat = params.lrlat;
        if (pullon < ullon) {
            pullon = ullon;
        }
        if (pullot > ullat) {
            pullot = ullat;
        }
        if (plrlat < lrlat) {
            plrlat = lrlat;
        }
        if (plrlon > lrlon) {
            plrlon = lrlon;
        }

        int x1 = (int) Math.floor(((ullat - pullot) / (ullat - lrlat)) * boxes);
        int x2 = (int) Math.floor(((ullat - plrlat) / (ullat - lrlat)) * boxes);
        int y1 = (int) Math.floor(((ullon - pullon) / (ullon - lrlon)) * boxes);
        int y2 = (int) Math.floor(((ullon - plrlon) / (ullon - lrlon)) * boxes);




        int[] coords = {x1, x2, y1, y2};
        return coords;
    }
    public double[] buildercoords(RasterRequestParams params, int depth) {
        double boxes = Math.pow(2, depth);
        double ullon = MapServer.ROOT_ULLON;
        double lrlon = MapServer.ROOT_LRLON;
        double ullat = MapServer.ROOT_ULLAT;
        double lrlat = MapServer.ROOT_LRLAT;
        double pullon = params.ullon;
        double plrlon = params.lrlon;
        double pullot = params.ullat;
        double plrlat = params.lrlat;
        if (pullon < ullon) {
            pullon = ullon;
        }
        if (pullot > ullat) {
            pullot = ullat;
        }
        if (plrlat < lrlat) {
            plrlat = lrlat;
        }
        if (plrlon > lrlon) {
            plrlon = lrlon;
        }
        int y1 = (int) Math.floor(((ullon - pullon) / (ullon - lrlon)) * boxes);
        int x1 = (int) Math.floor(((ullat - pullot) / (ullat - lrlat)) * boxes);
        int x2 = (int) Math.floor(((plrlat - lrlat) / (MapServer.ROOT_LAT_DELTA)) * boxes);
        int y2 = (int) Math.floor(((lrlon - plrlon) / (MapServer.ROOT_LON_DELTA)) * boxes);


        double r1 = x1 / boxes;
        double r2 = x2 / boxes;
        double r3 = y1 / boxes;
        double r4 = y2 / boxes;



        double bullat = ullat - (r1 * MapServer.ROOT_LAT_DELTA);
        double bullon = ullon + (r3 * MapServer.ROOT_LON_DELTA);
        double bllat = lrlat + (r2 * MapServer.ROOT_LAT_DELTA);
        double bllon = lrlon - (r4 * MapServer.ROOT_LON_DELTA);



        double[] bcoords = {bullat, bullon, bllat, bllon};

        return  bcoords;
    }

    /**
     * Calculates the lonDPP of an image or query box
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */




    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    private int depthChooser(double querrydpp) {
        int depthvalue = 0;
        double d0dpp = MapServer.ROOT_LONDPP;


        for (int i = 1; i <= MAX_DEPTH; i++) {
            depthdict.add(d0dpp / Math.pow(2, i - 1));
        }

        if (querrydpp < depthdict.get(depthdict.size() - 1)) {
            return 7;
        } else {
            for (int i = 0; i < depthdict.size(); i++) {
                depthvalue = i;
                if (depthdict.get(i) < querrydpp) {
                    return depthvalue;
                }
            }
        }
        return 7;
    }


}

/*
Start the formulas for the query box to get the images
then return a 2D array of the images using a for loop.
 */
