package de.uni_freiburg.bioinf.mica.model;

import java.util.LinkedList;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;

/**
 * Class which handles the import and export of profile objects. Is is possible
 * to switch the file format for the import and export mechanism. One possible
 * file format is the CSV file format.
 * 
 * @author mbeck
 * 
 */
public class ImportExport {
	/**
	 * Is the file format specification.
	 */
	IFileFormat f = null;

	/**
	 * Constructor of this object, which sets the default file format to CSV.
	 */
	public ImportExport() {
		/**
		 * Set default file format.
		 */
		setFileFormat(new FileFormatCsv(";"));
	}

	/**
	 * Function to change the file format to the desired one. To change the file
	 * format, the new file format hat so implement the IFileFormat interface.
	 * 
	 * @param format
	 *            Is the new file format specification.
	 */
	public void setFileFormat(IFileFormat format) {
		f = format;
	}

	/**
	 * Function to retrieve the current file format of the ImportExport object.
	 * 
	 * @return
	 */
	public IFileFormat getFileFormat() {
		return f;
	}

	/**
	 * Function to load a profile from a file on the hard disk. Before using
	 * this function the correct file format has to be specified in previous.
	 * 
	 * @param file
	 *            Is the file which contains the profile which shall be
	 *            imported.
	 * @param col
	 *            Specifies the column in the file where the data for this
	 *            profile is stored.
	 * @param hdr
	 *            Specifies whether the file contains a header line or not.
	 * @return Is the imported profile or null if something is going wrong
	 *         during the import step.
	 */
	public Curve load(String file, int col, boolean hdr) {
		Curve p = f.load(file, col, hdr);
		return p;
	}

	/**
	 * Function to get the number of columns in the file.
	 * 
	 * @param file
	 *            The file name.
	 * @return The number of columns.
	 */
	public int getNumberOfColumns(String file) {
		return f.getNumberCols(file);
	}

	/**
	 * Function to export a profile to the hard disk. Before using this function
	 * the file format has to be specified.
	 * 
	 * @param file
	 *            Is the file which contains after this function call the
	 *            profile which his data.
	 * @param pset
	 *            Is the profile for the export step.
	 */
	public void save(String file, LinkedList<Curve> pset) {
		f.save(file, pset);
	}

//	/**
//	 * Internal function to normalize the data of a profile. This function is
//	 * called after every load function call. Before returning the loaded
//	 * profile the data of the profile will be normalized.
//	 * 
//	 * @param data
//	 *            Is the data from the imported profile which will be normalized
//	 * @param Number
//	 *            of desired points in the result data set.
//	 * @return Is the normalized data of the profile.
//	 */
//	public ArrayList<Double> normalizeProfileData(ArrayList<Double> data,
//			int desiredPoints) {
//		return relativePercentageScale(amplitudeNormalization(data),
//				desiredPoints);
//	}

//	/**
//	 * Function to normalize the Y-axis of the data set. This is a amplitude
//	 * normalization which uses the mean value and the standard deviation to
//	 * modify each data point in the set.
//	 * 
//	 * @param data
//	 *            Is the input set with the profile data points
//	 * @return Is the modified set with the normalized amplitude values of the
//	 *         data set.
//	 */
//	private ArrayList<Double> amplitudeNormalization(ArrayList<Double> data) {
//		/**
//		 * Calculate needed parameters for amplitude normalization
//		 */
//		double meanValue = MeanValueOf(data);
//		double stdDeviation = StdDeviationOf(data);
//		/**
//		 * Apply amplitude normalization to each data point
//		 */
//		ArrayList<Double> amplitudeNormalize = new ArrayList<Double>();
//		for (Double d : data) {
//			double normalizedValue = (d - meanValue) / stdDeviation;
//			amplitudeNormalize.add(normalizedValue);
//		}
//		return amplitudeNormalize;
//	}

//	/**
//	 * Function to normalize the X-axis of the data set. This function converts
//	 * the data set from a absolute scale to a relative scale. After his
//	 * function call the number of data points in the set is equals to the
//	 * object internal definition of numberDesiredPointsInSet.
//	 * 
//	 * @param data
//	 *            Is the input data set of the profile.
//	 * @param desiredPoints
//	 *            is the number of desired points in the result.
//	 * @return Is the relative scale of the data set according to the desired
//	 *         number of data points.
//	 */
//	private ArrayList<Double> relativePercentageScale(ArrayList<Double> data,
//			int desiredPoints) {
//		/**
//		 * Derive a new set of data points according to the desired number of
//		 * data points (insert new points or remove)
//		 */
//		return ProfileUtil.interpolateDataPoints(data, desiredPoints);
//	}
//
//	/**
//	 * Function to calculate the mean value of from the amplitude data of the
//	 * profile.
//	 * 
//	 * @param data
//	 *            Is the data from the profile.
//	 * @return The mean value of the profile data.
//	 */
//	private double MeanValueOf(ArrayList<Double> data) {
//		double sum = 0;
//		/**
//		 * Sum all values up
//		 */
//		for (Double d : data) {
//			sum += d;
//		}
//		/**
//		 * Return the mean value
//		 */
//		return sum / data.size();
//	}

//	/**
//	 * Function to determine the standard deviation of the profile data.
//	 * 
//	 * @param data
//	 *            The data from the profile.
//	 * @return The standard deviation of the profile.
//	 */
//	private double StdDeviationOf(ArrayList<Double> data) {
//		/**
//		 * Calculate the mean value of the set
//		 */
//		double meanValue = MeanValueOf(data);
//
//		/**
//		 * Calculate the variant
//		 */
//		double varianz = 0;
//		for (Double d : data) {
//			varianz += Math.pow(d - meanValue, 2);
//		}
//		varianz = varianz / data.size();
//		varianz = Math.pow(varianz, 2);
//
//		/**
//		 * Return the standard deviation
//		 */
//		return Math.sqrt(varianz);
//	}
}
