package calb;


import java.awt.Polygon;
import java.util.HashMap;

import org.jblas.DoubleMatrix;

import org.jblas.Solve;


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
        int index = 0;
        //HashMap<Integer, Integer> buffer = new HashMap<Integer, Integer>(60000);
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                RGBUtil.DecodeRGB(imagePixels[index], tempRGB);
                imgv.put(0, index, 1);
                imgv.put(1, index, tempRGB[0]);
                imgv.put(2, index, tempRGB[1]);
                imgv.put(3, index, tempRGB[2]);
                imgv.put(4, index, tempRGB[0] * tempRGB[1]);
                imgv.put(5, index, tempRGB[0] * tempRGB[2]);
                imgv.put(6, index, tempRGB[1] * tempRGB[2]);
                imgv.put(7, index, tempRGB[0] * tempRGB[0]);
                imgv.put(8, index, tempRGB[1] * tempRGB[1]);
                imgv.put(9, index, tempRGB[2] * tempRGB[2]);
                index++;
            }
        }
        
        
        DoubleMatrix tarxT = aT.mmul(imgv).transpose();
        
        for (int i = 0; i < tarxT.rows; i++) {
        	int r = (int)tarxT.get(i, 0);
        	int g = (int)tarxT.get(i, 1);
        	int b = (int)tarxT.get(i, 2);
        	imagePixels[i] = (r << 16) + (g << 8) + b;
        }

        imp.repaintWindow(); 

        //show calibration result
        CCRGBs = RGBUtil.getCCRGBs(imagePixels, poly.xpoints[1], poly.ypoints[0], poly.xpoints[0], poly.ypoints[1], w, h);
        System.out.println("Calibration Result");
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
        return DOES_ALL;
	}

    public static void main(String[] args) {
        Class<?> clazz = Calb_Plugin.class;
        String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
        String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
        System.out.println(pluginsDir);
        System.setProperty("plugins.dir", pluginsDir);

        // start ImageJ
        new ImageJ();

        // // open the sample
        ImagePlus image = IJ.openImage("/home/fulva/works/java/imagej/resource/5s/01_5s2.JPG");
        image.show();
        //
        // // run the plugin
        // IJ.runPlugIn(clazz.getName(), "");
    }
}
