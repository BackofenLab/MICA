package de.uni_freiburg.bioinf.mica.controller;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestMicaR {

	
	
	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	final double precisionDelta = 0.0001;
	

			
	@Test
	public final void testConstruction() {
		
		MicaR micaR = new MicaR( 1, 1000, 10d, 1d, 0,  0.1d, 1, 0 );
		
	}
	
	@Test
	public final void testGetAnnotations() {
		
		double[] y = DoubleStream.of(5.14,4.40,3.94,3.65,3.58,3.64,3.73,3.79,3.84,3.85,3.82,3.79,3.76,3.74,3.73,3.72,3.72,3.73,3.76,3.79,3.82,3.85,3.87,3.89,3.90,3.89,3.88,3.87,3.87,3.91,3.98,4.07,4.13,4.18,4.20,4.24,4.27,4.32,4.37).toArray();
		double[] x = IntStream.range(0, y.length).asDoubleStream().toArray();
		
		MicaR micaR = null;
		
		micaR = new MicaR( 1, 1000, 10d, 1d, 0d, 0d, 1, 0d );
//		Debug.out.println("TestMicaR: "+Arrays.toString( micaR.getAnnotations(x, y)) );
//		
//		micaR = new MicaR( 1, 1000, 10d, 1d, 1 );
//		Debug.out.println("TestMicaR: "+Arrays.toString( micaR.getAnnotations(x, y)) );
//		
//		AnnotatedCurve curve = new AnnotatedCurve("d", x, y);
//		Debug.out.println("TestMicaR: "+ Arrays.toString(curve.getFilteredAnnotations().stream().map( a -> a.getIndex() ).toArray() ));
//		Debug.out.println("TestMicaR: "+ Arrays.toString(curve.getFilteredAnnotations().stream().map( a -> a.getType().value ).toArray() ));
//		curve.addAnnotationFilter( new CurveExtremaFilter(0.5));
//		Debug.out.println("TestMicaR: "+ Arrays.toString(curve.getFilteredAnnotations().stream().map( a -> a.getIndex() ).toArray() ));
//		Debug.out.println("TestMicaR: "+ Arrays.toString(curve.getFilteredAnnotations().stream().map( a -> a.getType().value ).toArray() ));
		
	}

	
}
