package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.List;
import java.util.ListIterator;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;

import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation.Type;


/**
 * Removes all pairs of opposing extrema which have a relative y-axis difference
 * lower than a given threshold.
 * Furthermore, all inflection points (DERIVATIVE_MIN/MAX) enclosed by the pair
 * the be filtered are removed as well.
 * 
 * The filtering is done iteratively starting with the pair with minimal difference.
 * 
 * @author Martin Mann - 2016
 *
 */
public class CurveExtremaFilter extends ObservableCurveAnnotationFilter {
	
	/**
	 * The minimal relative difference an extremum has to have to its
	 * neighbored extrema of different type (range [0,1])
	 */
	private double minRelNeighDiff;
	
	/**
	 * Creates a filter for the provided minimal relative difference value.
	 * 
	 * @param minRelNeighDiff
	 * 		The minimal relative difference an extremum has to have to its
	 * 		neighbored extrema of different type (range [0,1]).
	 *      A value of 0 causes no filtering, a value of 1 almost complete filtering.
	 * @throws OutOfRangeException if {@link #minRelNeighDiff} is not in range [0,1]
	 */
	public CurveExtremaFilter( double minRelNeighDiff ) throws OutOfRangeException {
		setMinRelNeighDiff( minRelNeighDiff );
	}

	@Override
	public void filter(List<CurveAnnotation> annotations)
			throws NullArgumentException, UnsupportedOperationException {
		// check input
		if (annotations == null) throw new NullArgumentException();
		
		// check if something left to filter
		if (annotations.size()<2) {
			return;
		}
		
		// direct access to curve
		AnnotatedCurve curve = annotations.get(0).getCurve(); 

		// get min/max value of curve data
		double minY = curve.getYmin();
		double maxY = curve.getYmax();
		// get minimal absolute difference for filtering
		double minDeltaY = getMinRelNeighDiff()* (maxY - minY); 
		
		// apply filtering
		int annotationsSize = annotations.size();
		do {
			// update list size for this iteration
			annotationsSize = annotations.size();
			
			// objects to remove
			CurveAnnotation toRem1 = null;
			CurveAnnotation toRem2 = null;
			int a1 = annotationsSize;
			int a2 = annotationsSize;
			
			// find list position of first extremum
			for(int i=0; i<annotationsSize; i++) {
				if ( annotations.get(i).getType().isExtremumY() ) {
					a1 = i;
					break;
				}
			}
			// check if an extremum was found
			if (a1 >= annotationsSize) {
				// if not, nothing to be done, stop here
				break;
			}
			
			// find list position of next extremum of opposite type
			a2 = getNextOppositeAnnotation( annotations, a1 );
			// store first pair of extrema
			if (a2 < annotationsSize) {
				// store pair for later inspection
				toRem1 = annotations.get(a1);
				toRem2 = annotations.get(a2);
			}
				
			// find remaining pairs and check for smaller neighbor difference
			while (a2<annotationsSize) {
				
				// check if this pair of extrema has lower difference
				if (Math.abs(curve.getY()[annotations.get(a1).getIndex()]-curve.getY()[annotations.get(a2).getIndex()])
					< Math.abs(curve.getY()[toRem1.getIndex()]-curve.getY()[toRem2.getIndex()])) 
				{
					// store pair for later inspection
					toRem1 = annotations.get(a1);
					toRem2 = annotations.get(a2);
				}
				// find next pair
				a1 = a2;
				a2 = getNextOppositeAnnotation(annotations, a1);
			};
			
			// check if this minimal neighbor difference is below threshold
			if (toRem1 != null && toRem2 != null 
					&& minDeltaY >= Math.abs(curve.getY()[toRem1.getIndex()]-curve.getY()[toRem2.getIndex()]))
			{
				
				// ### remove all inflection points between the extrema as well as the extremum pair
				
				// get iterator BEFORE first minimum to remove
				ListIterator<CurveAnnotation> it = annotations.listIterator(annotations.indexOf(toRem1));
				// remove first extremum
				CurveAnnotation cur = it.next();
				it.remove();
				// iterate through list until we hit second minimum to remove
				while (it.hasNext()) {
					cur = it.next();
					// check if we found the second minimum, ie. the end of the back-to-front iteration
					if (cur.equals(toRem2)){
						// remove second extremum
						it.remove();
						// stop iteration
						break; 
					}
					// check if enclosed automatic annotated inflection point (to be deleted)
					if (cur.getType().isInflection()) 
					{
						// remove the inflection point from the list
						it.remove();
					}
				}
			}
			
		// start next iteration if needed
		} while (annotations.size() < annotationsSize);
	}
	
	/**
	 * Finds the next extremum annotation with opposite type if there is any
	 * @param annotations the list of annotations to search in
	 * @param currentIndex the index of the current annotation from the list to compare to
	 * @return the index of the next opposite extremum annotation or annotations.size() if none was found
	 * @throws NullArgumentException
	 * @throws OutOfRangeException if currentIndex is not valid for annotations
	 */
	private static int getNextOppositeAnnotation( List<CurveAnnotation> annotations, 
			int currentIndex ) throws NullArgumentException, OutOfRangeException
	{
		if (annotations==null) throw new NullArgumentException();
		if (currentIndex<0 || currentIndex>=annotations.size()) throw new OutOfRangeException(currentIndex, 0, annotations.size());
		// direct access to current element
		CurveAnnotation curAnnotation = annotations.get(currentIndex);
		// find next extremum
		for (int i=currentIndex+1; i<annotations.size(); i++) {
			if ( Type.isOpposite(curAnnotation.getType(), annotations.get(i).getType()) )
				return i;
		}
		// no opposite extremum found -> return list size
		return annotations.size();
	}
	
	@Override
	public String getDescription() {
		return "Removes iteratively the pairs of neighbored extrema of opposite type"
				+ " that show the smallest difference and which is smaller"
				+ " than the filtering threshold. For each filtered pair, enclosed"
				+ " inflection points are filtered too."
				;
	}
	
	/**
	 * Access to the filtering value, i.e. the minimal relative y-axis 
	 * difference between two neighbored extrema.
	 * @return the minimal relative y-axis difference between two neighbored extrema
	 */
	public double getMinRelNeighDiff() {
		return minRelNeighDiff;
	}

	/**
	 * Sets the minimal relative y-axis difference between two neighbored extrema to be
	 * used for filtering and informs all registered listeners about the change.
	 * A value of 0 causes no filtering, a value of 1 almost complete filtering.
	 * @param newRelMinNeighDiff the new filter value in [0,1]
	 * @throws OutOfRangeException if the filter value is not in interval [0,1]
	 */
	public void setMinRelNeighDiff(double newRelMinNeighDiff) {
		if (minRelNeighDiff != newRelMinNeighDiff) {
			if (newRelMinNeighDiff < 0.0 || newRelMinNeighDiff > 1.0) throw new OutOfRangeException(newRelMinNeighDiff, 0, 1);
			this.minRelNeighDiff = newRelMinNeighDiff;
			// inform listener that the filter settings have changed
			this.setChanged();
			this.notifyObservers(minRelNeighDiff);
		}
	}

}
