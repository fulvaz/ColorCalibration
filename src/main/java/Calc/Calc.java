package Calc;

import java.awt.Color;
import java.awt.Polygon;

import org.apache.commons.math3.fitting.WeightedObservedPoints;

import PFC.Curve_FIt_Calibration;
import PFC.Util;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Calc  implements PlugInFilter {
	
	private ImagePlus imp;


	/**
	 * accept diagnol coordinates, from top-left to bottom-right
	 *  return array with all RGBs of the chart
	 * */
    public double[][] getCCRGBs(int[] imageData, int x0, int y0, int x1, int y1, int imageWidth,
            int imageHeight) {
        int chartColNum = 6;
        int chartRowNum = 4;
        double[][] chartMeasurements = new double[chartColNum * chartRowNum][];
        double deltaX = (x1 - x0) / (chartColNum - 1);
        double deltaY = (y1 - y0) / (chartRowNum - 1);
        double[] rgb;
        for (int i = 0; i < chartRowNum; i++) {
            int y = (int) Math.round(y0 + deltaY * i);
            for (int j = 0; j < chartColNum; j++) {
                int x = (int) Math.round(x0 + deltaX * j);
                rgb = Util.GetAvgRGB(imageData, x, y, 17, imageWidth, imageHeight);
                chartMeasurements[i * 6 + j] = rgb;
            }
        }
        return chartMeasurements;
    }
    
    



	@Override
	public void run(ImageProcessor ip) {
		 // Image dimensions, data and roi
        int w = imp.getWidth();
        int h = imp.getHeight();
        int[] imagePixels = (int[]) ip.getPixels();

        Roi roi = imp.getRoi();
        Polygon poly;
        poly = roi.getPolygon(); // TODO Needs If statement

        double[][] CCRGBs = getCCRGBs(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0], poly.ypoints[1], w, h);


        imp.repaintWindow();

        //show calibration result
        System.out.println("Calibration Result");
        CCRGBs = getCCRGBs(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0],
                poly.ypoints[1], w, h);
        for (int i = 0; i < CCRGBs.length; i++) {
            System.out.println("no " + (i + 1));
            for (int j = 0; j < CCRGBs[i].length; j++) {
                System.out.print(CCRGBs[i][j] + " ");
            }
            System.out.println(" ");
        }
	}
	@Override
	public int setup(String arg0, ImagePlus imp) {
        this.imp = imp;
        return DOES_RGB;
	}

    public static void main(String[] args) {
        // set the plugins.dir property to make the plugin appear in the Plugins
        // menu
        Class<?> clazz = Calc.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // // open the Clown sample
        ImagePlus image = IJ.openImage("/home/fulva/works/java/imagej/resource/5s/01_5s.JPG");
        image.show();
        //
        // // run the plugin
        // IJ.runPlugIn(clazz.getName(), "");
    }
}
