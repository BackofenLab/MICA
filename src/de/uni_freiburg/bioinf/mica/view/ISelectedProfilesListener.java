package de.uni_freiburg.bioinf.mica.view;

/**
 * Interface which will be implemented by the parse view to get the number of
 * currently selected profiles from the selection table.
 * 
 * @author mbeck
 * 
 */
public interface ISelectedProfilesListener {
	/**
	 * Function interface to receive the actual number of selected profiles by
	 * the user.
	 * 
	 * @param number
	 *            The actual number of selected profiles.
	 */
	public void numberSelectedProfilesChanged(int number);
}
