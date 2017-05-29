package de.uni_freiburg.bioinf.mica.controller;

import joptsimple.OptionSet;

public interface MicaController {
	
	
	/**
	 * Version information
	 */
	public static final String version = "2.0.0";

	/**
	 * To describe on what basis the distance function is to be computed
	 */
	public static enum DistanceBase {
		Y_DATA, 
		SLOPE
	}
	
	/**
	 * supported (long) argument names for the MICA program call
	 */
	public static enum Arguments {
		curves,
		output,
		csvDelim,
		csvNoHeader,
		filterExtrema,
		filterInflect,
		distBase,
		distSamples,
		alnMinLength,
		alnMaxWarp,
		alnMaxShift,
		alnReference
	}

	/**
	 * Starts MICA in the according mode given the provided command line
	 * arguments 
	 * 
	 * @param options parsed commandline arguments
	 * @return the exit status of the run
	 * 
	 * @throws Exception in case an error occured, an exception is raised
	 */
	public void start( OptionSet options ) throws Exception;
	
}
