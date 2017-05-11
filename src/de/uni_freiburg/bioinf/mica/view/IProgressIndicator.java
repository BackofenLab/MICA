package de.uni_freiburg.bioinf.mica.view;

import de.uni_freiburg.bioinf.mica.algorithm.MicaRunner;

/**
 * Interface for showing the state/progress of the current algorithm computation
 * 
 * @author mbeck
 * 
 */
public interface IProgressIndicator {
	
	enum Status {
		NOT_STARTED,
		RUNNING,
		FINISHED
	}
	
	/**
	 * Function to indicate the completion of a progress subtask
	 * @param current status
	 */
	public void updateProgressIndication( Status status);

	/**
	 * Starts MICA
	 * @param micaRunner the MICA runner to be started 
	 */
	public void startMica (MicaRunner micaRunner);

}
