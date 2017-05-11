/**
 * 
 */
package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.Observable;

/**
 * Default implementation of a {@link CurveAnnotationFilter} that implements a listener
 * interface such that changes of the filter options can be observed by other classes.
 * 
 * @author Mmann
 *
 */
public abstract class ObservableCurveAnnotationFilter extends Observable implements CurveAnnotationFilter 
{
	// use default observable implementation
}
