package de.uni_freiburg.bioinf.mica.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.algorithm.IntervalDecomposition;
import de.uni_freiburg.bioinf.mica.view.ColoredAnnotatedCurve;
import de.uni_freiburg.bioinf.mica.view.ColoredAnnotatedCurvePlot;

public class Debug {
	
	/**
	 * default colors to be used for visualization (ring list usage)
	 */
	static Color[] defaultColorSet = {
		Color.ORANGE
		,Color.MAGENTA
		,Color.PINK
		,Color.RED
		,Color.YELLOW
		,Color.GREEN
		,Color.LIGHT_GRAY
		,Color.BLUE
		,Color.CYAN
		};

	
	/**
	 * Plots the getCurve() objects for given set of curve decompositions 
	 * with the info string in the title of the JDialog with yes/no option
	 * 
	 * @param info the title of the dialog to show
	 * @param curves the decompositions to plot
	 * @return true if the user used the "yes" button, false otherwise
	 */
	public static boolean plotCurves(String info, IntervalDecomposition... curves) {
		Curve[] dat = new Curve[curves.length];
		for (int i=0; i<curves.length; i++)
			dat[i] = curves[i].getCurve();
		return plotCurves(info, dat );
	}
	
	
	/**
	 * plots the given curves in a JDialog with yes/no option
	 * 
	 * @param curves the curves to plot
	 * @return true if the user used the "yes" button, false otherwise
	 */
	public static boolean plotCurves(String info, Curve... curves) {
		LinkedList<ColoredAnnotatedCurve> profiles = new LinkedList<>();
		for( int c=0; c<curves.length; c++) {
			profiles.add( new ColoredAnnotatedCurve( new AnnotatedCurve( curves[c] ), defaultColorSet[c % defaultColorSet.length]));
		}
		
		ColoredAnnotatedCurvePlot plot = new ColoredAnnotatedCurvePlot();
		plot.plotProfiles( profiles );
		plot.setMinimumSize( new Dimension(1280, 640) );
		Dimension maxSize = Toolkit.getDefaultToolkit().getScreenSize();
		maxSize.width -= 150;
		maxSize.height-= 150;
		plot.setMaximumSize( maxSize );
		plot.setPreferredSize( maxSize );
		
		int selection = JOptionPane.showOptionDialog(null, plot, info, JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		return selection == JOptionPane.OK_OPTION;
	}
	
	/**
	 * general debug stream for MICA
	 */
	public static java.io.PrintStream out = System.err;
	
	

}
