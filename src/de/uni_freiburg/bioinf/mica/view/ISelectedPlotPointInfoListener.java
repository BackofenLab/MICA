package de.uni_freiburg.bioinf.mica.view;

import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation;

/**
 * Interface for receive the information from a selected point in the profile
 * plot
 * 
 * @author mbeck
 * 
 */
public interface ISelectedPlotPointInfoListener {
	/**
	 * Function to get the new point information if the user selects another
	 * point in the profile plot
	 * 
	 * @param x
	 *            The x value of the selected plot point or Double.NaN if the selection was canceled
	 * @param y
	 *            The y value of the selected plot point
	 * @param t
	 *            The annotation of the plot point
	 */
	public void selectedPlotPointChanged(double x, double y, CurveAnnotation.Type t);
	
}
