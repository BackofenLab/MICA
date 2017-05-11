/**
 * 
 */
package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

/**
 * Extends the CurveDistance for implementations that are based on a
 * fixed number of equidistant integration points.
 * 
 * @author Mmann
 *
 */
public abstract class SampledCurveDistance implements CurveDistance {
	
	/**
	 * Number of equidistant integration points used for the distance 
	 * computation.
	 */
	protected int sampleNumber;
	
	
	/**
	 * Constructs a distance function that uses the given number of interpolation
	 * points.
	 * @param sampleNumber number of interpolation points used for the distance computation (>=2)
	 * @throws OutOfRangeException if sampleNumber is < 2
	 */
	public SampledCurveDistance(int sampleNumber) throws OutOfRangeException {
		setSampleNumber(sampleNumber);
	}
	


	/**
	 * Access to the number of integration points used for the distance
	 * computation
	 * @return the number of integration points used
	 */
	public int getSampleNumber() {
		return sampleNumber;
	}
	
	/**
	 * Sets the number of integration points used for the distance computation 
	 * @param sampleNumber number of integration points to be used (>=2)
	 * @throws OutOfRangeException if sampleNumber < 2
	 */
	public void setSampleNumber( int sampleNumber ) throws OutOfRangeException {
		// check input
		if (sampleNumber<2) throw new OutOfRangeException(sampleNumber, 2, Integer.MAX_VALUE);
		// set data
		this.sampleNumber = sampleNumber;
	}
	
	/**
	 * Computes the x-coordinates of the interpolation points within an x-interval of the given
	 * length and beginning at curve.getX()[start].
	 * Note: the right boundary is always excluded from the returned set. 
	 * The left boundary is only included if addLeftBoundary==true.
	 * 
	 * @param curve the curve of interest
	 * @param start the index of the start coordinate of the interval
	 * @param end the index of the end coordinate of the interval
	 * @param length the length of the interval
	 * @param xShift shift of the x-coordinates to be applied
	 * @param addLeftBoundary if true the first sample point is the left boundary 
	 * 			of the interval; otherwise the left boundary is excluded
	 * @return null if no coordinates are to be sampled or the set of x-coordinates for which a distance has to be computed
	 * @throws NullArgumentException
	 * @throws OutOfRangeException
	 */
	public double[] getSamplePositions( Curve curve, int start, int end ) throws NullArgumentException, OutOfRangeException {
		// check input
		if (curve==null) throw new NullArgumentException();
		if (start<0 || start>curve.size()) throw new OutOfRangeException(start, 0, curve.size());
		if (end<0 || end>curve.size()) throw new OutOfRangeException(end, 0, curve.size());
		if (start >= end) throw new IllegalArgumentException("start has to be smaller than end");
		
		// get stepsize for sampling
		double stepSize = getStepSize(curve);
		
		// get the x-coordinate where to start sampling
		double startX = curve.getX()[start];
		double endX = curve.getX()[end];
		// get the first coordinate within the interval
		double curX = curve.getXmin() + stepSize * FastMath.max(0,FastMath.floor((startX-curve.getXmin())/stepSize));
		if(curX < startX) {
			curX += stepSize;
		}
		
		// check if we already left the interval
		if (curX > endX) {
			// check if rounding issue
			if (MicaPrecision.sameX(curX, endX, curve.length())) {
				// return only last element
				return new double[]{endX};
			} else {
				// return empty set of sample points
				return new double[0];
			}
		}
		
		// get number of sample points within the interval
		int toSample = FastMath.max(0, (int)FastMath.floor((endX-curX)/stepSize) +1 );

		// compute x-coordinates to sample
		double[] sampleX = new double[toSample];
		int sampled = 0;
		for (sampled=0; sampled<toSample; sampled++) {
			sampleX[sampled] = curX + (stepSize*((double)sampled));
			if (sampleX[sampled]>endX) {
				sampled++;
				break;
			}
		}
		sampled--;

		// check if last element is within x-range
		if (sampleX[sampled] > endX) {
			// check if rounding issue
			if (MicaPrecision.sameX(sampleX[sampled], endX, curve.length())) {
				// overwrite last element
				sampleX[sampled] = endX;
			} else {
				// truncate last element
				sampleX = Arrays.copyOf( sampleX, sampled );
			}
		}
		
		// length check
		if (sampleX.length != sampled+1) {
			// truncate last element
			sampleX = Arrays.copyOf( sampleX, sampled );
		}
		
		return sampleX;
		
	}
	
	/**
	 * Computes the x-coordinates of the interpolation points within an x-interval of the given
	 * length and beginning at curve.getX()[start].
	 * Note: the right boundary is always excluded from the returned set. 
	 * The left boundary is only included if addLeftBoundary==true.
	 * 
	 * @param curve the curve of interest
	 * @param start the index of the start coordinate of the interval
	 * @param end the index of the end coordinate of the interval
	 * @param length the length of the interval
	 * @param xShift shift of the x-coordinates to be applied
	 * @param addLeftBoundary if true the first sample point is the left boundary 
	 * 			of the interval; otherwise the left boundary is excluded
	 * @return null if no coordinates are to be sampled or the set of x-coordinates for which a distance has to be computed
	 * @throws NullArgumentException
	 * @throws OutOfRangeException
	 */
	protected double[] getSamplePositions( Curve curve, int start, int end, double length, double xShift, boolean addLeftBoundary ) throws NullArgumentException, OutOfRangeException {
		if (curve==null) throw new NullArgumentException();
		if (start<0 || start>curve.size()) throw new OutOfRangeException(start, 0, curve.size());
		if (end<0 || end>curve.size()) throw new OutOfRangeException(end, 0, curve.size());
		if (start >= end) throw new IllegalArgumentException("start has to be smaller than end");
		if (length<=0) throw new IllegalArgumentException("length has to be >0");
		
		// get stepsize for sampling
		double stepSize = getStepSize(curve);
		
		// get the x-coordinate where to start sampling
		double startX = curve.getX()[start] + xShift;
		double curLength = curve.getX()[end] - curve.getX()[start];
		if ((startX < curve.getXmin() && !MicaPrecision.sameX(startX,curve.getXmin(),curve.length())) 
				|| (startX > curve.getXmax() && !MicaPrecision.sameX(startX,curve.getXmax(),curve.length()))) throw new IllegalArgumentException("SampledCurveDistance.getSamplePositions() : given xShift "+String.valueOf(xShift)+" causes the start of the interval "+String.valueOf(curve.getX()[start])+" to leave the x-range of the curve");
		if ((startX+length < curve.getXmin() && !MicaPrecision.sameX(startX+length,curve.getXmin(),curve.length())) 
				|| (startX+length > curve.getXmax() && !MicaPrecision.sameX(startX+length,curve.getXmax(),curve.length()))) throw new IllegalArgumentException("SampledCurveDistance.getSamplePositions() : given xShift "+String.valueOf(xShift)+" causes the end of the interval to leave the x-range of the curve by "+String.valueOf(startX+length - curve.getXmax())+" with precisionDelta = "+String.valueOf(MicaPrecision.getPrecisionDelta(curve.length())));
		
		// get the first x-coordinate within the interval to be sampled
		double firstInRange = curve.getXmin() + (FastMath.floor((startX-curve.getXmin()) / stepSize) * stepSize);
		if (firstInRange <= startX) {
			firstInRange += stepSize;
		}
//		if (MicaPrecision.sameX(firstInRange,startX,curve.length())) {
		if (MicaPrecision.sameX(firstInRange,startX,curLength)) {
			firstInRange += stepSize;
		}
		// check if something is to be sampled
		if (firstInRange > startX+length) {
			if (addLeftBoundary) {
				return new double[]{curve.getX()[start]};
			}
			return new double[0];
		}
		
		// get number of additional points to be sampled within the interval AFTER firstInRange
		int sampleCount = FastMath.max( 0, (int) (FastMath.floor((length - (firstInRange-startX)) / stepSize)));
		
		// generate list of integration points to be sampled
		double[] toSample = new double[sampleCount+1];
		toSample[0] = firstInRange;
		for (int i=1; i<=sampleCount; i++) {
			toSample[i] = firstInRange + ((double)i)*stepSize;
		}
		
		// map them back to the current interval
		double curLengthRatio = (curve.getX()[end] - curve.getX()[start])/length;
		for (int i=0; i<toSample.length; i++) {
			toSample[i] = ((toSample[i]-startX)*curLengthRatio) + curve.getX()[start];
		}
		
		// check if right boundary included -> remove
		if (toSample.length > 0 && MicaPrecision.sameX(toSample[toSample.length-1],curve.getX()[end],length)) {
//		if (toSample.length > 0 && MicaPrecision.sameX(toSample[toSample.length-1],curve.getX()[end],curve.length())) {
			// remove last entry
			toSample = Arrays.copyOf( toSample, toSample.length-1 );
		}
		
		// check if left bound requested and not present -> add
		if (addLeftBoundary) {// && (toSample.length == 0 || !MicaPrecision.sameX(toSample[0],curve.getX()[start],curve.length()))) {
			// add start X
			double[] tmp = new double[toSample.length+1];
			tmp[0] = curve.getX()[start];
			for (int i=0; i<toSample.length; i++) {
				tmp[i+1] = toSample[i];
			}
			// overwrite toSample
			toSample = tmp;
		}
		
		// return final list
		return toSample;
	}
	
	
	/**
	 * Computes the step size for the given number of equidistant integration points.
	 * @param curve the curve of interest
	 * @return (curve.size())/(sampleNumber-1)
	 * @throws NullArgumentException
	 */
	protected double getStepSize( Curve curve ) throws NullArgumentException {
		if (curve==null) throw new NullArgumentException();
		return curve.length() / (sampleNumber-1);
	}
	
	/**
	 * Distance for two x-coordinates for two curves.
	 * @param curve1 the first curve
	 * @param curve2 the second curve
	 * @param x1 x-coordinate in curve1 to be used
	 * @param x2 x-coordinate in curve2 to be used
	 * @param lengthRatio1 the ratio (newIntervalLength)/(oldIntervalLength) of the interval of curve1 that encloses x1
	 * @param lengthRatio2 the ratio (newIntervalLength)/(oldIntervalLength) of the interval of curve2 that encloses x2
	 * @return the distance for the two coordinates
	 */
	abstract protected double getDistance( Curve curve2, Curve curve1, double x1, double x2, double lengthRatio1, double lengthRatio2 );
	
	
	/**
	 * Computes the final distance for the given distance sum, e.g. doing
	 * a normalization etc.
	 * @param distanceSum the accumulated sum of distances
	 * @param samples the number of distance samples summed
	 * @return the final distance
	 */
	abstract protected double finalDistance( double distanceSum, int samples );

	
	/**
	 * Reverse function to {@link #finalDistance(double, int)}
	 * 
	 * @param distance the final distance to reverse
	 * @param sampleNumber the number of samples used to compute the final distance
	 * @return the distance before calling {@link #finalDistance(double, int)}
	 */
	abstract protected double preFinalDistance( double distance, int sampleNumber );
	

	/**
	 * Computes the distance based on uniformly distributed integration points
	 * on both curves using the {@link #getDistance(Curve, Curve, double, double, double, double)}
	 * for each pair of integration points. Note, first and last points
	 * are always the first and last point of the curves.
	 */
	@Override
	public double getDistance(Curve curve1,
			Curve curve2) throws NullArgumentException {
		if (curve1 == null || curve2 == null) throw new NullArgumentException();
		
		// the distance to compute
		double distance = 0;
		// distance between interpolation points in the curves
		double xStart1 = curve1.getXmin();
		double xStart2 = curve2.getXmin();
		double stepSize1 = getStepSize(curve1);
		double stepSize2 = getStepSize(curve2);
		
		// get distance of first positions
		distance += getDistance(curve1, curve2, curve1.getXmin(), curve2.getXmin(), 1, 1);
		// get distance of enclosed positions (uniformly distributed)
		if (sampleNumber > 2) {
			distance += IntStream.range(1, sampleNumber-1).mapToDouble( i -> getDistance( curve1, curve2, xStart1 + i*stepSize1, xStart2 + i*stepSize2, 1, 1) ).sum();
		}
		// get distance of last positions
		distance += getDistance(curve1, curve2, curve1.getXmax(), curve2.getXmax(), 1, 1);
		// return final distance
		return finalDistance(distance, sampleNumber);
	}
	
	
	/**
	 * Computes the distance sum for the provided sample coordinates for the two curves
	 * @param curve1 the first curve
	 * @param curve2 the second curve
	 * @param xSamples1 the set of x-coordinates from curve1 to compute the distance for 
	 * @param xSamples2 the set of x-coordinates from curve2 to compute the distance for 
	 * @return the sum of the distances for the according coordinates for both curves
	 */
	public double getDistance(Curve curve1, Curve curve2, double[] xSamples1, double[] xSamples2, int start, int length) {
		// check input
		if (curve1==null) throw new NullArgumentException();
		if (curve2==null) throw new NullArgumentException();
		if (xSamples1==null) throw new NullArgumentException();
		if (xSamples2==null) throw new NullArgumentException();
		if (xSamples1.length!=xSamples2.length) throw new IllegalArgumentException("x samples differ in length");
		if (start < 0) throw new OutOfRangeException(start, 0, xSamples1.length-1);
		if (length < 0 || start+length > xSamples1.length) throw new OutOfRangeException(length, 0, xSamples1.length-start);
		
		double distance = 0d;
		
		// get distance for each coordinate of interest
		for (int i=start; i<start+length; i++) {
			distance += getDistance(curve1, curve2, xSamples1[i], xSamples2[i], 1,1);
		}
		
		return finalDistance(distance, length);
	}

	
}
