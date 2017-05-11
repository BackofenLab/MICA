package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.List;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestAnnotatedCurve {

	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	final double[] yOneMax = DoubleStream.of(1,2.5,4,4.5,5,5.1,5,4.5,4,2.5,1).toArray();
	final double[] yOneMaxOneMin = DoubleStream.of(1,2,3,2,3,4).toArray();

	final double precisionDelta = 0.0001;
	
	/**
	 * dummy filter without implementation for testing
	 */
	static class DummyFilter extends ObservableCurveAnnotationFilter {

		@Override
		public void filter(List<CurveAnnotation> annotations)
				throws NullArgumentException {
			// do nothing
		}

		@Override
		public String getDescription() {
			return "dummy filter without implementation";
		}
		
	}
	
	@Test
	public final void testConstruction() {
		
		AnnotatedCurve curve = null;
		// create object
		curve = new AnnotatedCurve("oneMax", yOneMax);
		// check
		Assert.assertNotNull(curve);
	}

	@Test
	public final void testGetAnnotation() {
		
		AnnotatedCurve curve = null;
		// create object
		curve = new AnnotatedCurve("oneMax", yOneMax);
		// check
		Assert.assertNotNull(curve);
		
		// check annotation length
		Assert.assertEquals( curve.getAnnotation().length, yOneMax.length);
		// check start/end annotation
		Assert.assertEquals( curve.getAnnotation()[0], CurveAnnotation.Type.IS_START );
		Assert.assertEquals( curve.getAnnotation()[yOneMax.length-1], CurveAnnotation.Type.IS_END );
	}

	@Test
	public final void testGetFilteredAnnotationsOneMax() {
		
		AnnotatedCurve curve = null;
		// create object
		curve = new AnnotatedCurve("oneMax", yOneMax);
		// check
		Assert.assertNotNull(curve);
		
		// check values (x,y,slope)
//		Debug.plotCurves("mpt", curve);
//		Debug.out.println("testGetFilteredAnnotationsOneMaxOneMin: "+Arrays.toString( curve.getFilteredAnnotations().toArray()));
		Assert.assertEquals( 3, curve.getFilteredAnnotations().size());
		
		// check start
		Assert.assertEquals( curve, curve.getFilteredAnnotations().get(0).getCurve() );
		Assert.assertEquals( 0, curve.getFilteredAnnotations().get(0).getIndex() );
		Assert.assertEquals( CurveAnnotation.Type.IS_START, curve.getFilteredAnnotations().get(0).getType() );
		
		// check maximum
		Assert.assertEquals( curve, curve.getFilteredAnnotations().get(curve.getFilteredAnnotations().size()/2).getCurve() );
		Assert.assertEquals( (int)FastMath.floor(yOneMax.length/2), curve.getFilteredAnnotations().get(curve.getFilteredAnnotations().size()/2).getIndex() );
		Assert.assertEquals( CurveAnnotation.Type.IS_MAXIMUM_AUTO, curve.getFilteredAnnotations().get(curve.getFilteredAnnotations().size()/2).getType());
		
		// check end
		Assert.assertEquals( curve, curve.getFilteredAnnotations().get(curve.getFilteredAnnotations().size()-1).getCurve() );
		Assert.assertEquals( yOneMax.length-1,curve.getFilteredAnnotations().get(curve.getFilteredAnnotations().size()-1).getIndex() );
		Assert.assertEquals( CurveAnnotation.Type.IS_END, curve.getFilteredAnnotations().get(curve.getFilteredAnnotations().size()-1).getType() );
	}
	
	@Test
	public final void testGetFilteredAnnotationsOneMaxOneMin() {
		
		AnnotatedCurve curve = null;
		// create object
		curve = new AnnotatedCurve("oneMaxOneMin", yOneMaxOneMin);
		// check
		Assert.assertNotNull(curve);
		
		// check annotation length
//		Debug.plotCurves("mpt", curve);
//		Debug.out.println("testGetFilteredAnnotationsOneMaxOneMin: "+Arrays.toString( curve.getFilteredAnnotations().toArray()));
		Assert.assertEquals( 4, curve.getFilteredAnnotations().size());
		
		// check start
		Assert.assertEquals( curve.getFilteredAnnotations().get(0).getCurve(), curve );
		Assert.assertEquals( curve.getFilteredAnnotations().get(0).getIndex(), 0 );
		Assert.assertEquals( CurveAnnotation.Type.IS_START, curve.getFilteredAnnotations().get(0).getType() );
		
		// check first inflection
		Assert.assertEquals( curve.getFilteredAnnotations().get(1).getCurve(), curve );
		Assert.assertEquals( 2, curve.getFilteredAnnotations().get(1).getIndex() );
		Assert.assertEquals( CurveAnnotation.Type.IS_MAXIMUM_AUTO, curve.getFilteredAnnotations().get(1).getType() );
		
		// check minimum
		Assert.assertEquals( curve.getFilteredAnnotations().get(2).getCurve(), curve );
		Assert.assertEquals( 3, curve.getFilteredAnnotations().get(2).getIndex() );
		Assert.assertEquals( CurveAnnotation.Type.IS_MINIMUM_AUTO, curve.getFilteredAnnotations().get(2).getType() );
		
		// check end
		Assert.assertEquals( curve.getFilteredAnnotations().get(3).getCurve(), curve );
		Assert.assertEquals( yOneMaxOneMin.length-1, curve.getFilteredAnnotations().get(3).getIndex() );
		Assert.assertEquals( CurveAnnotation.Type.IS_END, curve.getFilteredAnnotations().get(3).getType() );
	}

	
	@Test
	public final void testAddFilterNull() {
		thrown.expect(NullArgumentException.class);
		AnnotatedCurve curve = null;
		// create object
		curve = new AnnotatedCurve("oneMaxOneMin", yOneMaxOneMin);
		// check
		Assert.assertNotNull(curve);
		// add no filter
		curve.addAnnotationFilter(null);
	}
	
	
	@Test
	public final void testRemoveFilterNull() {
		thrown.expect(NullArgumentException.class);
		AnnotatedCurve curve = null;
		// create object
		curve = new AnnotatedCurve("oneMaxOneMin", yOneMaxOneMin);
		// check
		Assert.assertNotNull(curve);
		// add no filter
		curve.removeAnnotationFilter(null);
	}
	
	@Test
	public final void testRemoveFilter() {
		// create object
		AnnotatedCurve curve = new AnnotatedCurve("oneMaxOneMin", yOneMaxOneMin);
		// check
		Assert.assertNotNull(curve);
		// create filter
		DummyFilter filter = new DummyFilter();
		// check
		Assert.assertNotNull(filter);
		// check removal of non-existing filter
		Assert.assertEquals( curve.removeAnnotationFilter(filter), false);
		
		// add filter to be removed again
		curve.addAnnotationFilter(filter);
		Assert.assertEquals( curve.removeAnnotationFilter(filter), true);
		Assert.assertEquals( curve.removeAnnotationFilter(filter), false);
		
	}

}
