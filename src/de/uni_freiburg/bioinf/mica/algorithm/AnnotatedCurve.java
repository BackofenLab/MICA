package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;


public class AnnotatedCurve extends Curve
	implements Observer
{
	
	/**
	 * The annotation for each coordinate of the curve
	 */
	private CurveAnnotation.Type[] annotation;

	/**
	 * the list of annotation filters to be applied
	 */
	protected List<ObservableCurveAnnotationFilter> annotationFilter = new LinkedList<ObservableCurveAnnotationFilter>();
	
	/**
	 * list of annotation objects that have passed the filtering
	 */
	private List<CurveAnnotation> filteredAnnotations = null;

	
	/**
	 * Read access to the list of annotation filters
	 * @return the immutable annotation filter list
	 */
	public List<ObservableCurveAnnotationFilter> getAnnotationFilter() {
		return Collections.unmodifiableList(annotationFilter);
	}

	/**
	 * Adds an annotation filter to end of the internal list (applied last)
	 * @param annotationFilter the annotationFilter to add (!=null)
	 * @throws NullArgumentException 
	 */
	public void addAnnotationFilter(ObservableCurveAnnotationFilter annotationFilter) throws NullArgumentException {
		if (annotationFilter==null) throw new NullArgumentException();
		// add filter
		this.annotationFilter.add(annotationFilter);
		// register the curve as listener
		annotationFilter.addObserver(this);
		// clear filtered annotations since new filter present
		this.resetFilteredAnnotations();
	}

	/**
	 * Removes an annotation filter from the internal list if present
	 * @param annotationFilter the annotationFilter to remove (!=null)
	 * @return whether or not the filter was present
	 * @throws NullArgumentException 
	 */
	public boolean removeAnnotationFilter(ObservableCurveAnnotationFilter annotationFilter) throws NullArgumentException {
		if (annotationFilter==null) throw new NullArgumentException();
		// remove filter
		boolean wasPresent = this.annotationFilter.remove(annotationFilter);
		if (wasPresent) {
			// unregister this as listener
			annotationFilter.deleteObserver(this);;
			// clear filtered annotations since the filter was removed
			this.resetFilteredAnnotations();
		}
		return wasPresent;
	}
	
	/**
	 * resets the filteredAnnotations e.g. after a filter was added/removed
	 */
	public void resetFilteredAnnotations() {
		filteredAnnotations = null;
	}
	
	/**
	 * Creates an annotated curve object from an unannotated curve.
	 * 
	 * @param curve the curve to annotated
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 * @throws IllegalArgumentException if the index of one of the CurveAnnotation objects is out of range 
	 */
	public AnnotatedCurve( Curve curve )
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super( curve.getName(), curve.getX(), curve.getY() );
		
		// create new annotations
		this.annotation = new CurveAnnotation.Type[this.size()];
		// initialize with points
		Arrays.fill(this.annotation, CurveAnnotation.Type.IS_POINT);
		
		// define start and end
		this.annotation[0] = CurveAnnotation.Type.IS_START;
		this.annotation[this.annotation.length-1] = CurveAnnotation.Type.IS_END;
		
		// add local min/max annotations = priority over slope annotations 
		addAnnotations( this.annotation, getY(), CurveAnnotation.Type.IS_MINIMUM_AUTO, CurveAnnotation.Type.IS_MAXIMUM_AUTO, false );
		// add slope annotations but do not overwrite any other annotation
		addAnnotations( this.annotation, getSlope(), CurveAnnotation.Type.IS_DERIVATIVE_MINIMUM_AUTO, CurveAnnotation.Type.IS_DERIVATIVE_MAXIMUM_AUTO, false);
		// rewrite slope min/max to according inflection points
		relabelInflectionPoints( this.annotation, getSlope() );
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}
	
	/**
	 * Creates an annotated curve object copy
	 * 
	 * @param curve the curve to copy
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 * @throws IllegalArgumentException if the index of one of the CurveAnnotation objects is out of range 
	 */
	public AnnotatedCurve( AnnotatedCurve curve )
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super( curve.getName(), curve.getX(), curve.getY() );
		
		// copy annotations
		this.annotation = Arrays.copyOf( curve.annotation, this.size());
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}
	
	/**
	 * Creates an annotated curve object.
	 * 
	 * @param name the name of the curve
	 * @param xValues the x values of the coordinates 
	 * @param yValues the y values of the coordinates 
	 * @param annotations the list of annotations to be added (can be null or empty)
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 * @throws IllegalArgumentException if the index of one of the CurveAnnotation objects is out of range 
	 */
	public AnnotatedCurve(String name, double[] xValues, double[] yValues, List<CurveAnnotation> annotations )
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super(name, xValues, yValues);
		
		// copy annotations
		this.annotation = new CurveAnnotation.Type[this.size()];
		// initialize as points
		Arrays.fill(this.annotation, CurveAnnotation.Type.IS_POINT);
		// copy annotations
		if (annotations != null) {
			for ( CurveAnnotation ann : annotations) {
				// range check
				if( ann.getIndex() >= this.annotation.length) 
					throw new IllegalArgumentException("CurveAnnotation with index "+String.valueOf(ann.getIndex())+" exceeds coordinate number of "+String.valueOf(this.annotation.length));
				// set annotation
				this.annotation[ann.getIndex()] = ann.getType();
			}
		}
		// define start and end
		this.annotation[0] = CurveAnnotation.Type.IS_START;
		this.annotation[this.annotation.length-1] = CurveAnnotation.Type.IS_END;
		// final sanity check
		if (annotation[0] != CurveAnnotation.Type.IS_START) throw new IllegalArgumentException("annotation[0] != CurveAnnotation.Type.IS_START");
		if (annotation[annotation.length-1] != CurveAnnotation.Type.IS_END)  throw new IllegalArgumentException("annotation[0] != CurveAnnotation.Type.IS_END");
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}
	
	/**
	 * Creates an annotated curve object.
	 * 
	 * @param name the name of the curve
	 * @param xValues the x values of the coordinates 
	 * @param yValues the y values of the coordinates 
	 * @param annotations the annotation values for each coordinate
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 * @throws IllegalArgumentException if the first and last annotation is not 
	 *       {@link CurveAnnotation.IS_START} and {@link CurveAnnotation.IS_END} 
	 *       respectively
	 */
	public AnnotatedCurve(String name, double[] xValues, double[] yValues, int[] annotations )
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super(name, xValues, yValues);
		
		if (annotations[0] != CurveAnnotation.Type.IS_START.value) throw new IllegalArgumentException("annotations[0] != CurveAnnotation.Type.IS_START.value");
		if (annotations[annotations.length-1] != CurveAnnotation.Type.IS_END.value)  throw new IllegalArgumentException("annotations[0] != CurveAnnotation.Type.IS_END.value");
		
		// copy annotations
		this.annotation = new CurveAnnotation.Type[this.size()];
		IntStream.range(0, annotations.length).forEach( i -> this.annotation[i] = CurveAnnotation.Type.ofValue(annotations[i]) ); 
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}
	
	/**
	 * Creates an annotated curve object.
	 * 
	 * @param name the name of the curve
	 * @param xValues the x values of the coordinates 
	 * @param yValues the y values of the coordinates 
	 * @param annotations the annotations for each coordinate
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 * @throws IllegalArgumentException if the first and last annotation is not 
	 *       {@link CurveAnnotation.IS_START} and {@link CurveAnnotation.IS_END} 
	 *       respectively
	 */
	public AnnotatedCurve(String name, double[] xValues, double[] yValues, CurveAnnotation.Type[] annotations )
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super(name, xValues, yValues);
		
		if (annotations[0] != CurveAnnotation.Type.IS_START) throw new IllegalArgumentException("annotations[0] != CurveAnnotation.Type.IS_START.value");
		if (annotations[annotations.length-1] != CurveAnnotation.Type.IS_END)  throw new IllegalArgumentException("annotations[0] != CurveAnnotation.Type.IS_END.value");
		
		// copy annotations
		this.annotation = new CurveAnnotation.Type[this.size()];
		IntStream.range(0, annotations.length).forEach( i -> this.annotation[i] = annotations[i] ); 
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}
	
	/**
	 * Creates a curve object with annotations for each coordinate for
	 * local minimum/maximum or ascending/descending inflection point.
	 * 
	 * @param name the name of the curve
	 * @param xValues the x values of the coordinates 
	 * @param yValues the y values of the coordinates 
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 */
	public AnnotatedCurve(String name, double[] xValues, double[] yValues)
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super(name, xValues, yValues);
		
		// create new annotations
		this.annotation = new CurveAnnotation.Type[this.size()];
		// initialize with points
		Arrays.fill(this.annotation, CurveAnnotation.Type.IS_POINT);
		
		// define start and end
		this.annotation[0] = CurveAnnotation.Type.IS_START;
		this.annotation[this.annotation.length-1] = CurveAnnotation.Type.IS_END;
		
		// add local min/max annotations = priority over slope annotations 
		addAnnotations( this.annotation, getY(), CurveAnnotation.Type.IS_MINIMUM_AUTO, CurveAnnotation.Type.IS_MAXIMUM_AUTO, false );
		// add slope annotations but do not overwrite any other annotation
		addAnnotations( this.annotation, getSlope(), CurveAnnotation.Type.IS_DERIVATIVE_MINIMUM_AUTO, CurveAnnotation.Type.IS_DERIVATIVE_MAXIMUM_AUTO, false);
		// rewrite slope min/max to according inflection points
		relabelInflectionPoints( this.annotation, getSlope() );
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}
	
	/**
	 * Creates a curve object with annotations for each coordinate for
	 * local minimum/maximum or ascending/descending inflection point.
	 * 
	 * The x values of the coordinates are assumed to be uniformly distributed
	 * within the interval [0,(length[y]-1)]
	 *  
	 * @param name the name of the curve
	 * @param yValues the y values of the coordinates which are assumed to be uniformly distributed on x 
	 * @throws NullArgumentException see {@link Curve}
	 * @throws IllegalArgumentException see {@link Curve}
	 */
	public AnnotatedCurve(String name, double[] yValues)
			throws NullArgumentException, IllegalArgumentException {
		// call constructor of super-class
		super(name, yValues);
		
		// create annotation information
		this.annotation = new CurveAnnotation.Type[this.size()];
		// initialize as point data
		Arrays.fill(this.annotation, CurveAnnotation.Type.IS_POINT);
		
		// define start and end
		this.annotation[0] = CurveAnnotation.Type.IS_START;
		this.annotation[this.annotation.length-1] = CurveAnnotation.Type.IS_END;
		
		// add local min/max annotations = priority over slope annotations 
		addAnnotations( this.annotation, getY(), CurveAnnotation.Type.IS_MINIMUM_AUTO, CurveAnnotation.Type.IS_MAXIMUM_AUTO, false );
		// add slope annotations but do not overwrite any other annotation
		addAnnotations( this.annotation, getSlope(), CurveAnnotation.Type.IS_DERIVATIVE_MINIMUM_AUTO, CurveAnnotation.Type.IS_DERIVATIVE_MAXIMUM_AUTO, false);
		// rewrite slope min/max to according inflection points
		relabelInflectionPoints( this.annotation, getSlope() );
		
		// reset filtered annotation, since no filter was applied yet
		this.resetFilteredAnnotations();
	}

	/**
	 * Relabels automatically annotated slope min/max points with according
	 * inflection point annotations depending whether the point is within an
	 * ascent or descent.
	 * 
	 * In ascents and descents, only maxima and minima, resp., are annotated
	 * as inflection points. Minima in ascents and maxima in descents are 
	 * relabeled to point annotation.
	 * 
	 * @param annotation the annotations to update
	 * @param slope the according slope values for the annotations
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the data differ in length or have less than 2 entries
	 */
	private static void relabelInflectionPoints(CurveAnnotation.Type[] annotation, double[] slope)  throws NullArgumentException, IllegalArgumentException {
		// argument checks
		if (slope==null || annotation==null) throw new NullArgumentException();
		if (slope.length < 2) throw new IllegalArgumentException("length of slope < 2");
		if (slope.length != annotation.length) throw new IllegalArgumentException("slope and annotation differ in length");

		// overwrite slope min/max annotations with inflection point annotations
		for (int i=1; i+1<annotation.length; i++) {
			// skip manual annotations
			if (annotation[i].isManual()) {
				continue;
			}
			// check if this is a slope extremum
			if (annotation[i].isExtremumSlope()) 
			{
				// check whether we are in an ascent or descent
				// #### ASCENT + SLOPE MAX #####
				if (slope[i] > 0 && annotation[i]==CurveAnnotation.Type.IS_DERIVATIVE_MAXIMUM_AUTO) {
					// replace slope maxima in ascent with inflection
					annotation[i] = CurveAnnotation.Type.IS_INFLECTION_ASCENDING_AUTO;
				} else 
				// #### DESCENT + SLOPE MIN #####
				if (slope[i] < 0 && annotation[i]==CurveAnnotation.Type.IS_DERIVATIVE_MINIMUM_AUTO)  {
					// replace slope minima in descent with inflection
					annotation[i] = CurveAnnotation.Type.IS_INFLECTION_DESCENDING_AUTO;
				} else {
					// #### OVERWRITE REMAINING MIN/MAX WITH POINT  #####
					annotation[i] = CurveAnnotation.Type.IS_POINT;
				}
			}
		}
		
	}

	/**
	 * Computes the local minimum/maximum annotations for the provided coordinate data.
	 *   
	 * @param annotation the annotation array to be updated
	 * @param y the coordinate data to be used for annotation
	 * @param locMinAnnotation the annotation for a local minimum within y
	 * @param locMaxAnnotation the annotation for a local maximum within y
	 * @param overwrite whether or not it is allowed to overwrite automated annotations
	 * @throws NullArgumentException
	 * @throws IllegalArgumentException if the arrays differ in length or have less than 2 entries
	 */
	private static void addAnnotations( CurveAnnotation.Type[] annotation, double[] y, CurveAnnotation.Type locMinAnnotation, CurveAnnotation.Type locMaxAnnotation, boolean overwrite )
	throws NullArgumentException, IllegalArgumentException
	{
		// argument checks
		if (y==null || annotation==null) throw new NullArgumentException();
		if (y.length != annotation.length) throw new IllegalArgumentException("y and annotation differ in length");
		if (y.length < 2) throw new IllegalArgumentException("length of y < 2");
		
		// annotate rest from back to front
		for( int i=annotation.length-2; i>0; i-- ) {
			// going up (from back to front)
			if (y[i]>y[i+1]) {
				// local maximum
				if (y[i-1]<y[i]) {
					// check if to be overwritten and no user annotation
					if (annotation[i] == CurveAnnotation.Type.IS_POINT || (overwrite && CurveAnnotation.Type.compare(annotation[i],locMaxAnnotation)<0)) {
						annotation[i] = locMaxAnnotation;
					}
				} else
				// possible local maximum
				if (y[i-1]==y[i] && i>1) {
					// find next i that shows an y value different from y[i]
					int nextI = i-2;
					while(nextI>0 && y[nextI]==y[i]) {
						nextI--;
					}
					// middle of the interval [nextI,i] or left of middle if even length (int rounding = floor)
					int midI = i-((i-nextI)/2);
					// check if maximum
					if (y[nextI]<y[i]) {
						// check if to be overwritten and no user annotation
						if (annotation[midI] == CurveAnnotation.Type.IS_POINT || (overwrite && CurveAnnotation.Type.compare(annotation[midI],locMaxAnnotation)<0)) {
							annotation[midI] = locMaxAnnotation;
						}
					}
					// move i to end of checked interval
					i = nextI;
				}
			} else
			// going down (from back to front)
			if (y[i]<y[i+1]) {
				// local minimum
				if (y[i-1]>y[i]) {
					// check if to be overwritten and no user annotation
					if (annotation[i] == CurveAnnotation.Type.IS_POINT || (overwrite && CurveAnnotation.Type.compare(annotation[i],locMinAnnotation)<0)) {
						annotation[i] = locMinAnnotation;
					}
				} else
				// possible local minimum
				if (y[i-1]==y[i] && i>1) {
					// find next i that shows an y value different from y[i]
					int nextI = i-2;
					while(nextI>0 && y[nextI]==y[i]) {
						nextI--;
					}
					// middle of the interval [nextI,i] or left of middle if even length (int rounding = floor)
					int midI = i-((i-nextI)/2);
					// check if maximum
					if (y[nextI]>y[i]) {
						// check if to be overwritten and no user annotation
						if (annotation[midI] == CurveAnnotation.Type.IS_POINT || (overwrite && CurveAnnotation.Type.compare(annotation[midI],locMinAnnotation)<0)) {
							annotation[midI] = locMinAnnotation;
						}
					}
					// move i to end of checked interval
					i = nextI;
				}
			}
		}
	}

	/**
	 * Access to the non-filtered annotation information of the curve.
	 * A value > IS_POINT represents manual annotations
	 * 
	 * NOTE: if you are changing the annotation data, you have to call
	 * {@link #resetFilteredAnnotations()} afterwards!
	 * 
	 * @return the raw annotation data for this curve
	 */
	public CurveAnnotation.Type[] getAnnotation() {
		return annotation;
	}
	
	/**
	 * Provides an immutable view of the list of filtered annotations for 
	 * this curve
	 * @return the immutable list of annotations after filtering
	 */
	public List<CurveAnnotation> getFilteredAnnotations() {
		
		// check if the list has to be computed
		if (filteredAnnotations == null) {
			// create new list
			filteredAnnotations = new LinkedList<CurveAnnotation>();
			// add all non-zero annotations to list
			IntStream.range(0, annotation.length).filter(i -> annotation[i] != CurveAnnotation.Type.IS_POINT).forEach(i -> filteredAnnotations.add(new CurveAnnotation(this, i)));
			// apply all filters
			this.annotationFilter.stream().forEachOrdered( filter -> filter.filter( filteredAnnotations ));
		}
		
		return Collections.unmodifiableList(filteredAnnotations);
	}

	// Observer implementation for ObservableCurveAnnotationFilter
	@Override
	public void update(Observable o, Object arg) {
		// check if observer call for filter change
		if (o instanceof ObservableCurveAnnotationFilter) {
			// one of the filters was updated -> clear filtered data for recomputation
			resetFilteredAnnotations();
		}
	}
	
	
	@Override
	public String toString() {
		String ret = super.toString();
		ret += "\n" + getName() + ".A = ";
		ret += "{"+IntStream.range(0,size()).mapToObj(i->String.valueOf(getAnnotation()[i].value)).collect(Collectors.joining(","))+"}";
		return ret;
	}
	
	@Override
	public String toString(int digits) {
		String ret = super.toString(digits);
		ret += "\n" + getName() + ".A = ";
		ret += "{"+IntStream.range(0,size()).mapToObj(i->String.valueOf(getAnnotation()[i].value)).collect(Collectors.joining(","))+"}";
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		// check pointer
		if (this == obj) {
			return true;
		}
		// test class
		if (! (obj instanceof AnnotatedCurve)) {
			return false;
		}
		// cast
		AnnotatedCurve o = (AnnotatedCurve)obj;
		// check filter number
		if (this.annotationFilter.size() != o.annotationFilter.size()) {
			return false;
		}
		// more sophisticated checks
		if (super.equals(obj)) {
			// check annotation
			for (int i=0; i<this.size(); i++) {
				if (this.annotation[i] != o.annotation[i]) {
					return false;
				}
			}
			// check each filter
			for( int f=0; f<annotationFilter.size(); f++ ) {
				if ( ! this.annotationFilter.get(f).equals( o.annotationFilter.get(f) )) {
					return false;
				}
			}
			// seems to be equal
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void updateInterpolation() {
		// run the interpolation update
		super.updateInterpolation();
		// trigger an update of the annotations, since slope values have changed
		this.resetFilteredAnnotations();
	}
	
	/**
	 * ensures that the slope value of annotated maxima and minima is zero
	 */
	@Override
	protected void initSlope() {
		// run normal slope calculation
		super.initSlope();
		// update slope min/max
		slopeMin = Double.MAX_VALUE;
		slopeMax = Double.MIN_VALUE;
		// ensure maxima and minima have a slope of 0
		for (int i=0; i<this.size(); i++) {
			// check if pos i == extremum
			if (annotation[i].isExtremumY()) {
				// overwrite slope value to zero
				slope[i] = 0d;
			}
			// update min/max
			if (slope[i]<slopeMin) {
				slopeMin = slope[i];
			} 
			if (slope[i] > slopeMax) {
				slopeMax = slope[i];
			}
		}
	}

}
