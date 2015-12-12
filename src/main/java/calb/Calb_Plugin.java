package calb;


import java.awt.Polygon;
import java.util.HashMap;

import org.jblas.DoubleMatrix;

import org.jblas.Solve;

import CIFE.Util;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Calb_Plugin  implements PlugInFilter {
	
	private ImagePlus imp;

	@Override
	public void run(ImageProcessor ip) {
		 // Image dimensions, data and roi
        int w = imp.getWidth();
        int h = imp.getHeight();
        int[] imagePixels = (int[]) ip.getPixels();

        Roi roi = imp.getRoi();
        Polygon poly;
        poly = roi.getPolygon();

        
        double[][] CCRGBs = RGBUtil.getCCRGBs(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0], poly.ypoints[1], w, h);
        //feature scaling
        for (int i = 0; i < 24; i++) {
        	for (int j = 0; j < 3; j++) {
        		CCRGBs[i][j] = RGBUtil.InverseGammaCorrection(CCRGBs[i][j] / 255.0);
        	}
        }
        

        
        
        //col:  {1, R, G, B, RG, RB, BG, R^2, G^2, B^2 }
        //我知道空间相邻性
        double[][] vArray = new double[10][24];
        for(int i = 0; i < 24; i++) { 
        		vArray[0][i] = 1;
        		vArray[1][i] = CCRGBs[i][0]; //R
        		vArray[2][i] = CCRGBs[i][1]; //G
        		vArray[3][i] = CCRGBs[i][2]; //B
        		vArray[4][i] = CCRGBs[i][0] * CCRGBs[i][1]; //RG
        		vArray[5][i] = CCRGBs[i][0] * CCRGBs[i][2]; //RB
        		vArray[6][i] = CCRGBs[i][2] * CCRGBs[i][1]; //BG
        		vArray[7][i] = CCRGBs[i][0] * CCRGBs[i][0]; //RR
        		vArray[8][i] = CCRGBs[i][1] * CCRGBs[i][1];
        		vArray[9][i] = CCRGBs[i][2] * CCRGBs[i][2];
        }
        
        //R01  R02..... R0i 
        //G01  G2..... G0i 
        //B01  B02..... B0i 
        double[][] xArray = new double [3][24];
        

        for(int i = 0; i < 3; i++) {
        	for (int j = 0; j < 24; j++) {
        		xArray[i][j] = Config.targetRGBs[j][i];
        	}
        }
        
        //生成矩阵
        DoubleMatrix x = new DoubleMatrix(xArray);
        DoubleMatrix v = new DoubleMatrix(vArray);

        //算A X = A^T∗V     A = (V∗V^T)^−1∗(V∗X^T)
        //DoubleMatrix aT = Solve.solveLeastSquares(v, x);
        DoubleMatrix tmp = v.mmul((v.transpose()));
        tmp = Solve.pinv(tmp);
        tmp = tmp.mmul(v);
        tmp = tmp.mmul(x.transpose());
        //DoubleMatrix aT = Solve.pinv(v.mmul(v.transpose())).mmul(v).mmul(x.transpose());
        DoubleMatrix aT = tmp.transpose();

 
        //TODO 800w的数据要考虑空间相邻性
        //double[][] imgvArray = new double[10][900 * 10000];
        DoubleMatrix imgv = new DoubleMatrix(10, imagePixels.length);

      
        //准备数组
        int[]tempRGB = new int[3];
        double[]dtempRGB = new double[3];

        //HashMap<Integer, Integer> buffer = new HashMap<Integer, Integer>(60000);
		for (int i = 0; i < (w * h); i++) {
			RGBUtil.DecodeRGB(imagePixels[i], tempRGB);
			for (int j = 0; j < 3; j++) {
				dtempRGB[j] = RGBUtil.InverseGammaCorrection(tempRGB[j] / 255.0);
			}
			imgv.put(0, i, 1);
			imgv.put(1, i, dtempRGB[0]);
			imgv.put(2, i, dtempRGB[1]);
			imgv.put(3, i, dtempRGB[2]);
			imgv.put(4, i, dtempRGB[0] * dtempRGB[1]);
			imgv.put(5, i, dtempRGB[0] * dtempRGB[2]);
			imgv.put(6, i, dtempRGB[1] * dtempRGB[2]);
			imgv.put(7, i, dtempRGB[0] * dtempRGB[0]);
			imgv.put(8, i, dtempRGB[1] * dtempRGB[1]);
			imgv.put(9, i, dtempRGB[2] * dtempRGB[2]);
			ij.IJ.showProgress(i, w * h);
		}
		
        
        
        DoubleMatrix tarxT = aT.mmul(imgv).transpose();
        
        for (int i = 0; i < tarxT.rows; i++) {
        	int r = (int)(Util.GammaCorrection(tarxT.get(i, 0)) * 255);
        	int g = (int)(Util.GammaCorrection(tarxT.get(i, 1)) * 255);
        	int b = (int)(Util.GammaCorrection(tarxT.get(i, 2)) * 255);
        	imagePixels[i] = (r << 16) + (g << 8) + b;
        }

        imp.repaintWindow(); 

        //show calibration result
//        CCRGBs = RGBUtil.getCCRGBs(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0], poly.ypoints[1], w, h);
//        System.out.println("Calibration Result");
//        for (int i = 0; i < CCRGBs.length; i++) {
//            System.out.println("no " + (i + 1));
//            for (int j = 0; j < CCRGBs[i].length; j++) {
//                System.out.print(CCRGBs[i][j] + " ");
//            }
//            System.out.println(" ");
//        }
        
        //show error
        double maxErr = 0;
        double minErr = 0;
        double avgErr = 0;
        double totleErr = 0;
        for (int i = 0; i < Config.targetRGBs.length; i++) {
        		double err = 
	        		Math.pow((CCRGBs[i][0] - Config.targetRGBs[i][0]), 2) + 
	        		Math.pow((CCRGBs[i][1] - Config.targetRGBs[i][1]), 2) + 
	        		Math.pow((CCRGBs[i][2] - Config.targetRGBs[i][2]), 2);
        		if (i == 0) {
        			maxErr = err;
        			minErr = err;
        		}
        		totleErr += err;
        		if (err > maxErr) {
        			maxErr = err;
        		}
        		if (err < minErr) {
        			minErr = err;
        		}
        		totleErr += err;
        }
        avgErr = totleErr / Config.targetRGBs.length;
      System.out.println("Calibration result");
      System.out.println("avg error: " + avgErr);
      System.out.println("max error: " + maxErr);
      System.out.println("min error: " + minErr);
        
	}
	@Override
	public int setup(String arg0, ImagePlus imp) {
        //inverse gamma correction
        this.imp = imp;
        return DOES_ALL;
	}

    public static void main(String[] args) {
        Class<?> clazz = Calb_Plugin.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.out.println(pluginsDir);
        System.setProperty("plugins.dir", pluginsDir);
        
        //debug purpose
//        System.out.println("Original chart");
//        for (int i = 0; i < 24; i++) {
//        	System.out.print("{");
//            for (int j = 0; j < 3; j++) {
//                System.out.print(", " + Config.targetRGBs[i][j]);
//            }
//            System.out.println("},");
//        }

        // start ImageJ
        new ImageJ();

        // // open the sample
//        ImagePlus image = IJ.openImage("/home/fulva/works/java/imagej/resource/5s/01_5s2.JPG");
//        image.show();
        //
         // run the plugin
         //IJ.runPlugIn(clazz.getName(), "");
    }
}
