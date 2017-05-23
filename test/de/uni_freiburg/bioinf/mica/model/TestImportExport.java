package de.uni_freiburg.bioinf.mica.model;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.FastMath;
import org.junit.Assert;
import org.junit.Test;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.model.ImportExport.OutType;

public class TestImportExport {

	final double[] yIncrease3 = IntStream.rangeClosed(1,3).asDoubleStream().toArray();
	final double[] yIncrease5 = IntStream.rangeClosed(1,5).asDoubleStream().toArray();

	@Test
	public final void testExportCsv() {
		
		// dummy output
		StringWriter stringOutput = new StringWriter();
		
		// dummy data
		LinkedList< Curve > curves = new LinkedList<>();
		LinkedList< OutType > outPerCurve = new LinkedList<>();
		curves.add( new Curve("c1", yIncrease3) );
		outPerCurve.add( OutType.OutXY );
		curves.add( new Curve("c2", yIncrease5) );
		outPerCurve.add( OutType.OutX );
		curves.add( new Curve("c3", yIncrease3) );
		outPerCurve.add( OutType.OutY );

		try {
			
			// write output
			CsvFactory.exportCSV(stringOutput, curves, outPerCurve, ',');
			
			// check output
			String output = stringOutput.toString();
			String[] lines = output.split("\n");
			
			// check line number
			Assert.assertEquals( 1+curves.stream().flatMapToInt( c -> IntStream.of(c.size())).max().getAsInt(), lines.length);
			// check column number
			Assert.assertEquals(curves.size()+1, lines[0].split(",").length); 
			Assert.assertEquals(curves.size()+1, lines[1].split(",").length); 
			// check entries
			Assert.assertArrayEquals( new String[]{"X_c1","c1","X_c2","c3"}, lines[0].split(","));
			Assert.assertArrayEquals( new int[]{0,1,0,1}, Arrays.stream(lines[1].split(",")).flatMapToInt( s -> IntStream.of(Double.valueOf(s).intValue())).toArray() );
			
		} catch (Exception e) {
			fail("exception raised : "+e.getMessage());
		}
		
		
	}

}
