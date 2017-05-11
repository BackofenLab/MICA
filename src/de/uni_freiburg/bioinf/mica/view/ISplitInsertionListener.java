package de.uni_freiburg.bioinf.mica.view;

/**
 * Interface for split completion notification. This interface will be called if
 * the users inserts a split point to the current profile.
 * 
 * @author mbeck
 * 
 */
public interface ISplitInsertionListener {
	/**
	 * This function is called from the profile data plot object if the current
	 * profile gets a split point by the user.
	 */
	public void splitCompleted();
}
