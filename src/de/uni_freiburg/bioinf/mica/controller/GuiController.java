package de.uni_freiburg.bioinf.mica.controller;

import java.util.LinkedList;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.ToolTipManager;

import joptsimple.OptionSet;

import org.apache.commons.math3.exception.OutOfRangeException;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.algorithm.DoubleRange;
import de.uni_freiburg.bioinf.mica.algorithm.SlopeMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.model.DuplicateProfileNameException;
import de.uni_freiburg.bioinf.mica.model.FileFormatCsv;
import de.uni_freiburg.bioinf.mica.model.ImportExport;
import de.uni_freiburg.bioinf.mica.model.Model;
import de.uni_freiburg.bioinf.mica.view.ColoredAnnotatedCurve;
import de.uni_freiburg.bioinf.mica.view.ColoredAnnotatedCurvePlot;
import de.uni_freiburg.bioinf.mica.view.DoubleParameter;
import de.uni_freiburg.bioinf.mica.view.MicaMainFrame;
import de.uni_freiburg.bioinf.mica.view.ViewExportSelection;
import de.uni_freiburg.bioinf.mica.view.ViewParse;
import de.uni_freiburg.bioinf.mica.view.ViewPngExpSettings.SourceType;

/**
 * Controller object of the MVC pattern. The controller implements the solution
 * distributor interface. If the model receives a solution from the algorithm
 * the model object will inform the controller about the existing solution over
 * this interface.
 * 
 * @author mbeck
 * @author Mmann
 * 
 */
public class GuiController implements MicaController, ISolutionDistributor {
	
	public static final int minimalNumberDataPoints = 3;
	
	/**
	 * Main window which will appear at start up
	 */
	private MicaMainFrame mainView = null;
	/**
	 * Sub view for parse the input
	 */
	private ViewParse parseView = null;
	private LinkedList<String> importFileNames = null;
	/**
	 * Object to import or export profiles.
	 */
	private ImportExport ie = null;
	/**
	 * Model object which provides access to the alignment algorithm.
	 */
	private Model model = new Model();
	
	/**
	 * Wrapper for parameter maxWarpFactor model data
	 */
	private DoubleParameter paramMaxWarpFactor = new DoubleParameter() {
		
		DoubleRange range = new DoubleRange(model.getMaxWarpFactorMin(), model.getMaxWarpFactorMax());
		
		@Override
		public double get() {
			return model.getMaxWarpFactor();
		}
		
		@Override
		public void setValue(double newValue) throws OutOfRangeException {
			model.setMaxWarpFactor(newValue);
		}
		
		@Override
		public DoubleRange getRange() {
			return range;
		}
		
		@Override
		public double getStepWidth() {
			return 0.1;
		}
		
		@Override
		public String getName() {
			return "Max. warping factor";
		}
		
		@Override
		public String getDescription() {
			return "<b>Maximum warping factor</b> restricts <br>"
					+ "the interval warping, ie. the length <br>"
					+ "ratio (long/short) can be at most the <br>"
					+ "given value.<br>"
					+ "The <b>allowed range</b> is <b>[" + getRange().getMin()+ "," + getRange().getMax() + "]</b>."
					;
		}
	};
	
	
	
	/**
	 * Wrapper for parameter minIvalDecomp model data
	 */
	private DoubleParameter paramMinIvalDecomp = new DoubleParameter() {
		
		DoubleRange range = new DoubleRange(model.getMinIvalDecompMin(), model.getMinIvalDecompMax());
		
		@Override
		public double get() {
			return model.getMinIvalDecompLen();
		}
		
		@Override
		public void setValue(double newValue) throws OutOfRangeException {
			model.setMinIvalDecomp(newValue);
		}
		
		@Override
		public double getStepWidth() {
			return 0.01;
		}
		
		@Override
		public DoubleRange getRange() {
			return range;
		}
		
		@Override
		public String getName() {
			return "Min. rel. interval length";
		}
		
		@Override
		public String getDescription() {
			return "<b>Minimal relative length</b><br>"
					+ " of an interval to be considered<br>"
					+ " for further decomposition.<br>"
					+ " Has to be in range <b>["
					+ getRange().getMin()+","+getRange().getMax()
					+"]</b>.";
		}
	};
	
	
	/**
	 * Wrapper for parameter minIvalDecomp model data
	 */
	private DoubleParameter paramMaxRelXShift = new DoubleParameter() {
		
		DoubleRange range = new DoubleRange(model.getMaxRelXShiftMin(), model.getMaxRelXShiftMax());
		
		@Override
		public double get() {
			return model.getMaxRelXShift();
		}
		
		@Override
		public void setValue(double newValue) throws OutOfRangeException {
			model.setMaxRelXShift(newValue);
		}
		
		@Override
		public double getStepWidth() {
			return 0.01;
		}
		
		@Override
		public DoubleRange getRange() {
			return range;
		}
		
		@Override
		public String getName() {
			return "Max. rel. x-shift";
		}
		
		@Override
		public String getDescription() {
			return "<b>Maximal relative shift</b><br>"
					+ " of x-coordinates allowed.<br>"
					+ " Has to be in range <b>["
					+ getRange().getMin()+","+getRange().getMax()
					+"]</b>.";
		}
	};
	
	
	/**
	 * Wrapper for extrema filter value within model data
	 */
	private DoubleParameter paramFilterExtremaValue = new DoubleParameter() {
		
		DoubleRange range = new DoubleRange(0, 1);
		
		@Override
		public double get() {
			return model.getCurveExtremaFilter().getMinRelNeighDiff();
		}
		
		@Override
		public void setValue(double newValue) throws OutOfRangeException {
			model.getCurveExtremaFilter().setMinRelNeighDiff(newValue);
		}
		
		@Override
		public double getStepWidth() {
			return 0.01;
		}
		
		@Override
		public DoubleRange getRange() {
			return range;
		}
		
		@Override
		public String getName() {
			return "Min. extrema difference";
		}
		
		@Override
		public String getDescription() {
			return "<b>Minimal rel. y-difference</b><br>"
					+ " of neighbored extrema.<br>"
					+ " Has to be in range <b>["
					+ getRange().getMin()+","+getRange().getMax()
					+"]</b>.";
		}
	};
	
	
	/**
	 * Wrapper for extrema filter value within model data
	 */
	private DoubleParameter paramFilterInflectionValue = new DoubleParameter() {
		
		DoubleRange range = new DoubleRange(0, 1);
		
		@Override
		public double get() {
			return model.getCurveInflectionFilter().getMinRelHeight();
		}
		
		@Override
		public void setValue(double newValue) throws OutOfRangeException {
			model.getCurveInflectionFilter().setMinRelHeight(newValue);
		}
		
		@Override
		public double getStepWidth() {
			return 0.01;
		}
		
		@Override
		public DoubleRange getRange() {
			return range;
		}
		
		@Override
		public String getName() {
			return "Min. inflection height";
		}
		
		@Override
		public String getDescription() {
			return "<b>Minimal rel. slope value</b><br>"
					+ " of inflection points.<br>"
					+ " Has to be in range <b>["
					+ getRange().getMin()+","+getRange().getMax()
					+"]</b>.";
		}
	};
	
	
	/**
	 * Wrapper for number of distance samples for distance computation
	 */
	private DoubleParameter paramDistanceSamples = new DoubleParameter() {
		
		DoubleRange range = new DoubleRange(0, 100000);
		
		@Override
		public double get() {
			return getDistanceSamples();
		}
		
		@Override
		public void setValue(double newValue) throws OutOfRangeException {
			setDistanceSamples( (int)newValue );
		}
		
		@Override
		public double getStepWidth() {
			return 1;
		}
		
		@Override
		public DoubleRange getRange() {
			return range;
		}
		
		@Override
		public String getName() {
			return "Sample number";
		}
		
		@Override
		public String getDescription() {
			return "<b>Number of equidistant samples</b><br>"
					+ "to be used for distance computation.<br>"
					+ " Has to be in range <b>["
					+ getRange().getMin()+","+getRange().getMax()
					+"]</b>.";
		}
	};
	
	/**
	 * @return the paramMaxWarpFactor
	 */
	public DoubleParameter getParamMaxWarpFactor() {
		return paramMaxWarpFactor;
	}
	
	/**
	 * @return the paramMinIvalDecomp
	 */
	public DoubleParameter getParamMinIvalDecomp() {
		return paramMinIvalDecomp;
	}
	
	/**
	 * @return the paramMaxRelXShift
	 */
	public DoubleParameter getParamMaxRelXShift() {
		return paramMaxRelXShift;
	}
	
	/**
	 * @return the paramFilterExtremaValue
	 */
	public DoubleParameter getParamFilterExtremaValue() {
		return paramFilterExtremaValue;
	}
	
	/**
	 * @return the paramFilterInflectionValue
	 */
	public DoubleParameter getParamFilterInflectionValue() {
		return paramFilterInflectionValue;
	}
	
	/**
	 * @return the paramDistanceSamples
	 */
	public DoubleParameter getParamDistanceSamples() {
		return paramDistanceSamples;
	}
	
	/**
	 * the column separator to be used to write and parse CSV files
	 */
	protected String fileColumnSeparator = ";"; 
	
	/**
	 * whether or not CSV files are to be parsed with or without header
	 */
	protected boolean fileColumnHeader = true; 


	/**
	 * Access to  
	 * the column separator to be used to write and parse CSV files
	 * @return the fileColumnSeparator
	 */
	public String getFileColumnSeparator() {
		return fileColumnSeparator;
	}

	/**
	 * Sets
	 * the column separator to be used to write and parse CSV files
	 * @param fileColumnSeparator the fileColumnSeparator to set
	 */
	public void setFileColumnSeparator(String fileColumnSeparator) {
		this.fileColumnSeparator = fileColumnSeparator;
	}

	/**
	 * Constructor which creates a new model and the main window.
	 */
	public GuiController() {
		/**
		 * Creates the model
		 */
		model.registerSolutionDistributor(this);

		/**
		 * Create the other objects
		 */
		mainView = new MicaMainFrame(this);
		ie = new ImportExport();
		importFileNames = new LinkedList<String>();
	}

	/**
	 * Function to start the parse and import step.
	 * 
	 * @param file
	 *            The file name which contains the data for import.
	 */
	public void startParseAndImport(String file) {
		/**
		 * Store the file name internally
		 */
		importFileNames.add(file);
		/**
		 * Create the parse view. Set the parameter of the minimal allowed data
		 * points for import to double the minimal interval length plus five.
		 * this ensures the user has the possibility to set at least one split
		 * point.
		 */
		parseView = new ViewParse(mainView, file, this, minimalNumberDataPoints, this.fileColumnHeader);
		parseView.setVisible(true);
		LinkedList<Curve> impProf = parseView.getSelectedProfiles();
		/**
		 * Add profile to the model
		 */
		for (Curve p : impProf) {
			try {
				model.addProfile(p);
			} catch (DuplicateProfileNameException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(),
						"Duplicate curve name", JOptionPane.ERROR_MESSAGE);
			}
		}
		parseView = null;
		updateViewProfileList();
	}

	/**
	 * Function to start the reference multiple alignment
	 * 
	 * @param referenceProfileIndex
	 *            Is the index of the profile which is selected for reference
	 *            multiple alignment
	 */
	public void startMica(int referenceProfileIndex) {
		/**
		 * Clear the view output stuff
		 */
		mainView.clearOutput();
		
		// clear reference data
		model.clearReferenceProfileSelection();
		// set reference data if needed
		if (referenceProfileIndex >= 0 && referenceProfileIndex < model.getProfileNameSet().size()) {
			/**
			 * Select the single profile as reference profile
			 */
			LinkedList<Integer> selection = new LinkedList<Integer>();
			selection.add(referenceProfileIndex);
			model.setReferenceProfileSelection(selection);
		}
		/**
		 * Start the alignment
		 */
		model.calcAlignment( mainView.getProgressIndicator(), referenceProfileIndex >= 0);
	}

	
	/**
	 * Access to the number of samples used for distance computation
	 * @return
	 */
	public int getDistanceSamples() {
		return model.getDistanceFunction().getSampleNumber();
	}
	
	/**
	 * Attempts to set the number of samples used for distance computation
	 * if the provided value is reasonable
	 * @param number the new number of samples to be used
	 */
	public void setDistanceSamples(int number) {
		try {
			model.getDistanceFunction().setSampleNumber( number );
		} catch (Exception ex) 
		{ /* do nothing, just ignore */ }
	}
	
	/**
	 * Whether or not the distance computation is slope based
	 * @return true if the distance function uses slope values
	 */
	public boolean isDistanceSlopeBased() {
		return model.getDistanceFunction() instanceof SlopeMeanAbsoluteDistance;
	}
	
	/**
	 * Whether or not the distance computation is slope based
	 * @return true if the distance function uses slope values
	 */
	public void setDistanceSlopeBased( boolean setSlopeBased ) {
		// check if something to do
		if (setSlopeBased != isDistanceSlopeBased()) {
			// store current sample number
			int sampleNumber = model.getDistanceFunction().getSampleNumber();
			// create new distance function
			if (setSlopeBased) {
				model.setDistanceFunction(new SlopeMeanAbsoluteDistance(sampleNumber));
			} else {
				model.setDistanceFunction(new CurveMeanAbsoluteDistance(sampleNumber));
			}
		}
	}
	


	/**
	 * Function to get the number if profiles
	 * 
	 * @return The number of profiles in the model
	 */
	public int getNumberProfiles() {
		return model.getAvailableProfileData().size();
	}

	/**
	 * Function to get the minimal allowed decomposition length of a interval
	 * allowed in this algorithm
	 * 
	 * @return The minimal allowed length of an interval.
	 */
	public final double getMinIvalDecompLen() {
		return model.getMinIvalDecompLen();
	}

	/**
	 * Function to get the max warp factor from the model
	 * 
	 * @return The max war factor
	 */
	public double getMaxWarpFactor() {
		return model.getMaxWarpFactor();
	}

	/**
	 * Function to set the max warp factor in the model
	 * 
	 * @param factor
	 *            The new value for the max warp factor
	 * @return Is true if the new value is in the range of the models max warp
	 *         factor boundaries, otherwise false
	 */
	public boolean setMaxWarpFactor(double factor) {
		return model.setMaxWarpFactor(factor);
	}

	/**
	 * Function to set the minimal interval decomposition length
	 * 
	 * @param len
	 *            The new interval decomposition length
	 * @return True if the new value is accepted by the model, otherwise false.
	 */
	public boolean setMinIvalDecomp(int len) {
		return model.setMinIvalDecomp(len);
	}

	/**
	 * Function to get the minimal allowed max warp factor value.
	 * 
	 * @return The minimal value
	 */
	public final double getMaxWarpFactorMin() {
		return model.getMaxWarpFactorMin();
	}

	/**
	 * Function to get the maximal allowed max warp factor value.
	 * 
	 * @return The maximal value
	 */
	public final double getMaxWarpFactorMax() {
		return model.getMaxWarpFactorMax();
	}

	/**
	 * Function to get the minimal allowed min ival decomp length
	 * 
	 * @return The minimal value
	 */
	public final double getMinIvalDecompMin() {
		return model.getMinIvalDecompMin();
	}

	/**
	 * Function to get the maximal allowed min ival decomp length
	 * 
	 * @return The maximal value
	 */
	public final double getMinIvalDecompMax() {
		return model.getMinIvalDecompMax();
	}

	/**
	 * Function to get the number of clusters in an arbitrary profile
	 * 
	 * @return The number of clusters in a profile
	 */
	public int getNumberSplitClusters() {
		/**
		 * receive the first profile data, because every profile has the same
		 * number of split points
		 */
		ColoredAnnotatedCurve cpd = model.getAvailableProfileData().getFirst();
		int numClusters = 0;
		if (cpd != null) {
			/**
			 * Store the number of split points
			 */
			numClusters = cpd.getSplitPoints().size();
			/**
			 * add one to the number of clusters, because one split point
			 * generates two clusters
			 */
			numClusters++;
		}
		return numClusters;
	}

	/**
	 * Function to check the uniqueness of a profile name.
	 * 
	 * @param name
	 *            Is the profile name for comparison against the available
	 *            profile names.
	 * @return True if the parameter name is unique
	 */
	public boolean isProfileNameGlobalUnique(String name) {
		LinkedList<String> availableProfileNames = model.getProfileNameSet();
		for (String s : availableProfileNames) {
			/**
			 * Check if they are identically
			 */
			if (s.compareTo(name) == 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Function to export the result to hard disk.
	 * 
	 * @param file
	 *            The file on the hard disk which contains the profile after
	 *            export.
	 */
	public void exportResult(String file) {
		/**
		 * Retrieve the alignment profiles from the model
		 */
		LinkedList<ColoredAnnotatedCurve> inputSet = model.getAvailableProfileData();
		LinkedList<ColoredAnnotatedCurve> resultSet = model.getAlignmentData();
		/**
		 * Separation sets for the export view
		 */
		LinkedList<Curve> input = null;
		LinkedList<Curve> alignment = null;
		/**
		 * Extract the profiles from the sets
		 */
		for (ColoredAnnotatedCurve cpd : inputSet) {
			if (input == null)
				input = new LinkedList<>();
			input.add(cpd.getCurve());
		}
		for (ColoredAnnotatedCurve cpd : resultSet) {
			if (alignment == null)
				alignment = new LinkedList<>();
			// Add all profiles from the mica result to the align set
			alignment.add(cpd.getCurve());
		}
		/**
		 * Attributes for the final user notification
		 */
		int type = JOptionPane.INFORMATION_MESSAGE;
		String message = "";
		/**
		 * Create the view for profiles selection for export
		 */
		ViewExportSelection ves = new ViewExportSelection(mainView, input,
										alignment, model.getAlignmentResult(),
										this.fileColumnSeparator);
		ves.setVisible(true);
		/**
		 * Retrieve the selected profiles and export the set
		 */
		if (ves.getSelectedProfiles().size() > 0) {
			/**
			 * Update the delimiter for export by overwriting the old file
			 * format delimiter
			 */
			ie.setFileFormat(new FileFormatCsv(ves.getDelimiter()));
			ie.save(file, ves.getSelectedProfiles());
			message = "Successful exported curves: "
					+ ves.getSelectedProfiles().size() + "\nDestination: "
					+ file;
		} else {
			type = JOptionPane.WARNING_MESSAGE;
			message = "Abortion! No curves selected or you aborted the export.";
		}
		/**
		 * Show the user notification
		 */
		JOptionPane.showMessageDialog(null, message,
				"Export notification", type);
	}


	/**
	 * Function to remove a profile by index.
	 * 
	 * @param selectedIndex
	 *            The index of the profile for removal.
	 */
	public void removeProfileByIndex(int selectedIndex) {
		model.removeProfileDataByIndex(selectedIndex);
		updateViewProfileList();
	}



	/**
	 * Function to get a profile data object by index.
	 * 
	 * @param index
	 *            The index of the profile data object.
	 * @return The profile data object.
	 */
	public ColoredAnnotatedCurve getProfileDataByIndex(int index) {
		return model.getAvailableProfileData().get(index);
	}



	/**
	 * Function to update the view component list with the profile names.
	 */
	public void updateViewProfileList() {
		/**
		 * Create a set of current profile names
		 */
		LinkedList<String> pNames = getProfileNameSet();
		/**
		 * Update the view component.
		 */
		mainView.updateProfileList(pNames);
	}

	/**
	 * Function to retrieve a set of profile names in the same order the
	 * profiles are imported in the model
	 * 
	 * @return The set of profile names in the model
	 */
	public LinkedList<String> getProfileNameSet() {
		LinkedList<String> pNames = new LinkedList<String>();
		LinkedList<ColoredAnnotatedCurve> available = model
				.getAvailableProfileData();
		for (ColoredAnnotatedCurve pd : available) {
			pNames.add(pd.getCurve().getName());
		}
		return pNames;
	}




	public void discardAlignment() {
		model.discardAlignment();
	}

	/**
	 * Function to clear all sets of split points from all available profiles
	 */
	public void removeAllSplitPoints() {
		LinkedList<ColoredAnnotatedCurve> pset = model.getAvailableProfileData();
		for (ColoredAnnotatedCurve cpd : pset) {
			cpd.clearSplitPoints();
		}
	}

	/**
	 * Function to push the alignment from the model to the view for
	 * visualization.
	 */
	@Override
	public void distributeSolution() {
		mainView.displayResultPlot(
				model.getAlignmentData(),
				model.getAlignmentConsensus(),
				model.getAlignmentDistance(),
				model.getAlignmentDuration());
	}

	/**
	 * Function which creates a plot object according to the source type
	 * definition
	 * 
	 * @param sourceTypeSelection
	 *            Is the source type definition for the plot
	 * @return Is the plot containing the required profiles for picture export.
	 */
	public ColoredAnnotatedCurvePlot createPlotForPicture(
			SourceType sourceTypeSelection) {

		ColoredAnnotatedCurvePlot plot = new ColoredAnnotatedCurvePlot( false );
		LinkedList<ColoredAnnotatedCurve> p = new LinkedList<ColoredAnnotatedCurve>();
		switch (sourceTypeSelection) {
		case INPUT:
			/**
			 * Create a plot object with all input files
			 */
			for (ColoredAnnotatedCurve cpd : model.getAvailableProfileData()) {
				p.add(cpd);
			}
			break;
		case ALIGN:
			/**
			 * Create a plot object with the warped results
			 */
			for (ColoredAnnotatedCurve cpd : model.getAlignmentData()) {
				if (model.getAlignmentData().getLast() != cpd) {
					p.add(cpd);
				}
			}
			break;
		case ALIGNCONS:
			/**
			 * Create a plot object with warped results and the consensus
			 */
			for (ColoredAnnotatedCurve cpd : model.getAlignmentData()) {
				p.add(cpd);
			}
			break;
		case NONE:
		default:
			break;
		}
		/**
		 * Add the set of profiles and select the last profile as foreground
		 */
		plot.plotProfiles(p);
		plot.setSelectedCurve(p.size() - 1);
		/**
		 * return the plot object
		 */
		return plot;
	}


	
	/**
	 * Registers a listener for filter value changes
	 * @param observer the filter value change observer to register
	 */
	public void registerFilterChangeListener( Observer observer) {
		// add observer to all filters
		model.getCurveExtremaFilter().addObserver( observer );
		model.getCurveInflectionFilter().addObserver( observer );
	}

	/*
	 * run MICA graphical user interface
	 * 
	 * @see de.uni_freiburg.bioinf.mica.controller.MicaController#start(joptsimple.OptionSet)
	 */
	@Override
	public void start(OptionSet options) throws Exception {
		
		// parse options and set in model/view
		if (options.has(Arguments.alnMaxShift.toString())) {
			this.paramMaxRelXShift.set( (Double)options.valueOf(Arguments.alnMaxShift.toString()) );
		}
		if (options.has(Arguments.alnMaxWarp.toString())) {
			this.paramMaxWarpFactor.set( (Double)options.valueOf(Arguments.alnMaxWarp.toString()) );
		}
		if (options.has(Arguments.alnMinLength.toString())) {
			this.paramMinIvalDecomp.set( (Double)options.valueOf(Arguments.alnMinLength.toString()) );
		}
		if (options.has(Arguments.alnReference.toString())) {
			Debug.out.println("WARNING: argument "+Arguments.alnReference+" is ignored");
		}
		if (options.has(Arguments.csvDelim.toString())) {
			this.fileColumnSeparator = options.valueOf(Arguments.csvDelim.toString()).toString();
		}
		this.fileColumnHeader = ! options.has(Arguments.csvNoHeader.toString());
		if (options.has(Arguments.distBase.toString())) {
			mainView.setDistanceSlopeBased( (DistanceBase)options.valueOf(Arguments.distBase.toString()) == DistanceBase.SLOPE );
		}
		if (options.has(Arguments.distSamples.toString())) {
			this.paramDistanceSamples.set( (Integer)options.valueOf(Arguments.distSamples.toString()) );
		}
		if (options.has(Arguments.filterExtrema.toString())) {
			this.paramFilterExtremaValue.set( (Integer)options.valueOf(Arguments.filterExtrema.toString()) );
		}
		if (options.has(Arguments.filterInflect.toString())) {
			this.paramFilterInflectionValue.set( (Integer)options.valueOf(Arguments.filterInflect.toString()) );
		}
		if (options.has(Arguments.output.toString())) {
			Debug.out.println("WARNING: argument "+Arguments.output+" is ignored");
		}


		// Setting the tool tip delay time
		ToolTipManager.sharedInstance().setDismissDelay(20000);
		ToolTipManager.sharedInstance().setInitialDelay(250);
	
		// start graphical user interface
		mainView.repaint();
		mainView.setVisible(true);

		
	}

}
