package de.uni_freiburg.bioinf.mica.algorithm;

import org.apache.commons.math3.util.FastMath;

public class IntRange {
	
	/**
	 * minimal value of the range
	 */
	private int min;
	
	/**
	 * maximal value of the range
	 */
	private int max;
	
	
	/**
	 * initializes the range with [0,0]
	 */
	public IntRange() {
		this.min = 0;
		this.max = 0;
	}
	
	/**
	 * initializes the given range [min,max]
	 * Note: the range is not checked for sanity (min <= max).
	 * @param min the minimal value
	 * @param max the maximal value
	 */
	public IntRange( int min, int max ) {
		set(min,max);
	}
	
	/**
	 * Access to the minimal value of the range
	 * @return the minimal value
	 */
	public int getMin() {
		return min;
	}
	
	/**
	 * Access to the maximal value of the range
	 * @return the maximal value
	 */
	public int getMax() {
		return max;
	}
	
	/**
	 * Returns the range of max-min
	 * @return (max-min)
	 */
	public int getRange() {
		return max-min;
	}

	/**
	 * Sets the minimal value.
	 * Note: the range is not checked for sanity (min <= max).
	 * @param min the new minimal value to set
	 */
	public void setMin( int min ) {
		this.min = min;
	}
	
	/**
	 * Sets the maximal value. 
	 * Note: the range is not checked for sanity (min <= max).
	 * @param max the new maximal value
	 */
	public void setMax( int max ) {
		this.max = max;
	}
	
	/**
	 * sets both boundaries of the range
	 * @param min the new minimal value
	 * @param max the new maximal value
	 */
	public void set( int min, int max ) {
		this.min=min;
		this.max=max;
	}
	
	/**
	 * Updates the minimal value for the range if necessary
	 * @param min the new possible minimal value
	 */
	public void updateMin( int min ) {
		this.min = FastMath.min( min, this.min );
	}
	
	/**
	 * Updates the maximal value for the range if necessary
	 * @param max the new possible maximal value
	 */
	public void updateMax( int max ) {
		this.max = FastMath.max( max, this.max );
	}
	
	@Override
	public String toString() {
		return "["+this.min+","+this.max+"]";
	}
}
