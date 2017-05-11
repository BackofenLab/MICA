package de.uni_freiburg.bioinf.mica.algorithm;

import org.apache.commons.math3.exception.NullArgumentException;


/**
 * Defines the interface for distance functions operating on two 
 * {@link Curve} objects.
 *  
 * @author Mmann
 *
 */
public interface CurveDistance {
	
	/**
	 * Computes the overall distance value for the given curves based on an 
	 * equidistant distribution of integration points for each curve.
	 * 
	 * @param curve1 the first curve
	 * @param curve2 the second curve
	 * @return the distance value for the pair of curves
	 * @throws NullArgumentException
	 */
	public double getDistance( Curve curve1, Curve curve2 ) throws NullArgumentException;
	
	/**
	 * Provides a string description for the distance function to be displayed.
	 * @return the string description
	 */
	public String getDescription();

}
