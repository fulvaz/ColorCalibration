package PFC;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 *
 * @author vhaegy
 */
public class Util {

  //data: rgb of all pixels
  //x: insterest point position
  //windowSize:  ?? what
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

  public static void InverseGammaCorrection(double[] gammaRGB, double[] RGB) {
    for(int i = 0; i < 3; i ++) RGB[i] = InverseGammaCorrection(gammaRGB[i] / 255.0);
  }

  public static void InverseGammaCorrection(int[] gammaRGB, double[] RGB) {
    for(int i = 0; i < 3; i ++) RGB[i] = InverseGammaCorrection(gammaRGB[i] / 255.0);
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

  public static double InverseGammaCorrection(double sIn) {
    if (sIn <= 0.081) {
      return sIn / 4.5;
    } else {
      return Math.pow(((sIn + 0.099) / 1.099), 2.22222222);
    }
  }
  
  //calculate your chart rgb
  public static void main(String[] args) {
      double[][] rgb = {{50, 50, 50}, {100, 100, 100}, {143, 143, 142}, {200, 200, 200}, {230, 230, 230}, {252, 252, 252}};
      for (int i = 0; i < rgb.length; i++) {
          InverseGammaCorrection(rgb[i], rgb[i]);
          System.out.println("point " + i + " : ");
          for (double val : rgb[i]) {
              System.out.println(val);
          }
      }
      
}
}
