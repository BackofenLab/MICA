package de.uni_freiburg.bioinf.mica.algorithm;

/**
 * Enumeration for the point type. This enumeration is used to specify the type
 * of the reference point object. Two types are possible inflection or extreme.
 * 
 * @author mbeck
 * 
 */
public enum PointType {
	/**
	 * Are the types of the point type.
	 * 
	 * EXTREME_MAX - Is the type for a reference point of the type extreme,
	 * where the extreme point is a maximum.
	 * 
	 * EXTREME_MIN - Is the type for a reference point of the type extreme,
	 * where the extreme point is a minimum.
	 * 
	 * INFLECTION_ASC - Is the type for a reference point of the type
	 * inflection, where the inflection point slope is ascending.
	 * 
	 * INFLECTION_DES - Is the type for a reference point of the type
	 * inflection, where the inflection point slope is descending.
	 * 
	 * REFPOINT_START - Is the type fir a reference point to specify the
	 * leftmost point of the data point set.
	 * 
	 * REFPOINT_END - Is the type fir a reference point to specify the rightmost
	 * point of the data point set.
	 * 
	 * NONE - Specifies that this point is no reference point. In fact this
	 * means the point is an data point.
	 */
	EXTREME_MAX, EXTREME_MIN, INFLECTION_ASC, INFLECTION_DES, REFPOINT_START, REFPOINT_END, NONE;
	
	public static PointType toPointType( CurveAnnotation a ) {
		switch( a.getType() ) {
		case IS_MAXIMUM_MAN : return EXTREME_MAX;
		case IS_MINIMUM_MAN : return EXTREME_MIN;
		case IS_MAXIMUM_AUTO : return EXTREME_MAX;
		case IS_MINIMUM_AUTO : return EXTREME_MIN;
		case IS_INFLECTION_ASCENDING_MAN : return INFLECTION_ASC;
		case IS_INFLECTION_DESCENDING_MAN : return INFLECTION_DES;
		case IS_INFLECTION_ASCENDING_AUTO : return INFLECTION_ASC;
		case IS_INFLECTION_DESCENDING_AUTO : return INFLECTION_DES;
		case IS_START : return REFPOINT_START;
		case IS_END : return REFPOINT_END;
		default: return NONE;
		}
	}
}
