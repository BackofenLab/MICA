package de.uni_freiburg.bioinf.mica.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.exception.OutOfRangeException;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation;
import de.uni_freiburg.bioinf.mica.algorithm.CurveExtremaFilter;
import de.uni_freiburg.bioinf.mica.algorithm.CurveInflectionFilter;
import de.uni_freiburg.bioinf.mica.algorithm.IMsaSolutionReceiver;
import de.uni_freiburg.bioinf.mica.algorithm.MICA;
import de.uni_freiburg.bioinf.mica.algorithm.MicaRunner;
import de.uni_freiburg.bioinf.mica.algorithm.SampledCurveDistance;
import de.uni_freiburg.bioinf.mica.algorithm.SlopeMeanAbsoluteDistance;
import de.uni_freiburg.bioinf.mica.controller.Debug;
import de.uni_freiburg.bioinf.mica.controller.ISolutionDistributor;
import de.uni_freiburg.bioinf.mica.view.ColoredAnnotatedCurve;
import de.uni_freiburg.bioinf.mica.view.IProgressIndicator;

/**
 * Model object of the system. Encapsulates the algorithm.
 * 
 * @author mbeck
 * 
 */
public class Model implements Serializable, IMsaSolutionReceiver {
	private static final long serialVersionUID = 1L;
	
	/**
	 * default filter value for global curveExtramFilter
	 */
	private static final double DEFAULT_CURVEEXTRAMAFILTER_VALUE = 0.01;
	/**
	 * default filter value for global curveInflectionFilter
	 */
	private static final double DEFAULT_CURVEINFLECTIONFILTER_VALUE = 0.01;
	
	
	/**
	 * Internal set which contains all profile data objects from the import
	 * step.
	 */
	private LinkedList<ColoredAnnotatedCurve> curves = new LinkedList<>();
	/**
	 * Set which contains all available default colors. This set is used as ring
	 * buffer to set the imported profiles different colors at startup. Also
	 * there exists an index for the next default color which will bet set to a
	 * profile.
	 */
	private Color[] defaultColorSet = new Color[]{Color.ORANGE,Color.MAGENTA,Color.PINK,Color.RED,Color.YELLOW,Color.GREEN,Color.LIGHT_GRAY,Color.BLUE,Color.CYAN};
	private int nextDefaultColor = 0;
	
	/**
	 * The curve extrema filter to be applied to all curves
	 */
	private CurveExtremaFilter curveExtremaFilter = new CurveExtremaFilter( DEFAULT_CURVEEXTRAMAFILTER_VALUE );
	
	/**
	 * The global inflection point filter to be applied to all curves
	 */
	private CurveInflectionFilter curveInflectionFilter = new CurveInflectionFilter( DEFAULT_CURVEINFLECTIONFILTER_VALUE );
	

	/**
	 * Set which contains the result of the alignment
	 */
	private MICA.MicaData alignmentResult = null;
	
	/**
	 * Set which contains the curves of the alignment
	 */
	private LinkedList<ColoredAnnotatedCurve> alignmentData = new LinkedList<>();
	
	/**
	 * Set which contains the curves of the alignment
	 */
	private ColoredAnnotatedCurve alignmentConsensus = null;
	/**
	 * Distance of the alignment
	 */
	private double alignmentDistance = 0;
	/**
	 * Duration of the alignment computation
	 */
	private long alignmentRuntime = 0l;
	/**
	 * Definition for the minimal interval length the algorithm tries
	 * to decompose an interval
	 */
	private double minIvalDecomp = 0.05;
	private final double minIvalDecompMin = 0;
	private final double minIvalDecompMax = 1d;
	/**
	 * Constant definition for the maximal relative x-shift of reference points
	 */
	private double maxRelXShift = 0.2;
	private final double maxRelXShiftMin = 0;
	private final double maxRelXShiftMax = 1d;
	/**
	 * Attribute for the max warp factor
	 */
	private double maxWarpFactor = 2.0;
	private final double maxWarpFactorMin = 1.0;
	private final double maxWarpFactorMax = 999.0;
	
	private SampledCurveDistance distanceFunction = new SlopeMeanAbsoluteDistance(100);
	/**
	 * Solution distributor reference. This is used to distribute an existing
	 * solution to update the user front end with the alignment. This reference
	 * will not be serialized and has to be re-registered after the model is
	 * imported from hard disk.
	 */
	private transient ISolutionDistributor solDistr = null;

	/**
	 * Model object constructor which initialized the internal objects.
	 */
	public Model() {

	}
	
	

	/**
	 * Function to register a solution distributor to the model object.
	 * 
	 * @param sd
	 *            Is the solution distributor reference
	 */
	public void registerSolutionDistributor(ISolutionDistributor sd) {
		if (solDistr != null)
			Debug.out
					.println("Already registered solution distributor detected!");
		solDistr = sd;
	}


	/**
	 * Function to get the names of the profiles in the same order as they are
	 * imported.
	 * 
	 * @return A set of profile names.
	 */
	public LinkedList<String> getProfileNameSet() {
		LinkedList<String> nameSet = new LinkedList<String>();
		for (ColoredAnnotatedCurve pd : curves) {
			nameSet.add(pd.getCurve().getName());
		}
		return nameSet;
	}

	/**
	 * Function to get the final alignment data
	 * 
	 * @return Is null if no resulting profile is available, otherwise the
	 *         resulting alignment.
	 */
	public MICA.MicaData getAlignmentResult() {
		return alignmentResult;
	}
	
	/**
	 * Function to get the aligned curve data
	 * 
	 * @return Is null if no resulting profile is available, otherwise the
	 *         resulting profiles.
	 */
	public LinkedList<ColoredAnnotatedCurve> getAlignmentData() {
		return alignmentData;
	}



	/**
	 * Function do add an profile to the model. This function also inserts the
	 * defined global files to the profile.
	 * 
	 * @param p
	 *            Profile for insertion.
	 * @throws DuplicateProfileNameException
	 *             If the same profile name is already registered in the model
	 */
	public void addProfile(Curve p) throws DuplicateProfileNameException {
		// Check if the profile name already exists
		for (ColoredAnnotatedCurve cpd : curves) {
			if (cpd.getCurve().getName().equalsIgnoreCase(p.getName())) {
				throw new DuplicateProfileNameException("Profile name "
						+ p.getName() + " already registered in the model.");
			}
		}
		// Create the profile data object
		AnnotatedCurve pd = new AnnotatedCurve(p);
		// Add all available global filters to the object and directly execute all filters
		pd.addAnnotationFilter( this.curveExtremaFilter );
		pd.addAnnotationFilter( this.curveInflectionFilter );

		// create colored data
		curves.add(new ColoredAnnotatedCurve(pd, defaultColorSet[(nextDefaultColor++)%defaultColorSet.length]));
	}

	/**
	 * Function to get all available profile data objects.
	 * 
	 * @return The set of available profile data objects.
	 */
	public LinkedList<ColoredAnnotatedCurve> getAvailableProfileData() {
		return curves;
	}


	/**
	 * Function to calculate the multiple alignment with all available profiles
	 * in the model. In this special case the reference bases mica will be
	 * used.F
	 * 
	 * @param progressIndicator
	 *            Is the reference to the progress indicator object.
	 * @param referenceBased 
	 * 			whether or not a reference-based alignment is to be computed
	 */
	public void calcAlignment(IProgressIndicator progressIndicator, boolean referenceBased) {
		
		// copy data references
		LinkedList<AnnotatedCurve> inputSet = new LinkedList<>();
		int referenceIndex = -1;
		for (ColoredAnnotatedCurve cpd : curves) {
			// copy input curve
			inputSet.add(cpd.getCurve());
			// check whether it is a reference curve
			if (cpd.isReferenceProfile()) {
				referenceIndex = inputSet.size()-1;
			}
		}
		// create aligner
		MICA mica = new MICA( distanceFunction, maxWarpFactor, maxRelXShift, minIvalDecomp );
		// create alignment runner
		MicaRunner msica = new MicaRunner(inputSet, mica, this, progressIndicator, referenceIndex);
		
		// start computation
		progressIndicator.startMica(msica);

	}

	/**
	 * Function to clear the reference profile selection
	 */
	public void clearReferenceProfileSelection() {
		/**
		 * Set all reference selection to false
		 */
		for (ColoredAnnotatedCurve cpd : curves) {
			cpd.setReferenceProfile(false);
		}
	}

	/**
	 * Function to set all profiles in the selection set as reference profile.
	 * Calling this function discards all previous selected reference profiles.
	 * 
	 * @param sel
	 *            According to that set the profiles are set as reference
	 *            profiles.
	 */
	public void setReferenceProfileSelection(LinkedList<Integer> sel) {
		/**
		 * Clear previous selection
		 */
		clearReferenceProfileSelection();
		/**
		 * Select reference profiles
		 */
		for (Integer i : sel) {
			curves.get(i).setReferenceProfile(true);
		}
	}

	/**
	 * Function to get the minimal allowed decomposition length of a interval
	 * allowed in this algorithm
	 * 
	 * @return The minimal allowed length of an interval.
	 */
	public final double getMinIvalDecompLen() {
		return minIvalDecomp;
	}

	/**
	 * Function to get the value of the max warping factor
	 * 
	 * @return The max warping factor
	 */
	public double getMaxWarpFactor() {
		return maxWarpFactor;
	}

	/**
	 * Function to set the max warping factor
	 * 
	 * @param factor
	 *            The new value of the max warping factor
	 * @return True if the value is set successfully, otherwise false
	 */
	public boolean setMaxWarpFactor(double factor) {
		if (factor >= maxWarpFactorMin && factor <= maxWarpFactorMax) {
			maxWarpFactor = factor;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Function to get the minimal allowed max warp factor value.
	 * 
	 * @return The minimal value
	 */
	public final double getMaxWarpFactorMin() {
		return maxWarpFactorMin;
	}

	/**
	 * Function to get the maximal allowed max warp factor value.
	 * 
	 * @return The maximal value
	 */
	public final double getMaxWarpFactorMax() {
		return maxWarpFactorMax;
	}

	/**
	 * Function to set the minimal interval decomposition length
	 * 
	 * @param len
	 *            The new interval decomposition length. This length will be
	 *            compared against the allowed range for this value defined by
	 *            the model.
	 * @return True if the value is set successfully, otherwise false.
	 */
	public boolean setMinIvalDecomp(double len) {
		if (len >= minIvalDecompMin && len <= minIvalDecompMax) {
			minIvalDecomp = len;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Function to get the allowed min ival decomp length
	 * 
	 * @return The minimal value
	 */
	public final double getMinIvalDecomp() {
		return minIvalDecomp;
	}

	/**
	 * Function to get the minimal allowed min ival decomp length
	 * 
	 * @return The minimal value
	 */
	public final double getMinIvalDecompMin() {
		return minIvalDecompMin;
	}

	/**
	 * Function to get the maximal allowed min ival decomp length
	 * 
	 * @return The maximal value
	 */
	public final double getMinIvalDecompMax() {
		return minIvalDecompMax;
	}

	/**
	 * Function to get the rmsd value of the alignment
	 * 
	 * @return
	 */
	public double getAlignmentDistance() {
		return alignmentDistance;
	}

	/**
	 * Function to get the duration of the alignment computation
	 * 
	 * @return The duration in milliseconds
	 */
	public long getAlignmentDuration() {
		return alignmentRuntime;
	}

	/**
	 * Function to remove a profile data object by index.
	 * 
	 * @param selectedIndex
	 *            The index of the profile data in set for removal.
	 */
	public void removeProfileDataByIndex(int selectedIndex) {
		curves.remove(selectedIndex);
	}



	/**
	 * Function to discard a previous calculated alignment.
	 */
	public void discardAlignment() {
		alignmentResult = null;
		alignmentConsensus = null;
		alignmentData.clear();
	}

	/**
	 * Function to receive the result from the algorithm object. This function
	 * also informs the solution distributor to update the view with the
	 * results.
	 */
	@Override
	public void receiveMultipleSplitAlignmentSolution(long duration,
			double distance, MICA.MicaData alignment) {
		/**
		 * Retrieve the result from the mica algorithm
		 */
		discardAlignment();
		
		this.alignmentResult = alignment;
		
		// store consensus
		this.alignmentConsensus = new ColoredAnnotatedCurve( new AnnotatedCurve(alignment.consensus.getCurve()), Color.black);
		// set consensus
		this.alignmentConsensus.getCurve().setName("consensus");
		// overwrite annotation
		for (int i=1; i+1<this.alignmentConsensus.getCurve().size(); i++) {
			this.alignmentConsensus.getCurve().getAnnotation()[i] = CurveAnnotation.Type.IS_POINT;
		}
		this.alignmentConsensus.getCurve().resetFilteredAnnotations();
		
		for (int c = 0; c < alignment.curves.size(); c++) {
			// default color
			Color curveColor = Color.BLACK;
			// get color from original curve
			for (ColoredAnnotatedCurve oriCurve : curves) {
				if (oriCurve.getCurve().equals( alignment.curves.get(c).getCurveOriginal())) {
					curveColor = oriCurve.getColor();
				}
			}
			
			// grep aligned curve (first copy original curve)
			AnnotatedCurve alignedCurve = new AnnotatedCurve(alignment.curves.get(c).getCurveOriginal());
			{
				// overwrite x-coordinates
				// remove all annotations that are not interval boundaries
				double[] alignedXcoord = alignment.curves.get(c).getCurve().getX();
				CurveAnnotation.Type[] alignedCurveAnnotation = alignedCurve.getAnnotation();
				for (int i=0; i<alignedXcoord.length; i++) {
					alignedCurve.getX()[i] = alignedXcoord[i];
					if (!alignedCurveAnnotation[i].isIntervalBoundary()) {
						alignedCurveAnnotation[i] = CurveAnnotation.Type.IS_POINT;
					}
				}
				// update slope computation etc.
				alignedCurve.updateInterpolation();
			}
			
			// Add the color profile to the result set
			alignmentData.add(new ColoredAnnotatedCurve( alignedCurve, curveColor));
		}
		/**
		 * Store the distance and the duration of the alignment
		 */
		alignmentDistance = distance;
		alignmentRuntime = duration;
		/**
		 * Inform the solution distributor to collect the solutions from the
		 * model for visualization
		 */
		if (solDistr != null)
			solDistr.distributeSolution();
	}

	/**
	 * @return the curveExtremaFilter
	 */
	public CurveExtremaFilter getCurveExtremaFilter() {
		return curveExtremaFilter;
	}

	/**
	 * @return the curveInflectionFilter
	 */
	public CurveInflectionFilter getCurveInflectionFilter() {
		return curveInflectionFilter;
	}

	/**
	 * Function to translate the duration in milliseconds to a readable time
	 * string
	 * 
	 * @param duration
	 *            Duration in milliseconds
	 * @return Human readable time string
	 */
	public static String generateTimeString(long msec) {
		long min = TimeUnit.MILLISECONDS.toMinutes(msec);
		msec -= min * 60000;
		long sec = TimeUnit.MILLISECONDS.toSeconds(msec);
		msec -= sec * 1000;
		return String.format("%dm %ds %dms", min, sec, msec);
	}
	
	/**
	 * Access to the current maximally allowed relative x-shift of points
	 * @return the maxRelXShift
	 */
	public double getMaxRelXShift() {
		return maxRelXShift;
	}

	/**
	 * Sets the maximally allowed relative x-shift of points
	 * @param maxRelXShift the value to set
	 */
	public void setMaxRelXShift(double maxRelXShift) {
		if (maxRelXShift < this.maxRelXShiftMin || maxRelXShift > this.maxRelXShiftMax)
			throw new OutOfRangeException(maxRelXShift, this.maxRelXShiftMin, this.maxRelXShiftMax);
		this.maxRelXShift = maxRelXShift;
	}

	/**
	 * The upper bound for the maximally allowed relative x-shift of points
	 * @return the upper bound
	 */
	public double getMaxRelXShiftMin() {
		return maxRelXShiftMin;
	}

	/**
	 * The lower bound for the maximally allowed relative x-shift of points
	 * @return the lower bound
	 */
	public double getMaxRelXShiftMax() {
		return maxRelXShiftMax;
	}

	/**
	 * Access to the used distance function
	 * @return the distanceFunction
	 */
	public SampledCurveDistance getDistanceFunction() {
		return distanceFunction;
	}

	/**
	 * Sets a new distance function
	 * @param distanceFunction the distanceFunction to set
	 */
	public void setDistanceFunction(SampledCurveDistance distanceFunction) {
		this.distanceFunction = distanceFunction;
	}


	/**
	 * Access to the consensus of the last alignment computed 
	 * @return the consensus curve or null if none computed so far
	 */
	public ColoredAnnotatedCurve getAlignmentConsensus() {
		return alignmentConsensus;
	}


}
