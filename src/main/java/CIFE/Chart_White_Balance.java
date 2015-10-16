/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package CIFE;

/**
 *
 * @author vhaegy
 */
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.filter.*;
import java.io.*;


// White balance based on black and white patch of color chart
// Needs a line from the center of black to the center of white before starting the macro
// Yves Vander Haeghen
// v1.0, 29 dec. 2008
// v1.1, 3 jan. 2009: Added support for intermediate gray patches
public class Chart_White_Balance implements PlugInFilter {

  ImagePlus imp;

  public int setup(String arg, ImagePlus imp) {
    this.imp = imp;
    //return DOES_RGB;
    return DOES_RGB;
  }

  public void run(ImageProcessor ip) {
    boolean plotLUT = false, saveLUT = false, loadLUT = false;
    int LUTs[][];
    double distance = 0;

    // Image dimensions, data and roi
    int w = imp.getWidth();
    int h = imp.getHeight();
    int[] imagePixels = (int[]) ip.getPixels();

    Roi roi = imp.getRoi();
    Polygon poly = null;

    if ((roi != null) && (roi.isLine())) {
      IJ.write("Chart White Balance - Patches indicated from black to white, computing  LUT ...");
      poly = roi.getPolygon();

      String[] charts = new String[]{"Small MacBeth ColorChecker Chart", "A4 MacBeth ColorChecker Chart", "QP201 chart"};

      GenericDialog dialog = new GenericDialog("Chart White Balance Options");
      dialog.addChoice("Chart:", charts, charts[0]);
      dialog.addCheckbox("Include intermediary gray patches", true);
      dialog.addCheckbox("Show LUTs", false);
      dialog.addCheckbox("Save LUTs", false);
      dialog.showDialog();
      if (dialog.wasCanceled()) {
        IJ.write("Chart White Balance - User quit plugin");
        return;
      }

      int chartType = dialog.getNextChoiceIndex();
      boolean includeIntermediaryPatches = dialog.getNextBoolean();
      plotLUT = dialog.getNextBoolean();
      saveLUT = dialog.getNextBoolean();

      double[][] chartValues;
      if (chartType != 2) {
        //MBCCC charts
        IJ.write("Chart White Balance - Selected MBCCC chart");
        if (!includeIntermediaryPatches) {
          chartValues = new double[][]{{0.0317, 0.0310, 0.0315}, {0.893, 0.887, 0.844}};
        } else {
          IJ.write("Chart White Balance - Using intermediate gray patches");
//          chartValues = new double[][]{{0.0317, 0.0310, 0.0315}, {0.0862, 0.0871, 0.0873}, {0.196, 0.195, 0.191},
//                    {0.355, 0.357, 0.356}, {0.585, 0.585, 0.581}, {0.893, 0.887, 0.844}};
            chartValues = new double[][]{{0.0538, 0.0538, 0.0538}, {0.1670, 0.1670, 0.1670}, {0.3218, 0.3218, 0.3218},
            {0.6153, 0.6153, 0.6153}, {0.8124, 0.8124, 0.8124}, {0.9763, 0.9763, 0.9763}};
        }

        if (chartType == 0) {
          //distance = 6.35;
            distance = 7.3;  //change to　diagonal distance 
        } else {
          distance = 23.4;
        }
      } else {
        //QP 201 chart
        IJ.write("Chart White Balance - Selected QP 201 chart");
        chartValues = new double[][]{{0.0474, 0.0457, 0.0449}, {0.866, 0.870, 0.831}};
        distance = 6.0;
      }

      //Measure the pixels and extract the RGB values.
      int nrPts = chartValues.length;
      double[][] greyChartValues = MeasureGreyChartsValue(imagePixels, poly.xpoints[0], poly.ypoints[1], poly.xpoints[1], poly.ypoints[1], nrPts, w, h);
      
      //Compute LUT's
      LUTs = ComputeLUT(greyChartValues, chartValues);
    } else {
      IJ.write("Chart White Balance - No patches indicated, loading existing LUT");
      LUTs = LoadLUTs();

      if (LUTs == null) {
        IJ.write("Chart White Balance - User cancelled loading LUT");
        return;
      }
      saveLUT = false;
      plotLUT = true;
      loadLUT = true;
    }

    //Plot if necessary
    if (plotLUT) {
      double[] x = new double[256], r = new double[256], g = new double[256], b = new double[256];
      for (int i = 0; i < 256; i++) {
        x[i] = i;
        r[i] = LUTs[0][i];
        g[i] = LUTs[1][i];
        b[i] = LUTs[2][i];
      }
      PlotWindow pw = new PlotWindow("LUTs", "Input pixel values", "Output pixel value", x, r);
      pw.setLimits(0, 255, 0, 255);
      pw.setColor(Color.green);
      pw.addPoints(x, g, PlotWindow.LINE);
      pw.setColor(Color.blue);
      pw.addPoints(x, b, PlotWindow.LINE);
      pw.setColor(Color.red);
      pw.draw();
      pw.setVisible(true);
    }

    //Modify LUT to speed them up by including the necessary bit shifts
    //This speeds up its application to the image
    for (int i = 0; i < 256; i++) {
      LUTs[0][i] = LUTs[0][i] << 16;
      LUTs[1][i] = LUTs[1][i] << 8;
    }

    //Do Image transformation
    int[] tempRGB = new int[3];
    int index = 0;
    for (int y = 0; y < h; y++) {
      IJ.showProgress(y, h);
      for (int x = 0; x < w; x++) {
        Util.DecodeRGB(imagePixels[index], tempRGB);
        imagePixels[index] = LUTs[0][tempRGB[0]] + LUTs[1][tempRGB[1]] + LUTs[2][tempRGB[2]];
        index++;
      }
    }

    //Reset LUT for saving and plot
    for (int i = 0; i < 256; i++) {
      LUTs[0][i] = LUTs[0][i] >> 16;
      LUTs[1][i] = LUTs[1][i] >> 8;
    }

    if (loadLUT == false) {
      //Set the pixel size allowing measurements afterwards.
      //7.3
      double pixelSize = distance / Math.sqrt(Math.pow(poly.xpoints[1] - poly.xpoints[0], 2) + Math.pow(poly.ypoints[1] - poly.ypoints[0], 2));
      ij.measure.Calibration cal = imp.getCalibration();
      cal.pixelHeight = pixelSize;
      cal.pixelWidth = pixelSize;
      cal.setUnit("cm");
      IJ.write("Chart White Balance - Resolution is " + 1 / pixelSize + " pixels per cm");
    }

    imp.repaintWindow();
    
    //show calibration result
    double[][] allRGBValueCalculated = measureAllChartColor(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0], poly.ypoints[1], w, h);
    for (int i = 0; i < allRGBValueCalculated.length; i++) {
        System.out.println("no " + (i + 1));
        for (int j = 0; j < allRGBValueCalculated[i].length; j++) {
            System.out.print(allRGBValueCalculated[i][j] + " ");
        }
        System.out.println(" ");
    }
    
    if (saveLUT) {
      SaveLUTs(LUTs);
    }
  }

  private static double[][] MeasureGreyChartsValue(int[] imageData, int x1, int y1, int x2, int y2, int nrPts, int imageWidth, int imageHeight) {
    //Measure the pixels and extract the RGB values.
    double[][] chartMeasurements = new double[nrPts][];
    double deltaX = (x2 - x1) / (nrPts - 1);
    double deltaY = (y2 - y1) / (nrPts - 1);
    double[] rgb;
    for (int i = 0; i < nrPts; i++) {
      int x = (int) Math.round(x1 + deltaX * i);
      int y = (int) Math.round(y1 + deltaY * i);
      rgb = Util.GetAvgRGB(imageData, x, y, 17, imageWidth, imageHeight);
      Util.InverseGammaCorrection(rgb, rgb);
      IJ.write("Chart White Balance - Measuring patch " + i + " at " + x + "," + y + ": " + rgb[0] + "," + rgb[1] + "," + rgb[2]);
      chartMeasurements[i] = rgb;
    }
    return chartMeasurements;
  }
  
  //accept diagnol coordinates, from top-left to bottom-right
  //return array with all RGBs of the chart
  //ps: MeasureGreyChartsValue() returns values after inverse gamma correction, this mothod returns RGBs
  public static double[][] measureAllChartColor(int[] imageData, int x0, int y0, int x1, int y1, int imageWidth, int imageHeight) {
      int chartColNum = 6;
      int chartRowNum = 4;
      double[][] chartMeasurements = new double[chartColNum * chartRowNum][]; //stores rgb[3]
      double deltaX = (x1 - x0) / (chartColNum - 1);
      double deltaY = (y1 - y0) / (chartRowNum - 1);
      double[] rgb;
      for (int i = 0; i < chartRowNum; i++) {
          int y = (int) Math.round(y0 + deltaY * i);
          for (int j = 0; j < chartColNum; j++) {
              int x = (int) Math.round(x0 + deltaX * j);
              rgb = Util.GetAvgRGB(imageData, x, y, 17, imageWidth, imageHeight);
              IJ.write("Chart White Balance - Measuring patch " + i + " at " + x + "," + y + ": " + rgb[0] + "," + rgb[1] + "," + rgb[2]);
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

      //Check for saturation in the measured patches
      int nrPts = CheckForSaturation(chartMeasurements, chartValues, lutNr);

      //IJ.write("Chart White Balance - Computing LUT with " + nrPts + " points");
      //PrintArray("LUT control points X", x);
      //PrintArray("LUT control points Y", y);

      //Index is the index of x in the data array juast bigger the x we want to compute
      //First slope is extrapolated!
      int index = 0;
      double slope = (chartValues[1][lutNr] - chartValues[0][lutNr]) / (chartMeasurements[1][lutNr] - chartMeasurements[0][lutNr]);
 
      for (int i = 0; i < 256; i++) {
        double temp = Util.InverseGammaCorrection(i / 255.0);

        //Increase index if necessary and recompute slope
        //Last slope is also extrapolated!
        if (index < nrPts) {
          if (temp > chartMeasurements[index][lutNr]) {
            index++;
            if ((index > 0) && (index < nrPts)) {
              slope = (chartValues[index][lutNr] - chartValues[index - 1][lutNr]) / (chartMeasurements[index][lutNr] - chartMeasurements[index - 1][lutNr]);
            }
          }
        }

        //Compute value
        if (index > 0) {
          LUT[i] = (int) Math.max(Math.min(Math.round(Util.GammaCorrection((temp - chartMeasurements[index - 1][lutNr]) * slope + chartValues[index - 1][lutNr]) * 255), 255), 0);
        } else {
          LUT[i] = (int) Math.max(Math.min(Math.round(Util.GammaCorrection((temp - chartMeasurements[0][lutNr]) * slope + chartValues[0][lutNr]) * 255), 255), 0);
        }
      }

      LUTs[lutNr] = LUT;
    }

    return LUTs;
  }

  private static int CheckForSaturation(double[][] x, double y[][], int color) {
    // Check if values are saturated and remove them from computations!
    // We do this for the first and last patch if more than 2 patches are used.
    int nrPts = x.length;
    if (nrPts > 2) {
      if ((x[0][color] <= 0.005) || ((x[1][color] - x[0][color]) < 0)) {
        //Too small, saturation on patch zero, removing it.
        IJ.write("Chart White Balance - Removing black patch  from computations due to saturation!");
        IJ.write("Chart White Balance - Black patch and colors with low values for color " + color + " possibly unreliable");
        IJ.showMessageWithCancel("Warning", "Black patch and colors with low values for color " + color + " possibly unreliable! Ok continues, Cancel to exit");
        for (int i = 0; i < (nrPts - 1); i++) {
          x[i][color] = x[i + 1][color];
          y[i][color] = y[i + 1][color];
        }
        nrPts--;
      }

      if ((x[nrPts - 1][color] > 0.995) || ((x[nrPts - 1][color] - x[nrPts - 2][color]) < 0)) {
        //Almost 1, remove last patch
        IJ.write("Chart White Balance - White patch for color " + color + ": " + x[nrPts - 1][color]);
        IJ.write("Chart White Balance - Removing white patch from computations due to saturation!");
        IJ.write("Chart White Balance - White patch and colors with high values for color " + color + " possibly unreliable");
        IJ.showMessageWithCancel("Warning", "White patch and colors with high values for color " + color + " possibly unreliable!Ok continues, Cancel to exit");
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
    //Save LUT in text file
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
          //Split, each line should contain 256 entries
          str = str.trim();
          String[] parts = str.split(",");
          //IJ.write("Chart White Balance - Read line of " + str.length() + " characters with " + parts.length + " parts");
          //IJ.write("Chart White Balance - '" + str + "'");

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
      // set the plugins.dir property to make the plugin appear in the Plugins menu
      Class<?> clazz = Chart_White_Balance.class;
      String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
      System.out.println(clazz.getName());
      System.out.println(url);
      String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
      System.out.println(pluginsDir);
      System.setProperty("plugins.dir", pluginsDir);

      // start ImageJ
      new ImageJ();

//    // open the Clown sample
//    ImagePlus image = IJ.openImage("http://imagej.net/images/clown.jpg");
//    image.show();a
//
//    // run the plugin
//    IJ.runPlugIn(clazz.getName(), "");
  }
}
