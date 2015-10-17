package PFC;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Polygon;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.sun.tools.javah.resources.l10n;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Curve_FIt_Calibration implements PlugInFilter {

    ImagePlus imp;
    private static int LUTs[][] = new int[3][]; //0 1 2 : R[] G[] B[]
    private static double distance = 7.3;
    private static int chartRow = 4;
    private static int charColumn = 6;
    private static int polynomialDegree = 2;
    private static boolean plotLUT = true;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        // Image dimensions, data and roi
        int w = imp.getWidth();
        int h = imp.getHeight();
        int[] imagePixels = (int[]) ip.getPixels();

        Roi roi = imp.getRoi();
        Polygon poly = null;
        poly = roi.getPolygon(); // TODO Needs If statement

        double[][] allChartValues = measureAllChartColor(imagePixels, poly.xpoints[1], poly.ypoints[0],
                poly.xpoints[0], poly.ypoints[1], w, h);
        WeightedObservedPoints[] rgbFitPoints = initPoints(allChartValues); //0 1 2 : R G B
        double[] RCoefficients = curveFit(rgbFitPoints[0]);
        double[] GCoefficients = curveFit(rgbFitPoints[1]);
        double[] BCoefficients = curveFit(rgbFitPoints[2]);

        LUTs[0] = calculateLUT(RCoefficients); //0 1 2 : R G B
        LUTs[1] = calculateLUT(GCoefficients);
        LUTs[2] = calculateLUT(BCoefficients);
        
        //draw plot
        if (plotLUT) {
            double[] x = new double[256], r = new double[256], g = new double[256], b = new double[256];
            for (int i = 0; i < 256; i++) {
              x[i] = i;
              r[i] = LUTs[0][i];
              g[i] = LUTs[1][i];
              b[i] = LUTs[2][i];
            }
            Plot plot = new Plot("LUT", "Input", "Output", x, r);
            plot.addPoints(x, r, PlotWindow.LINE);
            plot.setColor(Color.GREEN);
            plot.addPoints(x, g, PlotWindow.LINE);
            plot.setColor(Color.BLUE);
            plot.addPoints(x, b, PlotWindow.LINE);
            plot.setColor(Color.RED);
            plot.show();
          }

        // Do Image transformation
        int[] tempRGB = new int[3];
        int index = 0;
        for (int y = 0; y < h; y++) {
            IJ.showProgress(y, h);
            for (int x = 0; x < w; x++) {
                Util.DecodeRGB(imagePixels[index], tempRGB);
                int r = rgbMinMaxFilter(LUTs[0][tempRGB[0]]);
                int g = rgbMinMaxFilter(LUTs[1][tempRGB[1]]);
                int b = rgbMinMaxFilter(LUTs[2][tempRGB[2]]);
                
                imagePixels[index] = (r << 16) + (g << 8) + b;
                index++;
            }
        }

        imp.repaintWindow();

        // show calibration result
//        System.out.println("here is the calibration result");
//        allChartValues = measureAllChartColor(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0],
//                poly.ypoints[1], w, h);
//        for (int i = 0; i < allChartValues.length; i++) {
//            System.out.println("no " + (i + 1));
//            for (int j = 0; j < allChartValues[i].length; j++) {
//                System.out.print(allChartValues[i][j] + " ");
//            }
//            System.out.println(" ");
//        }
        

        int[] LUT = new int[256];

    }
    
    public static int rgbMinMaxFilter(int x) {
        if (x < 0) return 0;
        if (x > 255) return 255;
        return x;
    }
    
    //TODO review
    public static int[] calculateLUT(double[] coefficients) {
        PolynomialFunction pFunction = new PolynomialFunction(coefficients);
        int[] LUT = new int[256];
        for (int i = 0; i < 256; i++) {
            LUT[i] = (int) pFunction.value(i);
        }
        return LUT;
    }

    public static WeightedObservedPoints[] initPoints(double[][] allRGBValueCalculated) {
        final WeightedObservedPoints R = new WeightedObservedPoints();
        final WeightedObservedPoints G = new WeightedObservedPoints();
        final WeightedObservedPoints B = new WeightedObservedPoints();
        // prepare points
        R.add((int)allRGBValueCalculated[0][0], 115);
        R.add((int)allRGBValueCalculated[1][0], 204);
        R.add((int)allRGBValueCalculated[2][0], 101);
        R.add((int)allRGBValueCalculated[3][0], 89);
        R.add((int)allRGBValueCalculated[4][0], 141);
        R.add((int)allRGBValueCalculated[5][0], 132);
        R.add((int)allRGBValueCalculated[6][0], 249);
        R.add((int)allRGBValueCalculated[7][0], 80);
        R.add((int)allRGBValueCalculated[8][0], 222);
        R.add((int)allRGBValueCalculated[9][0], 91);
        R.add((int)allRGBValueCalculated[10][0], 173);
        R.add((int)allRGBValueCalculated[11][0], 255);
        R.add((int)allRGBValueCalculated[12][0], 44);
        R.add((int)allRGBValueCalculated[13][0], 74);
        R.add((int)allRGBValueCalculated[14][0], 179);
        R.add((int)allRGBValueCalculated[15][0], 250);
        R.add((int)allRGBValueCalculated[16][0], 191);
        R.add((int)allRGBValueCalculated[17][0], 6);
        R.add((int)allRGBValueCalculated[18][0], 252);
        R.add((int)allRGBValueCalculated[19][0], 230);
        R.add((int)allRGBValueCalculated[20][0], 200);
        R.add((int)allRGBValueCalculated[21][0], 143);
        R.add((int)allRGBValueCalculated[22][0], 100);
        R.add((int)allRGBValueCalculated[23][0], 50);

        G.add((int)allRGBValueCalculated[0][1], 82);
        G.add((int)allRGBValueCalculated[1][1], 161);
        G.add((int)allRGBValueCalculated[2][1], 134);
        G.add((int)allRGBValueCalculated[3][1], 109);
        G.add((int)allRGBValueCalculated[4][1], 137);
        G.add((int)allRGBValueCalculated[5][1], 228);
        G.add((int)allRGBValueCalculated[6][1], 118);
        G.add((int)allRGBValueCalculated[7][1], 91);
        G.add((int)allRGBValueCalculated[8][1], 91);
        G.add((int)allRGBValueCalculated[9][1], 63);
        G.add((int)allRGBValueCalculated[10][1], 232);
        G.add((int)allRGBValueCalculated[11][1], 164);
        G.add((int)allRGBValueCalculated[12][1], 56);
        G.add((int)allRGBValueCalculated[13][1], 148);
        G.add((int)allRGBValueCalculated[14][1], 42);
        G.add((int)allRGBValueCalculated[15][1], 226);
        G.add((int)allRGBValueCalculated[16][1], 81);
        G.add((int)allRGBValueCalculated[17][1], 142);
        G.add((int)allRGBValueCalculated[18][1], 252);
        G.add((int)allRGBValueCalculated[19][1], 230);
        G.add((int)allRGBValueCalculated[20][1], 200);
        G.add((int)allRGBValueCalculated[21][1], 143);
        G.add((int)allRGBValueCalculated[22][1], 100);
        G.add((int)allRGBValueCalculated[23][1], 50);

        B.add((int)allRGBValueCalculated[0][2], 69);
        B.add((int)allRGBValueCalculated[1][2], 141);
        B.add((int)allRGBValueCalculated[2][2], 179);
        B.add((int)allRGBValueCalculated[3][2], 61);
        B.add((int)allRGBValueCalculated[4][2], 194);
        B.add((int)allRGBValueCalculated[5][2], 208);
        B.add((int)allRGBValueCalculated[6][2], 35);
        B.add((int)allRGBValueCalculated[7][2], 182);
        B.add((int)allRGBValueCalculated[8][2], 125);
        B.add((int)allRGBValueCalculated[9][2], 123);
        B.add((int)allRGBValueCalculated[10][2], 91);
        B.add((int)allRGBValueCalculated[11][2], 26);
        B.add((int)allRGBValueCalculated[12][2], 142);
        B.add((int)allRGBValueCalculated[13][2], 81);
        B.add((int)allRGBValueCalculated[14][2], 50);
        B.add((int)allRGBValueCalculated[15][2], 21);
        B.add((int)allRGBValueCalculated[16][2], 160);
        B.add((int)allRGBValueCalculated[17][2], 172);
        B.add((int)allRGBValueCalculated[18][2], 252);
        B.add((int)allRGBValueCalculated[19][2], 230);
        B.add((int)allRGBValueCalculated[20][2], 200);
        B.add((int)allRGBValueCalculated[21][2], 143);
        B.add((int)allRGBValueCalculated[22][2], 100);
        B.add((int)allRGBValueCalculated[23][2], 50);

        WeightedObservedPoints[] rgbFitPoints = { R, G, B };
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
        final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(polynomialDegree);

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

    /**
     * Main method for debugging.
     *
     * For debugging, it is convenient to have a method that starts ImageJ,
     * loads an image and calls the plugin, e.g. after setting breakpoints.
     *
     * @param args
     *            unused
     */
    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins
        // menu
        Class<?> clazz = Curve_FIt_Calibration.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // // open the Clown sample
        ImagePlus image = IJ.openImage("/home/fulva/imagej/resource/IMG_0431.JPG");
        image.show();
        //
        // // run the plugin
        // IJ.runPlugIn(clazz.getName(), "");
    }
}
