package de.uni_freiburg.bioinf.mica.algorithm;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.Assert;
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
public class TestCurveMeanAbsoluteDistance {
	
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
		new CurveMeanAbsoluteDistance(1);
	}

			
	@Test
	public final void testCurveAreaDistance() {
		
		Curve curve1 = new Curve("ones", yOne);
		// ensure construction went fine
		Assert.assertNotNull(curve1);
		Curve curve2 = new Curve("twos", yTwo);
		// ensure construction went fine
		Assert.assertNotNull(curve2);
		Curve curve3 = new Curve("increase", yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve3);
		
		CurveMeanAbsoluteDistance dist = new CurveMeanAbsoluteDistance(2);
		
		// test diff on first and last only
		Assert.assertEquals( 0, dist.getDistance(curve1, curve1), precisionDelta);
		Assert.assertEquals( 0, dist.getDistance(curve2, curve2), precisionDelta);
		Assert.assertEquals( 1, dist.getDistance(curve1, curve2), precisionDelta);
		Assert.assertEquals( 4.5, dist.getDistance(curve1, curve3), precisionDelta);
		
		// increase number of interpolation points but still use original points
		dist.setSampleNumber(4);
		Assert.assertEquals( 0, dist.getDistance(curve1, curve1), precisionDelta);
		Assert.assertEquals( 0, dist.getDistance(curve2, curve2), precisionDelta);
		Assert.assertEquals( 1, dist.getDistance(curve1, curve2), precisionDelta);
		Assert.assertEquals( 4.5, dist.getDistance(curve1, curve3), precisionDelta);
		
		// set number of interpolation points to spline-based points
		dist.setSampleNumber(3);
		Assert.assertEquals( 0, dist.getDistance(curve1, curve1), precisionDelta);
		Assert.assertEquals( 0, dist.getDistance(curve2, curve2), precisionDelta);
		Assert.assertEquals( 1, dist.getDistance(curve1, curve2), precisionDelta);
		Assert.assertEquals( 4.5, dist.getDistance(curve1, curve3), precisionDelta);
	}
	
	
	@Test
	public void testGetSamplePoints() {
		
		double[] x = IntStream.range(0, 16).asDoubleStream().toArray();
		double[] y = x;
		Curve curve = new Curve("test",x,y);
		
		CurveMeanAbsoluteDistance dist = new CurveMeanAbsoluteDistance(16);
		Assert.assertEquals( 1, dist.getStepSize(curve), precisionDelta );
		
		double[] s = dist.getSamplePositions(curve, 3, 12, 4, 0, true);
		Assert.assertEquals( 4, s.length );
		Assert.assertEquals( 3d, s[0], precisionDelta );
		Assert.assertEquals( 7.5, s[2], precisionDelta );
//		Debug.out.println(Arrays.toString(s));
		
		s = dist.getSamplePositions(curve, 3, 5, 4, 0, false);
		Assert.assertEquals( 3, s.length );
		Assert.assertEquals( 4d, s[1], precisionDelta );
//		Debug.out.println(Arrays.toString(s));
		
		s = dist.getSamplePositions(curve, 5, 12, 5, +2, false);
		Assert.assertEquals( 4, s.length );
//		Debug.out.println(Arrays.toString(s));
		Assert.assertEquals( 6.4, s[0], precisionDelta );
		Assert.assertEquals( 10.6, s[3], precisionDelta );
	}
	
	
	@Test
	public void testGetSamplePointsInterval() {
		
		double[] x = IntStream.range(0, 16).asDoubleStream().toArray();
		double[] y = x;
		Curve curve = new Curve("test",x,y);
		
		CurveMeanAbsoluteDistance dist = new CurveMeanAbsoluteDistance(16);
		Assert.assertEquals( 1, dist.getStepSize(curve), precisionDelta );
		
		double[] s = dist.getSamplePositions(curve, 3, 12);
//		Debug.out.println(Arrays.toString(s));
		Assert.assertEquals( 10, s.length );
		Assert.assertEquals( 3d, s[0], precisionDelta );
		Assert.assertEquals( 12d, s[9], precisionDelta );
		
	}

	
}
