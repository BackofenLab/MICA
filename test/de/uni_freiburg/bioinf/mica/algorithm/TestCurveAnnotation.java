package de.uni_freiburg.bioinf.mica.algorithm;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation.Type;

public class TestCurveAnnotation {

	// generally expect no exception
	@Rule
 	public ExpectedException thrown= ExpectedException.none();

	
	@Test
	public final void testTypeCompare() {
		Assert.assertTrue( Type.compare( Type.IS_DERIVATIVE_MAXIMUM_AUTO, Type.IS_INFLECTION_ASCENDING_AUTO) < 0);
		Assert.assertTrue( Type.compare( Type.IS_DERIVATIVE_MINIMUM_AUTO, Type.IS_INFLECTION_ASCENDING_AUTO) < 0);
		Assert.assertTrue( Type.compare( Type.IS_DERIVATIVE_MAXIMUM_AUTO, Type.IS_INFLECTION_DESCENDING_AUTO) < 0);
		Assert.assertTrue( Type.compare( Type.IS_DERIVATIVE_MINIMUM_AUTO, Type.IS_INFLECTION_DESCENDING_AUTO) < 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_ASCENDING_AUTO, Type.IS_MAXIMUM_AUTO ) < 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_DESCENDING_AUTO, Type.IS_MAXIMUM_AUTO ) < 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_ASCENDING_AUTO, Type.IS_MINIMUM_AUTO ) < 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_DESCENDING_AUTO, Type.IS_MINIMUM_AUTO ) < 0);
		
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_ASCENDING_AUTO, Type.IS_DERIVATIVE_MAXIMUM_AUTO) > 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_DESCENDING_AUTO, Type.IS_DERIVATIVE_MAXIMUM_AUTO) > 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_ASCENDING_AUTO, Type.IS_DERIVATIVE_MINIMUM_AUTO) > 0);
		Assert.assertTrue( Type.compare( Type.IS_INFLECTION_DESCENDING_AUTO, Type.IS_DERIVATIVE_MINIMUM_AUTO) > 0);
		Assert.assertTrue( Type.compare( Type.IS_MAXIMUM_AUTO, Type.IS_INFLECTION_ASCENDING_AUTO ) > 0);
		Assert.assertTrue( Type.compare( Type.IS_MAXIMUM_AUTO, Type.IS_INFLECTION_DESCENDING_AUTO ) > 0);
	}


}
