package de.uni_freiburg.bioinf.mica.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.algorithm.IntervalDecomposition;

/**
 * Class FileFormatCsv which implements the IFileFormat interface. Provide a
 * load and save function to import a profile object from the hard disk or to
 * export a Curve to the hard disk. The file format is of the type comma
 * separated value
 * 
 * @author mbeck
 * 
 */
public class FileFormatCsv implements IFileFormat {

	/**
	 * Defines the separator value of the CSV file.
	 */
	private String separator = null;
	/**
	 * Reader object which is used to import profile data from a file.
	 */
	private BufferedReader br = null;
	/**
	 * Writer object to export profile data into a file.
	 */
	private BufferedWriter bw = null;

	/**
	 * Constructor of the CSV file format object. Initialized only the separator
	 * symbol.
	 */
	public FileFormatCsv(String delimiter) {
		separator = delimiter;
	}

	/**
	 * Function implementation from the IFileFormat interface. This function
	 * provides a interface to load a profile data object from a file on the
	 * hard disk.
	 * 
	 * @param file
	 *            Is the string to the file which contains the profile data.
	 * @return Is the profile object which contains the data from the imported
	 *         file.
	 */
	@Override
	public Curve load(String file, int col, boolean header) {
		/**
		 * Flag which indicates the the special case of the csv file
		 */
		boolean specialCase = isFileASpecialCase(file);
		/**
		 * Function internal attributes
		 */
		Curve profile = null;
		ArrayList<Double> data = null;
		String line = null;
		String headerName = null;
		try {
			br = new BufferedReader(new FileReader(file));
			/**
			 * Read until the end of file is reached.
			 */
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(separator);
				
				// skip line if not enough entries (e.g last column and last rows are without data)
				if (col >= entry.length)
					continue;
				
				/**
				 * Skip first line of the CSV file, because this is only header
				 * information.
				 */
				if (header) {
					header = false;
					/**
					 * If in special case the first column header name is empty
					 * take also the next one, otherwise use the first one
					 */
					if (specialCase
							&& entry[0].replaceAll("\\s+", "").isEmpty()) {
						headerName = new String(entry[col + 1]);
					} else {
						headerName = new String(entry[col]);
					}
				} else {
					if (data == null) {
						data = new ArrayList<Double>();
					}
					/**
					 * Read the profile values.
					 */
					try {
						/**
						 * In the special case access the next column
						 */
						if (!specialCase) {
							data.add(Double.parseDouble(entry[col]));
						} else {
							data.add(Double.parseDouble(entry[col + 1]));
						}
					} catch (NumberFormatException e) {
						/**
						 * Parsing for this column only until this point
						 */
						break;
					}
				}
			}
			/**
			 * Close and cleanup not needed objects.
			 */
			br.close();
			br = null;
			line = null;
		} catch (ArrayIndexOutOfBoundsException e) {
			/**
			 * Array out of bounds exception if the column index is larger than
			 * allowed. This case can be ignored.
			 */
				e.printStackTrace();
		} catch ( IOException e) {
		}

		/**
		 * Check if any data can be parsed at this point.
		 */
		if (data != null && data.size() == 0) {
			/**
			 * Avoid the construction of the profile object.
			 */
			data = null;
		}
		/**
		 * If the CSV file contains any data create the profile object and
		 * return.
		 */
		if (data != null) {
			// copy data to array
			double[] yData = new double[data.size()];
			final ArrayList<Double> tmp = data;
			IntStream.range(0, data.size()).forEach( i -> yData[i] = tmp.get(i));
			profile = new Curve(headerName, yData );
		}
		return profile;
	}

	/**
	 * Function implementation from the IFileFromat interface. This function
	 * provides an interface to export a profile object to the hard disk in the
	 * CSV file format.
	 * 
	 * @param file
	 *            Is the String to the file which will be created automatically.
	 *            After this function invocation this file contains the data
	 *            from the profile.
	 * @param pset
	 *            Is the profile set which will be exported into the defined
	 *            file on the hard disk.
	 */
	@Override
	public void save(String file, LinkedList<Curve> pset) {
		/**
		 * Create set for containing the string lines for export
		 */
		LinkedList<String> lines = new LinkedList<String>();
		/**
		 * Create the header line in the csv file and determine the maximum
		 * number of data lines
		 */
		String hdr = "";
		int maxDataLines = 0;
		for (int j = 0; j < pset.size(); j++) {
			Curve p = pset.get(j);
			/**
			 * Add separator for all columns expect the first one
			 */
			if (j != 0)
				hdr += separator;
			/**
			 * Add the profile name
			 */
			hdr += p.getName();
			/**
			 * Determine the maximum number of data lines
			 */
			if (maxDataLines < p.size())
				maxDataLines = p.size();
		}
		/**
		 * Add the header line
		 */
		lines.add(hdr);

		/**
		 * Create the lines for the data
		 */
		for (int i = 0; i < maxDataLines; i++) {
			String dataLine = "";
			for (int j = 0; j < pset.size(); j++) {
				Curve p = pset.get(j);
				/**
				 * Separators for all columns expect the first one
				 */
				if (j != 0)
					dataLine += separator;
				/**
				 * Add the data values
				 */
				if (i < p.size()) {
					dataLine += p.getY()[i];
				}
			}
			/**
			 * Add the line
			 */
			lines.add(dataLine);
		}

		/**
		 * Write the lines to the csv file
		 */
		try {
			bw = new BufferedWriter(new FileWriter(file));
			/**
			 * Over all generated lines strings
			 */
			for (String l : lines) {
				bw.write(l);
				bw.newLine();
			}
			/**
			 * Close and cleanup
			 */
			bw.close();
			bw = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public int getNumberCols(String file) {
		int numberCols = 0;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(file));
			/**
			 * Read until the end of file is reached.
			 */
			while ((line = br.readLine()) != null) {
				String[] entry = line.split(separator);
				numberCols = entry.length;
				break;
			}
			/**
			 * Close and cleanup not needed objects.
			 */
			br.close();
			br = null;
			line = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return numberCols;
	}

	/**
	 * Function to check if the input file has a special structure. This means
	 * that the file has a first column which doesn't contain usable data. The
	 * detection works by comparison of the first line columns and the second
	 * line columns. Are they different the function will indicate the file as
	 * special case.
	 * 
	 * @param file
	 *            Is the file for checking
	 * @return True if the file is written in a special case, false if the file
	 *         is written in a normal case.
	 */
	private boolean isFileASpecialCase(String file) {
		int numberCols = 0;
		String line = null;
		try {
			br = new BufferedReader(new FileReader(file));
			/**
			 * Count the columns in the first line of the file
			 */
			if ((line = br.readLine()) != null) {
				String[] entry = line.split(separator);
				for (String s : entry) {
					/**
					 * Replace all white spaces
					 */
					if (!s.replaceAll("\\s+", "").isEmpty())
						numberCols++;
				}
			}
			/**
			 * Count the columns in the second line of the file and subtract
			 * from the count attribute
			 */
			if ((line = br.readLine()) != null) {
				String[] entry = line.split(separator);
				for (String s : entry) {
					if (!s.isEmpty())
						numberCols--;
				}
			}
			/**
			 * Close and cleanup not needed objects.
			 */
			br.close();
			br = null;
			line = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		/**
		 * Return true if both lines have an unequal number of columns,
		 * otherwise return false
		 */
		if (numberCols != 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Writes the x-coordinates of the given curves in CSV format.
	 * 
	 * @param writer the writer to write the table data to
	 * @param curves the curves to get the data from
	 * @param newX whether or not to use the new coordinates or the original
	 * @param printHeader whether or not to print header information
	 * @throws IOException
	 */
	public void writeX(
			Writer writer, 
			List<IntervalDecomposition> curves,
			final boolean newX,
			final boolean printHeader
			) 
				throws IOException 
	{

		// write header if needed
		if (printHeader) {
			String header = "";
			for (IntervalDecomposition c : curves) {
				header += (header.isEmpty()?"":separator) + c.getCurveOriginal().getName();
			}
			writer.write(header+"\n");
		}
		
		// print row-wise the new x-coordinates
		// get maximal row number
		final int maxRows = curves.stream().mapToInt( c -> c.getCurve().size() ).max().getAsInt();
		for (int r=0; r<maxRows; r++) {
			String line = "";
			for (int i=0; i<curves.size(); i++) {
				// get current curve
				IntervalDecomposition c = curves.get(i);
				// print x-coordinate if r is within coordinate number range
				line += (i==0?"":separator);
				if (newX) {
					line += (c.getCurve().size()>r ? c.getCurve().getX()[r] : "");
				} else {
					line += (c.getCurveOriginal().size()>r ? c.getCurveOriginal().getX()[r] : "");
				}
			}
			writer.write(line+"\n");
		}

	}

}
