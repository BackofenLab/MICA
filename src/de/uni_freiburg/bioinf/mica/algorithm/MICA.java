package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;


/**
 * MICA = Multiple Interval-based Curve Alignment.
 * 
 * Given a set of {@link IntervalDecomposition} objects, MICA identifies 
 * a multiple alignment of the curves. This alignment is created using
 * a progressive alignment scheme that is based on pairwise alignments (PICA).
 * 
 * Therefore, all pairwise alignments are computed. The according distance
 * matrix is used to derive a guide tree for the successive progressive alignment.
 * 
 * @author Mmann
 *
 */
public class MICA {

	/**
	 * The distance function to be used for evaluation
	 */
	final SampledCurveDistance distanceFunction;
	
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
	 * The minimal relative length of an interval to be considered for further decomposition
	 */
	final double minRelIntervalLength;
	
	/**
	 * The scaling factor for the warping factor when combining warping and
	 * distance. Set to negative value if no warping correction of the distances is
	 * to be applied.
	 */
	final double warpScaling;

	/**
	 * Constructs an aligner that uses the given distance function and
	 * ensures that the during each iteration an interval length is not more
	 * distorted than the allowed ratio
	 * 
	 * @param distanceFunction the distance function to be used (!=null)
	 * @param maxDistortionRatio the maximal distortion ratio allowed (>=1), 
	 * 			where 1 disallows any distortion and e.g.
	 * 			2 allows for a maximal distortion to double or halve of the original length
	 * @param maxRelXShift the maximal relative shift of x-coordinates allowed [0,1], 
	 * 			where 0 disallows any distortion and 1 allows for maximal distortion 
	 * @param minRelIntervalLength the minimal relative length of an interval to be considered for further decomposition
	 * @throws NullArgumentException
	 * @throws OutOfRangeException if maxDistortionRatio < 1
	 * @throws OutOfRangeException if minRelIntervalLength < 0 || minRelIntervalLength > 1
	 */
	public MICA( SampledCurveDistance distanceFunction
			, double maxDistortionRatio
			, double maxRelXShift
			, double minRelIntervalLength ) 
			throws NullArgumentException, OutOfRangeException
	{
		if (distanceFunction == null) throw new NullArgumentException();
		if (maxDistortionRatio < 1) throw new OutOfRangeException(maxDistortionRatio, 1, Double.MAX_VALUE);
		if (maxRelXShift < 0 || maxRelXShift > 1) throw new OutOfRangeException(maxRelXShift, 0, 1);
		if (minRelIntervalLength < 0) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
		if (minRelIntervalLength > 1) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
		
		// setup data
		this.distanceFunction = distanceFunction;
		this.maxDistortionRatio = maxDistortionRatio;
		this.maxRelXShift = maxRelXShift;
		this.minRelIntervalLength = minRelIntervalLength;
		this.warpScaling = -1d;
	}
	
	/**
	 * Constructs an aligner that uses the given distance function and
	 * ensures that the during each iteration an interval length is not more
	 * distorted than the allowed ratio
	 * 
	 * @param distanceFunction the distance function to be used (!=null)
	 * @param maxDistortionRatio the maximal distortion ratio allowed (>=1), 
	 * 			where 1 disallows any distortion and e.g.
	 * 			2 allows for a maximal distortion to double or halve of the original length
	 * @param maxRelXShift the maximal relative shift of x-coordinates allowed [0,1], 
	 * 			where 0 disallows any distortion and 1 allows for maximal distortion 
	 * @param minRelIntervalLength the minimal relative length of an interval to be considered for further decomposition
	 * @param warpScaling the scaling factor when combining warping and distance
	 * @throws NullArgumentException
	 * @throws OutOfRangeException if maxDistortionRatio < 1
	 * @throws OutOfRangeException if minRelIntervalLength < 0 || minRelIntervalLength > 1
	 */
	public MICA( SampledCurveDistance distanceFunction
			, double maxDistortionRatio
			, double maxRelXShift
			, double minRelIntervalLength
			, double warpScaling ) 
					throws NullArgumentException, OutOfRangeException
	{
		if (distanceFunction == null) throw new NullArgumentException();
		if (maxDistortionRatio < 1) throw new OutOfRangeException(maxDistortionRatio, 1, Double.MAX_VALUE);
		if (maxRelXShift < 0 || maxRelXShift > 1) throw new OutOfRangeException(maxRelXShift, 0, 1);
		if (minRelIntervalLength < 0) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
		if (minRelIntervalLength > 1) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
		if (warpScaling < 0.00000001) throw new OutOfRangeException(warpScaling, 0.00000001, Double.MAX_VALUE);
		
		// setup data
		this.distanceFunction = distanceFunction;
		this.maxDistortionRatio = maxDistortionRatio;
		this.maxRelXShift = maxRelXShift;
		this.minRelIntervalLength = minRelIntervalLength;
		this.warpScaling = warpScaling;
	}
	
	
	/**
	 * Computes a progressive multiple alignment of the given curves. 
	 * The alignment is based on the pairwise alignment of consensus curves. 
	 * 
	 * The curves are scaled to equal length, where each curve length is weighted according to
	 * the number of curves represented by a consensus, see {@link PICA#getMeanLength(double, double, double, double)}.
	 * 
	 * @param curves an array of curves to align
	 * @return the final alignment including all subalignments in a tree data structure
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the curves are not compatible, i.e. show initially an incompatible decomposition
	 */
	public MicaData align( IntervalDecomposition... curves )
		throws NullArgumentException, IllegalArgumentException
	{
		// stop if computation is to be interrupted
		if (Thread.currentThread().isInterrupted())
			return null;
		
		// check if array given
		if (curves==null) throw new NullArgumentException();
		// check if array does not contain null
		if (Arrays.stream(curves).anyMatch( c -> c == null)) throw new NullArgumentException();
		// throw illegal argument exception if nothing is added
		if (curves.length == 0) throw new IllegalAccessError("no curve given to be aligned"); 
		// check all curves compatible to first curve
		if (Arrays.stream(curves).anyMatch(c -> !curves[0].isCompatible(c)) ) throw new IllegalArgumentException("given curves are incompatible");
		// check if all curve names differ (by adding them one by one to a HashSet)
		if ( ! Arrays.stream(curves).map( c -> c.getCurveOriginal().getName()).allMatch( new HashSet<String>()::add )) throw new IllegalArgumentException("some curve names are not unique");

		// create pairwise aligner
		PICA pica = null;
		if (warpScaling < 0) {
			pica = new PICA(distanceFunction,maxDistortionRatio,maxRelXShift,minRelIntervalLength);
		} else {
			pica = new PICA(distanceFunction,maxDistortionRatio,maxRelXShift,minRelIntervalLength,warpScaling);
		}
		
		// create distance handler
		ProgressiveAlignmentHandler handler = new ProgressiveAlignmentHandler( pica );
		
		// fill list of initial alignments = one curve per alignment
		// add to progressive alignment handler (computes automatically distances)
		IntStream.range(0, curves.length).forEach( c -> handler.addSubAlignment( new MicaData(curves[c]) ) );
		
		// fuse subalignments until all are fused to one overall alignment
		while ( handler.size() > 1 ) {
			
			// stop if computation is to be interrupted
			if (Thread.currentThread().isInterrupted())
				return null;
			
			// get next subalignment to be fused
			Pair<MicaData,MicaData> nextToFuse = handler.getMinDistPair();
			// get pairwise alignment information
			PICA.PicaData nextToFusePica = handler.getPairwiseAlignment( nextToFuse );
			
			// remove subalignments from handler
			handler.removeSubAlignment( nextToFuse.getLeft() );
			handler.removeSubAlignment( nextToFuse.getRight() );
			
			// create new fused alignment data
			MicaData newAlignment = fuseAlignments( nextToFuse.getLeft(), nextToFuse.getRight(), nextToFusePica );
			
			// stop if computation is to be interrupted
			if (Thread.currentThread().isInterrupted())
				return null;
			
			// add new alignment to handler
			handler.addSubAlignment( newAlignment );
		}

		// get final alignment
		MicaData finalAlignment = handler.getAlignments().get(0);
		
		// sort final alignment such that curve order equals input order
		setOriginalSorting( curves, finalAlignment.curves );
		
		// stop if computation is to be interrupted
		if (Thread.currentThread().isInterrupted())
			return null;
		
		// return final alignment including all according subalignments
		return finalAlignment;
	}
	/**
	 * Computes a pairwise alignment that minimizes the distance function for the
	 * two given curves.
	 * The alignment is based on the pairwise alignment of consensus curves. 
	 * 
	 * The curves are scaled to equal length, where each curve length is weighted according to
	 * the number of curves represented by a consensus, see {@link PICA#getMeanLength(double, double, double, double)}.
	 * 
	 * When a consensus is aligned with a consensus that represents the reference curve,
	 * only the weight of the reference is considered.
	 * 
	 * @param reference the reference to align all other curves to
	 * @param curves an array of curves to align
	 * @return the final alignment including all subalignments in a tree data structure
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the curves are not compatible, i.e. show initially an incompatible decomposition
	 */
	public MicaData alignToReference( IntervalDecomposition reference, IntervalDecomposition... curves )
			throws NullArgumentException, IllegalArgumentException
	{
		// check if array given
		if (curves==null) throw new NullArgumentException();
		// check if array does not contain null
		if (Arrays.stream(curves).anyMatch( c -> c == null)) throw new NullArgumentException();
		// throw illegal argument exception if nothing is added
		if (curves.length == 0) throw new IllegalAccessError("no curve given to be aligned"); 
		// check all curves compatible to first curve
		if (Arrays.stream(curves).anyMatch(c -> !curves[0].isCompatible(c)) ) throw new IllegalArgumentException("given curves are incompatible");
		// check if all curve names differ (by adding them one by one to a HashSet)
		if ( ! Arrays.stream(curves).map( c -> c.getCurveOriginal().getName()).allMatch( new HashSet<String>()::add )) throw new IllegalArgumentException("some curve names are not unique");
		
		// create pairwise aligner
		PICA pica = null;
		if (warpScaling < 0) {
			pica = new PICA(distanceFunction,maxDistortionRatio,maxRelXShift,minRelIntervalLength);
		} else {
			pica = new PICA(distanceFunction,maxDistortionRatio,maxRelXShift,minRelIntervalLength,warpScaling);
		}
		
		// create distance handler
		ProgressiveReferenceAlignmentHandler handler = new ProgressiveReferenceAlignmentHandler( pica, new MicaData(reference) );
		
		// fill list of initial alignments = one curve per alignment
		// add to progressive alignment handler (computes automatically distances)
		IntStream.range(0, curves.length).forEach( c -> handler.addSubAlignment( new MicaData(curves[c])) );
		
		// fuse subalignments until all are fused to one overall alignment
		while ( handler.size() > 1 ) {
			
			// stop if thread is interrupted
			if (Thread.currentThread().isInterrupted())
				return null;
			
			// get next subalignment to be fused
			Pair<MicaData,MicaData> nextToFuse = handler.getMinDistPair();
			// get pairwise alignment information
			PICA.PicaData nextToFusePica = handler.getPairwiseAlignment( nextToFuse );
			
			// check if we handle the reference
			boolean containsReference = handler.isReference( nextToFuse.getLeft()) || handler.isReference( nextToFuse.getRight());
			
			// remove subalignments from handler
			handler.removeSubAlignment( nextToFuse.getLeft() );
			handler.removeSubAlignment( nextToFuse.getRight() );
			
			// create new fused alignment data
			MicaData newAlignment = fuseAlignments( nextToFuse.getLeft(), nextToFuse.getRight(), nextToFusePica );
			
			// stop if thread is interrupted
			if (Thread.currentThread().isInterrupted())
				return null;
			
			// add new alignment to handler
			if (containsReference) {
				handler.addReference( newAlignment );
			} else {
				handler.addSubAlignment( newAlignment );
			}
		}

		// get final alignment
		MicaData finalAlignment = handler.getAlignments().get(0);
		
		// sort final alignment such that curve order equals input order
		setOriginalSorting( curves, finalAlignment.curves );
		
		// stop if computation is to be interrupted
		if (Thread.currentThread().isInterrupted())
			return null;
		
		// return final alignment including all according subalignments
		return finalAlignment;
	}

	
	/**
	 * Computes the fused overall alignment of the two subalignments al1 and al2 
	 * based on the pairwise alignment of their consensi.
	 * 
	 * @param al1 the first subalignment to be fused
	 * @param al2 the second subalignment to be fused
	 * @param alignment the pairwise alignment information for their consensi
	 * @return the multiple alignment container representing the fused alignment
	 * @throws NullArgumentException
	 */
	protected MicaData fuseAlignments(MicaData al1, MicaData al2, PICA.PicaData alignment ) 
					throws NullArgumentException
	{
		
		// stop if thread is interrupted
		if (Thread.currentThread().isInterrupted())
			return null;
		
		if (al1 == null || al2 == null || alignment == null) throw new NullArgumentException();
		if ( ! alignment.dec1.getCurveOriginal().equals(al1.consensus.getCurveOriginal()) ) throw new IllegalArgumentException("alignment dec1 is not about al1.consensus");
		if ( ! alignment.dec2.getCurveOriginal().equals(al2.consensus.getCurveOriginal()) ) throw new IllegalArgumentException("alignment dec2 is not about al2.consensus");

		// the fused alignment
		IntervalDecomposition[] fusedCurves = new IntervalDecomposition[al1.curves.size()+al2.curves.size()];

		// al1 curves : copy and update x-coordinates according to pairwise alignment data
		{
			// get relative positioning of consensus for x-coordinate localization
			double[] consRelPos = getRelativePositions( al1.consensus.getCurve().getX(), al1.consensus.getCurve().getXmin() );
			// get x-coordinate correction factors
			double[] consLengthRatio = getLengthRatio(alignment.dec1.getCurve().getX(), al1.consensus.getCurve().getX());
			// copy and update data
			for (int i=0; i<al1.curves.size(); i++) {
				
				// stop if thread is interrupted
				if (Thread.currentThread().isInterrupted())
					return null;

				// copy original subalignment data
				fusedCurves[i] = new IntervalDecomposition(al1.curves.get(i));
				// get x-shift
				double xShift = alignment.dec1.getCurve().getXmin()-al1.curves.get(i).getCurve().getXmin();
				// update x coordinates of fusedCurves[i]
				updateX(fusedCurves[i], consRelPos, consLengthRatio, al1.consensus.getCurve().length(), xShift);
			}
		}
		// al2 curves : copy and update x-coordinates according to pairwise alignment data
		{
			// get relative positioning of consensus for x-coordinate localization
			double[] consRelPos = getRelativePositions( al2.consensus.getCurve().getX(), al2.consensus.getCurve().getXmin() );
			// get x-coordinate correction factors
			double[] consLengthRatio = getLengthRatio(alignment.dec2.getCurve().getX(), al2.consensus.getCurve().getX());
			for (int i=0; i<al2.curves.size(); i++) {
				
				// stop if thread is interrupted
				if (Thread.currentThread().isInterrupted())
					return null;

				// position of i in fusedCurves
				int fi = i+al1.curves.size();
				// copy original subalignment data
				fusedCurves[fi] = new IntervalDecomposition(al2.curves.get(i));
				// get x-shift
				double xShift = alignment.dec2.getCurve().getXmin()-al2.curves.get(i).getCurve().getXmin();
				// update x coordinates of fusedCurves[fi]
				updateX(fusedCurves[fi], consRelPos, consLengthRatio, al2.consensus.getCurve().length(), xShift);
			}
		}
		
		// update spline information of the curves
		IntStream.range(0, fusedCurves.length).forEach( i -> fusedCurves[i].getCurve().updateInterpolation());
		
		
		// create new alignment container
		MicaData fusedData = new MicaData( fusedCurves );
		
		// store fusing information
		fusedData.fuseGuide = alignment;
		
		// add subaligments
		fusedData.fusedAlignments.add(al1);
		fusedData.fusedAlignments.add(al2);

		// stop if thread is interrupted
		if (Thread.currentThread().isInterrupted())
			return null;

		// create new alignment representation
		return fusedData;
	}
	
	/**
	 * Corrects the order of the curve set to be sorted to the order within
	 * the original curve set
	 * 
	 * @param curvesOriginal the curves that define the order
	 * @param curves2sort the curves to be sorted
	 */
	private void setOriginalSorting(
			final IntervalDecomposition[] curvesOriginal,
			List<IntervalDecomposition> curves2sort) 
	{
		
		if (curvesOriginal == null) throw new NullArgumentException();
		if (curves2sort == null) throw new NullArgumentException();
		
		// get position for each curve within original order
		int[] sortedPos = new int[curves2sort.size()];
		for (int i=0; i<curves2sort.size(); i++) {
			// get the curve to be found
			final AnnotatedCurve toFind = curves2sort.get(i).getCurveOriginal();
			// search for it
			for (int j=0; j<curvesOriginal.length; j++) {
				if (curvesOriginal[j].getCurveOriginal().equals(toFind)) {
					// store sorting position
					sortedPos[i] = j;
					break;
				}
			}
		}
		
		// sort
		for (int i=0; i+1<curves2sort.size(); i++) {
			// get current minimum in remaining range
			int curMinI = i;
			for (int j=curMinI+1; j<sortedPos.length; j++) {
				if (sortedPos[j] < sortedPos[curMinI]) {
					curMinI = j;
				}
			}
			// check if to be sorted
			if (curMinI != i) {
				// swap entries
				IntervalDecomposition tmp = curves2sort.get(i);
				curves2sort.set(i,curves2sort.get(curMinI));
				curves2sort.set(curMinI,tmp);
				// updated sorting position
				sortedPos[curMinI] = sortedPos[i];
			}
		}
		
	}

	/**
	 * Utility function to update the x-coordinates of a curve given the 
	 * relative position shift of the consensus derived from the pairwise
	 * alignment
	 * @param toBeUpdated the curve data to be updated
	 * @param consRelPos the relative x-coordinates of the original consensus
	 * @param consLengthRatio the length ratio factors to be applied to according x-coordinates from toBeUpdated
	 * @param curveLength the length of the curve (used for identification of equivalent x-coordinates)
	 * @param xShift the shift of all x-coordinates to be applied
	 */
	private void updateX( IntervalDecomposition toBeUpdated, double[] consRelPos, double[] consLengthRatio, double curveLength, double xShift )
	{
		double xMin = toBeUpdated.getCurve().getXmin();
		double[] curveiX = toBeUpdated.getCurve().getX();
		// shift all x-coordinates
		curveiX[0] += xShift; // special handling for first coordinate to reduce rounding issues
		for (int p=1; p<curveiX.length; p++) {
			// find ix[p] entry in consensus to get correct coordinate index
			int pInAl2Cons = getPosToInsert( consRelPos, curveiX[p]-xMin, curveLength );
			// check if not existing 
			if ( pInAl2Cons < 0 ) {
				throw new RuntimeException("could not identify x-coordinate "+String.valueOf(curveiX[p]-xMin)+" of curve "+toBeUpdated.getCurve().getName()+" within x-data : "+Arrays.toString(consRelPos));
			}
			// shift x-coordinate at position p given the absolute (xShift) and relative (length ratio) shift of the consensus
			curveiX[p] = xMin + xShift + ( (curveiX[p]-xMin) * (consLengthRatio[pInAl2Cons]) );
		}
	}
	
	
	/**
	 * Computes the length ratios (relative to first coordinate)
	 * 
	 *   (x1[i]-x1[0]) / (x2[i]-x2[0])
	 *   
	 * for all entries. Note, ratio[0] = 1.
	 * 
	 * @param x1 the first array of x-coordinates (should be sorted, or x1[0] should be minimum)
	 * @param x2 the second array of x-coordinates (should be sorted, or x2[0] should be minimum)
	 * @return the array or length ratios
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the lengths differ
	 */
	static protected double[] getLengthRatio( double[] x1, double[] x2 )
		throws NullArgumentException, IllegalArgumentException
	{
		if (x1 == null || x2 == null) throw new NullArgumentException();
		if (x1.length != x2.length) throw new IllegalArgumentException("x1 and x2 differ in lengths");
		// create ratio array
		double[] ratio = new double[x1.length];
		// ratio of first entry is 1 per definition (no length, ie. 0/0 computation)
		ratio[0] = 1;
		// compute entries
		IntStream.range(1,ratio.length).forEach( i -> ratio[i] = (x1[i]-x1[0])/(x2[i]-x2[0]) );
		// return final length ratios
		return ratio;
	}
	
	
	/**
	 * Localizes an entry within a sorted data set or the position 
	 * where it should be inserted. Two x-coordinates are handled as the same
	 * point according to {@link MicaPrecision#sameX(double, double, double)} given
	 * the length of the curve.
	 * 
	 * @param sortedData the sorted data to search in
	 * @param entry the entry to be found 
	 * @param curveLength the length of the curve
	 * @return an index >= 0 if the entry is within the sortedData (given the precisionDelta);
	 * 		or (-posToInsert-1) if not found, ie. the negated position where it should be inserted -1 
	 */
	static protected int getPosToInsert( double[] sortedData, double entry, double curveLength ) {
		
		// find ix[p] entry in al1 to get correct coordinate index
		int pos = Arrays.binarySearch( sortedData, entry );
		// check if not existing and to be inserted
		if ( pos < 0 ) {
			// get index where it would be inserted
			int posAbs = FastMath.abs(pos+1);
			// check if position at current position is within precisionDelta
			if ( posAbs < sortedData.length && MicaPrecision.sameX(sortedData[posAbs],entry,curveLength) ) {
				return posAbs;
			} else  
				// check if position in front is within precisionDelta
				if (posAbs > 0 && MicaPrecision.sameX(sortedData[posAbs-1],entry,curveLength) ) {
					return posAbs-1;
				}
		}
		// either it was found (>=0) or is to be inserted (<0)
		return pos;

	}
	
	/**
	 * Computes the relative positions for all entries in data and returns a new vector.
	 * @param data the position data of interest 
	 * @param anchorValue the anchor value to be used for position computation, i.e. relPos[i] = (data[i]-anchorValue)
	 * @return a new array holding the relative positions
	 */
	static protected double[] getRelativePositions( double[] data, double anchorValue ) {
		double[] newData = new double[data.length];
		// compute relative positions for data and store in newData
		IntStream.range(0, data.length).forEach( i -> newData[i] = data[i] - anchorValue );
		// return normalized data
		return newData;
	}

	/**
	 * Container to represent a (partial) alignment during the the progressive alignment. 
	 * 
	 * Each is represented by the consensus profile of the according aligned curves.
	 * 
	 * This consensus can then be used to progressively align this node with other nodes.
	 * 
	 * NOTE: if you manually change the list of curves, you have to 
	 * manually call {@link MicaData#computeConsensus()} as well!
	 * 
	 * @author Mmann
	 *
	 */
	public static class MicaData
	{
		/**
		 * aligned curves represented by this node
		 */
		public List< IntervalDecomposition > curves = new ArrayList<>();
		
		/**
		 * the consensus of the 
		 */
		public IntervalDecomposition consensus = null;
		
		/**
		 * list of subalignments that were fused to create this alignment
		 */
		public List< MicaData > fusedAlignments = new LinkedList<>();
		
		/**
		 * The pairwise alignment used to fuse the {@link #fusedAlignments}
		 */
		public PICA.PicaData fuseGuide = null;

		/**
		 * empty construction
		 */
		public MicaData()
		{
		}

		/**
		 * construction from array
		 * @param curves array of curves
		 */
		public MicaData( IntervalDecomposition... curves ) 
		{
			// store curves
			this.curves.addAll( Arrays.asList(curves) );
			// create consensus
			computeConsensus();
		}
		
		/**
		 * construction from list
		 * @param curves list of curves
		 */
		public MicaData( List<IntervalDecomposition> curves ) 
		{
			// store curves
			this.curves.addAll( curves );
			// create consensus
			computeConsensus();
		}
		
		/**
		 * computes the consensus for the current set of curves represented by this node
		 */
		public void computeConsensus()
		{
			this.consensus = MICA.getConsensusCurve( curves );
		}
		
		/**
		 * Recursive structure of the alignment guide tree
		 * @return String representation of the alignment guide tree
		 */
		public String getGuideTree() {
			String ret = "";
			// check if no children
			if (this.fusedAlignments.isEmpty()) {
				// get curve names
				ret += curves.stream().map( c -> c.getCurveOriginal().getName() ).collect( Collectors.joining(",") );
				return ret;
			} else {
				// else recursively call children for structure
				ret += "(" 
					+ fusedAlignments.stream().map( a -> a.getGuideTree() ).collect( Collectors.joining(",") )
					+ ")";
			}
			return ret;
		}
		
		
	}
	
	
	/**
	 * Handler that automatically updates the pairwise alignment distance
	 * table when sub-alignments are added or removed.
	 * 
	 * @author Mmann
	 *
	 */
	static protected class ProgressiveAlignmentHandler {
		
		/**
		 * Container that holds the 
		 */
		HashMap< Pair<MicaData,MicaData>, PICA.PicaData > distData = new HashMap<>();
		
		/**
		 * List of all subaligmments that are already stored/handled
		 */
		List< MicaData > alignments = new LinkedList<>();
		
		/**
		 * the pairwise aligner to be used for distance computations
		 */
		PICA aligner;
		
		/**
		 * Constructs a handler for the progressive alignment 
		 * @param pica the pairwise aligner to be used for distance computations
		 * @throws NullArgumentException
		 */
		public ProgressiveAlignmentHandler( PICA pica ) throws NullArgumentException {
			if (pica == null) throw new NullArgumentException();
			this.aligner = pica;
		}
		
		/**
		 * Number of subalignments handled so far
		 * @return the number of subalignments handled by this
		 */
		public int size() {
			return alignments.size();
		}

		/**
		 * Access to the pairwise alignment data for a given pair of subalignments
		 * @param pair the pair to get the data for
		 * @return the pairwise alignment data or null if the pair is unknown
		 */
		public PICA.PicaData getPairwiseAlignment( Pair<MicaData, MicaData> pair) {
			return distData.get(pair);
		}

		/**
		 * Removes a subalignment from the handler together with all according distances
		 * @param al the alignment to be removed
		 */
		public void removeSubAlignment( MicaData al ) {
			if (al == null) return;
			if (!alignments.contains( al )) return;
			// remove al from list of alignments
			alignments.remove(al);
			// remove all elements from distData that are about al
			for ( MicaData al2 : alignments ) {
				distData.remove( getOrderedPair( al, al2) );
			}
		}
		
		/**
		 * Adds a subalignment to the handler and automatically computes all
		 * pairwise distances to all already handled subalignments.
		 * 
		 * @param al the alignment to add
		 */
		public void addSubAlignment( MicaData al ) {
			if (al == null) return;
			// check if not already present (avoid duplicates
			if (alignments.contains( al )) return;
			// add distances to all already existing alignments
			// TODO maybe parallelize
			for( MicaData al2 : alignments) {
				// stop if computation is to be interrupted
				if (Thread.currentThread().isInterrupted())
					return;
				// get ordered pair
				Pair<MicaData,MicaData> key = getOrderedPair(al, al2);
				// compute distance on consensi with according weights and store pairwise alignment data 
				distData.put( key, aligner.align(key.getLeft().consensus, key.getLeft().curves.size(), key.getRight().consensus, key.getRight().curves.size())); 
			}
			// add all to list of alignments
			alignments.add(al);
		}
		
		/**
		 * Returns the current minimal distance pair of subalignments
		 * @return the pair of subalignment with the current minimal distance
		 */
		public Pair<MicaData, MicaData> getMinDistPair() {
			// get pair with minimal distance
			return distData.keySet().stream().min( (k1,k2) -> Double.compare(distData.get(k1).distance,distData.get(k2).distance) ).get();
		}
		
		/**
		 * Creates an ordered pair of two alignments.
		 * The order is based on the lex-order of the lex-smallest curve name from each alignment
		 * @param al1 the first alignment data to be paired
		 * @param al2 the second alignment data to be paired
		 * @return an ordered pair of al1 and al2
		 * @throws NullArgumentException
		 */
		protected static Pair<MicaData, MicaData> getOrderedPair( MicaData al1, MicaData al2) throws NullArgumentException {
			if (al1 == null || al2 == null) throw new NullArgumentException();
			// define order on lex order of lex-smallest curve name from each alignment
			int compareResult = al1.curves.stream().map( c -> c.getCurveOriginal().getName()).min(String::compareTo).get().compareTo( 
									al2.curves.stream().map( c -> c.getCurveOriginal().getName()).min(String::compareTo).get());
			if ( compareResult < 0 ) 
			{
				return Pair.of(al1, al2);
			} else {
				return Pair.of(al2, al1);
			}
		}
		
		/**
		 * Returns an unmodifiable view of the lists of subalignments currently handled
		 * @return immutable list of alignments handled right now
		 */
		public List<MicaData> getAlignments() {
			return Collections.unmodifiableList( alignments );
		}
		
	}
	
	static class ProgressiveReferenceAlignmentHandler extends ProgressiveAlignmentHandler {
		
		MicaData currentReference;
		
		/**
		 * Constructs a handler for the progressive alignment given a reference
		 * @param pica the pairwise aligner to be used for distance computations
		 * @param reference the reference to align to
		 * @throws NullArgumentException
		 */
		public ProgressiveReferenceAlignmentHandler( PICA pica, MicaData reference ) throws NullArgumentException {
			super(pica);
			this.currentReference = reference;
			this.addSubAlignment(reference);
		}
		
		@Override
		public void removeSubAlignment(MicaData al) {
			// check if we are removing the reference
			if (al!=null && this.currentReference!=null) {
				if (al.equals(this.currentReference)) {
					this.currentReference = null;
				}
			}
			// remove
			super.removeSubAlignment(al);
		}
		
		/**
		 * Adds the given sub-alignment and defines it as the new reference.
		 * Note, a RuntimeException is thrown if the old reference was not
		 * removed before calling this method!
		 * @param newRef the new reference sub-alignment
		 * @throws NullArgumentException
		 * @throws RuntimeException if the reference is still set (was not removed before)
		 */
		public void addReference(MicaData newRef) throws NullArgumentException, RuntimeException {
			if (newRef == null) throw new NullArgumentException();
			if (this.currentReference != null) throw new RuntimeException("trying to reset the reference while there is a non-null reference available");
			// store new reference
			this.currentReference = newRef;
			// add the new reference and compute pairwise distances
			addSubAlignment( newRef );
		}
		
		@Override
		public void addSubAlignment(MicaData al) {
			if (al == null) return;
			// check if not already present (avoid duplicates
			if (alignments.contains( al )) return;
			// add distances to all already existing alignments
			// TODO maybe parallelize
			for( MicaData al2 : alignments) {
				// stop if computation is to be interrupted
				if (Thread.currentThread().isInterrupted()) return;
				// get ordered pair
				Pair<MicaData,MicaData> key = getOrderedPair(al, al2);
				
				// get weights for each sub-alignment
				double weightLeft = key.getLeft().curves.size(), weightRight = key.getRight().curves.size();
				// update weights if one of the sub-alignments is the reference
				// -> weight of the other alignment == 0
				if (isReference(key.getLeft())) { weightRight = 0; }
				else if (isReference(key.getRight())) { weightLeft = 0; }
				
				// compute distance on consensi with according weights and store pairwise alignment data 
				distData.put( key, aligner.align(key.getLeft().consensus, weightLeft, key.getRight().consensus, weightRight)); 
			}
			// add al to list of alignments
			alignments.add(al);
		}
		
		/**
		 * Checks whether or not the given sub-alignment is the reference
		 * @param toCheck
		 * @return true if toCheck is the reference sub-alignment
		 * @throws NullArgumentException
		 */
		public boolean isReference( MicaData toCheck) throws NullArgumentException {
			if (toCheck==null) throw new NullArgumentException();
			if (this.currentReference == null) return false;
			return toCheck.equals( this.currentReference );
		}
		
	}
	

	/**
	 * Constructs the name of the consensus curve for the given curves.
	 * If no curve is given, the name is empty.
	 * If only one curve is given, the curve name is returned.
	 * For a set of curves, the consensus name is "c(name1,name2,...)"
	 * 
	 * @param curves the list of curves the name is for
	 * @return the consensus curve name
	 */
	static public String getConsensusName( List<IntervalDecomposition> curves ) {
		// no curve
		if (curves == null || curves.isEmpty()) {
			return "";
		}
		// one curve
		if (curves.size() == 1) {
			return curves.get(0).getCurveOriginal().getName();
		}
		// multiple curves -> c(name1,name2,...)
		return "[" + curves.stream().map( c -> c.getCurveOriginal().getName()).collect(Collectors.joining(",")) + "]";
	}
	
	
	/**
	 * Computes the consensus decomposition for the given curves.
	 * 
	 * If no curve is given, the consensus is null.
	 * If only one curve is given, a copy of the curve is returned as consensus.
	 * For a set of curves, the consensus is computed as the mean coordinate for 
	 * any coordinate present in one of the curves. The sets of annotations are also 
	 * joined for the consensus.
	 * 
	 * NOTE: the annotations are automatically generated and NOT derived from the aligned curves.
	 * 
	 * NOTE: only the annotation filters common to ALL curves are added to the consensus.
	 * 
	 * @param curves the set of curves a consensus has to be computed
	 * @return the consensus representing the curves; is null if no curve was given
	 */
	static public IntervalDecomposition getConsensusCurve( List<IntervalDecomposition> curves )
	{
		// no curve
		if (curves.isEmpty()) {
			return null;
		}
		
		// only one curve
		if (curves.size() == 1) {
			// consensus = copy of only curve
			return new IntervalDecomposition( curves.get(0) );
		}
		
		
		// ensure curves are compatible = same number of intervals
		if (! curves.stream().allMatch( c -> c.isCompatible( curves.get(0) ) )) 
			throw new IllegalArgumentException("curves are incompatible");
		// ensure all overall lengths are equal (precision delta 0.01)
		if (! curves.stream().allMatch( c -> FastMath.abs( c.getCurve().length() - curves.get(0).getCurve().length() ) <= 0.01 ) ) 
			throw new IllegalArgumentException("curves are not of equal lengths");
		// ensure all interval lengths are equal (precision delta 0.01)
		for (int i=0; i<curves.get(0).size(); i++) {
			final int pos = i;
			if (! curves.stream().allMatch( c -> FastMath.abs( c.getIntervalLength(pos) - curves.get(0).getIntervalLength(pos) ) <= 0.01 ) ) 
				throw new IllegalArgumentException("curves are not of equal lengths");
		}
		
		// get
		final double precisionDelta = curves.get(0).getCurve().length() * MicaPrecision.precisionDeltaLengthFactor;

		// initialize the consensus x-coordinates with first curve of list
		double[] consX = ArrayUtils.clone( curves.get(0).getCurve().getX() );
		// normalize x-coordinates to start with 0
		for (int i=0; i<consX.length; i++) {
			consX[i] -= curves.get(0).getCurve().getXmin();
		}
		
//		Debug.out.println(ArrayUtils.toString(consX));
		
		
		// store the indices of the annotations used for decomposition in the curves
		int[] consDecIdx = curves.get(0).getDecomposition().stream().mapToInt( ann -> ann.getIndex()).toArray();
		
//		Debug.out.println(ArrayUtils.toString(consDecIdx));
		
		// add missing coordinates that are part of other curves (!=1)
		for (int c=1; c<curves.size(); c++) {
			
			final double[] curX = curves.get(c).getCurve().getX();
//			CurveAnnotation.Type[] curA = curves.get(c).getCurveOriginal().getAnnotation();
//			int[] filteredCurAIndices = curves.get(c).getCurveOriginal().getFilteredAnnotations().stream().mapToInt( ann -> ann.getIndex()).toArray();
			
			// check all x coordinates if already present in curve
			// ignore first and last coordinate (should be 0 and length)
			for (int i=1; i+1<curX.length; i++) {
				
				// get relative position in X
				double curRelX = curX[i] - curves.get(c).getCurve().getXmin();
				// find index in curve where to put the coordinate
				int insertPos = Arrays.binarySearch( consX, curRelX );
				// check if not existing and to be inserted
				if ( insertPos < 0 ) {
					
					insertPos = FastMath.abs(insertPos+1);
					
					if ( (insertPos < consX.length &&  FastMath.abs(consX[insertPos]-curRelX)<=precisionDelta )
						|| (insertPos > 0 && FastMath.abs(consX[insertPos-1]-curRelX)<=precisionDelta )) 
					{
						continue;
					}
					
					// update decomposition index information
					// identify where is the insertPos within the decomposition
					int idxOfConsDexIdx = Arrays.binarySearch( consDecIdx, insertPos);
					// increase all decomposition anchors indices by one for all anchors with index >= insertPos
					for (int j=FastMath.abs(idxOfConsDexIdx); j<consDecIdx.length; j++){
						consDecIdx[j] += 1;
					}
					
					// insert x-coordinate (shift all following coordinates by one to the right)
					consX = ArrayUtils.add( consX, insertPos, curRelX );
					
				} else { // it was already present
					
					// update annotation if needed

				}
			}
		}
		
		
		// compute consensus y-coordinates
		double[] consY = new double[consX.length];
		for (int i=0; i<consY.length; i++) {
			// get mean y-coordinate for position
			for (int c=0; c<curves.size(); c++) {
				// sum y-coordinates for according positions
				consY[i] += curves.get(c).getCurve().getY( FastMath.min( consX[i]+curves.get(c).getCurve().getXmin(), curves.get(c).getCurve().getXmax() ) );
			}
			// normalize by number of curves
			consY[i] /= (double) curves.size();
		}
		
		// update x-coordinates with average start position
		double avgXmin = curves.stream().mapToDouble( c -> c.getCurve().getXmin() ).sum() / (double)curves.size();
		// shift x-coordinates to start at avgXmin
		for (int i=0; i<consX.length; i++) {
			consX[i] += avgXmin;
		}
		
		// create consensus curve data with automated annotation
		AnnotatedCurve consensus = new AnnotatedCurve( getConsensusName(curves)
				, consX
				, consY
				);
		// add split points from first curve
		// copy splits
		AnnotatedCurve curve0 = curves.get(0).getCurveOriginal();
//Debug.out.println("\nconsensus of "+consensus.getName()+" splits = ");
		for (int i=0; i<curve0.getAnnotation().length; i++) {
			// check if split point
			if (curve0.getAnnotation()[i].isIntervalBoundary()) {
				// find index of this point within consensus and copy annotation
				consensus.getAnnotation()[
                    consensus.getClosestPoint(
                    	// map aligned coordinate to consensus x-range
                    	curves.get(0).getCurve().getX()[i]
                		- curves.get(0).getCurve().getXmin()
                		+ avgXmin
                    )] = curve0.getAnnotation()[i];
//Debug.out.println(" "+consensus.getX()[consensus.getClosestPoint( curves.get(0).getCurve().getX()[i] - curves.get(0).getCurve().getXmin() + avgXmin )]);
			}
		}
		
		// add common filter (screen filters of first curve)
		for ( ObservableCurveAnnotationFilter f : curves.get(0).getCurveOriginal().getAnnotationFilter()) {
			// check if this filter if common among all curves
			boolean isCommon = true;
			for (int i=1; isCommon && i<curves.size(); i++) {
				isCommon = curves.get(i).getCurveOriginal().getAnnotationFilter().contains(f);
			}
			// add this filter if common among all curves
			if (isCommon) {
				consensus.addAnnotationFilter(f);
			}
		}
		
		
		// create IntervalDecomposition object for consensus
		IntervalDecomposition consensusDec = new IntervalDecomposition( consensus );
		
//		Debug.out.println(ArrayUtils.toString(consX));
//		Debug.out.println(ArrayUtils.toString(consA));
//		Debug.out.println(ArrayUtils.toString(consDecIdx));
		
		// final consensus decomposition
		return consensusDec;
	}
	
	
}
