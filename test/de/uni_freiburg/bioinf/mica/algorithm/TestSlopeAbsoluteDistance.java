package de.uni_freiburg.bioinf.mica.algorithm;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * 
 */

/**
 * @author Mmann
 *
 */
public class TestSlopeAbsoluteDistance {
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double[] yIncrease = IntStream.rangeClosed(1,10).asDoubleStream().toArray();
	final double[] yOne = IntStream.range(0, 10).map(i -> 1).asDoubleStream().toArray();
	final double[] yTwo = IntStream.range(0, 10).map(i -> 2).asDoubleStream().toArray();
	
	
	final double precisionDelta = 0.0001;
	
	
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testTooFew() {
		thrown.expect( OutOfRangeException.class );
		new SlopeMeanAbsoluteDistance(1);
	}

	
//	@Test
//	public void testIntervalDistanceDecomp() {
//		
//		double[] yOneMax  = DoubleStream.of(1,1.5,3,4,5,3,1.5).toArray();
//		double[] xOneMax1 = DoubleStream.of(1,2,3,4,5,6,7).toArray();
//		double[] xOneMax2 = DoubleStream.of(1,1.2,1.3,1.5,2,4,7).toArray();
//		AnnotatedCurve curveOneMax1 = new AnnotatedCurve("oneMax1", xOneMax1, yOneMax);
//		AnnotatedCurve curveOneMax2 = new AnnotatedCurve("oneMax2", xOneMax2, yOneMax);
//
//		IntervalDecomposition dec1 = new IntervalDecomposition( curveOneMax1 );
//		IntervalDecomposition dec2 = new IntervalDecomposition( curveOneMax2 );
//		
////		Debug.out.println(dec1.getIntervalAnnotations(0));
////		Debug.out.println(dec2.getIntervalAnnotations(0));
//		
//		// create alignment
//		IntervalDecomposition alg1 = new IntervalDecomposition( dec1 ); 
//		IntervalDecomposition alg2 = new IntervalDecomposition( dec2 );
//		// setup alignment data
//		double newRelPos = 0.5;
//		CurveAnnotation split1 = dec1.getIntervalAnnotations(0).get(0);
//		CurveAnnotation split2 = dec2.getIntervalAnnotations(0).get(1);
//		// do decomposition
//		alg1.decompose(0, split1, newRelPos);
//		alg2.decompose(0, split2, newRelPos);
//		
////		Debug.plotCurves("bl", dec1,dec2,alg1,alg2);
//
//		
////		Debug.out.println(dec1.getCurve());
////		Debug.out.println(dec2.getCurve());
//		SlopeMeanAbsoluteDistance dist = new SlopeMeanAbsoluteDistance(20);
//		
//		// compute distance after alignment
//		double decompDist = dist.getDistance( alg1.getCurve(), alg2.getCurve());
//		// compute distance based on fragments without alignment
//		double leftDist  = dist.getDistance( dec1.getCurve(), dec2.getCurve(), 0, split1.getIndex(), 0, 0, split2.getIndex(), 0, dec1.getIntervalLength(0)*newRelPos, false);
//		double xShift1 = (dec1.getCurve().getXmin()+(dec1.getIntervalLength(0)*newRelPos)) - dec1.getCurve().getX()[split1.getIndex()];
//		double xShift2 = (dec2.getCurve().getXmin()+(dec2.getIntervalLength(0)*newRelPos)) - dec2.getCurve().getX()[split2.getIndex()];
//		double rightDist = dist.getDistance( dec1.getCurve(), dec2.getCurve(), split1.getIndex(), xOneMax1.length-1, xShift1, split2.getIndex(), xOneMax2.length-1, xShift2, dec1.getIntervalLength(0)*newRelPos, true);
//		
//		Assert.assertEquals( decompDist, leftDist+rightDist, precisionDelta);
//		
//	}
	
}
