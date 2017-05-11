package de.uni_freiburg.bioinf.mica.controller;

/**
 * Solution distributor interface. This interface is used to trigger the
 * distribution of an existing alignment result.
 */
public interface ISolutionDistributor {
	/**
	 * Function to start the distribution of the alignment solution
	 */
	public void distributeSolution();
}
