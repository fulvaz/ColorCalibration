package calb;

public class RGBUtil {
	/**
	 * accept diagnol coordinates, from top-left to bottom-right
	 *  return array with all RGBs of the chart
	 * */
    public static double[][] getCCRGBs(int[] imageData, int x0, int y0, int x1, int y1, int imageWidth,  int imageHeight) {
        int chartColNum = Config.CCCol;
        int chartRowNum = Config.CCRow;
        double[][] chartMeasurements = new double[chartColNum * chartRowNum][];
        double deltaX = (x1 - x0) / (chartColNum - 1);
        double deltaY = (y1 - y0) / (chartRowNum - 1);
        double[] rgb;
        for (int i = 0; i < chartRowNum; i++) {
            int y = (int) Math.round(y0 + deltaY * i);
            for (int j = 0; j < chartColNum; j++) {
                int x = (int) Math.round(x0 + deltaX * j);
                rgb = RGBUtil.GetAvgRGB(imageData, x, y, 17, imageWidth, imageHeight);
                chartMeasurements[i * 6 + j] = rgb;
            }
        }
        return chartMeasurements;
    }
	
	
	/**
	 * data: rgb of all pixels get from imagej
	 * x: insterest point position
	 * windowSize:  size of single small chart
	 * */
	  public static double[] GetAvgRGB(int[] data, int x, int y, int windowSize, int imageWidth, int imageHeight) {
	    double[] rgb = new double[3];
	    int[] rgbTemp = new int[3];
	    int offset = (windowSize - 1) / 2;
	    int x1 = Math.max(0, x - offset); //insterest point position region
	    int x2 = Math.min(imageWidth - 1, x + offset);
	    int y1 = Math.max(0, y - offset);
	    int y2 = Math.min(imageHeight - 1, y + offset);

	    for (int yp = y1; yp <= y2; yp++) {
	      for (int xp = x1; xp <= x2; xp++) {
	        DecodeRGB(data, xp, yp, imageWidth, rgbTemp);
	        for (int i = 0; i < 3; i++) {
	          rgb[i] += rgbTemp[i]; //calculate sum
	        }
	      }
	    }

	    int nrPixels = (x2 - x1 + 1) * (y2 - y1 + 1);
	    if (nrPixels > 0) {
	      for (int i = 0; i < 3; i++) {
	        rgb[i] /= nrPixels;
	      }
	    }
	    return rgb; 
	  }

	  public static void DecodeRGB(int[] data, int x, int y, int width, int[]gammaRGB)
	  {
	      DecodeRGB(data[x + y * width], gammaRGB);
	  }

	  public static void DecodeRGB(int rawRGB, int[]gammaRGB)
	  {
	      gammaRGB[0] = (rawRGB >> 16) & 0xff;
	      gammaRGB[1] = (rawRGB >> 8) & 0xff;
	      gammaRGB[2] = rawRGB & 0xff;
	  }
	  
	public static double GammaCorrection(double sIn) {
		if (sIn > 0.018) {
			if (sIn >= 1.0) {
				return 1.0;
			} else {
				return 1.099 * (Math.pow(sIn, 0.45)) - 0.099;
			}
		} else {
			if (sIn < 0.0) {
				return 0.0;
			} else {
				return 4.5 * sIn;
			}
		}
	}
	
	public static int[] GammaCorrection(double[] sIn) {
		int[] sOut = new int[sIn.length];
		for (int i = 0; i < sIn.length; i++) {
			sOut[i] = (int)(GammaCorrection(sIn[i]) * 255.0);
		}
		return sOut;
	}

	public static double InverseGammaCorrection(double sIn) {
		if (sIn <= 0.081) {
			return sIn / 4.5;
		} else {
			return Math.pow(((sIn + 0.099) / 1.099), 2.22222222);
		}
	}
	
	public static double[] InverseGammaCorrection(double[] sIn) {
		double[] sOut = new double[sIn.length];
		for (int i = 0; i < sIn.length; i++) {
			sOut[i] = InverseGammaCorrection(sIn[i]);
		}
		return sOut;
	}
}
