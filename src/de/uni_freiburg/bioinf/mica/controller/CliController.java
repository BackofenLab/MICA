package de.uni_freiburg.bioinf.mica.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveExtremaFilter;
import de.uni_freiburg.bioinf.mica.algorithm.CurveInflectionFilter;
import de.uni_freiburg.bioinf.mica.algorithm.CurveMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.algorithm.IntervalDecomposition;
import de.uni_freiburg.bioinf.mica.algorithm.MICA;
import de.uni_freiburg.bioinf.mica.algorithm.MICA.MicaData;
import de.uni_freiburg.bioinf.mica.algorithm.SampledCurveDistance;
import de.uni_freiburg.bioinf.mica.algorithm.SlopeMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.model.FileFormatCsv;
import joptsimple.OptionSet;


/**
 * Command line interface controller for running MICA for a single
 * given input and parameter setup.
 * 
 * @author Martin Mann
 * 
 */
public class CliController implements MicaController {

	/*
	 * runs MICA in single job mode
	 * 
	 * @see de.uni_freiburg.bioinf.mica.controller.MicaController#start(joptsimple.OptionSet)
	 */
	@Override
	public void start(OptionSet options) throws Exception {
		
		if (!options.has(Arguments.curves.toString()))
			throw new RuntimeException("no "+Arguments.curves+" argument found");
		
		// parse curves from input files with given delimiter
		final String csvColDelim = options.valueOf(Arguments.csvDelim.toString()).toString();
		FileFormatCsv csvFileHandler = new FileFormatCsv( csvColDelim );
		File curvesFile = (File)options.valueOf(Arguments.curves.toString());
		final int numberOfCurves = csvFileHandler.getNumberCols(curvesFile.getAbsolutePath());
		List<AnnotatedCurve> curves = new LinkedList<>();
		final boolean csvHeader = !options.has(Arguments.csvNoHeader.toString());
		// parse each curve and store
		for (int c=0; c<numberOfCurves; c++) {
			Curve curve_c = csvFileHandler.load(curvesFile.getAbsolutePath(), c, csvHeader);
			if (curve_c != null) {
				if (!csvHeader) {
					curve_c.setName("c"+c);
				}
				try {
					curves.add(new AnnotatedCurve(curve_c));
				} catch (Exception e) {
					Debug.out.println("WARNING: cannot create curve from column "+c+" from file "+curvesFile.getAbsolutePath()+" due to : "+e.getMessage()+" : skipped");
				}
			} else {
				Debug.out.println("WARNING: cannot load non-empty curve from column "+c+" from file "+curvesFile.getAbsolutePath()+" : skipped");
			}
		}
		
		// check enough successfully parsed
		if (curves.size() < 2) {
			Debug.out.println("WARNING: Only "+curves.size()+" curves successfully parsed : too few : stopping here ...");
			return;
		}
		
		// setup filter 
		CurveExtremaFilter filterExtrema = new CurveExtremaFilter( (Double)options.valueOf(Arguments.filterExtrema.toString()) );
		CurveInflectionFilter filterInflect = new CurveInflectionFilter( (Double)options.valueOf(Arguments.filterInflect.toString()) );
		for ( AnnotatedCurve c : curves ) {
			c.addAnnotationFilter(filterExtrema);
			c.addAnnotationFilter(filterInflect);
		}
		
		// setup data to be aligned
		IntervalDecomposition[] curvesToAlign = new IntervalDecomposition[curves.size()];
		try {
			for (int c=0; c<curves.size(); c++) {
				curvesToAlign[c] = new IntervalDecomposition(curves.get(c));
			}
		} catch (Exception e) {
			throw new RuntimeException("cannot generate initial interval decomposition : "+e.getMessage());
		}
		
		// setup distance function
		SampledCurveDistance distanceFunction = null;
		switch( (DistanceBase)options.valueOf(Arguments.distBase.toString()) ) {
		case SLOPE:
			distanceFunction = new SlopeMeanAbsoluteDistance( (Integer)options.valueOf(Arguments.distSamples.toString()) );
			break;
		case Y_DATA:
			distanceFunction = new CurveMeanAbsoluteDistance( (Integer)options.valueOf(Arguments.distSamples.toString()) );
			break;
		}
		
		// setup aligner
		MICA aligner = new MICA( 
					distanceFunction,
					(Double)options.valueOf(Arguments.alnMaxWarp.toString()),
					(Double)options.valueOf(Arguments.alnMaxShift.toString()),
					(Double)options.valueOf(Arguments.alnMinLength.toString())
					);
		
		// compute alignment
		MicaData alignment = aligner.align( curvesToAlign );
		
		// write alignment's new x-coordinates
		if (options.has(Arguments.output.toString())) 
		{
			
			// open writer
			FileWriter fileWriter = null;
			BufferedWriter writer = null;
			try {
				// open writer
				fileWriter = new FileWriter((File)options.valueOf(Arguments.output.toString()));
				writer = new BufferedWriter(fileWriter);
				
				// write to FILE
				csvFileHandler.writeX(writer, alignment.curves, true, csvHeader);
				
				// close writer
				writer.close();
				fileWriter.close();
				
			} catch (Exception e) {
				// close writer
				if (writer != null) writer.close();
				if (fileWriter != null) fileWriter.close();
				// forward exception
				throw new IOException("cannot write output file "+(File)options.valueOf(Arguments.output.toString())+" : "+e.getMessage());
			}

		} else {
			// write to STDOUT
			PrintWriter sysout = null;
			try {
				sysout = new PrintWriter(System.out);
				csvFileHandler.writeX( sysout, alignment.curves, true, csvHeader);
				sysout.flush();
				sysout.close();
			} catch (Exception e) {
				// close writer
				if (sysout != null) sysout.close();
				// forward exception
				throw new IOException("cannot write output file "+(File)options.valueOf(Arguments.output.toString())+" : "+e.getMessage());
			}
		}
		
	}

}
