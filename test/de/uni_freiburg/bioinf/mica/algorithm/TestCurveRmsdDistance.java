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
public class TestCurveRmsdDistance {
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double[] yOne = IntStream.range(0, 10).map(i -> 1).asDoubleStream().toArray();
	final double[] yTwo = IntStream.range(0, 10).map(i -> 2).asDoubleStream().toArray();
	
	
	final double precisionDelta = 0.0001;
	
	
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testTooFew() {
		thrown.expect( OutOfRangeException.class );
		new CurveRmsdDistance(1);
	}

			
	@Test
	public final void testCurveRmsdDistance() {
		
		Curve curve1 = new Curve("ones", yOne);
		// ensure construction went fine
		Assert.assertNotNull(curve1);
		Curve curve2 = new Curve("twos", yTwo);
		// ensure construction went fine
		Assert.assertNotNull(curve2);
		
		CurveRmsdDistance dist = new CurveRmsdDistance(2);
		
		// test diff on first and last only
		Assert.assertEquals( 0, dist.getDistance(curve1, curve1), precisionDelta);
		Assert.assertEquals( 0, dist.getDistance(curve2, curve2), precisionDelta);
		Assert.assertEquals( 1, dist.getDistance(curve1, curve2), precisionDelta);
		
		// increase number of interpolation points but still use original points
		dist.setSampleNumber(4);
		Assert.assertEquals( 0, dist.getDistance(curve1, curve1), precisionDelta);
		Assert.assertEquals( 0, dist.getDistance(curve2, curve2), precisionDelta);
		Assert.assertEquals( 1, dist.getDistance(curve1, curve2), precisionDelta);
		
		// set number of interpolation points to spline-based points
		dist.setSampleNumber(3);
		Assert.assertEquals( 0, dist.getDistance(curve1, curve1), precisionDelta);
		Assert.assertEquals( 0, dist.getDistance(curve2, curve2), precisionDelta);
		Assert.assertEquals( 1, dist.getDistance(curve1, curve2), precisionDelta);
	}
	

}
