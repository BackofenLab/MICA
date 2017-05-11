package de.uni_freiburg.bioinf.mica.view;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation;

/**
 * Class for adding additional information around the profile data object. This
 * class is a composition of a color object and a profile data object. The
 * additional color information is only needed in the mica main view.
 * 
 * @author mbeck
 * @author Mmann
 * 
 */
public class ColoredAnnotatedCurve implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Object reference to the color for the profile
	 */
	private Color color;
	/**
	 * Reference to the profile data object.
	 */
	private AnnotatedCurve curve;
	/**
	 * whether or not this is a reference profile for alignment
	 */
	private boolean isReference = false;
	
	/**
	 * list of the indices of added split points
	 */
	private LinkedList<Integer> originalSplitIndex = new LinkedList<>();
	/**
	 * list of the original type of added split points
	 */
	private LinkedList<CurveAnnotation.Type> originalSplitAnnotation = new LinkedList<>();

	/**
	 * Constructor which stores the profile data reference and sets the initial
	 * color to black RGB(0,0,0).
	 * 
	 * @param curve
	 *            The reference to a profile data instance.
	 */
	public ColoredAnnotatedCurve(AnnotatedCurve curve) {
		this.color = new Color(0, 0, 0);
		this.curve = curve;
	}

	/**
	 * Constructor which stored the profile data reference and sets the initial
	 * color to the color of the second parameter.
	 * 
	 * @param curve
	 *            The reference to a profile data instance.
	 * @param c
	 *            The initial color of the profile.
	 */
	public ColoredAnnotatedCurve(AnnotatedCurve curve, Color c) {
		this.color = c;
		this.curve = curve;
	}


	
	/**
	 * Sets a new color for the profile.
	 * 
	 * @param c
	 *            The new color object
	 */
	public void setColor(Color c) {
		color = c;
	}

	/**
	 * Access function for getting the current color of the profile.
	 * 
	 * @return The color of the profile.
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Function to get the profile data object.
	 * 
	 * @return The profile data object
	 */
	public AnnotatedCurve getCurve() {
		return curve;
	}

	/**
	 * Function to insert a split point index to the profile. The set of split
	 * points will be sorted by using the access function.
	 * 
	 * @param splitIndex
	 *            The new split point index
	 */
	public void addSplitPoint(int splitIndex) {
		
		if (splitIndex >= 0 && splitIndex < curve.size()) {
			// store where added 
			originalSplitIndex.add(splitIndex);
			// store original annotation
			originalSplitAnnotation.add(curve.getAnnotation()[splitIndex]);
			// set split point in curve
			curve.getAnnotation()[splitIndex] = CurveAnnotation.Type.IS_SPLIT;
			// update filtered annotations
			curve.resetFilteredAnnotations();
		}
	}

	/**
	 * Function to get the sorted set of split points of this profile. Accessing
	 * this function returns a copy of the internal unsorted set of split points
	 * for this profile. But this copy will be sorted.
	 * 
	 * @return The set of split points
	 */
	public LinkedList<Integer> getSplitPoints() {
		LinkedList<Integer> sortedRet = new LinkedList<Integer>(originalSplitIndex);
		Collections.sort(sortedRet);
		return sortedRet;
	}

	/**
	 * Function to remove the last split point of the split point set if the set
	 * is non empty
	 * @return true if a split point was deleted; false otherwise
	 */
	public boolean removeLastSplitPoint() {
		if (!originalSplitIndex.isEmpty()) {
			// undo split add
			curve.getAnnotation()[originalSplitIndex.getLast()] = originalSplitAnnotation.getLast();
			// remove split information
			originalSplitIndex.removeLast();
			originalSplitAnnotation.removeLast();
			// report deletion
			return true;
		}
		// nothing deleted
		return false;
	}

	/**
	 * Function to clear the set of split points
	 */
	public void clearSplitPoints() {
		// iteratively remove all split points
		while (removeLastSplitPoint()) 
		{}
	}

	/**
	 * Defines whether or not this curve is to be used as a reference profile within 
	 * the alignment 
	 * @param isReference 
	 */
	public void setReferenceProfile(boolean isReference) {
		this.isReference = isReference;
	}
	
	/**
	 * Defines whether or not this curve is to be used as a reference profile within 
	 * the alignment 
	 * @return true if this curve is to be considered a reference for the alignment 
	 */
	public boolean isReferenceProfile() {
		return this.isReference;
	}
}
