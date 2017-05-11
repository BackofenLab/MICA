package de.uni_freiburg.bioinf.mica.view;

import java.util.LinkedList;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableDoubleValue;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.util.FastMath;

import de.uni_freiburg.bioinf.mica.algorithm.DoubleRange;


public abstract class DoubleParameter implements ObservableDoubleValue {

	/**
	 * Sets the value of the parameter if it is within the allowed value range
	 * and informs the registered change listener
	 * @param newValue the new value
	 * @throws OutOfRangeException if the value exceeds the allowed range
	 */
	abstract protected void setValue( double newValue ) throws OutOfRangeException;
	
	/**
	 * Provides the allowed range for values of this parameter
	 * @return the min/max boundaries of the allowed range
	 */
	abstract public DoubleRange getRange();
	
	/**
	 * The step width for values selectable in the interface
	 * @return the step width
	 */
	abstract public double getStepWidth();
	
	/**
	 * Name to be displayed for the parameter
	 * @return the name
	 */
	abstract public String getName();
	
	/**
	 * Verbose description of the parameter to be shown e.g. as a tooltip
	 * @return the description
	 */
	abstract public String getDescription();
	
	
	/**
	 * Sets the value of the parameter if it is within the allowed value range
	 * and informs the registered change listener
	 * @param newValue the new value
	 * @throws OutOfRangeException if the value exceeds the allowed range
	 */
	public void set(double newValue) throws OutOfRangeException {
		if (newValue != get()) {
			// check value
			if (newValue < getRange().getMin() || newValue > getRange().getMax()) {
				throw new OutOfRangeException(newValue, getRange().getMin(), getRange().getMax());
			}
			// update value
			double oldValue = get();
			setValue(newValue);
			// inform listener
			for (ChangeListener<? super Number> listener : this.changeListener) {
				listener.changed( this, oldValue, newValue);
			}
		}
	}
	

	@Override
	public double doubleValue() {
		return get();
	}

	@Override
	public float floatValue() {
		return (float)get();
	}

	@Override
	public int intValue() {
		return (int)FastMath.floor(get());
	}

	@Override
	public long longValue() {
		return (long)FastMath.floor(get());
	}
	
	@Override
	public Number getValue() {
		return get();
	}
	
	/**
	 * list of change listener
	 */
	protected LinkedList<ChangeListener<? super Number>> changeListener = new LinkedList<>();

	@Override
	public void addListener(ChangeListener<? super Number> listener) {
		if (listener != null && !changeListener.contains(listener)) {
			changeListener.add(listener);
		}
	}

	@Override
	public void removeListener(ChangeListener<? super Number> listener) {
		if (listener != null) {
			changeListener.remove( listener );
		}
	}
	
	@Override
	public void addListener(InvalidationListener arg0) {
		throw new RuntimeException("not implemented");
	}

	@Override
	public void removeListener(InvalidationListener arg0) {
		throw new RuntimeException("not implemented");
	}

	
	
}
