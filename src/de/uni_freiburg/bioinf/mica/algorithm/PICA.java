package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation.Type;


/**
 * PICA = Pairwise Interval-based Curve Alignment.
 * 
 * Given two {@link IntervalDecomposition} objects, PICA identifies the best
 * pairwise alignment of the curves based on greedy interval decompositions.
 * To this end, a greedy decomposition strategy is applied, ie. iteratively
 * each interval is screened for the pair of alignable {@link CurveAnnotation} 
 * objects that minimize the distance score for the interval if the two annotations
 * are aligned, which results in a decomposition of the according interval.
 * This is repeated until no decomposition is possible anymore or does not reduce
 * the distance.
 * 
 * @author Mmann
 *
 */
public class PICA {

	/**
	 * The distance function to be used for evaluation
	 */
	final SampledCurveDistance distanceFunction;
	
	/**
	 * The distance correction function to be used
	 */
	final CurveDistanceWarpCorrection distanceCorrectionFunction;
	
	/**
	 * The maximal ratio allowed during decomposition, ie. it has to hold
	 *  max( newLength/oldLength, oldLength/newLength ) <= maxDistortionRatio
	 */
	final double maxDistortionRatio;
	
	/**
	 *  maxRelXShift the maximal relative shift of x-coordinates allowed [0,1]
	 * 	where 0 disallows any distortion and 1 allows for maximal distortion 
	 */
	final double maxRelXShift;
	
	/**
	 * The minimal relative length of an interval to be considered for further
	 * interval decomposition.
	 */
	final double minRelIntervalLength;

	
	/**
	 * Data structure to store the result of a pairwise alignment
	 */
	static public class PicaData {
		public IntervalDecomposition dec1 = null;
		public IntervalDecomposition dec2 = null;
		public double distance = Double.NaN;
	}

	
	/**
	 * Constructs an aligner that uses the given distance function and
	 * ensures that the during each iteration an interval length is not more
	 * distorted than the allowed ratio
	 * 
	 * @param distanceFunction the distance function to be used (!=null)
	 * @param maxDistortionRatio the maximal distortion ratio allowed (>=1), 
	 * 			where 1 disallows any distortion and 
	 * 			2 allows for a maximal distortion to double or halve of the orginal length 
	 * @param maxRelXShift the maximal relative shift of x-coordinates allowed [0,1], 
	 * 			where 0 disallows any distortion and 1 allows for maximal distortion 
	 * @param minRelIntervalLength the minimal relative length of an interval to be considered for further decomposition
	 * @throws NullArgumentException
	 * @throws OutOfRangeException if maxDistortionRatio < 1
	 * @throws OutOfRangeException if minRelIntervalLength < 0 || minRelIntervalLength > 1
	 */
	public PICA( SampledCurveDistance distanceFunction, double maxDistortionRatio, double maxRelXShift, double minRelIntervalLength ) throws NullArgumentException, OutOfRangeException
	{
		if (distanceFunction == null) throw new NullArgumentException();
		if (maxDistortionRatio < 1) throw new OutOfRangeException(maxDistortionRatio, 1, Double.MAX_VALUE);
		if (maxRelXShift < 0 ||maxRelXShift > 1) throw new OutOfRangeException(maxRelXShift, 0, 1);
		if (minRelIntervalLength < 0 || minRelIntervalLength > 1) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
		
		// setup data
		this.distanceFunction = distanceFunction;
		// setup distance function
		this.distanceCorrectionFunction = new NoCurveDistanceWarpingCorrection();
		// setup decomposition constraints
		this.maxDistortionRatio = maxDistortionRatio;
		this.maxRelXShift = maxRelXShift;
		this.minRelIntervalLength = minRelIntervalLength;
	}
	
	/**
	 * Constructs an aligner that uses the given distance function and
	 * ensures that the during each iteration an interval length is not more
	 * distorted than the allowed ratio
	 * 
	 * @param distanceFunction the distance function to be used (!=null)
	 * @param maxDistortionRatio the maximal distortion ratio allowed (>=1), 
	 * 			where 1 disallows any distortion and 
	 * 			2 allows for a maximal distortion to double or halve of the original length 
	 * @param maxRelXShift the maximal relative shift of x-coordinates allowed [0,1], 
	 * 			where 0 disallows any distortion and 1 allows for maximal distortion 
	 * @param minRelIntervalLength the minimal relative length of an interval to be considered for further decomposition
	 * @param distWarpScaling the scaling factor to be used for a linear distance correction using the warping factor
	 * @throws NullArgumentException
	 * @throws OutOfRangeException if maxDistortionRatio < 1
	 * @throws OutOfRangeException if minRelIntervalLength < 0 || minRelIntervalLength > 1
	 */
	public PICA( SampledCurveDistance distanceFunction, double maxDistortionRatio, double maxRelXShift, double minRelIntervalLength, double distWarpScaling ) throws NullArgumentException, OutOfRangeException
	{
		if (distanceFunction == null) throw new NullArgumentException();
		if (maxDistortionRatio < 1) throw new OutOfRangeException(maxDistortionRatio, 1, Double.MAX_VALUE);
		if (maxRelXShift < 0 || maxRelXShift > 1) throw new OutOfRangeException(maxRelXShift, 0,1);
		if (minRelIntervalLength < 0 || minRelIntervalLength > 1) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
		double minDistWarpMultiplyer = 0.0000000001;
		if (distWarpScaling < minDistWarpMultiplyer) throw new OutOfRangeException(distWarpScaling, minDistWarpMultiplyer, Double.MAX_VALUE);
		
		// setup data
		this.distanceFunction = distanceFunction;
		// setup distance function
		this.distanceCorrectionFunction = new LinearCurveDistanceWarpingCorrection(distWarpScaling);
		// setup decomposition constraints
		this.maxDistortionRatio = maxDistortionRatio;
		this.maxRelXShift = maxRelXShift;
		this.minRelIntervalLength = minRelIntervalLength;
	}
	
	
	/**
	 * Computes a pairwise alignment where the second curve is best aligned to the 
	 * first (reference) curve.
	 * 
	 * The second curve is scaled to the reference curve's length.
	 * 
	 * @param curveRef the reference curve to align to - NOT changed during alignment
	 * @param curve2 the second curve to align against the reference - changed during alignment
	 * @return the pairwise alignment data
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the curves are not compatible, i.e. show initially an incompatible decomposition
	 */
	public PicaData alignToReference( IntervalDecomposition curveRef, IntervalDecomposition curve2)
					throws NullArgumentException, IllegalArgumentException
	{
		// call pairwise aligner where curve2 has no length weight, 
		// i.e. interval lengths' of first curve (reference) are maintained
		return align( curveRef, 1.0, curve2, 0.0 );
	}
	
	/**
	 * Computes a pairwise alignment that minimizes the distance function for the
	 * two given curves.
	 * 
	 * The curves are scaled to equal length, where each curve length is weighted according to
	 * the given factors, see {@link PICA#getMeanLength(double, double, double, double)}.
	 * 
	 * @param curve1 the first curve to align - changed during alignment
	 * @param weight1 the weight of the first curve within the alignment
	 * @param curve2 the second curve to align - changed during alignment
	 * @param weight2 the weight of the second curve within the alignment
	 * @return the pairwise alignment data
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the curves are not compatible, i.e. show initially an incompatible decomposition
	 */
	public PicaData align( final IntervalDecomposition curve1, final double weight1, 
							final IntervalDecomposition curve2, final double weight2 )
		throws NullArgumentException, IllegalArgumentException
	{
		// stop if thread is interrupted
		if (Thread.currentThread().isInterrupted()) return null;

		if (curve1==null || curve2==null) throw new NullArgumentException();
		if (!curve1.isCompatible(curve2)) throw new IllegalArgumentException("given curves are incompatible");
		if (weight1 < 0.0) throw new IllegalArgumentException("weight 1 is negative");
		if (weight2 < 0.0) throw new IllegalArgumentException("weight 2 is negative");
		
		// final result (will be updated in the following)
		PicaData result = new PicaData();
		result.dec1 = new IntervalDecomposition( curve1 );
		result.dec2 = new IntervalDecomposition( curve2 );

		// get weighted mean length of both curves to start global alignment (=equal length)
		double globalMeanLength = getMeanLength( curve1.getCurve().length(), weight1, 
											curve2.getCurve().length(), weight2);
		
		// create working copies of equal length
		IntervalDecomposition curCurve1 = new IntervalDecomposition( result.dec1, globalMeanLength );
		IntervalDecomposition curCurve2 = new IntervalDecomposition( result.dec2, globalMeanLength );

		// warp curves such that all present intervals are of the same length
		{
			// direct access for simpler notation
			double[] x1 = curCurve1.getCurve().getX();
			double[] x2 = curCurve2.getCurve().getX();
			// warp curves such that all present intervals are of the same length
			for (int i=0; i<curCurve1.size(); i++) {
				// get old right end x-coordinate (will be overwritten)
				double xRight1old = x1[ curCurve1.getIntervalEnd(i).getIndex() ];
				double xRight2old = x2[ curCurve2.getIntervalEnd(i).getIndex() ];
				// get new length of the interval i
				double intervalLength = getMeanLength( result.dec1.getIntervalLength(i), weight1, 
											result.dec2.getIntervalLength(i), weight2);
				// warp x-coordinates of current interval
				curCurve1.warpIntervalLeft( i, intervalLength/curCurve1.getIntervalLength(i));
				curCurve2.warpIntervalLeft( i, intervalLength/curCurve2.getIntervalLength(i));
				// shift x-coordinates of all following intervals
				if (curCurve1.getIntervalEnd(i).getIndex()+1 < x1.length) {
					double xShift1 = x1[curCurve1.getIntervalEnd(i).getIndex()] - xRight1old;
					IntStream.range(curCurve1.getIntervalEnd(i).getIndex()+1, x1.length).forEach( p -> x1[p] += xShift1);
					double xShift2 = x2[curCurve2.getIntervalEnd(i).getIndex()] - xRight2old;
					IntStream.range(curCurve2.getIntervalEnd(i).getIndex()+1, x2.length).forEach( p -> x2[p] += xShift2);
				}
			}
			// update spline information
			curCurve1.getCurve().updateInterpolation();
			curCurve2.getCurve().updateInterpolation();
			// sanity check if warping was maintaining overall length
			if ( FastMath.max(curCurve1.getCurve().length()/globalMeanLength, globalMeanLength/curCurve1.getCurve().length()) >= 1.01 ) {
				throw new RuntimeException("overall length after initial interval warping ("
						+ String.valueOf(curCurve1.getCurve().length())
						+ ") differs expected overall length ("
						+ String.valueOf(globalMeanLength)
						+ ") for curve1");
			}
			if ( FastMath.max(curCurve2.getCurve().length()/globalMeanLength, globalMeanLength/curCurve2.getCurve().length()) >= 1.01 ) {
				throw new RuntimeException("overall length after initial interval warping ("
						+ String.valueOf(curCurve2.getCurve().length())
						+ ") differs expected overall length ("
						+ String.valueOf(globalMeanLength)
						+ ") for curve2");
			}
		}
		// shift x-range if necessary
		if (!MicaPrecision.sameX( curCurve1.getCurve().getXmin(), curCurve2.getCurve().getXmin(), curCurve1.getCurve().length())) {
			// get weighted mean length of both curves to start global alignment (=equal length)
			double globalMeanXmin = getMeanLength( curCurve1.getCurve().getXmin(), weight1, 
					curCurve2.getCurve().getXmin(), weight2);
			// shift curve 1
			if (!MicaPrecision.sameX( curCurve1.getCurve().getXmin(), globalMeanXmin, curCurve1.getCurve().length())) {
				double xShift = globalMeanXmin-curCurve1.getCurve().getXmin();
				for (int i=0; i<curCurve1.getCurve().size(); i++) {
					curCurve1.getCurve().getX()[i] += xShift;
				}
				// update interpolation 
				curCurve1.getCurve().updateInterpolation();
			}
			// shift curve 2
			if (!MicaPrecision.sameX( curCurve2.getCurve().getXmin(), globalMeanXmin, curCurve2.getCurve().length())) {
				double xShift = globalMeanXmin-curCurve2.getCurve().getXmin();
				for (int i=0; i<curCurve2.getCurve().size(); i++) {
					curCurve2.getCurve().getX()[i] += xShift;
				}
				// update interpolation 
				curCurve2.getCurve().updateInterpolation();
			}
		}

		// copy working copies back since it is the current best alignment
		result.dec1.copy( curCurve1, curCurve1.getCurve().length());
		result.dec2.copy( curCurve2, curCurve2.getCurve().length());
		// compute current best distance = initial distance
		result.distance = distanceCorrectionFunction.getWarpCorrectedDistance(1, distanceFunction.getDistance(result.dec1.getCurve(), result.dec2.getCurve()));
		
		// compute minimal interval length to be considered for decomposition
		double minIntervalLengthForDecomposition = result.dec1.getCurve().length()*minRelIntervalLength;
		// the interval index currently focus for decomposition
		int curInterval = 0;
		// iterate while within one of the intervals
		while (curInterval < curCurve1.size()) {

			// stop if thread is interrupted
			if (Thread.currentThread().isInterrupted()) return null;
			
			// check if the interval is smaller than the minimal length
			if (result.dec1.getIntervalLength(curInterval) < minIntervalLengthForDecomposition) {
				// go to next interval
				curInterval++;
				continue;
			}
			
			boolean noBetterAlignmentFound = true;
			
			// make working copies of best alignment without decomposition of this interval
			curCurve1.copy( result.dec1, result.dec1.getCurve().length());
			curCurve2.copy( result.dec2, result.dec2.getCurve().length());
			
			// initialize temporary variables for lazy computation when needed
			double curIntervalLength = -1d;
			double curIntervalStartX1 = 0.0;
			double curIntervalStartX2 = 0.0;
			double curIntervalEndX1 = 0.0;
			double curIntervalEndX2 = 0.0;
			
			
			// get coordinates within this interval that are used for distance computation
			double[] curDistSampleX1 = null;
			double[] curDistSampleX2 = null;
			int curDistToSample = 0;
			double[] decDistSampleX1 = null;
			double[] decDistSampleX2 = null;
			
			// initialize current minimal local distance for lazy computation
			double curMinDistanceLoc = -1;
			CurveAnnotation curMinDistanceA1 = null;
			CurveAnnotation curMinDistanceA2 = null;
			double curMinDistanceSplitPos = Double.NaN;

			//  ### iterate over all compatible annotation pairs within the interval
			for ( CurveAnnotation a1 : curCurve1.getIntervalAnnotations( curInterval )) {
				for ( CurveAnnotation a2 : curCurve2.getIntervalAnnotations( curInterval )) {
					// check if annotations are of alignable type
					if ( Type.isAlignable( a1.getType(), a2.getType() ) ) {
						
						// stop if thread is interrupted
						if (Thread.currentThread().isInterrupted()) return null;
						
						// start lazy computation
						if (curIntervalLength < 0) {
							curIntervalLength = curCurve1.getIntervalLength(curInterval);
							curIntervalStartX1 = curCurve1.getCurve().getX()[curCurve1.getIntervalStart(curInterval).getIndex()];
							curIntervalStartX2 = curCurve2.getCurve().getX()[curCurve2.getIntervalStart(curInterval).getIndex()];
							curIntervalEndX1 = curCurve1.getCurve().getX()[curCurve1.getIntervalEnd(curInterval).getIndex()];
							curIntervalEndX2 = curCurve2.getCurve().getX()[curCurve2.getIntervalEnd(curInterval).getIndex()];
							
							// get coordinates within this interval that are used for distance computation
							curDistSampleX1 = distanceFunction.getSamplePositions( curCurve1.getCurve(), curCurve1.getIntervalStart(curInterval).getIndex(), curCurve1.getIntervalEnd(curInterval).getIndex());
							curDistSampleX2 = distanceFunction.getSamplePositions( curCurve2.getCurve(), curCurve2.getIntervalStart(curInterval).getIndex(), curCurve2.getIntervalEnd(curInterval).getIndex());
							// #### HACK TO ENSURE EQUAL NUMBER OF SAMPLES WHICH MIGHT DIFFER DUE TO ROUNDING ISSUES ####
							curDistToSample = FastMath.min(curDistSampleX1.length, curDistSampleX2.length);
							if (curDistSampleX1.length>curDistToSample) curDistSampleX1 = Arrays.copyOf(curDistSampleX1, curDistToSample);
							if (curDistSampleX2.length>curDistToSample) curDistSampleX2 = Arrays.copyOf(curDistSampleX2, curDistToSample);
							
							decDistSampleX1 = Arrays.copyOf( curDistSampleX1, curDistToSample );
							decDistSampleX2 = Arrays.copyOf( curDistSampleX2, curDistToSample );

						}
						
						
						// direct access for simpler notation
						double[] x1 = curCurve1.getCurve().getX();
						double[] x2 = curCurve2.getCurve().getX();
						double a1x = x1[a1.getIndex()];
						double a2x = x2[a2.getIndex()];
						
						// length of left interval after warping
						double leftMeanLength = getMeanLength( a1x-curIntervalStartX1, weight1, a2x-curIntervalStartX2, weight2);
						
						// get global relative positions before and after warping
						double a1relXwarped = (curIntervalStartX1+leftMeanLength-curCurve1.getCurve().getXmin())/(curCurve1.getCurve().getXmax()-curCurve1.getCurve().getXmin());
						double a1relXoriginal = (curCurve1.getCurveOriginal().getX()[a1.getIndex()]-curCurve1.getCurveOriginal().getXmin())/(curCurve1.getCurveOriginal().getXmax()-curCurve1.getCurveOriginal().getXmin());
						double a1relXShift = FastMath.abs( a1relXwarped - a1relXoriginal );
						double a2relXwarped = (curIntervalStartX2+leftMeanLength-curCurve2.getCurve().getXmin())/(curCurve2.getCurve().getXmax()-curCurve2.getCurve().getXmin());
						double a2relXoriginal = (curCurve2.getCurveOriginal().getX()[a2.getIndex()]-curCurve2.getCurveOriginal().getXmin())/(curCurve2.getCurveOriginal().getXmax()-curCurve2.getCurveOriginal().getXmin());
						double a2relXShift = FastMath.abs( a2relXwarped - a2relXoriginal );
						
						
						// check if distortion is larger than allowed
						if ( this.maxRelXShift < a1relXShift || this.maxRelXShift < a2relXShift ) {
							// skip this decomposition option
							continue;
						}
						
						// get new relative positions of a1 and a2 within the interval
						double newRelPosSplit = leftMeanLength / curIntervalLength;
						double curIntervalWarpingFactor = FastMath.max( newRelPosSplit, 1/newRelPosSplit );

						// check if distortion is larger than allowed
						if ( this.maxDistortionRatio < curIntervalWarpingFactor ) {
							// skip this decomposition option
							continue;
						}

						// now we really have to compute distances ...
						// compute initial distance if not already done
						if (curMinDistanceLoc < 0) {
							curMinDistanceLoc = distanceFunction.getDistance(curCurve1.getCurve(), curCurve2.getCurve(), curDistSampleX1, curDistSampleX2, 0, curDistSampleX1.length);
						}
						
						double a1xWarped = curIntervalStartX1 + curIntervalLength*newRelPosSplit;
						double a2xWarped = curIntervalStartX2 + curIntervalLength*newRelPosSplit;
						
						// get warped distance sampling positions
						int curDistSampleX1LastLeft = -1;
						for (int i=0;i<curDistToSample; i++) {
							// curve1 : left interval
							if (curDistSampleX1[i]<a1xWarped) {
								// compute position before warping (left = fixed)
								decDistSampleX1[i] = curIntervalStartX1 + ((curDistSampleX1[i]-curIntervalStartX1)*(a1x-curIntervalStartX1)/(a1xWarped-curIntervalStartX1));
								// update boundary control
								curDistSampleX1LastLeft = i;
							} else {
								// curve1 : right interval
								// compute position before warping (right = fixed)
								decDistSampleX1[i] = curIntervalEndX1 - ((curIntervalEndX1-curDistSampleX1[i])*(curIntervalEndX1-a1x)/(curIntervalEndX1-a1xWarped));
							}
							// curve2 : left interval
							if (curDistSampleX2[i]<a2xWarped) {
								// compute position before warping (left = fixed)
								decDistSampleX2[i] = curIntervalStartX2 + ((curDistSampleX2[i]-curIntervalStartX2)*(a2x-curIntervalStartX2)/(a2xWarped-curIntervalStartX2));
							} else {
								// curve2 : right interval
								// compute position before warping (right = fixed)
								decDistSampleX2[i] = curIntervalEndX2 - ((curIntervalEndX2-curDistSampleX2[i])*(curIntervalEndX2-a2x)/(curIntervalEndX2-a2xWarped));
							}
						}
						
						// compute distance resulting from the alignment
						double curDistanceLoc = distanceCorrectionFunction.getWarpCorrectedDistance( curIntervalWarpingFactor, 
								distanceFunction.finalDistance(
								// distance of left interval of the decomposition
								(curDistSampleX1LastLeft==-1 ? 0 : distanceFunction.preFinalDistance( distanceFunction.getDistance(curCurve1.getCurve(), curCurve2.getCurve(), decDistSampleX1, decDistSampleX2, 0, curDistSampleX1LastLeft+1), curDistSampleX1LastLeft+1))
								// distance of right interval of the decomposition
								+ (curDistSampleX1LastLeft+1>=decDistSampleX1.length ? 0 : distanceFunction.preFinalDistance( distanceFunction.getDistance(curCurve1.getCurve(), curCurve2.getCurve(), decDistSampleX1, decDistSampleX2, curDistSampleX1LastLeft+1, decDistSampleX2.length-curDistSampleX1LastLeft-1), decDistSampleX2.length-curDistSampleX1LastLeft-1))
								, decDistSampleX1.length)
										);

						// check if current distance is better than best so far
						if (curDistanceLoc < curMinDistanceLoc) {

							// remember we have found something better
							noBetterAlignmentFound = false;
							
							// store new minimal distance
							curMinDistanceLoc = curDistanceLoc;
							curMinDistanceA1 = a1;
							curMinDistanceA2 = a2;
							curMinDistanceSplitPos = newRelPosSplit;
						}
					}
				}
			}
			
			// go to next interval, if there was no better decomposition
			if (noBetterAlignmentFound) {
				// go to next interval
				curInterval++;
			} else {
				
				// store best alignment for this interval decomposition
				result.dec1.copy(curCurve1, curCurve1.getCurve().length());
				result.dec2.copy(curCurve2, curCurve2.getCurve().length());
				
				// decompose and warp the current interval within the minimum curve
				result.dec1.decompose( curInterval, curMinDistanceA1, curMinDistanceSplitPos);
				result.dec2.decompose( curInterval, curMinDistanceA2, curMinDistanceSplitPos);
				
				// get current distance after warping/decomposition
				result.distance = distanceCorrectionFunction.getWarpCorrectedDistance(1, distanceFunction.getDistance(result.dec1.getCurve(), result.dec2.getCurve()));
				// keep interval index, since left interval of new best decomposition has the same index
			}
		}
		
		// stop if thread is interrupted
		if (Thread.currentThread().isInterrupted()) return null;
		
		// return final alignment data 
		return result;
	}



	/**
	 * Computes a weighted mean length, i.e. 
	 *   meanLen = (l1*w1 + l2*w2) / (w1 + w2);
	 *   
	 * @param l1 first length
	 * @param w1 first weight
	 * @param l2 second length
	 * @param w2 second weight
	 * @return the weighted mean length
	 */
	double getMeanLength( double l1, double w1, double l2, double w2 ) {
		return (l1*w1 + l2*w2) / (w1+w2);
	}
	
}
