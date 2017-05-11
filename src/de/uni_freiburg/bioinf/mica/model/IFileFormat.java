package de.uni_freiburg.bioinf.mica.model;

import java.util.LinkedList;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;

/**
 * Interface for the file formats. In general this interface consists of two
 * functions. The first one is the load interface which imports a profile object
 * from a file on the hard disk. The second interface it the save interface
 * which allows the export of a profile object into a file on the hard disk.
 * 
 * @author mbeck
 * 
 */
public interface IFileFormat {
	/**
	 * Interface to load a profile from the hard disk.
	 * 
	 * @param file
	 *            Is the file name which contains the profile on the hard disk.
	 * @param col
	 *            Is the column in the file where the profile data is located.
	 * @param header
	 *            Specified whether the header line is available in the file or
	 *            should be skipped.
	 * @return Is the imported profile object from the file.
	 */
	public Curve load(String file, int col, boolean header);

	/**
	 * Function to determine the number of columns in the file.
	 * 
	 * @param file
	 *            The file where the number of columns will be determined.
	 * 
	 * @return The number of columns
	 */
	public int getNumberCols(String file);

	/**
	 * This Interface exports a profile object to the hard disk.
	 * 
	 * @param file
	 *            Is the file which will be created to store the profile.
	 * @param pset
	 *            Is the set of profiles for exporting to the file.
	 */
	public void save(String file, LinkedList<Curve> pset);
}
