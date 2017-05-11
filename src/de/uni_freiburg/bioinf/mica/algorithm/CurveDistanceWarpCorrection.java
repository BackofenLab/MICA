package de.uni_freiburg.bioinf.mica.algorithm;


/**
 * Defines an interface for curve distance correction functions
 * for a given warping factor.
 * 
 * @author Mmann
 *
 */
public interface CurveDistanceWarpCorrection {

	/**
	 * Computes a warping-factor-corrected distance for the given distance
	 * @param warpingFactor the warping factor to be considered
	 * @param distance the distance to be correcter
	 * @return the warping-corrected distance value
	 */
	public double getWarpCorrectedDistance( double warpingFactor, double distance );
	
}
