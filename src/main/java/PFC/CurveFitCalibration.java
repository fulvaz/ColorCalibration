package PFC;



import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import java.io.*;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

// Color calibrate using poly fit

public class CurveFitCalibration implements PlugInFilter {

    ImagePlus imp;


    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        int LUTs[][];
        double distance = 7.3;
        int chartRow = 4;
        int charColumn = 6;

        // Image dimensions, data and roi
        int w = imp.getWidth();
        int h = imp.getHeight();
        int[] imagePixels = (int[]) ip.getPixels();

        Roi roi = imp.getRoi();
        Polygon poly = null;
        poly = roi.getPolygon();  //TODO Needs If statement

        double[][] allRGBValueCalculated = measureAllChartColor(imagePixels, poly.xpoints[1], poly.ypoints[0],
                poly.xpoints[0], poly.ypoints[1], w, h);
        WeightedObservedPoints[] rgbFitPoints = initPoints(allRGBValueCalculated); //0: R 1: G 2: B
        double[] RCoefficients = curveFit(rgbFitPoints[0]);
        double[] GCoefficients = curveFit(rgbFitPoints[1]);
        double[] BCoefficients = curveFit(rgbFitPoints[2]);
        IJ.log("hi~");

        // Do Image transformation
//        int[] tempRGB = new int[3];
//        int index = 0;
//        for (int y = 0; y < h; y++) {
//            IJ.showProgress(y, h);
//            for (int x = 0; x < w; x++) {
//                Util.DecodeRGB(imagePixels[index], tempRGB);
//                imagePixels[index] = LUTs[0][tempRGB[0]] + LUTs[1][tempRGB[1]] + LUTs[2][tempRGB[2]];
//                index++;
//            }
//        }

        imp.repaintWindow();

        // show calibration result
        System.out.println("here is the calibration result");
        allRGBValueCalculated = measureAllChartColor(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0],
                poly.ypoints[1], w, h);
        for (int i = 0; i < allRGBValueCalculated.length; i++) {
            System.out.println("no " + (i + 1));
            for (int j = 0; j < allRGBValueCalculated[i].length; j++) {
                System.out.print(allRGBValueCalculated[i][j] + " ");
            }
            System.out.println(" ");
        }
    }
    
    public WeightedObservedPoints[] initPoints(double[][] allRGBValueCalculated) {
        final WeightedObservedPoints R = new WeightedObservedPoints();
        final WeightedObservedPoints G = new WeightedObservedPoints();
        final WeightedObservedPoints B = new WeightedObservedPoints();
        //prepare points
        R.add(allRGBValueCalculated[0][0], 115);
        R.add(allRGBValueCalculated[0][1], 204);
        R.add(allRGBValueCalculated[0][2], 101);
        R.add(allRGBValueCalculated[0][3], 89);
        R.add(allRGBValueCalculated[0][4], 141);
        R.add(allRGBValueCalculated[0][5], 132);
        R.add(allRGBValueCalculated[0][6], 249);
        R.add(allRGBValueCalculated[0][7], 80);
        R.add(allRGBValueCalculated[0][8], 222);
        R.add(allRGBValueCalculated[0][9], 91);
        R.add(allRGBValueCalculated[0][10], 173);
        R.add(allRGBValueCalculated[0][11], 255);
        R.add(allRGBValueCalculated[0][12], 44);
        R.add(allRGBValueCalculated[0][13], 74);
        R.add(allRGBValueCalculated[0][14], 179);
        R.add(allRGBValueCalculated[0][15], 250);
        R.add(allRGBValueCalculated[0][16], 191);
        R.add(allRGBValueCalculated[0][17], 6);
        R.add(allRGBValueCalculated[0][18], 252);
        R.add(allRGBValueCalculated[0][19], 230);
        R.add(allRGBValueCalculated[0][20], 200);
        R.add(allRGBValueCalculated[0][21], 143);
        R.add(allRGBValueCalculated[0][22], 100);
        R.add(allRGBValueCalculated[0][23], 50);
        
        G.add(allRGBValueCalculated[1][0], 82);
        G.add(allRGBValueCalculated[1][1], 161);
        G.add(allRGBValueCalculated[1][2], 134);
        G.add(allRGBValueCalculated[1][3], 109);
        G.add(allRGBValueCalculated[1][4], 137);
        G.add(allRGBValueCalculated[1][5], 228);
        G.add(allRGBValueCalculated[1][6], 118);
        G.add(allRGBValueCalculated[1][7], 91);
        G.add(allRGBValueCalculated[1][8], 91);
        G.add(allRGBValueCalculated[1][9], 63);
        G.add(allRGBValueCalculated[1][10], 232);
        G.add(allRGBValueCalculated[1][11], 164);
        G.add(allRGBValueCalculated[1][12], 56);
        G.add(allRGBValueCalculated[1][13], 148);
        G.add(allRGBValueCalculated[1][14], 42);
        G.add(allRGBValueCalculated[1][15], 226);
        G.add(allRGBValueCalculated[1][16], 81);
        G.add(allRGBValueCalculated[1][17], 142);
        G.add(allRGBValueCalculated[1][18], 252);
        G.add(allRGBValueCalculated[1][19], 230);
        G.add(allRGBValueCalculated[1][20], 200);
        G.add(allRGBValueCalculated[1][21], 143);
        G.add(allRGBValueCalculated[1][22], 100);
        G.add(allRGBValueCalculated[1][23], 50);
        
        B.add(allRGBValueCalculated[2][0], 69);
        B.add(allRGBValueCalculated[2][1], 141);
        B.add(allRGBValueCalculated[2][2], 179);
        B.add(allRGBValueCalculated[2][3], 61);
        B.add(allRGBValueCalculated[2][4], 194);
        B.add(allRGBValueCalculated[2][5], 208);
        B.add(allRGBValueCalculated[2][6], 35);
        B.add(allRGBValueCalculated[2][7], 182);
        B.add(allRGBValueCalculated[2][8], 125);
        B.add(allRGBValueCalculated[2][9], 123);
        B.add(allRGBValueCalculated[2][10], 91);
        B.add(allRGBValueCalculated[2][11], 26);
        B.add(allRGBValueCalculated[2][12], 142);
        B.add(allRGBValueCalculated[2][13], 81);
        B.add(allRGBValueCalculated[2][14], 50);
        B.add(allRGBValueCalculated[2][15], 21);
        B.add(allRGBValueCalculated[2][16], 160);
        B.add(allRGBValueCalculated[2][17], 172);
        B.add(allRGBValueCalculated[2][18], 252);
        B.add(allRGBValueCalculated[2][19], 230);
        B.add(allRGBValueCalculated[2][20], 200);
        B.add(allRGBValueCalculated[2][21], 143);
        B.add(allRGBValueCalculated[2][22], 100);
        B.add(allRGBValueCalculated[2][23], 50);
        
        WeightedObservedPoints[] rgbFitPoints = {R, G, B};
        return rgbFitPoints;
    }

    public static double[] curveFit(WeightedObservedPoints obs) {
        // Collect data.
        // final WeightedObservedPoints obs = new WeightedObservedPoints();
        // obs.add(-1.00, 2.021170021833143);
        // obs.add(-0.99, 2.221135431136975);
        // obs.add(-0.98, 2.09985277659314);
        // obs.add(-0.97, 2.0211192647627025);
        // // ... Lots of lines omitted ...
        // obs.add(0.99, -2.4345814727089854);

        // Instantiate a third-degree polynomial fitter.
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(5);

        // Retrieve fitted parameters (coefficients of the polynomial function).
        final double[] coeff = fitter.fit(obs.toList());
        return coeff;
    }

    private static double[][] MeasureGreyChartsValue(int[] imageData, int x1, int y1, int x2, int y2, int nrPts,
            int imageWidth, int imageHeight) {
        // Measure the pixels and extract the RGB values.
        double[][] chartMeasurements = new double[nrPts][];
        double deltaX = (x2 - x1) / (nrPts - 1);
        double deltaY = (y2 - y1) / (nrPts - 1);
        double[] rgb;
        for (int i = 0; i < nrPts; i++) {
            int x = (int) Math.round(x1 + deltaX * i);
            int y = (int) Math.round(y1 + deltaY * i);
            rgb = Util.GetAvgRGB(imageData, x, y, 17, imageWidth, imageHeight);
            Util.InverseGammaCorrection(rgb, rgb);
            IJ.write("Chart White Balance - Measuring patch " + i + " at " + x + "," + y + ": " + rgb[0] + "," + rgb[1]
                    + "," + rgb[2]);
            chartMeasurements[i] = rgb;
        }
        return chartMeasurements;
    }

    // accept diagnol coordinates, from top-left to bottom-right
    // return array with all RGBs of the chart
    // ps: MeasureGreyChartsValue() returns values after inverse gamma
    // correction, this mothod returns RGBs
    public static double[][] measureAllChartColor(int[] imageData, int x0, int y0, int x1, int y1, int imageWidth,
            int imageHeight) {
        int chartColNum = 6;
        int chartRowNum = 4;
        double[][] chartMeasurements = new double[chartColNum * chartRowNum][]; // stores
                                                                                // rgb[3]
        double deltaX = (x1 - x0) / (chartColNum - 1);
        double deltaY = (y1 - y0) / (chartRowNum - 1);
        double[] rgb;
        for (int i = 0; i < chartRowNum; i++) {
            int y = (int) Math.round(y0 + deltaY * i);
            for (int j = 0; j < chartColNum; j++) {
                int x = (int) Math.round(x0 + deltaX * j);
                rgb = Util.GetAvgRGB(imageData, x, y, 17, imageWidth, imageHeight);
                IJ.write("Chart White Balance - Measuring patch " + i + " at " + x + "," + y + ": " + rgb[0] + ","
                        + rgb[1] + "," + rgb[2]);
                chartMeasurements[i * 6 + j] = rgb;
            }
        }
        return chartMeasurements;
    }

    private static int[][] ComputeLUT(double[][] chartMeasurements, double[][] chartValues) {
        // x's MUST be in ascending order!
        int[][] LUTs = new int[3][];

        for (int lutNr = 0; lutNr < 3; lutNr++) {
            IJ.write("Chart White Balance - Computing LUT " + lutNr);
            int[] LUT = new int[256];

            // Check for saturation in the measured patches
            int nrPts = CheckForSaturation(chartMeasurements, chartValues, lutNr);

            // IJ.write("Chart White Balance - Computing LUT with " + nrPts + "
            // points");
            // PrintArray("LUT control points X", x);
            // PrintArray("LUT control points Y", y);

            // Index is the index of x in the data array juast bigger the x we
            // want to compute
            // First slope is extrapolated!
            int index = 0;
            double slope = (chartValues[1][lutNr] - chartValues[0][lutNr])
                    / (chartMeasurements[1][lutNr] - chartMeasurements[0][lutNr]);

            for (int i = 0; i < 256; i++) {
                double temp = Util.InverseGammaCorrection(i / 255.0);

                // Increase index if necessary and recompute slope
                // Last slope is also extrapolated!
                if (index < nrPts) {
                    if (temp > chartMeasurements[index][lutNr]) {
                        index++;
                        if ((index > 0) && (index < nrPts)) {
                            slope = (chartValues[index][lutNr] - chartValues[index - 1][lutNr])
                                    / (chartMeasurements[index][lutNr] - chartMeasurements[index - 1][lutNr]);
                        }
                    }
                }

                // Compute value
                if (index > 0) {
                    LUT[i] = (int) Math.max(Math.min(Math.round(Util.GammaCorrection(
                            (temp - chartMeasurements[index - 1][lutNr]) * slope + chartValues[index - 1][lutNr])
                            * 255), 255), 0);
                } else {
                    LUT[i] = (int) Math.max(
                            Math.min(Math.round(Util.GammaCorrection(
                                    (temp - chartMeasurements[0][lutNr]) * slope + chartValues[0][lutNr]) * 255), 255),
                            0);
                }
            }

            LUTs[lutNr] = LUT;
        }

        return LUTs;
    }

    private static int CheckForSaturation(double[][] x, double y[][], int color) {
        // Check if values are saturated and remove them from computations!
        // We do this for the first and last patch if more than 2 patches are
        // used.
        int nrPts = x.length;
        if (nrPts > 2) {
            if ((x[0][color] <= 0.005) || ((x[1][color] - x[0][color]) < 0)) {
                // Too small, saturation on patch zero, removing it.
                IJ.write("Chart White Balance - Removing black patch  from computations due to saturation!");
                IJ.write("Chart White Balance - Black patch and colors with low values for color " + color
                        + " possibly unreliable");
                IJ.showMessageWithCancel("Warning", "Black patch and colors with low values for color " + color
                        + " possibly unreliable! Ok continues, Cancel to exit");
                for (int i = 0; i < (nrPts - 1); i++) {
                    x[i][color] = x[i + 1][color];
                    y[i][color] = y[i + 1][color];
                }
                nrPts--;
            }

            if ((x[nrPts - 1][color] > 0.995) || ((x[nrPts - 1][color] - x[nrPts - 2][color]) < 0)) {
                // Almost 1, remove last patch
                IJ.write("Chart White Balance - White patch for color " + color + ": " + x[nrPts - 1][color]);
                IJ.write("Chart White Balance - Removing white patch from computations due to saturation!");
                IJ.write("Chart White Balance - White patch and colors with high values for color " + color
                        + " possibly unreliable");
                IJ.showMessageWithCancel("Warning", "White patch and colors with high values for color " + color
                        + " possibly unreliable!Ok continues, Cancel to exit");
                nrPts--;
            }
        }
        return nrPts;
    }

    private static void PrintArray(String text, int[] array) {
        IJ.write(text);
        for (int i = 0; i < array.length; i++) {
            IJ.write(Integer.toString(array[i]));
        }
    }

    private static void SaveLUTs(int[][] LUTs) {
        // Save LUT in text file
        FileDialog fd = new FileDialog(new Frame(), "Save LUT (.dat)", FileDialog.SAVE);
        fd.setVisible(true);
        if (fd.getFile() == null) {
            IJ.write("Chart White Balance - User did not save LUT");
            return;
        } else {

            try {
                File file = new File(fd.getDirectory(), fd.getFile());
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));

                for (int j = 0; j < 3; j++) {
                    for (int i = 0; i < 256; i++) {
                        if (i < 255) {
                            bw.write(Integer.toString(LUTs[j][i]) + ",");
                        } else {
                            bw.write(Integer.toString(LUTs[j][i]));
                        }
                    }
                    bw.newLine();
                }
                bw.close();
                IJ.write("Chart White Balance - LUT saved to " + file.getPath());
            } catch (IOException ioe) {
                IJ.write("Chart White Balance - Failed to save LUT");
            }
        }
    }

    private static int[][] LoadLUTs() {
        FileDialog fd = new FileDialog(new Frame(), "Choose LUT (.dat)", FileDialog.LOAD);
        fd.setVisible(true);
        if (fd.getFile() == null) {
            IJ.write("Chart White Balance - User did not load LUT");
            return null;
        } else {
            int[][] LUTs = new int[3][];
            try {
                File file = new File(fd.getDirectory(), fd.getFile());
                BufferedReader in = new BufferedReader(new FileReader(file));
                String str;
                int line = 0;
                while ((str = in.readLine()) != null) {
                    // Split, each line should contain 256 entries
                    str = str.trim();
                    String[] parts = str.split(",");
                    // IJ.write("Chart White Balance - Read line of " +
                    // str.length() + " characters with " + parts.length + "
                    // parts");
                    // IJ.write("Chart White Balance - '" + str + "'");

                    if (parts.length >= 256) {
                        int[] LUT = new int[256];
                        for (int i = 0; i < 256; i++) {
                            LUT[i] = Integer.valueOf(parts[i]).intValue();
                        }
                        LUTs[line] = LUT;
                        line++;
                    }
                }
                in.close();
            } catch (IOException e) {
            }
            return LUTs;
        }
    }

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins
        // menu
        Class<?> clazz = CurveFitCalibration.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        System.out.println(clazz.getName());
        System.out.println(url);
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.out.println(pluginsDir);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

//         // open the Clown sample
//         ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
//         image.show();
//        
//         // run the plugin
//         IJ.runPlugIn(clazz.getName(), "");
    }
}
