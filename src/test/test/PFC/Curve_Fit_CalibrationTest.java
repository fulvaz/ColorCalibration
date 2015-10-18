//package test.PFC;
//
//import static org.junit.Assert.*;
//
//
//import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
//import org.apache.commons.math3.fitting.WeightedObservedPoints;
//import org.junit.Test;
//
//
//import PFC.Curve_FIt_Calibration;
//
//public class Curve_Fit_CalibrationTest {
//    final double[][] allChartValues = {{119.4325259515571, 72.22491349480968, 60.21799307958477}, {224.09342560553634, 151.37370242214533, 137.59861591695503}, {102.32525951557093, 124.32525951557093, 171.32525951557093}, {84.66435986159169, 92.83391003460207, 50.24913494809689}, {155.8650519031142, 138.6955017301038, 178.91695501730104}, {116.1522491349481, 199.1522491349481, 181.8166089965398}, {254.49134948096886, 116.67474048442907, 46.8235294117647}, {74.22491349480968, 76.58131487889273, 157.9031141868512}, {249.9273356401384, 73.92733564013841, 96.49826989619378}, {94.16955017301038, 44.44290657439446, 74.80622837370242}, {156.66089965397924, 180.50173010380624, 70.88235294117646}, {244.10034602076124, 142.53287197231833, 40.193771626297575}, {25.33910034602076, 21.33910034602076, 46.6159169550173}, {54.02768166089965, 108.93079584775087, 51.778546712802765}, {183.26643598615917, 36.0795847750865, 42.0795847750865}, {235.92387543252596, 173.01384083044982, 30.363321799307958}, {170.93771626297578, 53.294117647058826, 98.16262975778547}, {39.35294117647059, 110.56055363321799, 142.560553633218}, {209.93079584775086, 178.7923875432526, 159.9134948096886}, {183.28373702422147, 150.7266435986159, 133.2560553633218}, {146.98615916955018, 123.76124567474048, 110.21107266435986}, {117.12456747404845, 96.70934256055364, 85.99307958477509}, {70.68512110726644, 57.59861591695502, 52.73010380622837}, {44.02768166089965, 32.91349480968858, 28.70242214532872}};
//    final WeightedObservedPoints[] rgbFitPoints = Curve_FIt_Calibration.initPoints(allChartValues);
//    final  double[] RCoefficients = Curve_FIt_Calibration.curveFit(rgbFitPoints[0]);
//    final  double[] GCoefficients = Curve_FIt_Calibration.curveFit(rgbFitPoints[1]);
//    final  double[] BCoefficients = Curve_FIt_Calibration.curveFit(rgbFitPoints[2]);    
//    
//    public Curve_Fit_CalibrationTest() {
//        
//    }
//    
//    @Test
//    public void calculateLUTTest() {
//       int[] LUTR = Curve_FIt_Calibration.calculateLUT(RCoefficients);
//       assertTrue(LUTR[224] > 200 && LUTR[224] < 240);
//       System.out.println(LUTR[224]);
//       int[] LUTG = Curve_FIt_Calibration.calculateLUT(GCoefficients);
//       assertTrue(LUTG[224] > 200 && LUTG[224] < 240);
//       System.out.println(LUTG[224]);
//       int[] LUTB = Curve_FIt_Calibration.calculateLUT(BCoefficients);
//       assertTrue(LUTB[224] > 200 && LUTB[224] < 240);
//       System.out.println(LUTB[224]);
//    }
//    
//    public static void main(String[] args) {
//        Curve_Fit_CalibrationTest test = new Curve_Fit_CalibrationTest();
//        
//        int[] LUTR = Curve_FIt_Calibration.calculateLUT(test.RCoefficients);
//        System.out.println(LUTR[224]);
//        int[] LUTG = Curve_FIt_Calibration.calculateLUT(test.GCoefficients);
//        System.out.println(LUTG[224]);
//        int[] LUTB = Curve_FIt_Calibration.calculateLUT(test.BCoefficients);
//        System.out.println(LUTB[224]);
//
//        PolynomialFunction functionB = new PolynomialFunction(test.RCoefficients);
//        System.out.println(functionB.value(224));
//    }
//}
