package de.uni_freiburg.bioinf.mica.algorithm;

import java.util.OptionalInt;
import java.util.stream.IntStream;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.util.FastMath;

/**
 * Instantiation of an annotation of a coordinate within an {@link AnnotatedCurve}.
 * 
 * @author Mmann
 *
 */
public class CurveAnnotation {

	
	/**
	 * Defines the different types of curve annotations considered 
	 * together with an order on them and an alignability check.
	 */
	public static enum Type {
		
		/** Standard annotation value for coordinates without further properties (default) */
		IS_POINT(0),
		
		/** Annotation value for a coordinate that is automatically annotated as a descending inflection point (local minimum in first derivative) */
		IS_DERIVATIVE_MINIMUM_AUTO(-1),
		/** Annotation value for a coordinate that is automatically annotated as an ascending inflection point (local maximum in first derivative) */
		IS_DERIVATIVE_MAXIMUM_AUTO(-2),
		
		/** Annotation value for an automatically annotated inflection point in an ascent */
		IS_INFLECTION_ASCENDING_AUTO(-3),
		/** Annotation value for an automatically annotated inflection point in a descent */
		IS_INFLECTION_DESCENDING_AUTO(-4),
		
		/** Annotation value for an automatically annotated local minimum coordinate */
		IS_MINIMUM_AUTO(-5),
		/** Annotation value for an automatically annotated local maximum coordinate */
		IS_MAXIMUM_AUTO(-6),
		
		/** Annotation value for the automatically annotated first coordinate */
		IS_START(-7),
		/** Annotation value for the automatically annotated last coordinate */
		IS_END(-8),
		
		
		///////////////  MANUAL ANNOTATIONS  ///////////////////
		
		/** Annotation value for a manually assigned fixed decomposition to be done */
		IS_SPLIT(-1*IS_START.value),
		
		/** Annotation value for a manually annotated local maximum coordinate */
		IS_MAXIMUM_MAN(-1*IS_MAXIMUM_AUTO.value),
		/** Annotation value for a manually annotated local minimum coordinate */
		IS_MINIMUM_MAN(-1*IS_MINIMUM_AUTO.value),
		
		/** Annotation value for a manually annotated inflection point in an ascent */
		IS_INFLECTION_ASCENDING_MAN(-1*IS_INFLECTION_ASCENDING_AUTO.value),
		/** Annotation value for a manually annotated inflection point in an descent */
		IS_INFLECTION_DESCENDING_MAN(-1*IS_INFLECTION_DESCENDING_AUTO.value),
		
		/** Annotation value for a coordinate that is manually annotated as an ascending inflection point (local maximum in first derivative) */
		IS_DERIVATIVE_MAXIMUM_MAN(-1*IS_DERIVATIVE_MAXIMUM_AUTO.value),
		/** Annotation value for a coordinate that is manually annotated as a descending inflection point (local minimum in first derivative) */
		IS_DERIVATIVE_MINIMUM_MAN(-1*IS_DERIVATIVE_MINIMUM_AUTO.value);
		
		/** the index value of this type; used for comparison and ordering */
		public final int value;
		
		/** Constructs a type with the given index value,
		 * @param value the index value of this type
		 */
		Type( int value ) {
			this.value=value;
		}
		
		/**
		 * returns whether or not this type represents an extremum of the y-coordinates
		 * @return true if this == IS_MAXIMUM_* or == IS_MINIMUM_*
		 */
		public boolean isExtremumY() {
			return FastMath.abs(this.value)==IS_MAXIMUM_MAN.value 
					|| FastMath.abs(this.value)==IS_MINIMUM_MAN.value;
		}
		
		/**
		 * returns whether or not this type represents an extremum of the y-coordinates
		 * @return true if this == IS_MAXIMUM_* or == IS_MINIMUM_*
		 */
		public boolean isExtremumSlope() {
			return FastMath.abs(this.value)==IS_DERIVATIVE_MAXIMUM_MAN.value 
					|| FastMath.abs(this.value)==IS_DERIVATIVE_MINIMUM_MAN.value;
		}
		
		/**
		 * returns whether or not this type represents an extremum of the y-coordinates
		 * @return true if this == IS_MAXIMUM_* or == IS_MINIMUM_*
		 */
		public boolean isInflection() {
			return FastMath.abs(this.value)==IS_INFLECTION_ASCENDING_MAN.value 
					|| FastMath.abs(this.value)==IS_INFLECTION_DESCENDING_MAN.value;
		}

		
		/**
		 * Returns the Type that shows the given value
		 * @param value the value of interest
		 * @return the type that holds the given value as index value
		 * @throws IllegalArgumentException if the value is not known for any type
		 */
		static Type ofValue( int value ) throws IllegalArgumentException {
			// find index for the type that holds the given value
			OptionalInt index = IntStream.range(0, Type.values().length).filter( i -> values()[i].value == value ).findAny();
			// check if found
			if (index.isPresent()) {
				return values()[index.getAsInt()];
			} else {
				throw new IllegalArgumentException("given type value is unkown");
			}
		}
		
		/**
		 * Compares two types and defines an order of them
		 * @param t1 the first type to compare
		 * @param t2 the second type to compare
		 * @return a negative value, zero, or a positive value if t1 smaller t2, t1 equals t2, or t1 larger t2, respectively.
		 */
		static int compare( Type t1, Type t2 ) {
			
			int order = 0; // assume equal
			
			// check manual annotations
			if (t1.value>0 || t2.value>0) {
				////////////////////////
				// one is > 0 -> manual annotation = highest order
				////////////////////////
				// just integer comparison, the highest value is best
				order = Integer.valueOf(t1.value).compareTo( t2.value );
			} else {
				////////////////////////
				// both are <= 0  -> automatic annotations : smallest value = highest order
				////////////////////////
				// reverse "normal" integer order, since lowest == best
				order = -1 * Integer.valueOf(t1.value).compareTo( t2.value );
			}
			
			// general order on
			return order;
		}
		
		/**
		 * Whether or not two types can be matched within alignments 
		 * @param t1 the first type
		 * @param t2 the second type
		 * @return true if t1 can be aligned with t2; falso otherwise
		 */
		static boolean isAlignable( Type t1, Type t2 ) {
			
			// check if identical
			if (t1.value==t2.value) {
				return true;
			}
			// check if corresponding manual/automatic annotations
			if (FastMath.abs(t1.value)==FastMath.abs(t2.value)) {
				return true;
			}
			// else do not align
			return false;
		}
		
		/**
		 * Returns a string representation of the type
		 * @param t
		 * @return
		 */
		static String toString( Type t ) {
			switch( t ) {
			case IS_POINT : return "point";
			case IS_MAXIMUM_MAN : return "maxYm";
			case IS_MINIMUM_MAN : return "minYm";
			case IS_MAXIMUM_AUTO : return "maxYa";
			case IS_MINIMUM_AUTO : return "minYa";
			case IS_INFLECTION_ASCENDING_MAN : return "infAm";
			case IS_INFLECTION_DESCENDING_MAN : return "infDm";
			case IS_INFLECTION_ASCENDING_AUTO : return "infAa";
			case IS_INFLECTION_DESCENDING_AUTO : return "infDa";
			case IS_DERIVATIVE_MAXIMUM_MAN : return "maxSm";
			case IS_DERIVATIVE_MINIMUM_MAN : return "minSm";
			case IS_DERIVATIVE_MAXIMUM_AUTO : return "maxSa";
			case IS_DERIVATIVE_MINIMUM_AUTO : return "minSa";
			case IS_SPLIT : return "split";
			case IS_START : return "start";
			case IS_END : return "end";
			}
			return "type("+String.valueOf(t.value)+")";
		}

		/**
		 * Checks whether or not two given types are opposite, 
		 * i.e. maximum vs. minimum;
		 * Other types are not handled 
		 * @param type1
		 * @param type2
		 * @return
		 */
		public static boolean isOpposite(Type type1, Type type2) {
			// check for opposite types
			switch (type1) {
			
			case IS_MAXIMUM_AUTO:
			case IS_MAXIMUM_MAN:
				return type2 == IS_MINIMUM_AUTO || type2 == IS_MINIMUM_MAN;
				
			case IS_MINIMUM_AUTO:
			case IS_MINIMUM_MAN:
				return type2 == IS_MAXIMUM_AUTO || type2 == IS_MAXIMUM_MAN;
				
			case IS_DERIVATIVE_MAXIMUM_AUTO:
			case IS_DERIVATIVE_MAXIMUM_MAN:
				return type2 == IS_DERIVATIVE_MINIMUM_AUTO || type2 == IS_DERIVATIVE_MINIMUM_MAN;
				
			case IS_DERIVATIVE_MINIMUM_AUTO:
			case IS_DERIVATIVE_MINIMUM_MAN:
				return type2 == IS_DERIVATIVE_MAXIMUM_AUTO || type2 == IS_DERIVATIVE_MAXIMUM_MAN;
				
				// default : not opposite
			default:
				return false;
			}
		}

		/**
		 * Whether or not this is a manual annotation
		 * @return true, if this is a manual annotation; false otherwise.
		 */
		public boolean isManual() {
			// check if positive value larger than point annotation
			return this.value > IS_POINT.value;
		}

		/**
		 * Whether or not this is an interval boundary annotation, i.e.
		 * START, END, or SPLIT
		 * @return true if this is START, END, or SPLIT
		 */
		public boolean isIntervalBoundary() {
			return 
					   this == IS_START 
					|| this == IS_END
					|| this == IS_SPLIT
					;
		}
	}
	
	
	/**
	 * Reference to the curve this annotation is about. 
	 */
	private final AnnotatedCurve curve;
	
	/**
	 * The index of the points within the curve's coordinates.
	 */
	private final int index;
	
	/**
	 * Creates a new annotation object which represents the annotation of
	 * one coordinate of a curve.
	 * @param curve the curve the annotation is about
	 * @param index the index within the curve's coordinates this annotation is about
	 */
	public CurveAnnotation( AnnotatedCurve curve, int index)
		throws NullArgumentException, IndexOutOfBoundsException
	{
		// sanity checks
		if (curve==null) throw new NullArgumentException();
		if (index < 0 || index >= curve.size()) throw new IndexOutOfBoundsException("index is out of the boundaries of the curve's coordinates");
		// setup data
		this.curve = curve;
		this.index = index;
	}

	/**
	 * Access to the type of the annotation. 
	 * A value > IS_POINT represents manual annotations.
	 * @return the annotation type
	 */
	public Type getType() {
		return this.curve.getAnnotation()[this.index];
	}
	
	/**
	 * Get the coordinate's index within the curve this annotation is about
	 * @return the index within the curve's coordinates
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Get the curve this coordinate annotation is for
	 * @return the curve this annotation is about
	 */
	public AnnotatedCurve getCurve() {
		return curve;
	}

	/**
	 * generates a string representation of this curve annotation type
	 */
	public String toString() {
		return "("+String.valueOf(getIndex())+","+Type.toString(getType())+")";
	}
	
}
