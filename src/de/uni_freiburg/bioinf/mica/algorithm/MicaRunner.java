package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.LinkedList;

import org.apache.commons.math3.exception.OutOfRangeException;

import de.uni_freiburg.bioinf.mica.controller.Debug;
import de.uni_freiburg.bioinf.mica.view.IProgressIndicator;

/**
 * Class which uses the split point information in the profile objects to create
 * multiple alignments in several blocks according to the split point
 * information. This class has to be run as own thread. The solution will be
 * distributed via the solution reception interface. The external object has to
 * be registered as solution receiver to get the alignment results.
 * 
 * @author mbeck
 * 
 */
public class MicaRunner implements Runnable {
	/**
	 * Set which contains the input profiles
	 */
	private LinkedList<AnnotatedCurve> profileSet = null;
	/**
	 * Contains the aligned profiles of the alignment
	 */
	private MICA.MicaData alignmentResult = null;
	/**
	 * Contains the distance after the result is computed
	 */
	private double alignmentDistance = 0.0;
	/**
	 * Specified the duration the system has need to compute the result
	 */
	private long alignmentDuration = 0l;
	/**
	 * Reference to the solution receiver
	 */
	private IMsaSolutionReceiver solRcv = null;
	/**
	 * Reference to the progress indicator
	 */
	private IProgressIndicator progInd = null;
	/**
	 * Mica object for multiple alignment
	 */
	private MICA mica = null;
	/**
	 * whether or not to compute a reference-based alignment
	 */
	private int referenceIndex = -1;

	/**
	 * Constructor which expects a set of profiles which split information
	 * 
	 * @param inputSet
	 *            Set with profiles and split information
	 * @param multipleAlignment
	 *            Is the object reference for multiple alignment
	 * @param solutionReceiver
	 *            Is the external object which will get the results of the
	 *            alignment.
	 * @param progressIndicator
	 *            Is used for display the state of the progress during the split
	 *            and the alignment
	 * @param referenceIndex
	 * 			  the index of the reference profile or -1 if no reference is to be used
	 */
	public MicaRunner(LinkedList<AnnotatedCurve> inputSet,
			MICA multipleAlignment,
			IMsaSolutionReceiver solutionReceiver,
			IProgressIndicator progressIndicator,
			int referenceIndex) {
		if (referenceIndex < -1 || referenceIndex >= inputSet.size()) throw new OutOfRangeException(referenceIndex, -1, inputSet.size()-1);
		/**
		 * Store references and init other object attributes
		 */
		profileSet = inputSet;
		solRcv = solutionReceiver;
		progInd = progressIndicator;
		/**
		 * Store the multiple alignment object
		 */
		mica = multipleAlignment;
		this.referenceIndex = referenceIndex;
	}


	/**
	 * Starts the thread and informs the solution receiver
	 */
	@Override
	public void run() {
		
		Thread currentThread = Thread.currentThread();
		
		// stop if needed
		if (currentThread.isInterrupted()) return;
		
		this.progInd.updateProgressIndication(IProgressIndicator.Status.RUNNING);
		
		// initialize data to be aligned
		IntervalDecomposition reference = null;
		IntervalDecomposition[] curves = new IntervalDecomposition[ referenceIndex < 0 ? this.profileSet.size() : this.profileSet.size() -1 ];
		int nextToFill = 0;
		for (int i=0; i<profileSet.size(); i++) {
			// check if reference index
			if (i == referenceIndex) {
				reference = new IntervalDecomposition( this.profileSet.get( i ) );
			} else {
				curves[nextToFill] = new IntervalDecomposition( this.profileSet.get( i ) );
				nextToFill++;
			}
		}
		
		// start time measurement
		long start = System.currentTimeMillis();
		
		// compute alignment
		if (reference == null) {
			alignmentResult = mica.align( curves );
		} else {
			alignmentResult = mica.alignToReference( reference, curves );
		}
		// stop if needed
		if (currentThread.isInterrupted()) return;
		
		// stop time measurement
		alignmentDuration = System.currentTimeMillis() - start;
		
		// compute final distance
		alignmentDistance = 0.0;
		int sumCounter = 0;
		// compute all pairwise distances
		for (int i=0; i<alignmentResult.curves.size(); i++) {
			for (int j=i+1; j<alignmentResult.curves.size(); j++) {
				// stop if needed
				if (currentThread.isInterrupted()) return;
				// compute and store distance
				try {
					alignmentDistance += mica.distanceFunction.getDistance( alignmentResult.curves.get(i).getCurve(), alignmentResult.curves.get(j).getCurve() );
					sumCounter++;
				} catch (Exception ex) {
					Debug.out.println("EXCEPTION(getPairwiseDistances("+i+","+j+")) = "+ex.getMessage());
				}
			}
		}
		// get mean distance
		alignmentDistance /= (double)sumCounter;
		
		// notify that computation is finished
		this.progInd.updateProgressIndication(IProgressIndicator.Status.FINISHED);
		
		// stop if needed
		if (currentThread.isInterrupted()) return;
		
		// Inform the solution receiver with the algorithm results
		solRcv.receiveMultipleSplitAlignmentSolution(alignmentDuration,
				alignmentDistance, alignmentResult);
	}
	
//
//	/**
//	 * Function to generate a set of profiles which are split according to the
//	 * split point information.
//	 * 
//	 * @return Is the two dimensional set of split profiles. The first dimension
//	 *         separates between the different input profiles. The second
//	 *         dimension differentiates between the different split blocks of a
//	 *         profile.
//	 */
//	private LinkedList<LinkedList<AnnotatedCurve>> split() {
//		LinkedList<LinkedList<AnnotatedCurve>> splitSet = new LinkedList<LinkedList<AnnotatedCurve>>();
//		/**
//		 * Over all input profiles
//		 */
//		for (AnnotatedCurve pdar : profileSet) {
//			/**
//			 * Get the split points
//			 */
//			LinkedList<Integer> s = pdar.getSplitPoints();
//			/**
//			 * Get the data points
//			 */
//			ArrayList<Double> d = pdar.getProfile().getDataPoints();
//			/**
//			 * Get the reference points
//			 */
//			ArrayList<ReferencePoint> r = pdar.getMergedReferencePoints();
//
//			/**
//			 * Split the data set
//			 */
//			LinkedList<ArrayList<Double>> splitData = splitDataSet(s, d);
//			/**
//			 * Split the reference point set
//			 */
//			LinkedList<ArrayList<ReferencePoint>> splitRefPoint = splitRefPointSet(
//					s, r);
//
//			if (splitRefPoint.size() != splitData.size())
//				Debug.out
//						.println("MultipleSplitAlignment:splitSet(): Warning split data and split refpoint not identical!");
//
//			/**
//			 * Generate the slit profile objects
//			 */
//			LinkedList<AnnotatedCurve> splitProfiles = new LinkedList<AnnotatedCurve>();
//			for (int i = 0; i < splitData.size(); i++) {
//				/**
//				 * Retrieve the split data and reference points
//				 */
//				ArrayList<Double> sd = splitData.get(i);
//				ArrayList<ReferencePoint> sr = splitRefPoint.get(i);
//				/**
//				 * Create the new split profile data
//				 */
//				AnnotatedCurve pd = new AnnotatedCurve(new Profile(pdar.getProfile()
//						.getName(), sd), sr);
//				/**
//				 * Register all global filters to the split profile data object
//				 */
//				LinkedList<IFilter> gFilterSet = pdar.getGlobalFilterSet();
//				for (IFilter f : gFilterSet) {
//					pd.addGlobalFilter(f);
//				}
//				/**
//				 * Also apply the reference selection information
//				 */
//				pd.setReferenceProfile(pdar.isReferenceProfile());
//				/**
//				 * Add the profile to the set
//				 */
//				splitProfiles.add(pd);
//			}
//			splitSet.add(splitProfiles);
//		}
//		return splitSet;
//	}
//
//	/**
//	 * Function to split a given set of reference points according to the split
//	 * information. The reference points in the result sets have adapted indices
//	 * to their split interval. Because if a split is performed the first
//	 * element of the new interval start always with index zero.
//	 * 
//	 * @param s
//	 *            Is the set which contains the split information
//	 * @param r
//	 *            Is the input set with the available reference points
//	 * @return Is the set which contains the split reference point sets.
//	 */
//	private LinkedList<ArrayList<ReferencePoint>> splitRefPointSet(
//			LinkedList<Integer> s, ArrayList<ReferencePoint> r) {
//		/**
//		 * Set which contains the split reference points
//		 */
//		LinkedList<ArrayList<ReferencePoint>> splitRefPoint = new LinkedList<ArrayList<ReferencePoint>>();
//		/**
//		 * Split the reference point set according to the split information
//		 */
//		int lastindex = 0;
//		for (int i = 0; i < s.size(); i++) {
//			int tmpIndex = lastindex;
//			while (r.get(tmpIndex).getIndex() < s.get(i)) {
//				tmpIndex++;
//			}
//			ArrayList<ReferencePoint> set = new ArrayList<ReferencePoint>(
//					r.subList(lastindex, tmpIndex));
//			/**
//			 * Decide if it is necessary to adapt the reference point indices
//			 */
//			if (lastindex == 0) {
//
//				/**
//				 * Add the end reference point to the subset. Check if the
//				 * position of the end reference point already an reference
//				 * point exists. Remove that reference point if exists.
//				 */
//				if (!set.isEmpty()
//						&& set.get(set.size() - 1).getIndex() == s.get(i) - 1) {
//					set.remove(set.size() - 1);
//				}
//				set.add(new ReferencePoint(s.get(i) - 1, PointType.REFPOINT_END));
//				/**
//				 * Adapt indices and add to the split result
//				 */
//				splitRefPoint.add(adaptRefPointIndices(set, 0));
//
//			} else {
//				/**
//				 * Add the end reference point to the subset. Check if the
//				 * position of the end reference point already an reference
//				 * point exists. Remove that reference point if exists.
//				 */
//				if (!set.isEmpty()
//						&& set.get(set.size() - 1).getIndex() == s.get(i) - 1) {
//					set.remove(set.size() - 1);
//				}
//				set.add(new ReferencePoint(s.get(i) - 1, PointType.REFPOINT_END));
//				/**
//				 * Same for the start reference point
//				 */
//				if (!set.isEmpty() && set.get(0).getIndex() == s.get(i - 1)) {
//					set.remove(0);
//				}
//				set.add(new ReferencePoint(s.get(i - 1),
//						PointType.REFPOINT_START));
//				Collections.sort(set);
//				/**
//				 * Adapt indices and add to the split result
//				 */
//				splitRefPoint.add(adaptRefPointIndices(set, s.get(i - 1)));
//			}
//			/**
//			 * Update the left boundary for the next interval
//			 */
//			lastindex = tmpIndex;
//		}
//		/**
//		 * Also handle the right most interval part of the left split point
//		 */
//		ArrayList<ReferencePoint> set = new ArrayList<ReferencePoint>(
//				r.subList(lastindex, r.size()));
//		if (s.isEmpty()) {
//			splitRefPoint.add(adaptRefPointIndices(set, 0));
//		} else {
//			/**
//			 * Add the start reference point to the subset. Check if the
//			 * position of the end reference point already an reference point
//			 * exists. Remove that reference point if exists.
//			 */
//			if (!set.isEmpty() && set.get(0).getIndex() == s.getLast()) {
//				set.remove(0);
//			}
//			set.add(new ReferencePoint(s.getLast(), PointType.REFPOINT_START));
//			Collections.sort(set);
//			/**
//			 * Adapt indices and add to the split result
//			 */
//			splitRefPoint.add(adaptRefPointIndices(set, s.getLast()));
//		}
//		/**
//		 * Return the split result sets
//		 */
//		return splitRefPoint;
//	}
//
//	/**
//	 * Function to adapt the indices of all reference points in a given set.
//	 * With this function a set of reference points can be placed into the
//	 * correct interval / split interval. Because if a split leads to two
//	 * intervals every interval starts with index 0. Facing this fact we need to
//	 * adapt the indices of the given reference points to the new interval start
//	 * index.
//	 * 
//	 * @param set
//	 *            Is the input set with the reference points
//	 * @param adaption
//	 *            Is the factor which will be subtracted from every reference
//	 *            points index value.
//	 * @return Is the set of the same reference points with adapted indices
//	 */
//	private ArrayList<ReferencePoint> adaptRefPointIndices(
//			ArrayList<ReferencePoint> set, int adaption) {
//		ArrayList<ReferencePoint> adaptedSet = new ArrayList<ReferencePoint>();
//		for (ReferencePoint r : set) {
//			adaptedSet.add(new ReferencePoint(r.getIndex() - adaption, r
//					.getType()));
//		}
//		return adaptedSet;
//	}
//
//	/**
//	 * Function to split a given data set into multiple sub parts.
//	 * 
//	 * @param s
//	 *            Set which contains the split information
//	 * @param d
//	 *            Data set which will be split
//	 * @return Set which contains the split sets
//	 */
//	private LinkedList<ArrayList<Double>> splitDataSet(LinkedList<Integer> s,
//			ArrayList<Double> d) {
//		/**
//		 * Set which contains the split data
//		 */
//		LinkedList<ArrayList<Double>> splitData = new LinkedList<ArrayList<Double>>();
//		/**
//		 * Split according to the split information
//		 */
//		int lastindex = 0;
//		for (Integer i : s) {
//			splitData.add(new ArrayList<Double>(d.subList(lastindex, i)));
//			lastindex = i;
//		}
//		splitData.add(new ArrayList<Double>(d.subList(lastindex, d.size())));
//		/**
//		 * Return the subsets
//		 */
//		return splitData;
//	}
//
//	/**
//	 * Computes the multiple alignment of all split blocks in the two
//	 * dimensional set which is created by the split function.
//	 * 
//	 * @param set
//	 *            Is the split set which is generates by the split function of
//	 *            this object
//	 */
//	private void compute(LinkedList<LinkedList<AnnotatedCurve>> set) {
//		LinkedList<LinkedList<AnnotatedCurve>> resultAlignment = new LinkedList<LinkedList<AnnotatedCurve>>();
//		/**
//		 * Over all split points
//		 */
//		for (int i = 0; i < set.getFirst().size(); i++) {
//			/**
//			 * Collect the number i split part from all profiles
//			 */
//			LinkedList<AnnotatedCurve> splitBlock = new LinkedList<AnnotatedCurve>();
//			for (LinkedList<AnnotatedCurve> psplit : set) {
//				splitBlock.add(psplit.get(i));
//			}
//			/**
//			 * Indicate the start of the current cluster
//			 */
//			progInd.updateProgressIndication();
//			/**
//			 * Compute the alignment
//			 */
//			mica.startAlignment(splitBlock);
//			resultAlignment.add(mica.getMultipleAlignment());
//			alignmentDistance += mica.getAlignmentDistance();
//		}
//		/**
//		 * Merge the split result alignment to the final alignment
//		 */
//		final int numBlocks = resultAlignment.getFirst().size();
//		for (int i = 0; i < numBlocks; i++) {
//			/**
//			 * Merging for reference and data points and split point determinism
//			 */
//			ArrayList<Double> mergeData = new ArrayList<Double>();
//			ArrayList<ReferencePoint> mergeRef = new ArrayList<ReferencePoint>();
//			LinkedList<Integer> splits = new LinkedList<Integer>();
//			for (LinkedList<AnnotatedCurve> splitp : resultAlignment) {
//				/**
//				 * Merge/concatenate data and reference points
//				 */
//				mergeData.addAll(splitp.get(i).getProfile().getDataPoints());
//				mergeRef.addAll(splitp.get(i).getMergedReferencePoints());
//				/**
//				 * Also retrieve the information about the split points from the
//				 * number of data points in each profiles block
//				 */
//				int previous = 0;
//				if (!splits.isEmpty())
//					previous = splits.getLast();
//				splits.add(previous
//						+ splitp.get(i).getProfile().getDataPoints().size());
//			}
//			/**
//			 * Remove all start and reference point types
//			 */
//			Iterator<ReferencePoint> iter = mergeRef.iterator();
//			while (iter.hasNext()) {
//				PointType t = iter.next().getType();
//				if (t == PointType.REFPOINT_START
//						|| t == PointType.REFPOINT_END) {
//					iter.remove();
//				}
//			}
//			/**
//			 * Add start and end reference point type to the set and sort
//			 */
//			mergeRef.add(new ReferencePoint(0, PointType.REFPOINT_START));
//			mergeRef.add(new ReferencePoint(mergeData.size() - 1,
//					PointType.REFPOINT_END));
//			Collections.sort(mergeRef);
//			/**
//			 * Insert the concatenated profile to the result and add the split
//			 * points information for the output plot
//			 */
//			AnnotatedCurve resPD = new AnnotatedCurve(new Profile(resultAlignment
//					.getFirst().get(i).getProfile().getName(), mergeData),
//					mergeRef);
//			for (Integer s : splits) {
//				resPD.addSplitPoint(s);
//			}
//			alignmentData.add(resPD);
//		}
//	}

}
