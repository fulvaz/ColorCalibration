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
import java.util.logging.Level;
import java.util.logging.Logger;

public class Color_Classification implements PlugInFilter {

  ImagePlus imp;

  private static class ColorClass {

    public String Name;
    public int[] Color;
    public int NrPixels;

    public ColorClass(String Name, int[] Color) {
      this.Name = Name;
      this.Color = Color;
    }
  }

  public int setup(String arg, ImagePlus imp) {
    this.imp = imp;
    //return DOES_RGB;
    return DOES_RGB + ROI_REQUIRED + SUPPORTS_MASKING;
  }

  public void run(ImageProcessor ip) {

    //Get height and width
    int width = ip.getWidth();
    int height = ip.getHeight();

    //Read a classification map
    FileDialog fd = new FileDialog(new Frame(), "Choose classification map (.cmp)", FileDialog.LOAD);
    fd.setVisible(true);
    if (fd.getFile() == null) {
      IJ.write("Color Classification - User did not load classification map");
      return;
    }

    File file = new File(fd.getDirectory(), fd.getFile());
    byte[] cmp;
    try {
      cmp = getBytesFromFile(file);
    } catch (IOException ex) {
      Logger.getLogger(Color_Classification.class.getName()).log(Level.SEVERE, null, ex);
      IJ.write("Color Classification - Failed to load classification map");
      return;
    }

    //Display what we loaded
    int nrClasses = 0;
    for (int i = 0; i < cmp.length; i++) {
      if (cmp[i] > nrClasses) {
        nrClasses = cmp[i];
      }
    }
    nrClasses++;
    IJ.write("Color Classification - Loaded classification map " + file.getPath() + " with " + nrClasses + " classes");

    //Try to load the classes file, sets default otherwise.
    java.util.ArrayList<ColorClass> classes = readColorClasses(new File(changeExtension(file.getPath(), ".cls")), nrClasses);

    //Create classification image
    ImagePlus cmpIm = NewImage.createByteImage("Classification", width, height, 1, NewImage.FILL_BLACK);

    //Do the classification
    ImageProcessor cmpImIP = cmpIm.getProcessor();
    int[] srcPixels = (int[]) ip.getPixels();
    byte[] dstPixels = (byte[]) cmpImIP.getPixels();
    int offset, totalnrPixels = 0;
    int[] tempRGB = new int[3];
    Rectangle roiRect = ip.getRoi();
    Roi roi = imp.getRoi();
    for (int i = 0; i < roiRect.height; i++) {
      IJ.showProgress(i, roiRect.height - 1);
      offset = (i + roiRect.y) * width + roiRect.x;
      for (int j = 0; j < roiRect.width; j++) {
        if (roi.contains(j + roiRect.x, i + roiRect.y)) {
          //Get red green and red?
          //If we change the order in which the array is saved we can speed this up!
          Util.DecodeRGB(srcPixels[offset], tempRGB);
          byte pixelClass = cmp[tempRGB[0] + 256 * (tempRGB[1] + 256 * tempRGB[2])];
          dstPixels[offset] = pixelClass;
          classes.get(pixelClass).NrPixels++;
          totalnrPixels++;
        }
        offset++;
      }
    }

    //Show results.
    IJ.write("Color Classification - Classified " + totalnrPixels + " pixels");
    byte[] reds = new byte[classes.size()];
    byte[] greens = new byte[classes.size()];
    byte[] blues = new byte[classes.size()];
    byte[] opacities = new byte[classes.size()];
    if (totalnrPixels > 0) {
      //Class 0 is for the pixels that werent classified, i.e. outside the ROI
      for (int j = 1; j < classes.size(); j++) {
        ColorClass cls = classes.get(j);
        IJ.write("Color Classification - Class '" + cls.Name + "' has " + cls.NrPixels + " pixels, " + 100.0 * cls.NrPixels / totalnrPixels + "%");
        opacities[j] = (byte) cls.Color[0];
        reds[j] = (byte) cls.Color[1];
        greens[j] = (byte) cls.Color[2];
        blues[j] = (byte) cls.Color[3];
      }
    }

    // Add color map
    java.awt.image.IndexColorModel cm = new java.awt.image.IndexColorModel(8, reds.length, reds, greens, blues, opacities);
    cmpImIP.setColorModel(cm);
    cmpIm.show();
    cmpIm.updateAndDraw();
  }

  // Returns the contents of the file in a byte array.
  public static byte[] getBytesFromFile(File file) throws IOException {
    InputStream is = new FileInputStream(file);

    // Get the size of the file
    long length = file.length();

    // You cannot create an array using a long type.
    // It needs to be an int type.
    // Before converting to an int type, check
    // to ensure that file is not larger than Integer.MAX_VALUE.
    if (length > Integer.MAX_VALUE) {
      // File is too large
    }

    // Create the byte array to hold the data
    byte[] bytes = new byte[(int) length];

    // Read in the bytes
    int offset = 0;
    int numRead = 0;
    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
      offset += numRead;
    }

    // Ensure all the bytes have been read in
    if (offset < bytes.length) {
      throw new IOException("Color Classification - Could not completely read file " + file.getName());
    }

    // Close the input stream and return bytes
    is.close();
    return bytes;
  }

  private static java.util.ArrayList<ColorClass> readColorClasses(File file, int nrClasses) {
    java.util.ArrayList<ColorClass> list = new java.util.ArrayList<ColorClass>();
    if (file.exists()) {
      //Read it and set
      try {
        BufferedReader in = new BufferedReader(new FileReader(file));
        String str;
        while ((str = in.readLine()) != null) {
          //Split with comments
          str = str.trim();
          String[] parts = str.split(",");
          //ARGB order
          ColorClass cls = new ColorClass(parts[0], new int[]{Integer.valueOf(parts[1]).intValue(), Integer.valueOf(parts[2]).intValue(), Integer.valueOf(parts[3]).intValue(), Integer.valueOf(parts[4]).intValue()});
          list.add(cls);
        }
        in.close();
      } catch (IOException e) {
      }
    } else {
      //Set it using a grayscale ramp and numeral class names
      //Entry 0 is transparent!
      int step = 255 / nrClasses;
      int opacity = 0;
      for (int i = 0; i < nrClasses; i++) {
        ColorClass cls = new ColorClass(Integer.toString(i), new int[]{opacity, i * step, i * step, i * step});
        list.add(cls);
        opacity = 255;
      }
    }
    return list;
  }

  static String changeExtension(String originalName, String newExtension) {
    int lastDot = originalName.lastIndexOf(".");
    if (lastDot != -1) {
      return originalName.substring(0, lastDot) + newExtension;
    } else {
      return originalName + newExtension;
    }
  }
}
