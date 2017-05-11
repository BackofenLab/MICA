package de.uni_freiburg.bioinf.mica.algorithm;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

/**
 * Implements the data object to represent a curve based on a set of 2D
 * coordinates. 
 * 
 * Coordinates in-between the stored coordinates are interpolated based
 * on a cubic spline.
 * 
 * The first derivative of the curve is also generated based on the first
 * derivative of the interpolation.
 * 
 * @author Martin Mann
 * 
 */
public class Curve {
	
	
	/**
	 * The name of the profile.
	 */
	protected String name;
	/**
	 * The y values of the curve's 2D coordinates 
	 */
	protected final double[] yCoord;
	/**
	 * The maximal y value of the curve's 2D coordinates 
	 */
	protected double yMax = Double.NaN;
	/**
	 * The minimal value of the curve's 2D coordinates 
	 */
	protected double yMin = Double.NaN;
	/**
	 * The x values of the curve's 2D coordinates
	 */
	protected final double[] xCoord;
	/**
	 * The interpolation of the curve's 2D coordinates
	 */
	protected PolynomialSplineFunction interpolation = null;
	/**
	 * The first derivative of the interpolation of the curve's 2D coordinates.
	 * This member is filled on request
	 */
	protected UnivariateFunction derivative;
	/**
	 * The first derivatives for each of the curve's 2D coordinates
	 * based on the interpolation of the coordinates.
	 * This member is filled on request by {@link #getSlope()}
	 */
	protected double[] slope = null;
	/**
	 * The maximal value of the curve's slope values 
	 */
	protected double slopeMax = Double.NaN;
	/**
	 * The minimal value of the curve's slope values 
	 */
	protected double slopeMin = Double.NaN;

	/**
	 * Initializes the curve based on the y-values only. 
	 *
	 * The x values of the coordinates are assumed to be uniformly distributed
	 * within the interval [0,(length[y]-1)]
	 * 
	 * @param name
	 *            Is the name of the profile (!= null)
	 * @param yValues
	 *            Are the data points for this profile (!= null && length >= 2)
	 *            
	 * @throws NullArgumentException If one of the parameters is null.
	 * @throws IllegalArgumentException If the length of yValues is smaller than 2 
	 * @throws IllegalArgumentException If the name is empty 
	 */
	public Curve(String name, double[] yValues) throws NullArgumentException, IllegalArgumentException {
		
		// check data
		if (yValues == null) throw new NullArgumentException();
		if (yValues.length < 2) throw new IllegalArgumentException("length of yValues is smaller than 2");
		
		// store name
		this.setName(name);

		// clone y values
		this.yCoord = yValues.clone();
		// initialize x data
		this.xCoord = new double[this.yCoord.length];
		IntStream.range(0, xCoord.length).forEach(i -> this.xCoord[i] = (double)i);
		
		// create interpolation
		updateInterpolation();
	}
	
	/**
	 * Initializes the curve based on coordinates. 
	 *
	 * @param name
	 *            Is the name of the profile (!= null)
	 * @param xValues
	 *            Are the x coordinates of the data points for this profile (!= null && length >= 2)
	 * @param yValues
	 *            Are the y coordinates of the data points for this profile (!= null && length >= 2)
	 *            
	 * @throws NullArgumentException If one of the parameters is null.
	 * @throws IllegalArgumentException If the length of yValues is smaller than 2 
	 * @throws IllegalArgumentException If the length of xValues and yValues differs 
	 * @throws IllegalArgumentException If the name is empty 
	 */
	public Curve(String name, double[] xValues, double[] yValues) throws NullArgumentException, IllegalArgumentException {
		
		// check data
		if (yValues == null) throw new NullArgumentException();
		if (xValues == null) throw new NullArgumentException();
		if (yValues.length < 2) throw new IllegalArgumentException("length of yValues is smaller than 2");
		if (xValues.length < 2) throw new IllegalArgumentException("length of yValues is smaller than 2");
		if (xValues.length != yValues.length) throw new IllegalArgumentException("length of xValues and yValues differs");
		
		// store name
		this.setName(name);

		// create data arrays
		this.xCoord = new double[xValues.length];
		this.yCoord = new double[yValues.length];
		// clone values
		System.arraycopy(xValues, 0, this.xCoord, 0, xValues.length);
		System.arraycopy(yValues, 0, this.yCoord, 0, yValues.length);
		
		// create spline interpolation
		updateInterpolation();
	}
	
	/**
	 * Updates the spline interpolation of the curve. 
	 * 
	 * NOTE, this function has to be called
	 * whenever the underlying data (x/y coordinates) is changed!
	 */
	public void updateInterpolation() {
//		SplineInterpolator interpolator = new SplineInterpolator();
//		AkimaSplineInterpolator interpolator = new AkimaSplineInterpolator();
		LinearInterpolator interpolator = new LinearInterpolator();
		interpolation = interpolator.interpolate(this.xCoord, this.yCoord);
		derivative = null;
		slope = null;
	}

	/**
	 * Function to set or overwrite the current name of the profile.
	 * 
	 * @param newName
	 *            The new name of the profile. (!= null)
	 * @throws NullArgumentException if the new name is null
	 * @throws IllegalArgumentException if the new name is empty 
	 */
	public void setName(String newName) throws NullArgumentException,IllegalArgumentException {
		if (newName == null) throw new NullArgumentException();
		if (newName.isEmpty()) throw new IllegalArgumentException("name is empty");
		name = new String(newName);
	}

	/**
	 * Function to access the name of the profile.
	 * 
	 * @return The name of the profile.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The number of data points the curve is based on
	 * @return
	 */
	public int size() {
		return yCoord.length;
	}

	/**
	 * Access to the x values of the curve's coordinates.
	 * 
	 * @return the x values of the curve
	 */
	public double[] getX() {
		return xCoord;
	}
	
	/**
	 * Access to the minimal x value covered by the curve
	 * @return
	 */
	public double getXmin() {
		return xCoord[0];
	}
	
	/**
	 * Access to the maximal x value covered by the curve
	 * @return
	 */
	public double getXmax() {
		return xCoord[this.size()-1];
	}

	/**
	 * @return the minimal y-value of the curve's coordinates 
	 */
	public double getYmin() {
		// check if lazy computation is necessary
		if (Double.isNaN(yMin)) {
			this.yMin = DoubleStream.of(this.yCoord).min().getAsDouble();
		}
		return yMin;
	}
	
	/**
	 * @return the maximal y-value of the curve's coordinates 
	 */
	public double getYmax() {
		// check if lazy computation is necessary
		if (Double.isNaN(yMax)) {
			this.yMax = DoubleStream.of(this.yCoord).max().getAsDouble();
		}
		return yMax;
	}
	
	/**
	 * Access to the length of the x-interval covered by the curve, 
	 * i.e. ({@link #getXmax()}-{@link #getXmin()})
	 * @return ({@link #getXmax()}-{@link #getXmin()})
	 */
	public double length() {
		return this.getXmax()-this.getXmin();
	}
	
	/**
	 * Access to the y values of the curve's coordinates.
	 * 
	 * @return the y values of the curve
	 */
	public double[] getY() {
		return yCoord;
	}
	
	
	/**
	 * Access to the interpolated y value for the given x coordinate.
	 * 
	 * The interpolation is done based on an interpolation of the curve's coordinates.
	 * 
	 * Note, the coordinate has to be in the range {@link #getXmin()} to {@link #getXmax()}
	 * 
	 * @param x the x value for the coordinate of interest
	 * @return the interpolated y value.
	 * 
	 * @throws OutOfRangeException if the x coordinate is out of range
	 */
	public double getY( double x ) throws OutOfRangeException {
		if (x < getXmin() || x > getXmax()) throw new OutOfRangeException( x, getXmin(), getXmax());
		// get interpolated value
		return interpolation.value( x );
	}
	
	/**
	 * Gives the first derivative for the requested x coordinate. 
	 * 
	 * The derivative is based on an interpolation of the curve's coordinates.
	 * 
	 * Note, the coordinate has to be in the range {@link #getXmin()} to {@link #getXmax()}
	 * 
	 * @param x the x value for the coordinate of interest
	 * @return the interpolated derivative value.
	 */
	public double getSlope( double x ) throws OutOfRangeException {
		// check range
		if (x < getXmin() || x > getXmax()) throw new OutOfRangeException( x, getXmin(), getXmax());
		// check if the derivative was already requested, if not create object
		if (derivative == null) {
			derivative = interpolation.derivative();
		}
		// get interpolated value
		return derivative.value( x );
	}
		
	
	/**
	 * Access to slope value, i.e. the first derivative, for each of the curve's coordinates.
	 * 
	 * Note, the value is the first derivative of the interpolation of the curve's coordinates.
	 * 
	 * @return the slope (first derivative) value for each coordinate
	 */
	public double[] getSlope() {
		// ensure the slope values are initialized
		initSlope();
		// slope access
		return slope;
	}

	/**
	 * The maximal slope value for the curve
	 * @return the slope's maximal value
	 */
	public double getSlopeMax() {
		initSlope();
		return slopeMax;
	}
	
	/**
	 * The minimal slope value for the curve
	 * @return the slope's minimal value
	 */
	public double getSlopeMin() {
		initSlope();
		return slopeMin;
	}
	
	/**
	 * initializes the slope values if not already done.
	 */
	protected void initSlope() {
		// check if slope was already computed
		if (slope == null) {
			// create new slope data object 
			slope = yCoord.clone();
			// initialize min/max
			slopeMin = Double.MAX_VALUE;
			slopeMax = Double.MIN_VALUE;
			// copy derivative values to slope
			for (int i=0; i<this.size(); i++) {
				// get slope value
				slope[i] = getSlope(getX()[i]);
				// update min/max
				if (slope[i]<slopeMin) {
					slopeMin = slope[i];
				} 
				if (slope[i] > slopeMax) {
					slopeMax = slope[i];
				}
			}
		}
	}
	
	/**
	 * Creates a string representation of the curve data
	 * @return a curve string representation
	 */
	public String toString() {
		String ret = "" + getName() + ".Y = ";
		ret += ArrayUtils.toString(getY());
		ret += "\n" + getName() + ".X = ";
		ret += ArrayUtils.toString(getX());
		return ret;
	}
	
	/**
	 * Creates a string representation of the curve data with a given maximal number of 
	 * digits for each number.
	 * @digits the maximal number of digits to be printed for each number [0,10]
	 * @return a curve string representation
	 */
	public String toString(int digits) {
		if (digits < 0 || digits > 10) throw new OutOfRangeException(digits, 0, 10);
		DecimalFormat df = new DecimalFormat("###,###,##0."+StringUtils.repeat('#', digits), new DecimalFormatSymbols(Locale.ENGLISH));
		df.setRoundingMode(RoundingMode.HALF_UP);
		String ret = "";
		ret += getName() + ".Y = ";
		ret += "{"+IntStream.range(0,size()).mapToObj(i->df.format(getY()[i])).collect(Collectors.joining(","))+"}";
		ret += "\n" + getName() + ".X = ";
		ret += "{"+IntStream.range(0,size()).mapToObj(i->df.format(getX()[i])).collect(Collectors.joining(","))+"}";
		;
		return ret;
	}
	
	/**
	 * Returns the hashcode based on the hashcode of the curve name
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	
	@Override
	public boolean equals(Object obj) {
		
		// check pointer
		if (this == obj) {
			return true;
		}
		// check class
		if (! (obj instanceof Curve) ) {
			return false;
		}
		// cast
		Curve o = (Curve)obj;
		// check size
		if (this.size() != o.size()) {
			return false;
		}
		// check length
		if (this.length() != o.length()) {
			return false;
		}
		// check name
		if ( ! this.name.equals( o.name )) {
			return false;
		}
		// check x and y coordinate
		for (int i=0; i<this.size(); i++) {
			if (this.xCoord[i] != o.xCoord[i] || this.yCoord[i] != o.yCoord[i]) {
				return false;
			}
		}
		// nothing found that is different
		return true;
	}
	
	/**
	 * Computes interpolated y-coordinates for a given number of equidistant x-coordinates.
	 * The first and last y-coordinate are maintained
	 * @param samples number of equidistant x-coordinate samples to interpolate (>=2)
	 * @return the interpolated y-coordinates
	 * @throws OutOfRangeException if samples leaves sane boundaries
	 */
	public double[] getYequiX( int samples ) throws OutOfRangeException {
		// input check
		if (samples < 2 || samples >= Integer.MAX_VALUE/2) throw new OutOfRangeException( samples, 2, Integer.MAX_VALUE/2);
		
		// get delta value
		double deltaX = length() / (double)(samples-1);
		
		double[] yNew = new double[samples];
		// copy first and last
		yNew[0] = getY()[0];
		yNew[samples-1] = getY()[size()-1];
		// interpolate enclosed values
		for (int i=1; i+1<samples; i++) {
			yNew[i] = getY( getXmin() + deltaX * (double)i ) ;
		}
		
		// return interpolated data
		return yNew;
	}
	
	
	/**
	 * Returns the index of the data point that is closest to the given
	 * x-coordinate.
	 * @param x the x-coordinate of interest
	 * @return the index of the data point closest to the given x-coordinate 
	 */
	public int getClosestPoint( double x ) {
		// do binary search
		int i = Arrays.binarySearch( getX(), x );
		
		// check if not explicitly found
		if (i < 0) {
			// get index in range
			i = -1*(i+1); 
			if (i > 0 && i<size()) {
				// check if distance to predecessor is smaller
				if ( FastMath.abs(x-getX()[i-1]) 
						<= FastMath.abs(getX()[i]-x) ) 
				{
					i -= 1;
				}
			}
		}
		
		// ensure upper bound
		if (i >= size()) {
			i = size()-1;
		}
		
		// return closest index
		return i;
	}
	
}
