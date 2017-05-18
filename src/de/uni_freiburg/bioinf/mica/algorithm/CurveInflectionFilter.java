package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.List;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;


/**
 * Removes all inflection points where the relative scale of their absolute slope value 
 * is lower than a given threshold.
 * 
 * @author Martin Mann - 2016
 *
 */
public class CurveInflectionFilter extends ObservableCurveAnnotationFilter {
	
	/**
	 * The minimal relative height of the slope of an inflection point
	 * to be kept by the filtering (range [0,1])
	 */
	private double minRelHeight;
	
	/**
	 * Creates a filter for the provided minimal relative difference value.
	 * 
	 * @param minRelHeight
	 * 		The minimal relative height of the slope of an inflection point to
	 * 		be kept by the filtering.
	 *      A value of 0 causes no filtering, a value of 1 complete filtering.
	 * @throws OutOfRangeException if {@link #minRelHeight} is not in range [0,1]
	 */
	public CurveInflectionFilter( double minRelHeight ) throws OutOfRangeException {
		setMinRelHeight( minRelHeight );
	}

	@Override
	public void filter(List<CurveAnnotation> annotations)
			throws NullArgumentException, UnsupportedOperationException {
		// check input
		if (annotations == null) throw new NullArgumentException();
		
		// check if something to filter
		if (annotations.size() == 0) return;
		
		// get minimal slope value to be kept
		double minSlopeHeight = -1; 
		
		int i=0;
		while (i < annotations.size()) {
			
			// get current element to investigate
			CurveAnnotation a = annotations.get(i);

			// check if a is not an inflection point
			if (! a.getType().isInflection() ) {
				// skip this entry
				i++;
				continue;
			}
			
			// compute maxSlope if not done so far (delay computation until an inflection point was found)
			if (minSlopeHeight< 0) {
				// setup range
				minSlopeHeight = minRelHeight * FastMath.max( FastMath.abs(a.getCurve().getSlopeMax()), FastMath.abs(a.getCurve().getSlopeMin()) ); 
			}
			
			if (FastMath.abs(a.getCurve().getSlope()[a.getIndex()]) <= minSlopeHeight ) {
				// remove current inflection point annotation
				annotations.remove(i);
			} else {
				// go to next annotation
				i++;
			}
		}
		
	}
	

	@Override
	public String getDescription() {
		return "Removes all inflection points where the relative absolute slope value is"
				+ " below the given threshold."
				;
	}
	
	/**
	 * Access to the filtering value, i.e. the minimal relative height
	 * of the slope value of an inflection point.
	 * @return the minimal relative slope value of an inflection point to be kept
	 */
	public double getMinRelHeight() {
		return minRelHeight;
	}

	/**
	 * Sets the minimal relative y-axis difference between two neighbored extrema to be
	 * used for filtering and informs all registered listeners about the change.
	 * A value of 0 causes no filtering, a value of 1 almost complete filtering.
	 * @param minRelHeight the new filter value in [0,1]
	 * @throws OutOfRangeException if the filter value is not in interval [0,1]
	 */
	public void setMinRelHeight(double minRelHeight) {
		// check if changed
		if (this.minRelHeight != minRelHeight) {
			if (minRelHeight < 0.0 || minRelHeight > 1.0) throw new OutOfRangeException(minRelHeight, 0, 1);
			this.minRelHeight = minRelHeight;
			// inform listener that the filter settings have changed
			this.setChanged();
			this.notifyObservers(minRelHeight);
		}
	}

}
