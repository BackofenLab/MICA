package de.uni_freiburg.bioinf.mica.algorithm;

import org.apache.commons.math3.util.FastMath;

public class MicaPrecision {

	/**
	 * for consensus computation, two x-coordinates x1 and y2 are assumed to be equal if their
	 * absolute difference is 
	 * 
	 * |x1-x2| > curve.length()*precisionDeltaLengthFactor
	 * 
	 */
	final static double precisionDeltaLengthFactor = 1/10000d;
	
	
	/**
	 * Determines whether two x-coordinates are describing the same position given
	 * the {@link #precisionDeltaLengthFactor} and the curve's length.
	 * 
	 * @param x1 the first coordinate
	 * @param x2 the second coordinate
	 * @param curveLength the length of the curve the coordinates are from
	 * @return true, if x1 and x2 are considered equal; false otherwise
	 */
	public static boolean sameX( double x1, double x2, double curveLength ) {
		return FastMath.abs( x1 - x2 ) <= getPrecisionDelta(curveLength);
	}

	/**
	 * Provides the precision delta value to be used for a given curve length
	 * @param curveLength
	 * @return
	 */
	public static double getPrecisionDelta( double curveLength ) {
		if (curveLength<=0) throw new IllegalArgumentException("curveLength has to be > 0");
		return precisionDeltaLengthFactor * curveLength;
	}
	
}
