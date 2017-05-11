package de.uni_freiburg.bioinf.mica.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.UIManager;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import de.uni_freiburg.bioinf.mica.algorithm.DoubleRange;
import de.uni_freiburg.bioinf.mica.controller.MicaController.Arguments;
import de.uni_freiburg.bioinf.mica.controller.MicaController.DistanceBase;

/**
 * Main class which either starts the command line interface or the
 * graphical user interface 
 * 
 * @author mbeck
 * @author Martin Mann
 * 
 */
public class MicaMain {
	
	
	static {
		// set locale to english globally
		Locale.setDefault( Locale.ENGLISH );
	}

	private static Map<Arguments,DoubleRange> setupArgumentParser( OptionParser parser ) {
		
		parser.posixlyCorrect(true);
		
		Map<Arguments,DoubleRange> ranges = new HashMap<Arguments, DoubleRange>();
		
		// TODO : maybe allow several input files or STDIN for one alignment
		parser.accepts(Arguments.curves.toString(),
				"Optional file name of the CSV file holding the curves to be align ('csvDelim' separated columns; equidistant y-coordinates assumed)."
				+ " If present, the alignment will be computed and written to 'output'."
				+ " If absent, the graphical user interface is started.")
			.withRequiredArg()
			.ofType( File.class )
			.describedAs("CSV")
			;
	
		parser.accepts(Arguments.output.toString(),
				"File name of the CSV file the new x-coordinates for each aligned y-coordinate is written to. If not provided, the alignment is written to the standard output stream.")
			.withRequiredArg()
			.ofType( File.class )
			.describedAs("CSV")
			;
	
		parser.accepts(Arguments.csvDelim.toString(),
				"The column deliminator to be used for CSV parsing and writing.")
				.withRequiredArg()
				.defaultsTo( ";")
				;
		
		parser.accepts(Arguments.csvNoHeader.toString(),
				"If present, the CSV files are parsed and written WITHOUT column headers.")
				;
		
		ranges.put(Arguments.filterExtrema, new DoubleRange(0, 1));
		parser.accepts(Arguments.filterExtrema.toString(),
				"Minimal difference of neighbored extrema (relative scale in range "+ranges.get(Arguments.filterExtrema)+") to be considered for alignment")
				.withRequiredArg()
				.ofType( Double.class )
				.defaultsTo(0.01)
				;
		
		ranges.put(Arguments.filterInflect, new DoubleRange(0, 1));
		parser.accepts(Arguments.filterInflect.toString(),
				"Minimal absolute slope of an inflection point (relative scale in range "+ranges.get(Arguments.filterInflect)+" to be considered for alignment")
				.withRequiredArg()
				.ofType( Double.class )
				.defaultsTo(0.01)
				;
		
		parser.accepts(Arguments.distBase.toString(),
				"The base on what to compute the distance function: "+Arrays.toString(DistanceBase.values()))
				.withRequiredArg()
				.ofType( DistanceBase.class )
				.defaultsTo(DistanceBase.SLOPE)
				;
		
		ranges.put(Arguments.distSamples, new DoubleRange(0, 99999));
		parser.accepts(Arguments.distSamples.toString(),
				"The number of equidistant x-coordinates to be used for distance calculation (range "+ranges.get(Arguments.distSamples)+")")
				.withRequiredArg()
				.ofType( Integer.class )
				.defaultsTo(100)
				;
		
		ranges.put(Arguments.alnMinLength, new DoubleRange(0,1));
		parser.accepts(Arguments.alnMinLength.toString(),
				"Minimal relative length of an interval to be considered for further decomposition (range "+ranges.get(Arguments.alnMinLength)+")")
				.withRequiredArg()
				.ofType( Double.class )
				.defaultsTo(0.05)
				;
		
		ranges.put(Arguments.alnMaxWarp, new DoubleRange(0,999));
		parser.accepts(Arguments.alnMaxWarp.toString(),
				"Maximal interval length warping factor (i.e. max{old/new,new/old}) (range "+ranges.get(Arguments.alnMaxWarp)+")")
				.withRequiredArg()
				.ofType( Double.class )
				.defaultsTo(2.0)
				;
		
		ranges.put(Arguments.alnMaxShift, new DoubleRange(0,1));
		parser.accepts(Arguments.alnMaxShift.toString(),
				"Maximal allowed relative x-coordinate shift per alignment (range "+ranges.get(Arguments.alnMaxShift)+")")
				.withRequiredArg()
				.ofType( Double.class )
				.defaultsTo(0.2)
				;
		
		ranges.put(Arguments.alnReference, new DoubleRange(1,999));
		parser.accepts(Arguments.alnReference.toString(),
				"Optional column index (>=1) of the curve to designate as reference for the alignment)")
				.withRequiredArg()
				.ofType( Integer.class )
				;
		
		parser.acceptsAll( Arrays.asList( "h","?","help" ), "show help" )
			.forHelp()
			;
		
		parser.acceptsAll( Arrays.asList( "v","version" ), "show version information" )
		;
		
		return ranges;
	}
	
	/**
	 * Checks for each key argument from ranges if the according option is present.
	 * If present, it is checked whether or not it is within the allowed ranges.
	 * If not, an OutOfRangeException is thrown.
	 *   
	 * @param options
	 * @param ranges
	 * @throws IllegalArgumentException
	 */
	private static void checkRanges( OptionSet options, Map<Arguments,DoubleRange> ranges )
	throws IllegalArgumentException
	{
		for ( Arguments arg : ranges.keySet() ) {
			if (options.has(arg.toString())) {
				Double val = (Double)options.valueOf(arg.toString());
				DoubleRange range = ranges.get(arg);
				if ( val < range.getMin() || val > range.getMax() ) {
					throw new IllegalArgumentException("argument "+arg+" : value "+String.valueOf(val)+" is out of range ["+ range.getMin()+","+range.getMax()+"]");
				}
			}
		}
	}
	
	private static void checkFiles( OptionSet options )
	throws FileNotFoundException, IllegalArgumentException
	{
		if ( options.has( Arguments.curves.toString() ) ) {
			File input = (File)options.valueOf(Arguments.curves.toString());
			if (!input.exists()) 
				throw new FileNotFoundException(Arguments.curves+" file "+input.getAbsolutePath()+" can not be found");
			if (!input.canRead()) 
				throw new IllegalArgumentException(Arguments.curves+" file "+input.getAbsolutePath()+" can not be read");
		}
	}
	
	/**
	 * prints the header and version information to stream
	 * @param out the stream to write to
	 */
	private static void printVersionHeader( PrintStream out) {
		out.println();
		out.println("################################################");
		out.println(" MICA - Multiple Interval-based Curve Alignment");
		out.println("################################################");
		out.println();
		out.println(" University Freiburg");
		out.println("  - Bioinformatics - http://www.bioinf.uni-freiburg.de/");
		out.println("  - Forest Growth - http://www.iww.uni-freiburg.de/");
		out.println();
		out.println(" Version "+MicaController.version);
		out.println();
	}

	public static void main(String[] args) {
		
		try {
			
			// get parser
			OptionParser parser = new OptionParser();
			Map<Arguments,DoubleRange> ranges = setupArgumentParser(parser);
		
			// get parsed options
//			OptionSet options = parser.parse( "--"+Arguments.curves, "011_1976_D.dat", "--"+Arguments.output, "tmp.out.txt" );
			OptionSet options = parser.parse( args );
			
			// check help
			if (options.has("help")) {
				printVersionHeader( System.out );
				parser.printHelpOn( System.out );
				System.exit(0);
			}
			
			// check version
			if (options.has("version")) {
				printVersionHeader( System.out );
				System.exit(0);
			}
			
			// check constraints
			checkRanges(options, ranges);
			checkFiles(options);
			
			// set LookAndFeel before any graphical user interface is loaded
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			// select controller 
			MicaController controller = options.has(Arguments.curves.toString()) ? new CliController() : new GuiController();
			
			// start MICA in according mode
			controller.start( options );
			
		
		} catch (IOException e) {
			System.err.println("\nERROR: IO problem : "+e.getMessage());
			System.exit(-3);
		} catch (OptionException e) {
			System.err.println("\nERROR: argument problem : "+e.getMessage());
			System.exit(-2);
		} catch (Exception e) {
			System.err.println("\nERROR: "+e.getMessage());
			if (e.getMessage()==null)
				e.printStackTrace();
			System.exit(-1);
		}
		
	}



}
