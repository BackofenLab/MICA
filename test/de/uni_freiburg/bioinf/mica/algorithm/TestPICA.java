package de.uni_freiburg.bioinf.mica.algorithm;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;
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
public class TestPICA {
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double precisionDelta = 0.0001;
	
	
	
	@Test
	public final void testNull() {
		thrown.expect( NullArgumentException.class );
		new PICA(null, 1, 1, 0);
	}
	
	@Test
	public final void testMinLengthToSmall() {
		thrown.expect( OutOfRangeException.class );
		new PICA(new CurveRmsdDistance(10), 0, 1, 0);
	}
	
	@Test
	public final void testConstruction() {
		Assert.assertNotNull(new PICA(new CurveRmsdDistance(10), 1, 1, 0));
		
	}

			
	@Test
	public final void testAlign() {
		
		final double[] yOneMax = DoubleStream.of(1, 1.5, 3, 4, 5, 3, 1.5, 1).toArray();
//		final double[] yOneMaxOneMin = DoubleStream.of(0.5,2,3,2.5,2,3,5).toArray();

		final double precisionDelta = 0.0001;
		
//		0 1 2 3 4 5 6 7 8 9 0 1 2 3
//	1	    x   x   x   x   A x x x   = 9/11
//	2	x x x x A   x   x   x       = 4/10
//	c1	  x x x x A x x x           = 11
//	c2	x x x x A x x x             = 11
		
		final AnnotatedCurve curveOneMax1 = new AnnotatedCurve("oneMax1", IntStream.of(2,4,6,8,10,11,12,13).asDoubleStream().toArray(), yOneMax);
		final AnnotatedCurve curveOneMax2 = new AnnotatedCurve("oneMax2", IntStream.of(0,1,2,3,4,5,6,7).asDoubleStream().toArray(), yOneMax);
		
		
		// create initial decomposition
		IntervalDecomposition dec1 = new IntervalDecomposition( curveOneMax1 );
		IntervalDecomposition dec2 = new IntervalDecomposition( curveOneMax2 );
		// create copies for comparison
//		IntervalDecomposition dec1orig = new IntervalDecomposition(dec1);
//		IntervalDecomposition dec2orig = new IntervalDecomposition(dec2);

		// create distance function
		SampledCurveDistance distance = new CurveRmsdDistance(20);
		// setup pairwise aligner
		PICA pica = new PICA( distance, 10, 1, 0);
		
//		Debug.out.println(dec1.getCurve());
//		Debug.out.println(Arrays.toString(dec1.getCurveOriginal().getFilteredAnnotations().toArray()));
//		Debug.out.println();
//		Debug.out.println(dec2.getCurve());
//		Debug.out.println(Arrays.toString(dec2.getCurveOriginal().getFilteredAnnotations().toArray()));
//		Debug.out.println();
		
		// compute alignment
		PICA.PicaData alignment = pica.align(dec1, 1, dec2, 1);
//		Debug.plotCurves("pica-test", alignment.dec1.getCurve(), alignment.dec2.getCurve(), alignment.dec1.getCurveOriginal(), alignment.dec2.getCurveOriginal());
//		Debug.out.println(alignment.dec1.getCurve());
//		Debug.out.println(alignment.dec2.getCurve());
		Assert.assertEquals( 0,	alignment.distance, precisionDelta );
		
//
//		Debug.out.println( MICA.getConsensusCurve( Arrays.asList( alignment.dec1, alignment.dec2 ) ).getCurve() );
		
	}
	
	
	
	@Test
	public final void testAlignToReference() {
		
		final double[] yOneMax = DoubleStream.of(1, 1.5, 3, 4, 5, 3, 1.5, 1).toArray();
//		final double[] yOneMaxOneMin = DoubleStream.of(0.5,2,3,2.5,2,3,5).toArray();
		
		final double precisionDelta = 0.0001;
		
//		0 1 2 3 4 5 6 7 8 9 0 1 2 3
//	1	    x   x   x   x   A x x x   = 9/11
//	2	x x x x A   x   x   x       = 4/10
//	c2	x   x   x   x   A x x x             = 11
		
		final AnnotatedCurve curveOneMax1 = new AnnotatedCurve("oneMax1", IntStream.of(2,4,6,8,10,11,12,13).asDoubleStream().toArray(), yOneMax);
		final AnnotatedCurve curveOneMax2 = new AnnotatedCurve("oneMax2", IntStream.of(0,1,2,3,4,5,6,7).asDoubleStream().toArray(), yOneMax);
		
		
		// create initial decomposition
		IntervalDecomposition dec1 = new IntervalDecomposition( curveOneMax1 );
		IntervalDecomposition dec2 = new IntervalDecomposition( curveOneMax2 );
		// create copies for comparison
//		IntervalDecomposition dec1orig = new IntervalDecomposition(dec1);
//		IntervalDecomposition dec2orig = new IntervalDecomposition(dec2);
		
		// create distance function
		SampledCurveDistance distance = new CurveRmsdDistance(10);
		// setup pairwise aligner
		PICA pica = new PICA( distance, 2, 1, 0);
		
//		Debug.out.println(dec1.getCurve());
//		Debug.out.println(dec2.getCurve());
		
		// compute alignment
		PICA.PicaData alignment = pica.align(dec1, 1, dec2, 1);
		Assert.assertEquals( 0,	alignment.distance, precisionDelta );
		
//		Debug.out.println(alignment.dec1.getCurve().toString(2)+"\n"); 
//		Debug.out.println(alignment.dec2.getCurve().toString(2)+"\n");
//		Debug.out.println(alignment.dec1.getCurveOriginal().toString(2)+"\n");
//		Debug.out.println(alignment.dec2.getCurveOriginal().toString(2)+"\n");
//		Debug.out.println( MICA.getConsensusCurve( Arrays.asList( alignment.dec1, alignment.dec2 ) ).getCurveOriginal()+"\n");
		
	}
	

}
