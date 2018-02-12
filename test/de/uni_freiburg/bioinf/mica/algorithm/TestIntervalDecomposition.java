package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation.Type;

public class TestIntervalDecomposition {

	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	final double[] yOneMax = DoubleStream.of(1,1.5,3,4,5,3,1.5).toArray();
	final double[] yOneMaxOneMin = DoubleStream.of(0.5,2,3,2.5,2,3,5).toArray();

	final double precisionDelta = 0.0001;
	
	
	final AnnotatedCurve curveOneMax = new AnnotatedCurve("oneMax", IntStream.range(3, yOneMax.length+3).asDoubleStream().toArray(), yOneMax);
	
	
	@Test
	public final void testConstruction() {

		// create
		IntervalDecomposition dec = new IntervalDecomposition( curveOneMax );
		// check
		Assert.assertNotNull(dec);
		Assert.assertEquals(dec.getCurveOriginal(), curveOneMax);
		Assert.assertEquals(dec.getCurve().size(), curveOneMax.size());
		Assert.assertEquals(1, dec.size());
		Assert.assertEquals(curveOneMax.size(), dec.getIntervalSize(0));
		Assert.assertEquals(CurveAnnotation.Type.IS_START, dec.getIntervalStart(0).getType());
		Assert.assertEquals(CurveAnnotation.Type.IS_END, dec.getIntervalEnd(0).getType());
		Assert.assertEquals((double)yOneMax.length-1, dec.getIntervalLength(0), precisionDelta);
	}
	
	@Test
	public final void testDecompose() {
		
		AnnotatedCurve curve = new AnnotatedCurve("dummy", curveOneMax.getX(), curveOneMax.getY());
		curve.getAnnotation()[4] = Type.IS_MAXIMUM_MAN;
		curve.getAnnotation()[5] = Type.IS_INFLECTION_DESCENDING_MAN;
		
		// create
		IntervalDecomposition dec = new IntervalDecomposition( curve );
		
//		Debug.plotCurves("curveOneMax", dec);
//		Debug.out.println("TestIntervalDecomposition.testDecompose: "+Arrays.toString(curve.getY()));
//		Debug.out.println("TestIntervalDecomposition.testDecompose: "+Arrays.toString(curve.getAnnotation()));
		
		List<CurveAnnotation> curAnn = curve.getFilteredAnnotations();
//		Debug.out.println("TestIntervalDecomposition.testDecompose: "+Arrays.toString(curAnn.toArray()));
		Assert.assertEquals( 5, curAnn.size());
		Assert.assertEquals( 1, dec.size());
		
//		Debug.out.println("TestIntervalDecomposition.testDecompose: "+Arrays.toString(dec.getCurve().getX()));
		dec.decompose(0, curAnn.get(2), 0.33333);
//		Debug.out.println("TestIntervalDecomposition.testDecompose: "+Arrays.toString(dec.getCurve().getX()));
		
		Assert.assertEquals( 2, dec.size());
		Assert.assertEquals( curve.length()*0.33333, dec.getIntervalLength(0), precisionDelta);
		Assert.assertEquals( curve.length()*(1-0.33333), dec.getIntervalLength(1), precisionDelta);
		Assert.assertEquals( Type.IS_MAXIMUM_MAN, dec.getIntervalEnd(0).getType());
		Assert.assertEquals( 5, dec.getCurve().getX()[4], 0.01);
		Assert.assertEquals( 7, dec.getCurve().getX()[5], 0.01);

	}
}
