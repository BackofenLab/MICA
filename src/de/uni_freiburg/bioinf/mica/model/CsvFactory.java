package de.uni_freiburg.bioinf.mica.model;

import java.io.IOException;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.stream.IntStream;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.model.ImportExport.OutType;

/**
 * factory class to handle CSV related functionality
 * 
 * @author Mmann
 *
 */
public class CsvFactory {
	
	
	/**
	 * Exports the given curves in CSV format and flushes the output writer
	 * at the end. Missing data is represented by an empty string.
	 * 
	 * @param writer where to write the data
	 * @param curves the data to be exported, i.e. one column per curve data
	 * @param outPerCurve the type of data to be exported per curve
	 * @param delimiter the delimiter to be used to separate columns (whitespaces are not allowed)
	 * 
	 * @throws IOException in case there are problems with calling output.write()
	 * 
	 */
	static
	void
	exportCSV(	Writer output
				, LinkedList< Curve > curves
				, LinkedList< OutType > outPerCurve
				, char delimiter
				) throws IOException
	{
		// sanity checks
		if (output == null) { throw new InvalidParameterException("CsvFactory.exportCsv() : no output provided"); }
		if (curves == null) { throw new InvalidParameterException("CsvFactory.exportCsv() : no curves provided"); }
		if (outPerCurve == null) { throw new InvalidParameterException("CsvFactory.exportCsv() : no outPerCurve provided"); }
		if (!curves.stream().noneMatch( c -> c==null)) { throw new InvalidParameterException("CsvFactory.exportCsv() : curves contains null entries"); }
		if (curves.size() != outPerCurve.size()) { throw new RuntimeException("CsvFactory.exportCsv() : curves and output annotation differ in size"); }
		if (String.valueOf(delimiter).matches("^\\s$")) { throw new InvalidParameterException("CsvFactory.exportCsv() : whitespace charactes are not allowed as delimiter"); }
		// fast abort
		if (curves.isEmpty()) { return; }
		
		// get maximal length of a 
		int maxLength = curves.stream().flatMapToInt( c -> IntStream.of( c.size() ) ).max().getAsInt();
		
		// print header
		for (int c=0; c < curves.size(); c++) {
			// skip curves without output
			if (outPerCurve.get(c) == OutType.OutNone) {
				continue;
			}
			// add delimiter
			if (c > 0) {
				output.write(delimiter);
			}
			switch ( outPerCurve.get(c) ) {
			case OutX:
				output.write("X_"+curves.get(c).getName());
				break;
			case OutY:
				output.write(curves.get(c).getName());
				break;
			case OutXY:
				output.write("X_"+curves.get(c).getName());
				output.write(delimiter);
				output.write(curves.get(c).getName());
				break;
			case OutNone:
				break;
			}
		}
		// line break at end of header
		output.write('\n');
		
		// print data (row-wise)
		for (int i=0; i<maxLength; i++) {
			// print each column
			for (int c=0; c<curves.size(); c++) {
				// skip curves without output
				if (outPerCurve.get(c) == OutType.OutNone) {
					continue;
				}
				// add delimiter prefix
				if (c > 0) {
					output.write(delimiter);
				}
				// check if valid data point, if not ignore
				if (i < curves.get(c).size()) {
					switch ( outPerCurve.get(c) ) {
					case OutX:
						output.write(String.valueOf(curves.get(c).getX()[i]));
						break;
					case OutY:
						output.write(String.valueOf(curves.get(c).getY()[i]));
						break;
					case OutXY:
						output.write(String.valueOf(curves.get(c).getX()[i]));
						output.write(delimiter);
						output.write(String.valueOf(curves.get(c).getY()[i]));
						break;
					case OutNone:
						break;
					}
				} else {
					// write additional delimiter if needed
					if (outPerCurve.get(c) == OutType.OutXY) {
						output.write(delimiter);
					}
				}
			} // c = column
			// line break at end of row
			output.write('\n');
		} // i = row
		
		// flush output
		output.flush();
		
	}
		
}
