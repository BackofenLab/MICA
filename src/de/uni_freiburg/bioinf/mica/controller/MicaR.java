package de.uni_freiburg.bioinf.mica.controller;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation;
import de.uni_freiburg.bioinf.mica.algorithm.CurveExtremaFilter;
import de.uni_freiburg.bioinf.mica.algorithm.CurveInflectionFilter;
import de.uni_freiburg.bioinf.mica.algorithm.CurveMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.algorithm.CurveRmsdDistance;
import de.uni_freiburg.bioinf.mica.algorithm.IntervalDecomposition;
import de.uni_freiburg.bioinf.mica.algorithm.MICA;
import de.uni_freiburg.bioinf.mica.algorithm.MICA.MicaData;
import de.uni_freiburg.bioinf.mica.algorithm.SampledCurveDistance;
import de.uni_freiburg.bioinf.mica.algorithm.SlopeMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.algorithm.SlopeRmsdDistance;

public class MicaR implements AutoCloseable {

	static {
		// set locale to english globally
		Locale.setDefault( Locale.ENGLISH );
	}

	/**
	 * The list of curves currently to be aligned
	 */
	ArrayList<IntervalDecomposition> originalCurves = new ArrayList<>();
	
	/**
	 * Index of the reference curve within originalCurves or -1 if no reference was set.
	 */
	int referenceIndex = -1;
	
	/**
	 * The alignment of originalCurves; null if not computed so far
	 */
	MicaData alignment = null;
	
	/**
	 * The mica aligner to be used
	 */
	MICA mica;
	
	/**
	 * The distance function used for alignment
	 */
	SampledCurveDistance distanceFunction;
	
	/**
	 * The curve extrema filter registered to ALL curves
	 */
	CurveExtremaFilter curveExtremaFilter;
	
	/**
	 * The curve inflection point filter registered to ALL curves
	 */
	CurveInflectionFilter curveInflectionFilter;
	
	/**
	 * buffer to get debug output to R
	 */
	ByteArrayOutputStream debugBuffer;
	
	/**
	 * Creates an R MICA controller and initializes the according objects for alignment
	 * 
	 * @param distanceSelection the distance function to be used : 
	 * 			0 = curve RMSD, 
	 * 			1 = slope RMSD, 
	 * 			2 = curve mean absolute distance, 
	 * 			3 = slope mean absolute distance,
	 * @param distanceSamples the number of equidistant samples used for distance computation >= 1
	 * @param maxWarpingFactor the maximal warping factor allows during alignment >= 1
	 * @param maxRelXShift the maximal relative shift of x-coordinates allowed [0,1], 
	 * 			where 0 disallows any distortion and 1 allows for maximal distortion 
	 * @param minRelIntervalLength the minimal relative length of an interval to be considered for further decomposition, in [0,1]
	 * @param minRelMinMaxDist the value for the used CurveExtremaFilter, has to be in [0,1]
	 * @param distWarpScaling scaling factor for the warping factor when multiplied with the distance. set to 0 to disable warping correction
	 * @param minRelSlopeHeight minimal relative height of the slope of an inflection point to be kept from filtering
	 */
	public MicaR( 
			  int distanceSelection
			, int distanceSamples
			, double maxWarpingFactor
			, double maxRelXShift
			, double minRelIntervalLength
			, double minRelMinMaxDist
			, double distWarpScaling
			, double minRelSlopeHeight ) {
			
		try {
			// reset buffer
			debugBuffer = new ByteArrayOutputStream();
			try {
				Debug.out = new PrintStream( debugBuffer, true, StandardCharsets.UTF_8.name());
			} catch (Exception ex) 
			{
				Debug.out.println("MicaR : cannot redirect debug output to debugBuffer");
			}
			
			// input check
			if (distanceSamples < 1) throw new OutOfRangeException(distanceSamples, 1, Integer.MAX_VALUE);
			if (maxWarpingFactor < 1) throw new OutOfRangeException(maxWarpingFactor, 1, Double.MAX_VALUE);
			if (maxRelXShift < 0 || maxRelXShift > 1) throw new OutOfRangeException(maxRelXShift, 0, 1);
			if (minRelIntervalLength < 0 || minRelIntervalLength > 1) throw new OutOfRangeException(minRelIntervalLength, 0, 1);
			if (minRelMinMaxDist<0 || minRelMinMaxDist>1) throw new OutOfRangeException(minRelMinMaxDist, 0, 1);
			if (minRelSlopeHeight<0 || minRelSlopeHeight>1) throw new OutOfRangeException(minRelSlopeHeight, 0, 1);
			if (distWarpScaling < 0) throw new OutOfRangeException(distWarpScaling, 0, Double.MAX_VALUE);
			
			// set the distance function
			switch (distanceSelection) {
				case 0 : this.distanceFunction = new CurveRmsdDistance(distanceSamples); break;
				case 1 : this.distanceFunction = new SlopeRmsdDistance(distanceSamples); break;
				case 2 : this.distanceFunction = new CurveMeanAbsoluteDistance(distanceSamples); break;
				case 3 : this.distanceFunction = new SlopeMeanAbsoluteDistance(distanceSamples); break;
				default : throw new IllegalArgumentException("unknown distanceSelection '"+distanceSelection+"'");
			}
			
			// set filter value
			this.curveExtremaFilter = new CurveExtremaFilter( minRelMinMaxDist );
			this.curveInflectionFilter = new CurveInflectionFilter( minRelSlopeHeight );
	
			// create alignment object
			if (distWarpScaling < 0.00000001) {
				// no warping correction of distances
				mica = new MICA( this.distanceFunction, maxWarpingFactor, maxRelXShift, minRelIntervalLength );
			} else {
				// no warping correction of distances
				mica = new MICA( this.distanceFunction, maxWarpingFactor, maxRelXShift, minRelIntervalLength, distWarpScaling );
			}
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(MicaR construction) = "+ex.getMessage());
			clearAlignment();
		}

	}
	
	/**
	 * Resets the debug output to System.err.
	 */
	@Override
	public void close() throws Exception {
		Debug.out = System.err;
		debugBuffer.close();
	}
	
	/**
	 * Resets the debug output to System.err.
	 */
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		Debug.out = System.err;
		debugBuffer.close();
	}
	
	
	/**
	 * Access to the accumulated debug output and resets the debug output stream.
	 * 
	 * @return the debug output accumulated by MICA functions
	 */
	public String getDebugOutput() {
		String debugOutput = "";
		try {
			debugOutput = debugBuffer.toString(StandardCharsets.UTF_8.name());
			debugBuffer.reset();
		} catch (Exception ex) 
		{
			debugOutput = "ERROR: could not read debugBuffer";
		}
		return debugOutput;
	}
	
	
	/**
	 * Adds a curve to the set of curves to be aligned. The name is set to its index in originalCurves
	 * @param x the curve's x coordinates
	 * @param y the curve's y coordinates
	 * @throws RuntimeException if the reference was already set before
	 */
	public void addReferenceCurve( double[] x, double[] y ) throws RuntimeException {
		try {
			if (this.referenceIndex>=0) throw new RuntimeException("reference was already set and is curve with index "+String.valueOf(this.referenceIndex)); 
			// add the curve
			addCurve(x,y);
			// store the index of the reference
			this.referenceIndex = this.originalCurves.size()-1;
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(addReferenceCurve) = "+ex.getMessage());
			clearAlignment();
		}
	}

	/**
	 * Adds a curve to the set of curves to be aligned. The name is set to its index in originalCurves
	 * @param x the curve's x coordinates
	 * @param y the curve's y coordinates
	 */
	public void addCurve( double[] x, double[] y ) {
		try {
			if (x.length != y.length) throw new IllegalArgumentException("x and y differ in lengths");
			if (x.length < 3) throw new IllegalArgumentException("x and y have less than 3 entries");
			AnnotatedCurve newCurve = new AnnotatedCurve( String.valueOf(originalCurves.size()), x, y );
			newCurve.addAnnotationFilter( this.curveExtremaFilter );
			newCurve.addAnnotationFilter( this.curveInflectionFilter );
			originalCurves.add(new IntervalDecomposition( newCurve ) );
			clearAlignment();
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(addCurve) = "+ex.getMessage());
			clearAlignment();
		}
	}
	

	/**
	 * Removes the alignment computed so far
	 */
	protected void clearAlignment() {
		alignment = null;
	}
	
	
	/**
	 * Computes a new alignment of the originalCurves
	 */
	public void align() {
		
		try {
			if (this.distanceFunction == null) throw new RuntimeException("MicaR.align(): distanceFunction == null");
			
			// check if no reference was set
			if (this.referenceIndex < 0) {
				
				// create array of curves
				IntervalDecomposition[] curves = new IntervalDecomposition[originalCurves.size()];
				IntStream.range(0, originalCurves.size()).forEach(i -> curves[i] = originalCurves.get(i));
				// align
				alignment = mica.align( curves );
				
			} else {
				
				// create array of non-reference curves and pick the reference
				IntervalDecomposition reference = null;
				IntervalDecomposition[] curves = new IntervalDecomposition[originalCurves.size()-1];
				int ni = 0;
				for (int i=0; i<originalCurves.size(); i++) {
					if (i==referenceIndex) {
						// store reference
						reference = originalCurves.get(i);
					} else {
						// copy curve to be aligned
						curves[ni] = originalCurves.get(i);
						ni++;
					}
				}
				// align
				alignment = mica.alignToReference(reference, curves );
		
			}
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(align) = "+ex.getMessage());
			clearAlignment();
		}
	}
	
	/**
	 * Access to the slope BEFORE ALIGNMENT of a given curve 
	 * @param curveIdx the index of the curve
	 * @return the slope values for the coordinates of the curve BEFORE alignment
	 */
	public double[] getCurveSlope( int curveIdx ) {
		if (curveIdx < 0 || curveIdx >= originalCurves.size()) throw new OutOfRangeException(curveIdx, 0, originalCurves.size()-1);
		
		if (isAligned()) {
			return originalCurves.get(curveIdx).getCurveOriginal().getSlope();
		} else {
			// emergency handling
			throw new IllegalStateException("no alignment available yet, did you call 'align' yet?");
		}
	}
	
	/**
	 * Access to the slope AFTER ALIGNMENT of a given curve 
	 * @param curveIdx the index of the curve
	 * @return the slope values for the coordinates of the curve AFTER alignment
	 */
	public double[] getAlignedSlope( int curveIdx ) {
		if (curveIdx < 0 || curveIdx >= originalCurves.size()) throw new OutOfRangeException(curveIdx, 0, originalCurves.size()-1);
		
		if (isAligned()) {
			
			AnnotatedCurve curveOfInterest = originalCurves.get(curveIdx).getCurveOriginal();
			
			for (IntervalDecomposition d : alignment.curves) {
				// find curve with according name
				if (d.getCurveOriginal().equals(curveOfInterest)) {
					// return x values
					return d.getCurve().getSlope();
				}
			}
			// emergency handling
			throw new RuntimeException("could not find the aligned data for curve"+String.valueOf(curveIdx));
		} else {
			// emergency handling
			throw new IllegalStateException("no alignment available yet, did you call 'align' yet?");
		}
	}
	
	
	/**
	 * Access to the x-coordinates AFTER ALIGNMENT of a given curve 
	 * @param curveIdx the index of the curve
	 * @return the x-coordinates of the curve AFTER alignment
	 */
	public double[] getAlignedX( int curveIdx ) {
		if (curveIdx < 0 || curveIdx >= originalCurves.size()) throw new OutOfRangeException(curveIdx, 0, originalCurves.size()-1);
		
		if (isAligned()) {
			
			AnnotatedCurve curveOfInterest = originalCurves.get(curveIdx).getCurveOriginal();

			for (IntervalDecomposition d : alignment.curves) {
				// find curve with according name
				if (d.getCurveOriginal().equals(curveOfInterest)) {
					// return x values
					return d.getCurve().getX();
				}
			}
			// emergency handling
			throw new RuntimeException("could not find the aligned data for curve"+String.valueOf(curveIdx));
		} else {
			// emergency handling
			throw new IllegalStateException("no alignment available yet, did you call 'align' yet?");
		}
	}
	
	/**
	 * Tests whether or not an alignment was computed so far
	 * @return
	 */
	public boolean isAligned() {
		return alignment != null;
	}

	/**
	 * Access to the x-coordinates AFTER ALIGNMENT of the consensus
	 * @return the x-coordinates of the consensus curve
	 */
	public double[] getConsensusX() {
		if (isAligned()) {
			return alignment.consensus.getCurve().getX();
		} else {
			// emergency handling
			throw new IllegalStateException("no alignment available yet, did you call 'align' yet?");
		}
	}
	
	/**
	 * Access to the y-coordinates AFTER ALIGNMENT of the consensus
	 * @return the y-coordinates of the consensus curve
	 */
	public double[] getConsensusY() {
		if (isAligned()) {
			return alignment.consensus.getCurve().getY();
		} else {
			// emergency handling
			throw new IllegalStateException("no alignment available yet, did you call 'align' yet?");
		}
	}
	
	/**
	 * table of pairwise distances between all curve before alignment when made equal in length. 
	 * Only entries with i<j are computed. All other entries are set -1.
	 * @return the pairwise distances (>=0 entries are computed; -1 are ignored) 
	 */
	public double[][] getOriginalPairwiseDistances() {

		try {
			// collect original curves normalized to mean length
			Curve[] curves = new Curve[originalCurves.size()];
			// compute mean length
			double meanLength = originalCurves.stream().mapToDouble(d -> d.getCurveOriginal().length()).sum() / (double)originalCurves.size();
			for (int i=0; i<curves.length; i++) {
				// get length normalized x-values
				final double[] originalX = originalCurves.get(i).getCurveOriginal().getX();
				final double originalLength = originalCurves.get(i).getCurveOriginal().length();
				double[] correctedX = new double[originalX.length];
				IntStream.range(0, correctedX.length).forEach( p -> correctedX[p] = originalX[p]*meanLength/originalLength );
				// create copy of original curve with new mean length
				curves[i] = new Curve(originalCurves.get(i).getCurveOriginal().getName(), correctedX, originalCurves.get(i).getCurveOriginal().getY());
			}
			// get distance table
			return getPairwiseDistances( curves  );
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(getOriginalPairwiseDistances) = "+ex.getMessage());
		}
		return new double[0][0];
	}
	
	/**
	 * table of pairwise distances between all curve after alignment. 
	 * Only entries with i<j are computed. All other entries are set -1.
	 * @return the pairwise distances (>=0 entries are computed; -1 are ignored) 
	 */
	public double[][] getAlignedPairwiseDistances() {
		
		if (isAligned()) {
			try {
				// collect original curves
				Curve[] curves = new Curve[originalCurves.size()];
				for (int i=0; i<curves.length; i++) {
					// curve of interest for current index
					AnnotatedCurve curveOfInterest = originalCurves.get(i).getCurveOriginal();
					// find according aligned curve
					for (IntervalDecomposition d : alignment.curves) {
						// find curve with according name
						if (d.getCurveOriginal().equals(curveOfInterest)) {
							// store according aligned curve
							curves[i] = d.getCurve();
						}
					}
				}
				// get distance table
				return getPairwiseDistances( curves  );
			} catch (Exception ex) {
				Debug.out.println("EXCEPTION(getAlignedPairwiseDistances) = "+ex.getMessage());
			}
		}	
		return new double[0][0];
	}
	
	/**
	 * table of pairwise distances between all curves. 
	 * Only entries with i<j are computed. All other entries are set -1.
	 * @param curves the curves to compute the distances for
	 * @return the pairwise distances (>=0 entries are computed; -1 are ignored) 
	 */
	protected double[][] getPairwiseDistances( Curve[] curves) {
		try {
			if (this.distanceFunction == null) throw new RuntimeException("MicaR.align(): distanceFunction == null");
			if (curves == null) throw new NullArgumentException();
			if (curves.length == 0) throw new IllegalArgumentException("no curves given");
			if (IntStream.range(0, curves.length).anyMatch(i -> curves[i]==null)) throw new NullArgumentException();
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(getPairwiseDistances) = "+ex.getMessage());
			return new double[0][0];
		}
		
		// create an initialize distance matrix
		double[][] pairDist = new double[curves.length][];
		for (int r=0; r<pairDist.length; r++) {
			pairDist[r] = new double[curves.length];
		}
		// compute all pairwise distances
		for (int i=0; i<curves.length; i++) {
			pairDist[i][i] = -1; // not computed
			for (int j=i+1; j<curves.length; j++) {
				// compute and store distance
				try {
					pairDist[i][j] = this.distanceFunction.getDistance( curves[i], curves[j] );
				} catch (Exception ex) {
					Debug.out.println("EXCEPTION(getPairwiseDistances("+i+","+j+")) = "+ex.getMessage());
					pairDist[i][j] = -2;
				}
				// not computed
				pairDist[j][i] = -1;
			}
		}
		
		return pairDist;
	}

	/**
	 * Access to the alignment guide tree
	 * @return the NEWICK string representation of the alignment guide tree
	 */
	public String getGuideTree() {
		if (isAligned()) {
			try {
				return alignment.getGuideTree();
			} catch (Exception ex) {
				Debug.out.println("EXCEPTION(getGuideTree) : "+ex.getMessage());
				return "EXCEPTION(getGuideTree) : "+ex.getMessage();
			}
		}
		return "";
	}
	
	
	/**
	 * 
	 * @param x x-coordinates of the curve
	 * @param y y-coordinates of the curve
	 * @param minRelMinMaxDist the value for CurveExtremaFilter
	 * @return a vector that provides the type for each coordinate
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException
	 */
	public int[] getAnnotations( double[] x, double[] y ) throws NullArgumentException, IllegalArgumentException {
		try {
			
			if (x==null || y==null) throw new NullArgumentException();
			if (x.length != y.length) throw new IllegalArgumentException("x and y differ in length");
			if (x.length<3) throw new IllegalArgumentException("at least 3 data points have to be provided");
	
			// create curve with according filter
			AnnotatedCurve curve = new AnnotatedCurve("dummy", x, y);
			curve.addAnnotationFilter(this.curveExtremaFilter);
			curve.addAnnotationFilter(this.curveInflectionFilter);
			
			// copy filtered annotations
			int[] annotations = new int[x.length]; // < initializes with 0 == IS_POINT
			for (CurveAnnotation a : curve.getFilteredAnnotations()) {
				annotations[a.getIndex()] = a.getType().value;
			}
			
			// unlink filter from temporary curve
			curve.removeAnnotationFilter(this.curveExtremaFilter);
			curve.removeAnnotationFilter(this.curveInflectionFilter);
			
			// return annotations
			return annotations;
			
		} catch (Exception ex) {
			Debug.out.println("EXCEPTION(getAnnotations) = "+ex.getMessage());
			return new int[0];
		}
	}
	
}
