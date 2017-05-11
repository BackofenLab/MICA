package de.uni_freiburg.bioinf.mica.algorithm;

import org.apache.commons.math3.util.FastMath;

public class DoubleRange {
	
	/**
	 * minimal value of the range
	 */
	private double min;
	
	/**
	 * maximal value of the range
	 */
	private double max;
	
	
	/**
	 * initializes the range with [0,0]
	 */
	public DoubleRange() {
		this.min = 0d;
		this.max = 0d;
	}
	
	/**
	 * initializes the given range [min,max]
	 * Note: the range is not checked for sanity (min <= max).
	 * @param min the minimal value
	 * @param max the maximal value
	 */
	public DoubleRange( double min, double max ) {
		set(min,max);
	}
	
	/**
	 * Access to the minimal value of the range
	 * @return the minimal value
	 */
	public double getMin() {
		return min;
	}
	
	/**
	 * Access to the maximal value of the range
	 * @return the maximal value
	 */
	public double getMax() {
		return max;
	}
	
	/**
	 * Returns the range of max-min
	 * @return (max-min)
	 */
	public double getRange() {
		return max-min;
	}

	/**
	 * Sets the minimal value.
	 * Note: the range is not checked for sanity (min <= max).
	 * @param min the new minimal value to set
	 */
	public void setMin( double min ) {
		this.min = min;
	}
	
	/**
	 * Sets the maximal value. 
	 * Note: the range is not checked for sanity (min <= max).
	 * @param max the new maximal value
	 */
	public void setMax( double max ) {
		this.max = max;
	}
	
	/**
	 * sets both boundaries of the range
	 * @param min the new minimal value
	 * @param max the new maximal value
	 */
	public void set( double min, double max ) {
		this.min=min;
		this.max=max;
	}
	
	/**
	 * Updates the minimal value for the range if necessary
	 * @param min the new possible minimal value
	 */
	public void updateMin( double min ) {
		this.min = FastMath.min( min, this.min );
	}
	
	/**
	 * Updates the maximal value for the range if necessary
	 * @param max the new possible maximal value
	 */
	public void updateMax( double max ) {
		this.max = FastMath.max( max, this.max );
	}
	
	@Override
	public String toString() {
		return "["+this.min+","+this.max+"]";
	}
	
}
