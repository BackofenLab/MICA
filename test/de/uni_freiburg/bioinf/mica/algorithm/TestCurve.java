package de.uni_freiburg.bioinf.mica.algorithm;
import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.exception.NullArgumentException;
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
public class TestCurve {
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double[] yIncrease = IntStream.rangeClosed(1,10).asDoubleStream().toArray();
	final double[] yOneMax = IntStream.of(1,2,3,4,5,6,5,4,3,2,1).asDoubleStream().toArray();
	
	
	final double precisionDelta = 0.0001;
	
	/**
	 * testing all members for a given curve object and initial data
	 * @param curve the object to test
	 * @param name the name used for construction
	 * @param yData the y data used for construction
	 */
	final void testCurveMembers( Curve curve, String name, double[] yData ) {
		
		// get min/max
		double yMin = DoubleStream.of(yData).min().getAsDouble();
		double yMax = DoubleStream.of(yData).max().getAsDouble();
		
		// check name
		Assert.assertEquals( curve.getName(), name );
		// check Y coordinates
		Assert.assertEquals( curve.size(), yData.length );
		Assert.assertTrue( Arrays.equals(curve.getY(), yData) );
		Assert.assertEquals( curve.getYmin(), yMin, precisionDelta );
		Assert.assertEquals( curve.getYmax(), yMax, precisionDelta );
		// check X coordinates
		Assert.assertEquals( curve.length(), (double)(yData.length-1), precisionDelta );
		Assert.assertEquals( curve.getX().length, yData.length );
		Assert.assertEquals( curve.getXmin(), 0, precisionDelta );
		Assert.assertEquals( curve.getX()[0], 0, precisionDelta );
		Assert.assertEquals( curve.getXmax(), (double)(yData.length-1), precisionDelta );
		Assert.assertEquals( curve.getX()[yData.length-1], yData.length-1, precisionDelta );
		Assert.assertTrue( ArrayUtils.isSorted( curve.getX() ) );
		// check slope
		Assert.assertEquals( curve.getSlope().length, yData.length );
	}
			
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArray() {
		
		Curve curve = null;
		
		curve = new Curve("straight", yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		// test members
		testCurveMembers(curve, "straight", yIncrease);
		
		curve = new Curve("oneMax", yOneMax);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		// test members
		testCurveMembers(curve, "oneMax", yOneMax);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayNull1() {
		thrown.expect( NullArgumentException.class );
		new Curve(null,null);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayNull2() {
		thrown.expect( NullArgumentException.class );
		new Curve(null,yIncrease);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayNull3() {
		thrown.expect( NullArgumentException.class );
		new Curve("bla",null);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayEmptyName() {
		thrown.expect( IllegalArgumentException.class );
		new Curve("",yIncrease);
	}

	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayNull1() {
		thrown.expect( NullArgumentException.class );
		new Curve(null,null,null);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayNull2() {
		thrown.expect( NullArgumentException.class );
		new Curve(null,null,yIncrease);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayNull3() {
		thrown.expect( NullArgumentException.class );
		new Curve("bla",null,null);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayNull4() {
		thrown.expect( NullArgumentException.class );
		new Curve(null,DoubleStream.of(2,3,4,5,6).toArray(),null);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayNull5() {
		thrown.expect( NullArgumentException.class );
		new Curve("bla",DoubleStream.of(2,3,4,5,6).toArray(),null);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayNull6() {
		thrown.expect( IllegalArgumentException.class );
		new Curve(null,DoubleStream.of(2,3,4,5,6).toArray(),yIncrease);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArrayDiffLength() {
		thrown.expect( IllegalArgumentException.class );
		new Curve("bla",DoubleStream.of(2,3,4,5,6).toArray(),yIncrease);
	}
	
	/**
	 * Test method for {@link Curve#Curve(java.lang.String, double[], double[])}.
	 */
	@Test
	public final void testCurveStringDoubleArrayDoubleArray() {
		Curve curve = new Curve("bla",IntStream.range(5, yIncrease.length+5).asDoubleStream().toArray(),yIncrease);
		
		Assert.assertEquals(curve.getXmin(), 5, precisionDelta);
		Assert.assertEquals(curve.length(), (double)yIncrease.length-1, precisionDelta);
	}
	
	/**
	 * Test method for {@link Curve#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetName() {
		String name = "bla";
		Curve curve = new Curve(name, yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		// check name
		Assert.assertEquals( curve.getName(), name);
		// ensure different objects
		Assert.assertFalse( curve.getName() == name );
		// change name
		name += "-2";
		curve.setName(name);
		// check name
		Assert.assertEquals( curve.getName(), name);
		// ensure different objects
		Assert.assertFalse( curve.getName() == name );
	}
	
	/**
	 * Test method for {@link Curve#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetNameNull() {
		String name = "bla";
		Curve curve = new Curve(name, yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		// check name
		Assert.assertEquals( curve.getName(), name);
		// change name
		thrown.expect(NullArgumentException.class);
		curve.setName(null);
	}
	
	/**
	 * Test method for {@link Curve#setName(java.lang.String)}.
	 */
	@Test
	public final void testSetNameEmpty() {
		String name = "bla";
		Curve curve = new Curve(name, yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		// check name
		Assert.assertEquals( curve.getName(), name);
		// change name
		thrown.expect(IllegalArgumentException.class);
		curve.setName("");
	}

	/**
	 * Test method for {@link Curve#getY(double)}.
	 */
	@Test
	public final void testGetYDouble() {
		
		Curve curve = null;
		
		curve = new Curve("straight", yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		
		// check interpolated value
		Assert.assertEquals( curve.getY(0.5), 1.5, precisionDelta );
	}

	/**
	 * Test method for {@link Curve#getSlope(double)}.
	 */
	@Test
	public final void testGetDerivative() {
		
		Curve curve = null;
		
		//////////////  STRAIGHT LINE  ////////////////
		
		curve = new Curve("straight", yIncrease);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		
		// check the slope in the middle of the curve is positive
		Assert.assertTrue( curve.getSlope(yIncrease.length/2) > 0);
		
		
		//////////////  ONE MAXIMUM  ////////////////
		
		curve = new Curve("oneMax", yOneMax);
		// ensure construction went fine
		Assert.assertNotNull(curve);
		
		// check the slope at the beginning of the curve is positive
		Assert.assertTrue( curve.getSlope(yIncrease.length/4) > 0);
//		// check the slope in the middle of the curve is 0 since it is a maximum
//		Assert.assertEquals( curve.getSlope((yOneMax.length-1)/2), 0, precisionDelta);
		// check the slope at the end of the curve is negative
		Assert.assertTrue( curve.getSlope((3*yIncrease.length)/4) < 0);
		
	}


}
