package de.uni_freiburg.bioinf.mica.algorithm;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

/**
 * Does a linear distance correction, i.e. the distance d is multiplied 
 * with the warpingFactor wf and a scaling factor for wf.
 * 
 * @author Mmann
 *
 */
public class LinearCurveDistanceWarpingCorrection implements
		CurveDistanceWarpCorrection {

	/**
	 * scaling factor for the warping factor
	 */
	final double warpScaling;
	
	
	/**
	 * Construction that sets the warpScaling to 1.
	 */
	public LinearCurveDistanceWarpingCorrection() {
		this.warpScaling = 1d;
	}
	
	/**
	 * Construction with explicit warp scaling setup.
	 * @param warpScaling the scaling factor for the warping factors.
	 */
	public LinearCurveDistanceWarpingCorrection(double warpScaling) {
		double minDistWarpMultiplyer = 0.0000000001;
		if (warpScaling < minDistWarpMultiplyer) throw new OutOfRangeException(warpScaling, minDistWarpMultiplyer, Double.MAX_VALUE);
		this.warpScaling = warpScaling;
	}
	
	
	/**
	 * Does a linear distance correction, i.e. the distance is multiplied with the warpingFactor.
	 * 
	 * That is it computes
	 * 
	 *   max(1, warpScaling*warpingFactor) * distance
	 * 
	 */
	@Override
	public double getWarpCorrectedDistance(double warpingFactor, double distance) {
		return FastMath.max(1d,warpScaling*warpingFactor)*distance;
	}

}
