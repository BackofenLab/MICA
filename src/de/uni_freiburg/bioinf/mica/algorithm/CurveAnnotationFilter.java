package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.List;

import org.apache.commons.math3.exception.NullArgumentException;

/**
 * Generic interface to be implemented by filters on CurveAnnotations.
 * 
 * @author Mmann
 *
 */
public interface CurveAnnotationFilter {
	
	/**
	 * Filters all non-conform CurveAnnotations from the given list
	 * @param annotations the list to filter (!= null)
	 * @throws NullArgumentException
	 * @throws UnsupportedOperationException if the list is immutable
	 */
	public void filter( List<CurveAnnotation> annotations ) throws NullArgumentException, UnsupportedOperationException;

	/**
	 * Provides a string description of the filtering applied.
	 * @return the string description of the filtering done
	 */
	public String getDescription();

}
