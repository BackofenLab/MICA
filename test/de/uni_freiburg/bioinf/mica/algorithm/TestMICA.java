package de.uni_freiburg.bioinf.mica.algorithm;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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
public class TestMICA {
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double precisionDelta = 0.0001;
	
	

			
	@Test
	public final void testAlign() {
		
		final double[] yOneMax1 = DoubleStream.of(1, 1.5, 3, 4, 5, 3, 1.5, 1).toArray();
		final double[] yOneMax2 = DoubleStream.of(yOneMax1).map(d -> d-3).toArray();
		final double[] yOneMax3 = DoubleStream.of(yOneMax1).map(d -> d*1.1).toArray();

		final double precisionDelta = 0.0001;
		
//		0 1 2 3 4 5 6 7 8 9 0 1 2 3
//	1	    x   x   x   x   A x x x   = 9/11
//	2	x x x x A   x   x   x       = 4/10
//	c1	  x x x x A x x x           = 11
//	c2	x x x x A x x x             = 11
		
		final AnnotatedCurve curveOneMax1 = new AnnotatedCurve("oneMax1", IntStream.of(2,4,6,8,10,11,12,13).asDoubleStream().toArray(), yOneMax1);
		final AnnotatedCurve curveOneMax2 = new AnnotatedCurve("oneMax2", IntStream.of(0,1,2,3,4,5,6,7).asDoubleStream().toArray(), yOneMax2);
//		final AnnotatedCurve curveOneMax3 = new AnnotatedCurve("oneMax3", DoubleStream.of(0,1,2,3,4,5,6,7).toArray(), yOneMax3);
		final AnnotatedCurve curveOneMax3 = new AnnotatedCurve("oneMax3", DoubleStream.of(0,0.1,0.3,0.6,1,3,5,7).toArray(), yOneMax3);
		
		
		// create initial decomposition
		IntervalDecomposition dec1 = new IntervalDecomposition( curveOneMax1 );
		IntervalDecomposition dec2 = new IntervalDecomposition( curveOneMax2 );
		IntervalDecomposition dec3 = new IntervalDecomposition( curveOneMax3 );
		// create copies for comparison
//		IntervalDecomposition dec1orig = new IntervalDecomposition(dec1);
//		IntervalDecomposition dec2orig = new IntervalDecomposition(dec2);

		// create distance function
//		SampledCurveDistance distance = new CurveRmsdDistance(10);
		SampledCurveDistance distance = new SlopeRmsdDistance(10);
		// setup pairwise aligner
		MICA mica = new MICA( distance, 10, 1, 0);
		
//		Debug.out.println(dec1.getCurve());
//		Debug.out.println(dec2.getCurve());
//		Debug.out.println(dec3.getCurve());
		
//		Debug.plotCurves(
//						"before alignment"
//						, dec1.getCurve()
//						, dec3.getCurve() 
//						, dec2.getCurve()
//						);
		
		// compute alignment
//		MICA.MicaData alignment = mica.align(dec1, dec3);
		MICA.MicaData alignment = mica.align(dec1, dec2, dec3);
//		Assert.assertEquals( 0,	alignment.distance, precisionDelta );
		
//		Debug.plotCurves(
//						alignment.consensus.getCurve().getName()
//						, dec1.getCurve(), alignment.curves.get(0).getCurve()
//						, dec3.getCurve(), alignment.curves.get(1).getCurve()
//						, dec2.getCurve(), alignment.curves.get(2).getCurve()
//						, alignment.consensus.getCurve()
//						);
//		Debug.plotCurves(
//						alignment.consensus.getCurve().getName()
//						, dec1.getCurve(), alignment.curves.get(0).getCurve()
//						, dec3.getCurve(), alignment.curves.get(1).getCurve()
//						, dec2.getCurve(), alignment.curves.get(2).getCurve()
//						, alignment.consensus.getCurve()
//						);

//		Debug.out.println(alignment.getGuideTree());
		
		
		

//		
//		for (MICA.MicaData child : alignment.fusedAlignments) {
//			Debug.out.println(child.consensus.getCurveOriginal().toString(2));
//		}

//		Debug.out.println( MICA.getConsensusCurve( Arrays.asList( alignment.dec1, alignment.dec2 ) ).getCurve() );
		
	}
	
	
	
	@Test
	public final void testSplitAlign() {
		
		final double[] yOneMax1 = DoubleStream.of(1, 1.5, 3, 4, 5, 3, 1.5, 1).toArray();
		final double[] yOneMax2 = DoubleStream.of(yOneMax1).map(d -> d-3).toArray();
		final double[] yOneMax3 = DoubleStream.of(yOneMax1).map(d -> d*1.1).toArray();
		
		final double precisionDelta = 0.0001;
		
//		0 1 2 3 4 5 6 7 8 9 0 1 2 3
//	1	    x   x   x   x   A x x x   = 9/11
//	2	x x x x A   x   x   x       = 4/10
//	c1	  x x x x A x x x           = 11
//	c2	x x x x A x x x             = 11
		
		final CurveAnnotation.Type[] annotation = new CurveAnnotation.Type[]{
				CurveAnnotation.Type.IS_START,
				CurveAnnotation.Type.IS_POINT,
				CurveAnnotation.Type.IS_POINT,
				CurveAnnotation.Type.IS_POINT,
				CurveAnnotation.Type.IS_MAXIMUM_MAN,
				CurveAnnotation.Type.IS_POINT,
				CurveAnnotation.Type.IS_POINT,
				CurveAnnotation.Type.IS_END
		};
		
		final AnnotatedCurve curveOneMax1 = new AnnotatedCurve("oneMax1", IntStream.of(2,4,6,8,10,11,12,13).asDoubleStream().toArray(), yOneMax1, annotation);
		final AnnotatedCurve curveOneMax2 = new AnnotatedCurve("oneMax2", IntStream.of(0,1,2,3,4,5,6,7).asDoubleStream().toArray(), yOneMax2, annotation);
//		final AnnotatedCurve curveOneMax3 = new AnnotatedCurve("oneMax3", DoubleStream.of(0,1,2,3,4,5,6,7).toArray(), yOneMax3);
		final AnnotatedCurve curveOneMax3 = new AnnotatedCurve("oneMax3", DoubleStream.of(0,0.1,0.3,0.6,1,3,5,7).toArray(), yOneMax3, annotation);
		
		
		curveOneMax1.getAnnotation()[2] = CurveAnnotation.Type.IS_SPLIT;
		curveOneMax2.getAnnotation()[5] = CurveAnnotation.Type.IS_SPLIT;
		curveOneMax3.getAnnotation()[3] = CurveAnnotation.Type.IS_SPLIT;
		
		// create initial decomposition
		IntervalDecomposition dec1 = new IntervalDecomposition( curveOneMax1 );
		IntervalDecomposition dec2 = new IntervalDecomposition( curveOneMax2 );
		IntervalDecomposition dec3 = new IntervalDecomposition( curveOneMax3 );
		// create copies for comparison
//		IntervalDecomposition dec1orig = new IntervalDecomposition(dec1);
//		IntervalDecomposition dec2orig = new IntervalDecomposition(dec2);
		
		// create distance function
//		SampledCurveDistance distance = new CurveRmsdDistance(10);
		SampledCurveDistance distance = new SlopeRmsdDistance(10);
		// setup pairwise aligner
		MICA mica = new MICA( distance, 10, 1, 0);
		
//		Debug.out.println(dec1.getCurve());
//		Debug.out.println(dec2.getCurve());
//		Debug.out.println(dec3.getCurve());
		
//		Debug.plotCurves(
//						"before alignment"
//						, dec1.getCurve()
//						, dec3.getCurve() 
//						, dec2.getCurve()
//						);
		
		// compute alignment
//		MICA.MicaData alignment = mica.align(dec1, dec3);
		MICA.MicaData alignment = mica.align(dec1, dec2, dec3);
//		Assert.assertEquals( 0,	alignment.distance, precisionDelta );
		
//		Debug.plotCurves(
//						alignment.consensus.getCurve().getName()
//						, dec1.getCurve(), alignment.curves.get(0).getCurve()
//						, dec3.getCurve(), alignment.curves.get(1).getCurve()
//						, dec2.getCurve(), alignment.curves.get(2).getCurve()
//						, alignment.consensus.getCurve()
//						);
//		Debug.plotCurves(
//						alignment.consensus.getCurve().getName()
//						, dec1.getCurve(), alignment.curves.get(0).getCurve()
//						, dec3.getCurve(), alignment.curves.get(1).getCurve()
//						, dec2.getCurve(), alignment.curves.get(2).getCurve()
//						, alignment.consensus.getCurve()
//						);
		
//		Debug.out.println(alignment.getGuideTree());
		
		
		
		
//		
//		for (MICA.MicaData child : alignment.fusedAlignments) {
//			Debug.out.println(child.consensus.getCurveOriginal().toString(2));
//		}
		
//		Debug.out.println( MICA.getConsensusCurve( Arrays.asList( alignment.dec1, alignment.dec2 ) ).getCurve() );
		
	}
	
	

}
