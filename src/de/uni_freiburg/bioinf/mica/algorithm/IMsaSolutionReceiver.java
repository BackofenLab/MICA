package de.uni_freiburg.bioinf.mica.algorithm;


/**
 * Interface for receiving the solution from the completed alignment algorithm.
 */
public interface IMsaSolutionReceiver {
	/**
	 * Function for solution reception.
	 */
	public void receiveMultipleSplitAlignmentSolution(long duration,
			double distance, MICA.MicaData alignment);
}
