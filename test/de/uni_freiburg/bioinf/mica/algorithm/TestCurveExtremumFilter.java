package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.stream.DoubleStream;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestCurveExtremumFilter {

	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	final double[] yOneMax = DoubleStream.of(1,1.5,3,4,5,4,3,1.5,1).toArray();
	final double[] yOneMaxOneMin = DoubleStream.of(0.5,2,3,2.5,2,3,5).toArray();

	final double precisionDelta = 0.0001;
	
	
	@Test
	public final void testConstruction() {

		// create
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// check
		Assert.assertNotNull(filter);
	}

	@Test
	public final void testGetDescription() {
		
		// create
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// check
		Assert.assertNotNull(filter.getDescription());
		Assert.assertNotEquals(filter.getDescription(),"");
	}

	@Test
	public final void testFilter1() {
		
		// create curve data
		AnnotatedCurve curve = new AnnotatedCurve("minMax", yOneMaxOneMin);
		
//		Debug.plotCurves("mpt", curve);

		// get initial size of annotations
		int oldSize = curve.getFilteredAnnotations().size();
		
		// create filter for 10%
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// register filter
		curve.addAnnotationFilter( filter );

		// check filtering
		Assert.assertEquals( oldSize, curve.getFilteredAnnotations().size());
		
		// change filter value to 50% -> should trigger an update
		filter.setMinRelNeighDiff(0.5);
		// check filtering
		Assert.assertEquals( oldSize-3, curve.getFilteredAnnotations().size());

	}
	
	@Test
	public final void testFilter2() {
		
		// create curve data
		AnnotatedCurve curve = new AnnotatedCurve("minMax", yOneMax);
		
		// check annotation length (all other points are inflection due to spline)
//		for(CurveAnnotation a : curve.getFilteredAnnotations()) {
//			Debug.out.println("index="+String.valueOf(a.getIndex())+" type="+String.valueOf(a.getType()));
//		}
		
		// get initial size of annotations
		int oldSize = curve.getFilteredAnnotations().size();
		
		// create filter for 10%
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// register filter
		curve.addAnnotationFilter( filter );
		// check filtering
		Assert.assertEquals( curve.getFilteredAnnotations().size(), oldSize);
		
		// change filter value to 50%
		filter.setMinRelNeighDiff(0.5);
		// update filter to trigger recomputation of filtered annotations
		curve.removeAnnotationFilter(filter);
		curve.addAnnotationFilter(filter);
		// check filtering
		Assert.assertEquals( curve.getFilteredAnnotations().size(), oldSize);
		
		// change filter value to 100%
		filter.setMinRelNeighDiff(1.0);
		// update filter to trigger recomputation of filtered annotations
		curve.removeAnnotationFilter(filter);
		curve.addAnnotationFilter(filter);
		// check filtering
		Assert.assertEquals( curve.getFilteredAnnotations().size(), oldSize);
		
	}
	
	@Test
	public final void testGetMinRelNeighDiff() {
		
		// create
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// check
		Assert.assertEquals(filter.getMinRelNeighDiff(), 0.1, precisionDelta);
	}
	
	@Test
	public final void testSetMinRelNeighDiff() {
		
		// create
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// call function to test
		filter.setMinRelNeighDiff(0.2);
		// check
		Assert.assertEquals(filter.getMinRelNeighDiff(), 0.2, precisionDelta);
		
	}
	
	@Test
	public final void testSetMinRelNeighDiffTooLow() {
		
		// create
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// call function to test
		thrown.expect( OutOfRangeException.class );
		filter.setMinRelNeighDiff(-0.2);
	}
		
	@Test
	public final void testSetMinRelNeighDiffTooHigh() {
		
		// create
		CurveExtremaFilter filter = new CurveExtremaFilter(0.1);
		// call function to test
		thrown.expect( OutOfRangeException.class );
		filter.setMinRelNeighDiff(2.0);
	}

}
