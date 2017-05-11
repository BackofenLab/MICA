package de.uni_freiburg.bioinf.mica.algorithm;

/**
 * Ignores the warping factor and does no distance correction.
 * 
 * @author Mmann
 *
 */
public class NoCurveDistanceWarpingCorrection implements
		CurveDistanceWarpCorrection {

	/**
	 * Does not correction of the distance and ignores the warpingFactor.
	 */
	@Override
	public double getWarpCorrectedDistance(double warpingFactor, double distance) {
		return distance;
	}

}
