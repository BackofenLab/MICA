package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;

import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation.Type;


/**
 * Interval decomposition information for a given {@link AnnotatedCurve} object.
 * 
 * The curve is decomposed based on annotated points.
 * 
 * Internally, a copy of the curve is maintained, which reflects the deformation of 
 * the curve, i.e. its x-coordinate changes.
 * 
 * @author Mmann
 *
 */
public class IntervalDecomposition {

	/**
	 * The curve this decomposition is for.
	 */
	private final AnnotatedCurve curveOriginal;
	
	/**
	 * The curve object deformed by the interval decomposition.
	 */
	private Curve curve;
	
	/**
	 * The list of curveOriginal annotations used as anchors for the decomposition
	 */
	private List<CurveAnnotation> decomposition = new LinkedList<>();
	
	
	/**
	 * Creates a new interval decomposition which comprises only of the interval between 
	 * start and end annotation of a curve and the annotations of type {@link CurveAnnotation#IS_SPLIT}.
	 *  
	 * @param curve the curve with annotations the decomposition is for
	 * @throws NullArgumentException 
	 * @throws IllegalArgumentException if the curve does not contain a start and end annotation
	 * @throws IllegalArgumentException if the curve length is <= 0
	 */
	public IntervalDecomposition( AnnotatedCurve curve ) throws NullArgumentException, IllegalArgumentException {
		
		// check data
		if (curve == null) throw new NullArgumentException();
		
		this.curveOriginal = curve;
		List<CurveAnnotation> annotations = curveOriginal.getFilteredAnnotations();
		if (annotations.size() < 2 || annotations.get(0).getType()!=CurveAnnotation.Type.IS_START || annotations.get(annotations.size()-1).getType()!=CurveAnnotation.Type.IS_END) throw new IllegalArgumentException("curve annotation start or end not correctly annotated (assumed to be first and last annotation)");
		
		// add start
		decomposition.add(annotations.get(0));
		// add end
		decomposition.add(annotations.get(annotations.size()-1));
		
		// copy annotated curve to working object
		this.curve = new Curve(this.curveOriginal.getName()+"'", this.curveOriginal.getX(), this.curveOriginal.getY());
		
		// check if we have to decompose already for given fixed split points
		for (CurveAnnotation an : annotations) {
			// check if current annotation is a split point
			if (an.getType() == CurveAnnotation.Type.IS_SPLIT) {
				// add annotation to according interval as new decomposition anchor
				decomposition.add(getInterval(curve.getX()[an.getIndex()])+1, an);
			}
		}
		
	}
	
	/**
	 * Creates a new interval decomposition which comprises only of the interval between 
	 * start and end annotation of a curve and the annotations of type {@link CurveAnnotation#IS_SPLIT}.
	 *  
	 * @param curve the curve with annotations the decomposition is for
	 * @param curveLength the length the curve should be scaled to
	 * @throws NullArgumentException 
	 * @throws IllegalArgumentException if the curve does not contain a start and end annotation
	 * @throws IllegalArgumentException if the curve length is <= 0
	 */
	public IntervalDecomposition( AnnotatedCurve curve, double curveLength ) throws NullArgumentException, IllegalArgumentException {
		
		// check data
		if (curve == null) throw new NullArgumentException();
		if (curveLength <= 0.0) throw new IllegalArgumentException("curve length <= 0");
		
		this.curveOriginal = curve;
		List<CurveAnnotation> annotations = curveOriginal.getFilteredAnnotations();
		if (annotations.size() < 2 || annotations.get(0).getType()!=CurveAnnotation.Type.IS_START || annotations.get(annotations.size()-1).getType()!=CurveAnnotation.Type.IS_END) throw new IllegalArgumentException("curve annotation start or end not correctly annotated (assumed to be first and last annotation)");
		
		// add start
		decomposition.add(annotations.get(0));
		// add end
		decomposition.add(annotations.get(annotations.size()-1));
		
		// copy annotated curve to working object
		this.curve = new Curve(this.curveOriginal.getName()+"'", this.curveOriginal.getX(), this.curveOriginal.getY());
		
		// check if we have to correct the length of the whole curve
		if (curveLength - getIntervalLength(0) > 0.0001) {
			warpIntervalLeft(0, curveLength/getIntervalLength(0));
			getCurve().updateInterpolation();
		}
		
		// check if we have to decompose already for given fixed split points
		for (CurveAnnotation an : annotations) {
			// check if current annotation is a split point
			if (an.getType() == CurveAnnotation.Type.IS_SPLIT) {
				// add annotation to according interval as new decomposition anchor
				decomposition.add(getInterval(curve.getX()[an.getIndex()])+1, an);
			}
		}
		
	}
	
	/**
	 * Copy constructor
	 * @param toCopy the object to make this a copy of
	 * @param curveLength the new overall length of the new decomposition
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the curve length is <= 0
	 */
	public IntervalDecomposition(IntervalDecomposition toCopy, double curveLength) throws NullArgumentException, IllegalArgumentException {
		if (toCopy==null) throw new NullArgumentException();
		if (curveLength <= 0.0) throw new IllegalArgumentException("curve length <= 0");
		
		// reference copies
		this.curveOriginal = toCopy.curveOriginal;
		// copy annotated curve to working object
		this.curve = new Curve(this.curveOriginal.getName()+"'", toCopy.getCurve().getX(), toCopy.getCurve().getY());
		// check if we have to correct the length of the whole curve
		if (curveLength - this.curve.length() > 0.0001) {
			// get factor for x-coordinate update
			double warpFactor = curveLength / this.curve.length();
			// update all x-coordinates excluding the first
			IntStream.range(1, this.curve.size()).forEach(i -> getCurve().getX()[i] *= warpFactor);
			// update spline
			getCurve().updateInterpolation();
		}
		// copy decomposition
		this.decomposition.addAll( toCopy.decomposition );
	}
	
	/**
	 * Copy constructor
	 * @param toCopy the object to make this a copy of
	 * @throws NullArgumentException
	 */
	public IntervalDecomposition(IntervalDecomposition toCopy) throws NullArgumentException {
		if (toCopy==null) throw new NullArgumentException();
		
		// reference copies
		this.curveOriginal = toCopy.curveOriginal;
		// copy annotated curve to working object
		this.curve = new Curve(this.curveOriginal.getName()+"'", toCopy.getCurve().getX(), toCopy.getCurve().getY());
		// copy decomposition
		this.decomposition.addAll( toCopy.decomposition );
	}
	
	/**
	 * Copies the data of {@link IntervalDecomposition} object into this.
	 * Note, the object to copy has to be a decomposition for the same original curve.
	 * 
	 * @param toCopy the object to make this a copy of
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if toCopy is representing a different original curve 
	 * @throws IllegalArgumentException if the curve length is <= 0
	 */
	public void copy( IntervalDecomposition toCopy, double curveLength ) throws NullArgumentException {
		if (toCopy==null) throw new NullArgumentException();
		if (!curveOriginal.equals(toCopy.curveOriginal)) throw new IllegalArgumentException("object 'toCopy' references a different original curve");
		if (curveLength <= 0.0) throw new IllegalArgumentException("curve length <= 0");

		// clear data structure
		this.decomposition.clear();

		// copy warped curve data (without allocation of new arrays)
		if (this.curve.size() != toCopy.curve.size()) throw new IllegalArgumentException("warped curve arrays differ in size");
		System.arraycopy( toCopy.curve.getX(), 0, this.curve.getX(), 0, toCopy.curve.size());
		System.arraycopy( toCopy.curve.getY(), 0, this.curve.getY(), 0, toCopy.curve.size());
		
		// check if we have to correct the length of the whole curve
		if (curveLength - this.curve.length() > 0.0001) {
			// get factor for x-coordinate update
			double warpFactor = curveLength / this.curve.length();
			// update all x-coordinates excluding the first
			IntStream.range(1, this.curve.size()).forEach(i -> this.curve.getX()[i] *= warpFactor);
		}
		// update spline information
		getCurve().updateInterpolation();
		
		// copy decomposition
		this.decomposition.addAll( toCopy.decomposition );
	}

	/**
	 * Corrects the x-coordinates within the interval, such that the interval length
	 * is newLength after the warping. This is done using the following update for
	 * all x-coordinates at index position p within the left/right interval boundaries l,r, respectively.
	 * Note, the left boundary is excluded from the warping and serves as warping anchor:
	 * 
	 *    x_new[l] = x[l]
	 *    x_new[p] = x_new[l] + (x[p]-x[l])*warpingFactor
	 *    
	 * NOTE: you have to call {@link #getCurve().updateSpline()} after calling this function
	 *  
	 * @param i index of the interval of interest
	 * @param warpingFactor the warping factor to be applied
	 * @throws OutOfRangeException if the index is no valid interval index
	 * @throws IllegalArgumentException if the warping factor is <= 0.0
	 */
	protected void warpIntervalLeft(int i, double warpingFactor) throws OutOfRangeException,IllegalArgumentException {
		// check data
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		if (warpingFactor <= 0.0) throw new IllegalArgumentException("warping factor <= 0.0");
		
		// check whether there is something enclosed
		if (getIntervalSize(i) < 2) {
			// interval is only one point
			return;
		}
		
		// get x coordinate of left interval boundary for correction
		double x_left = getCurve().getX()[getIntervalStart(i).getIndex()];
		
		// apply x-coordinate change to all points within the interval excluding the left boundary
		IntStream.rangeClosed(getIntervalStart(i).getIndex()+1, getIntervalEnd(i).getIndex()
				).forEach( p -> getCurve().getX()[p] = x_left + (getCurve().getX()[p]-x_left)*warpingFactor );
	}
	
	/**
	 * Corrects the x-coordinates within the interval, such that the interval length
	 * is newLength after the warping. This is done using the following update for
	 * all x-coordinates at position p within the left/right interval boundaries l,r, respectively.
	 * Note, the both boundaries are excluded from the warping and the right boundary serves as warping anchor:
	 * 
	 *    x_new[r] = x[r]
	 *    x_new[p] = x_new[r] - (x[r]-x[p])*warpingFactor
	 *  
	 * NOTE: you have to call {@link #getCurve().updateSpline()} after calling this function
	 *  
	 * @param i index of the interval of interest
	 * @param warpingFactor the warping factor to be applied
	 * @throws OutOfRangeException if the index is no valid interval index
	 * @throws IllegalArgumentException if the interval length is <= 0.0
	 */
	protected void warpIntervalRight(int i, double warpingFactor) throws OutOfRangeException,IllegalArgumentException {
		// check data
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		if (warpingFactor <= 0.0) throw new IllegalArgumentException("warping factor <= 0.0");
		
		// check whether there is something enclosed
		if (getIntervalSize(i) < 2) {
			// interval is only one point
			return;
		}

		// get x coordinate of left interval boundary for correction
		double x_right = getCurve().getX()[getIntervalEnd(i).getIndex()];
		
		// apply x-coordinate change to all points within the interval excluding the right boundary
		IntStream.rangeClosed( getIntervalStart(i).getIndex()+1, getIntervalEnd(i).getIndex()-1
				).forEach( p -> getCurve().getX()[p] = x_right - (x_right-getCurve().getX()[p])*warpingFactor );
	}
	
	/**
	 * Access to the CurveAnnotations used for the decomposition
	 * @return the annotations used for the decomposition
	 */
	public List<CurveAnnotation> getDecomposition() {
		return decomposition;
	}
	
	/**
	 * Access to the CurveAnnotations used for the decomposition
	 * @return the annotations used for the decomposition
	 */
	public String getDecompositionString() {
		return decomposition.stream().map( a -> String.valueOf(a.getIndex())+"("+Type.toString(a.getType())+")").collect(Collectors.joining(" "));
	}


	/**
	 * Access to the underlying curve object
	 * @return the curve this decomposition is about
	 */
	public AnnotatedCurve getCurveOriginal() {
		return curveOriginal;
	}
	
	/**
	 * Access to the warped curve object
	 * @return the curve warped (x-coordinate changed) by this decomposition
	 */
	public Curve getCurve() {
		return curve;
	}
	
	/**
	 * Access to the CurveAnnotation that marks the beginning of the interval
	 * 
	 * @param i the index of the interval of interest
	 * @return the annotation marking the beginning of the interval
	 * @throws OutOfRangeException if i is no valid interval index
	 */
	public CurveAnnotation getIntervalStart(int i) throws OutOfRangeException {
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		return this.getDecomposition().get(i);
	}
	
	/**
	 * Access to the CurveAnnotation that marks the end of the interval
	 * 
	 * @param i the index of the interval of interest
	 * @return the annotation marking the end of the interval
	 * @throws OutOfRangeException if i is no valid interval index
	 */
	public CurveAnnotation getIntervalEnd(int i) throws OutOfRangeException {
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		return this.getDecomposition().get(i+1);
	}
	
	/**
	 * Access to the current length of the i-th interval
	 * 
	 * @param i the index of the interval of interest
	 * @return the length of the i-th interval
	 * @throws OutOfRangeException if i is no valid interval index
	 */
	public double getIntervalLength(int i) throws OutOfRangeException {
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		return // X[i+1] - X[i] of warped curve
				this.curve.getX()[this.getIntervalEnd(i).getIndex()]
				- this.curve.getX()[this.getIntervalStart(i).getIndex()];
	}
	
	
	/**
	 * Access to the current number of coordinates within the i-th interval
	 * 
	 * @param i the index of the interval of interest
	 * @return the number of coordinates within the i-th interval
	 * @throws OutOfRangeException if i is no valid interval index
	 */
	public int getIntervalSize(int i) throws OutOfRangeException {
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		return // index[i+1] - index[i] + 1 
				this.getIntervalEnd(i).getIndex()
				- this.getIntervalStart(i).getIndex()
				+ 1 ;
	}
	
	
	/**
	 * Number of intervals within the decomposition 
	 * @return the decomposition size
	 */
	public int size() {
		return decomposition.size()-1;
	}
	
	/**
	 * Checks whether or not two decompositions are compatible, i.e. 
	 * are based on the same number of intervals and have the same 
	 * point annotations for the interval boundaries
	 * @param id2 the decomposition to compare to
	 * @return true if the number of intervals is equal and all 
	 * 		interval boundaries along the curves are of the same type
	 * @throws NullArgumentException 
	 */
	public boolean isCompatible(IntervalDecomposition id2) throws NullArgumentException {
		if (id2==null) throw new NullArgumentException();
		// check equal size of decompositions
		if (this.size() == id2.size()) {
			// check if all decomposition points are of alignable type
			return IntStream.range(0, this.decomposition.size()).allMatch(i -> Type.isAlignable(this.decomposition.get(i).getType(), id2.decomposition.get(i).getType()));
		}
		// not compatible
		return false; 
	}
	
	/**
	 * Access to the index of the interval covering the given x-coordinate.
	 * @param x the x-coordinate of interest
	 * @return the index of the interval covering the x-coordinate
	 * @throws OutOfRangeException if the x-coordinate is not within the curve data
	 */
	public int getInterval( double x ) throws OutOfRangeException {
		if (x<curve.getXmin() || x>curve.getXmax()) throw new OutOfRangeException(x, curve.getXmin(), curve.getXmax());
		// get minimal x-value
		double xSoFar = curve.getXmin();
		// screen all intervals
		for (int i=0; i<size(); i++) {
			// update right x-coordinate of current interval 
			xSoFar += getIntervalLength(i);
			// check if x is within current interval
			if (x <= xSoFar) {
				return i;
			}
		}
		// throw exception since sum of interval lengths + min(x) seems to be shorter than max(x)
		throw new RuntimeException("could not find x-coordinate "+String.valueOf(x)+" while last interval ends at "+String.valueOf(xSoFar)+" with max(x)="+String.valueOf(curve.getXmax()));
	}
	
	/**
	 * Decomposes an interval of the decomposition and returns a new
	 * 
	 * @param i index of the interval to decompose 
	 * @param splitPoint the annotated point within the interval to use for the decomposition
	 * @param newRelPosSplit the new relative position within the interval of the splitPoint
	 * 
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException
	 * @throws OutOfRangeException
	 */
	public void decompose(int i, CurveAnnotation splitPoint, double newRelPosSplit) 
		throws NullArgumentException, IllegalArgumentException, OutOfRangeException
	{
		// check data
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		if (newRelPosSplit<=0.0||newRelPosSplit>=1.0) throw new IllegalArgumentException("the relative position has to be the interval ]0,1[ excluding the boundaries");
		if (splitPoint==null) throw new NullArgumentException();
		if (!this.curveOriginal.getFilteredAnnotations().contains(splitPoint)) throw new IllegalArgumentException("the split point is not found in the filtered annotations of the underlying curve");
		if (this.getIntervalStart(i).getIndex()>splitPoint.getIndex() || this.getIntervalEnd(i).getIndex()<splitPoint.getIndex()) throw new IllegalArgumentException("split point not within the interval i, i.e. split index "+String.valueOf(splitPoint.getIndex())+" not in ["+String.valueOf(this.getIntervalStart(i).getIndex())+","+String.valueOf(this.getIntervalEnd(i).getIndex())+"]");
		
		// get current left interval length
		double fullIntervalLength = getIntervalLength(i);
		
		// add split point to decomposition (interval i becomes new left interval)
		this.decomposition.add(i+1, splitPoint);

		// get current left interval length
		double leftIntervalLength = getIntervalLength(i);
		double rightIntervalLength = fullIntervalLength - leftIntervalLength;
		
		// warp left interval segment (anchor = left boundary)
		this.warpIntervalLeft(i, newRelPosSplit/(leftIntervalLength/fullIntervalLength));
		// warp right interval segment (new interval i+1) (anchor = right boundary)
		this.warpIntervalRight(i+1, (1.0-newRelPosSplit)/(rightIntervalLength/fullIntervalLength));
		
		// update spline information
		getCurve().updateInterpolation();
	}
	
	/**
	 * Access to all curve annotations within the interval excluding its boundaries.
	 * @param i the index of the interval of interest
	 * @return the list of enclosed curve annotations or an empty list if none enclosed
	 * @throws OutOfRangeException if i is no valid index
	 */
	public List<CurveAnnotation> getIntervalAnnotations(int i) throws OutOfRangeException {
		// check data
		if (i<0||i>=this.size()) throw new OutOfRangeException(i, 0, this.size()-1);
		
		// get boundary positions within original list
		CurveAnnotation leftBoundary = getDecomposition().get(i);
		CurveAnnotation rightBoundary = getDecomposition().get(i+1);
		int leftIndex = this.curveOriginal.getFilteredAnnotations().indexOf(leftBoundary);
		int rightIndex = this.curveOriginal.getFilteredAnnotations().indexOf(rightBoundary);
		
		// get sublist if something is in-between
		if (leftIndex +1 < rightIndex) {
			return this.curveOriginal.getFilteredAnnotations().subList(leftIndex+1,rightIndex);
		}
		
		// empty list
		return new LinkedList<CurveAnnotation>();
	}
	
}
