package de.uni_freiburg.bioinf.mica.model;

/**
 * Exception class which indicates a duplicate profile name in the model.
 * 
 * @author mbeck
 * 
 */
public class DuplicateProfileNameException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor with message text for the exception
	 * 
	 * @param message
	 */
	public DuplicateProfileNameException(String message) {
		super(message);
	}
}
