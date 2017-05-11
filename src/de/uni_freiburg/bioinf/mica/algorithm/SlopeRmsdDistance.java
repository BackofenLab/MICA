/**
 * 
 */
package de.uni_freiburg.bioinf.mica.algorithm;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

/**
 * Implements a distance function which computes the root mean square deviation of the
 * slopes (first derivate) of the two curves based on a uniformly distributed number of 
 * integration points.
 * 
 * @author Mmann
 *
 */
public class SlopeRmsdDistance extends SampledCurveDistance {
	
	
	
	/**
	 * Constructs a distance function that uses the given number of interpolation
	 * points.
	 * @param sampleNumber number of interpolation points used for the distance computation (>=2)
	 * @throws OutOfRangeException if sampleNumber is < 2
	 */
	public SlopeRmsdDistance(int sampleNumber) throws OutOfRangeException {
		super(sampleNumber);
	}
	
	@Override
	public String getDescription() {
		return "Computes the slope RMSD on "+sampleNumber+" equidistant x-coordinate samples";
	}

	/**
	 * Computes the squared absolute distance of the slope at the given x-coordinates corrected
	 * by the given length warping, i.e.
	 * 
	 *  pow( (curve1.getSlope(x1)/lengthRatio1) - (curve2.getSlope(x2)/lengthRatio2), 2 )
	 *  
	 */
	@Override
	protected double getDistance(Curve curve1, Curve curve2, double x1, double x2, double lengthRatio1, double lengthRatio2) {
		return FastMath.pow( (curve1.getSlope( x1 )/lengthRatio1) - (curve2.getSlope( x2 )/lengthRatio2), 2);
	}
	

	/**
	 * Normalizes the distance sum with the sample number (mean) and takes the
	 * square root to get the final RMSD.
	 */
	@Override
	protected double finalDistance(double distanceSum, int samples) {
		if( samples < 0) throw new OutOfRangeException(samples, 0, Integer.MAX_VALUE);
		if (samples == 0) {
			return distanceSum;
		}
		return FastMath.sqrt(distanceSum/samples);
	}
	
	/**
	 * returns pow(distance,2) * samples.
	 */
	@Override
	protected double preFinalDistance(double distance, int samples) {
		return FastMath.pow(distance,2) * (double)samples;
	}

	
}
