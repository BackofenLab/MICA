package de.uni_freiburg.bioinf.mica.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.apache.commons.math3.util.FastMath;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation.Type;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotationFilter;
import de.uni_freiburg.bioinf.mica.algorithm.DoubleRange;
import de.uni_freiburg.bioinf.mica.algorithm.IntRange;
import de.uni_freiburg.bioinf.mica.controller.Debug;

/**
 * Object which is responsible for printing the profile data content as graph.
 * 
 * @author mbeck
 * @author Mmann
 * 
 */
public class ColoredAnnotatedCurvePlot extends JPanel implements MouseListener,
		MouseMotionListener, MouseWheelListener, Observer, ChangeListener<Number> {
	
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Set which contains all profile data objects for plotting
	 */
	private LinkedList<ColoredAnnotatedCurve> curves = new LinkedList<ColoredAnnotatedCurve>();
	/**
	 * X boundaries of all curves
	 */
	private DoubleRange curvesX = new DoubleRange();
	/**
	 * Y boundaries of all curves
	 */
	private DoubleRange curvesY = new DoubleRange();
	/**
	 * Index of the profile in the foreground
	 */
	private int selectCurveIndex = -1;
	/**
	 * whether or not the curve annotations should be plotted
	 */
	private boolean showAnnotations = true;
	/**
	 * Settings for the plot (margin and radius of the points) in pixel
	 */
	final private int margin = 40;
	/**
	 * Specifies the radius of the point in the foreground and background
	 * function in pixel.
	 */
	final private int foregrRadius = 3;
	final private int backgrRadius = 1;
	/**
	 * Specifies the the increase of the reference point radius size compared to
	 * the normal data point radius size.
	 */
	final private int refpointRadiusIncrease = 2;
	/**
	 * X boundaries of the visible plot area
	 */
	private DoubleRange plotAreaX = new DoubleRange();
	/**
	 * Y boundaries of the visible plot area
	 */
	private DoubleRange plotAreaY = new DoubleRange();
	/**
	 * Attribute for setting the legend position in the plot
	 */
	private LegendPos legendPos = LegendPos.RIGHT;
	/**
	 * index of the selected data point of the selected curve
	 */
	private int mouseHoverCurveIndex = -1;
	
	/**
	 * index of the selected data point of the selected curve
	 */
	private int mouseSelectionClickX = -1;

	/**
	 * boundaries for the zoom radius in % of the full curves' x-range
	 */
	private final IntRange zoomRangeBound = new IntRange(1, 100);
	/**
	 * Zoom radius in % of the full curves' x-range
	 */
	private int zoomRange = zoomRangeBound.getMax();
	
	/**
	 * anchor point that should be in the center of the plot area x-range 
	 */
	private double zoomAnchorX = 0;
	
	/**
	 * Attribute for mouse dragged event, this is used do get the direction of
	 * the movement for shifting the plot
	 */
	private double mouseDragX = Double.NaN;
	/**
	 * Color of the plot background
	 */
	private Color backgroundColor = Color.WHITE;;
	/**
	 * Sets the plot object to split point insertion mode
	 */
	private boolean splitInsertMode = false;
	/**
	 * Timer references for zoom up the plot if the user clicks in.
	 */
	private Timer t1 = null;
	private Timer t2 = null;
	
	/**
	 * Listener for split point insertion notification
	 */
	private LinkedList<ISplitInsertionListener> splitListener = new LinkedList<>();
	
	/**
	 * Listener for split point insertion notification
	 */
	private LinkedList<ISelectedPlotPointInfoListener> pointSelectionListener = new LinkedList<>();

	/**
	 * Constructor which add the mouse listener to the plot area and creates an
	 * empty set for storing the color profile data objects. The background
	 * color is set initial to white.
	 */
	public ColoredAnnotatedCurvePlot() {
		super();
		
		/**
		 * Add some event listeners
		 */
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		this.showAnnotations = false;
	}
	
	/**
	 * Constructor which add the mouse listener to the plot area and creates an
	 * empty set for storing the color profile data objects. The background
	 * color is set initial to white.
	 * @param showAnnotations whether or not the curve annotation should be plotted 
	 */
	public ColoredAnnotatedCurvePlot(boolean showAnnotations) {
		super();
		/**
		 * Add some event listeners
		 */
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.addMouseWheelListener(this);
		this.showAnnotations = showAnnotations;
	}

	/**
	 * Function to register timer references for sliding up the plot objects
	 * 
	 * @param timerDown
	 *            Is the timer which is responsible for sliding down this plot
	 * @param timerUp
	 *            Is the timer which is responsible for sliding up this plot.
	 */
	public void registerSlideTimer(Timer timerDown, Timer timerUp) {
		t1 = timerDown;
		t2 = timerUp;
	}

	/**
	 * Adds an split listener for getting notified if the user insert
	 * successfully a split point
	 * 
	 * @param l
	 *            The reference of the object which will be informed after a
	 *            split insert
	 */
	public void registerSplitListener(ISplitInsertionListener l) {
		if (l != null && !splitListener.contains(l)) {
			splitListener.add(l);
		}
	}
	
	/**
	 * Adds an split listener for getting notified if the user insert
	 * successfully a split point
	 * 
	 * @param l
	 *            The reference of the object which will be informed after a
	 *            split insert
	 */
	public void registerPointSelectionListener(ISelectedPlotPointInfoListener l) {
		if (l != null && !pointSelectionListener.contains(l)) {
			pointSelectionListener.add(l);
		}
	}

	/**
	 * Function to plot a single profile an wait for split point insertion.
	 * After the user inserts a split point this object informs the registered
	 * split insertion listeners about the action. This object inserts the split
	 * point automatically in the profile.
	 * 
	 * @param c
	 *            The profile which will get a split point
	 */
	public void plotProfileForSplit(ColoredAnnotatedCurve c) {
		
		if (c == null) throw new IllegalArgumentException("no curve given for split point insertion");
		
		// add the profile for split point insertion
		LinkedList<ColoredAnnotatedCurve> toPlot = new LinkedList<>();
		toPlot.add(c);
		
		// select added curve
		selectCurveIndex = 0;
		
		// plot the single profile
		plotProfiles(toPlot);
		
		// Enable the split mode
		splitInsertMode = true;
	}

	/**
	 * Function to change the background color of the plot. This function
	 * triggers a repaint of the plot.
	 * 
	 * @param c
	 *            The new color for the background
	 */
	public void setBackgroundColor(Color c) {
		if (backgroundColor != c) {
			backgroundColor = c;
			repaint();
		}
	}

	/**
	 * Function for setting a curve to the foreground of the plot
	 * 
	 * @param index
	 *            The index of the function which will be painted in foreground.
	 */
	public void setSelectedCurve(int index) {
		
		/**
		 * Inform the point listeners about the new selected point in the
		 * foreground profile.
		 */
		if (index >= -1 && index < curves.size()) {
			// check if selection has changed
			if (index != selectCurveIndex) {
				selectCurveIndex = index;
				// Disable the mouse selection if the user changes the foreground function
				mouseSelectionClickX = -1;
				// repaint plot
				repaint();
			}
		}
	}

	/**
	 * Function to overwrite the profile name by index. If the index is out of
	 * range nothing will be changed.
	 * 
	 * @param index
	 *            Index of the profile
	 * @param name
	 *            The new name of the profile which will be displayed in the
	 *            legend
	 */
	public void setProfileName(int index, String name) {
		if (index >= 0 && index < curves.size()) {
			curves.get(index).getCurve().setName(name);
			repaint();
		}
	}

	/**
	 * Function to overwrite the profile color by index. If the index is out of
	 * range nothing will be changed.
	 * 
	 * @param index
	 *            The index of the profile
	 * @param c
	 *            The new color of the profile which will be displayed in the
	 *            plot.
	 */
	public void setProfileColor(int index, Color c) {
		if (index >= 0 && index < curves.size()) {
			curves.get(index).setColor(c);
			repaint();
		}
	}

	/**
	 * Function to get the background color of the plot.
	 * 
	 * @return The background color of the plot.
	 */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/**
	 * Function to insert a set of color profile data objects. This function
	 * call removes all previous inserted profiles from the plot. Calling this
	 * function disables split mode automatically. If you are in split mode and
	 * use this function you have to set the split mode attributes after this
	 * function call again.
	 * 
	 * @param curvesToPlot the set of curves to plot
	 */
	public void plotProfiles(LinkedList<ColoredAnnotatedCurve> curvesToPlot) {
		// Ensure disabling the split mode in this function call
		splitInsertMode = false;
		
		// clear data
		curves.clear();
		
		// store curves
		if (curvesToPlot != null) {
			// store profiles for plotting
			curves.addAll(curvesToPlot);
			// reset curve boundaries
			curvesX.setMin(Double.MAX_VALUE);
			curvesX.setMax(Double.MIN_VALUE);
			curvesY.setMin(Double.MAX_VALUE);
			curvesY.setMax(Double.MIN_VALUE);
			// update x and y boundaries
			for (ColoredAnnotatedCurve c : curves) {
				curvesX.updateMin(c.getCurve().getXmin());
				curvesX.updateMax(c.getCurve().getXmax());
				curvesY.updateMin(c.getCurve().getYmin());
				curvesY.updateMax(c.getCurve().getYmax());
			}
		}
		// Reset the zoom to view all profiles
		resetZoom();
		// Repaint the plot
		repaint();
	}

	/**
	 * Function to clear the visualization of the profile plot.
	 */
	public void clearPlot() {
		plotProfiles(null);
		splitInsertMode = false;
	}

	/**
	 * Function to set the position of the legend box. Calling this function the
	 * plot repaints automatically.
	 * 
	 * @param position
	 */
	public void setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos position) {
		legendPos = position;
		repaint();
	}

	/**
	 * Function to slide up the plot according to the set timer references
	 */
	private void slideUpThePlot() {
		/**
		 * check if timers are registered to this plot objects than slide up the
		 * plot, because of user input action
		 */
		if (t1 != null && t2 != null) {
			// Enhance the plot timer 2 takes care about by starting them
			t1.stop();
			t2.start();
		}
	}


	/**
	 * Function to set the zoom range but ensures it stays within the allowed boundaries.
	 * 
	 * @param newZoomRadius
	 *            Is the new zoom radius
	 * @return true if the range was changed; false if nothing was updated
	 */
	private boolean setZoomRange(int newZoomRadius) {
		
		// set to new value within boundaries
		int newValue = FastMath.max( zoomRangeBound.getMin(), FastMath.min( newZoomRadius, zoomRangeBound.getMax()) );
		
		// update zoom if needed
		if (zoomRange != newValue) {
			zoomRange = newValue;
			updatePlotArea();
			return true;
		}
		
		// nothing updated
		return false;
	}



	/**
	 * Function to disable the zoom by resetting the zoom boundaries. 
	 */
	private void resetZoom() {
		
		// Move the selected point indication according to the zoom boundary
		if (mouseSelectionClickX != -1) {
			mouseSelectionClickX = -1;
		}
		if (mouseHoverCurveIndex != -1) {
			mouseHoverCurveIndex = -1;
		}

		// reset plot boundaries
		plotAreaX = new DoubleRange(curvesX.getMin(), curvesX.getMax());
		plotAreaY = new DoubleRange(curvesY.getMin(), curvesY.getMax());
		
		// reset zoom
		zoomRange = zoomRangeBound.getMax();
		
		// anchor zoom at left boundary as default
		zoomAnchorX = plotAreaX.getMin();

	}


	/**
	 * Function to translate a curve point to a point in the plot area.
	 * 
	 * @param x
	 *            The x-coordinate of the data point
	 * @param y
	 *            The y-coordinate of the data point
	 * @return The translated Point2D result in a double precision
	 */
	private Point transDataToPlot(double x, double y) {
		double width = this.getSize().width;
		double scaleX = (double)(width - margin - margin) / (double)plotAreaX.getRange();
		int xNew = (int) Math.round(((x-plotAreaX.getMin()) * scaleX) + margin);

		double height = this.getSize().height;
		double scaleY = (double)(height - margin - margin) / (double)plotAreaY.getRange();
		int yNew = (int) Math.round(height - margin - ((y-plotAreaY.getMin()) * scaleY));
		
		return new Point( xNew, yNew);
	}

	/**
	 * Function to translate a plot point to a relative data point position in [0,100] range.
	 * 
	 * @param x
	 *            The x part of the plot point
	 * @param y
	 *            The y part of the plot point
	 * @return The translated Point2D result in a double precision
	 */
	private Point2D transPlotToData(double x, double y) {
		
		
		double width = this.getSize().width;
		double scaleX = (double)(width - margin - margin) / (double)plotAreaX.getRange();
		double xOrig = ((x - margin) / scaleX) + plotAreaX.getMin();
		
		double height = this.getSize().height;
		double scaleY = (double)(height - margin - margin) / (double)plotAreaY.getRange();
		double yOrig = ((height - y - margin) / scaleY) + plotAreaY.getMin();

		return new Point2D.Double( xOrig, yOrig);
	}

	/**
	 * Function to determine the maximal values in y and x direction from the
	 * current set of profiles. The profile with the highest y value defines the
	 * maximal bound in y direction. The profile with the highest x value
	 * defines the maximal bound in x direction. This ensures that every profile
	 * can be plotted in the area.
	 */
	private void updatePlotArea() {

		
		// get absolute x-range of zoom area
		double absZoomRange = ((double)zoomRange)/200d*curvesX.getRange();
		
		// reset plot area boundaries for x-range given the anchor point
		if ( zoomAnchorX - absZoomRange < curvesX.getMin()) {
			plotAreaX.setMin( FastMath.max( curvesX.getMin(), zoomAnchorX - absZoomRange ) );
			plotAreaX.setMax( FastMath.min( curvesX.getMax(), plotAreaX.getMin() + (2d*absZoomRange) ) );
		} else 
		if ( zoomAnchorX + absZoomRange > curvesX.getMax()) {
			plotAreaX.setMax( FastMath.min( curvesX.getMax(), zoomAnchorX + absZoomRange ) );
			plotAreaX.setMin( FastMath.max( curvesX.getMin(), plotAreaX.getMax() - (2d*absZoomRange) ) );
		} else {
			plotAreaX.setMin( FastMath.max( curvesX.getMin(), zoomAnchorX - absZoomRange ) );
			plotAreaX.setMax( FastMath.min( curvesX.getMax(), zoomAnchorX + absZoomRange ) );
		}
		
		// reset y-boundaries
		plotAreaY.setMin(Double.MAX_VALUE);
		plotAreaY.setMax(Double.MIN_VALUE);
		
		// update y boundaries for each curve
		for (ColoredAnnotatedCurve c : curves) {
			
			// check left and right boundary (not necessary a data point of the curve)
			if (plotAreaX.getMin()>=c.getCurve().getXmin() && plotAreaX.getMin()<=c.getCurve().getXmax())
				plotAreaY.updateMin(c.getCurve().getY(plotAreaX.getMin()));
			if (plotAreaX.getMax()>=c.getCurve().getXmin() && plotAreaX.getMax()<=c.getCurve().getXmax())
				plotAreaY.updateMax(c.getCurve().getY(plotAreaX.getMax()));
			
			// find first point and last data point within range
			int startIndex = Arrays.binarySearch( c.getCurve().getX(), plotAreaX.getMin());
			if (startIndex < 0) {
				startIndex = -1 * (startIndex + 1);
			}
			int endIndex = Arrays.binarySearch( c.getCurve().getX(), plotAreaX.getMax());
			if (endIndex < 0) {
				endIndex = -1 * (endIndex+2);
			}
			
			// check if any data point enclosed
			if (startIndex > endIndex) {
				// nothing enclosed -> stop processing of this curve
				continue;
			}

			// update data
			for (int i = startIndex; i <= endIndex; i++) {
				// update the boundaries
				plotAreaY.updateMin(c.getCurve().getY()[i]);
				plotAreaY.updateMax(c.getCurve().getY()[i]);
			}
		}
		
		// Add +/- x% to the y boundaries
		final double marginPercent = 0.02;
		double plotAreaMargin = plotAreaY.getRange() * marginPercent;
		if (plotAreaMargin == 0) // if no plot range set to the margin value
			plotAreaMargin = marginPercent;
		
		// Modify the vertical axis boundaries accordingly
		plotAreaY.setMax( plotAreaY.getMax() + plotAreaMargin);
		plotAreaY.setMin( plotAreaY.getMin() - plotAreaMargin);
		
	}

	/**
	 * Function to paint the plot legend to the upper right corner of the
	 * profile plot. Every profile names is printed in the corresponding color.
	 * The legend box and the content updates automatically depending on the
	 * profile plot set content. This function needs proper initialized plot
	 * area max y and x values. If a profile name is larger than the allowed
	 * maximal string length the name will be truncated to the legend box
	 * limits.
	 * 
	 * @param g
	 *            Is the reference to the graphics 2d object for painting.
	 */
	private void paintPlotLegend(Graphics2D g) {
		/**
		 * Define the maximal length of the legend box content
		 */
		int maxStrLen = 30;
		/**
		 * Retrieve the font from the plot and get the width and height of the
		 * maximal allowed string. The width is the maximum of the profile names
		 * in the plot with respect to the maximum allowed string length
		 */
		FontMetrics fm = g.getFontMetrics();
		int maxW = 0;
		int maxH = fm.getHeight(); // Always the same height
		for (ColoredAnnotatedCurve cpd : curves) {
			String name = cpd.getCurve().getName();
			int currW = 0;
			if (name.length() < maxStrLen) {
				currW = fm.stringWidth(name);
			} else {
				currW = fm.stringWidth(name.substring(0, maxStrLen));
			}
			if (currW > maxW) {
				maxW = currW;
			}
		}
		/**
		 * Define the internal box margin pixel size
		 */
		int boxmargin = 6;
		
		int boxWidth = maxW + boxmargin + boxmargin;
		
		/**
		 * Get the position of the legend in the plot area according to the
		 * defines position. 
		 * p = upper left corner of the legend box
		 */
		Point p = null;
		switch (legendPos) {
		case LEFT:
			p = transDataToPlot(plotAreaX.getMin(), plotAreaY.getMax());
			p.x += boxmargin + 15;
			break;
		case CENTER:
			p = transDataToPlot( plotAreaX.getMin() + (plotAreaX.getMax() - plotAreaX.getMin()) / 2, plotAreaY.getMax());
			p.x -= (boxWidth/2);
			break;
		case RIGHT:
			p = transDataToPlot(plotAreaX.getMax(), plotAreaY.getMax());
			p.x -= boxWidth - boxmargin;
			break;
		case HIDDEN:
			// Box is set to hidden, leave this function to avoid painting the legend box
			return;
		default:
			Debug.out
			.println("paintPlotLegend(): undefined legend position enumeration "
					+ legendPos);
			break;
		}
		/**
		 * Get the position of the first entry of the legend box
		 */
		int xPos = p.x;
		int yPos = p.y + maxH;
		/**
		 * Draw the names of the profiles in the corresponding color to the
		 * legend box
		 */
		for (ColoredAnnotatedCurve cpd : curves) {
			/**
			 * Retrieve color and text from the profile
			 */
			g.setColor(cpd.getColor());
			String name = cpd.getCurve().getName();
			/**
			 * Check if the profile name is in the box boundaries
			 */
			if (name.length() >= maxStrLen) {
				name = name.substring(0, maxStrLen - 3);
				name += "...";
			}
			/**
			 * Draw the text and increase the offset of the next profile name.
			 */
			g.drawString(name, xPos, yPos);
			yPos += maxH;
		}
		/**
		 * Draw the border of the legend box
		 */
		g.setColor(Color.BLACK);
		g.drawRect(p.x - boxmargin, 
				p.y - boxmargin, 
				maxW + boxmargin * 2, 
				maxH * curves.size() + boxmargin * 3);
	}

	/**
	 * Function to display an information text into the plot to give the user
	 * feedback about the current split insertion step.
	 * 
	 * @param g
	 *            Reference to the graphics object for painting
	 */
	private void paintSplitInfoMsg(Graphics2D g) {
		/**
		 * Determine the start position in the plot
		 */
		Point p = transDataToPlot(plotAreaX.getMin(), plotAreaY.getMax());
		/**
		 * Update the font properties
		 */
		g.setFont(new Font(g.getFont().getFamily(), Font.BOLD, g.getFont()
				.getSize()));
		/**
		 * Determine the font height
		 */
		int fontHeight = g.getFontMetrics().getHeight();
		/**
		 * Set the color of the information text and display in the plot
		 */
		g.setColor(Color.RED);
		g.drawString("Select point to be aligned for profile "
				+ curves.getFirst().getCurve().getName() + ".",
					p.x, (int) (fontHeight * 1.5));
	}

	/**
	 * Function which displays the zoom factor directly to the upper right
	 * corner of the plot
	 * 
	 * @param g
	 *            Graphics reference
	 */
	private void paintZoomInfoMsg(Graphics2D g) {
		
		// Message for the zoom
		String msg = "Zoom = " + zoomRange + "%";
		/**
		 * Determine message length
		 */
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(msg);
		/**
		 * Determine the position for the text in the upper right corner
		 */
		Point2D p = transDataToPlot(plotAreaX.getMax(), plotAreaY.getMax());
		g.setFont(new Font(g.getFont().getFamily(), Font.BOLD, g.getFont()
				.getSize()));
		int fontHeight = g.getFontMetrics().getHeight();
		/**
		 * Set the color of the information text and display in the plot with
		 * text length shift to the left
		 */
		g.setColor(Color.BLUE);
		g.drawString(msg, (int) p.getX() - w, (int) (fontHeight * 1.5));
	}

	/**
	 * Function to paint the axis according to the panel size. At the end of
	 * each axis an arrow indicates the axis direction.
	 * 
	 * @param g2d
	 *            Reference for plotting to the panel.
	 */
	private void paintAxis(Graphics2D g2d) {

		// Get the dimensions of the drawing panel
		Dimension r = this.getSize();

		// Defines the overlap of the axis in the plot
		final int axisOverlap = 10;

		/**
		 * Clear the background of the panel and repaint with the background
		 * color.
		 */
		g2d.setBackground(backgroundColor);
		g2d.clearRect(0, 0, r.width, r.height);
		/**
		 * Set the color of the axis lines
		 */
		g2d.setColor(Color.BLACK);
		
		// Vertical axis
		g2d.drawLine(margin, r.height - margin + axisOverlap,
				margin, margin);
		// Vertical axis arrow head
		g2d.drawLine(margin, margin, margin + axisOverlap / 2, margin
				+ axisOverlap);
		g2d.drawLine(margin, margin, margin - axisOverlap / 2, margin
				+ axisOverlap);
		// Vertical axis label
		g2d.drawString("Y", margin - 10, margin - 5);
		
		// Horizontal axis
		g2d.drawLine(margin - axisOverlap, r.height - margin,
				r.width - margin, r.height - margin);
		// Horizontal axis arrow head
		g2d.drawLine(r.width - margin, r.height - margin,
				r.width - margin - axisOverlap, r.height
						- margin - axisOverlap / 2);
		g2d.drawLine(r.width - margin, r.height - margin,
				r.width - margin - axisOverlap, r.height
						- margin + axisOverlap / 2);
		// Horizontal axis label
		g2d.drawString("X", r.width - margin + 5,
				r.height - margin + 15);
	}

	/**
	 * This function has to be called before plotting any profile. Because this
	 * function determines the maximal values from the plot set to initialize
	 * the size of the plot area. Additional to that this function prints the
	 * dashed on the x and y axis.
	 * 
	 * @param g
	 *            The graphics2d reference for printing into the plot area
	 */
	private void paintAxisDashes(Graphics2D g) {
		/**
		 * Parameter for setting the length of the dashes on the axis
		 */
		final int dashlen = 3;
		/**
		 * Defines the empty space between the axis and the scaling number on
		 * the axis dash. This parameter moves the number on the x axis but also
		 * the numbers on the y axis from or to the axis.
		 */
		final int numberoffset = 20;
		/**
		 * Parameter to shift the numbers left or right in parallel to the axis.
		 */
		final int numbershift = 5;

		/**
		 * Set the color for the axis to black and set the thickness to thin
		 */
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(1));

		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#0.00", dfs);
		
		int dashStep = 50; // in pixel
		
		// Draw the horizontal axis dashes according to the maximal plot area boundaries
		int startPos = transDataToPlot(plotAreaX.getMin(), plotAreaY.getMin()).x;
		int endPos = transDataToPlot(plotAreaX.getMax(), plotAreaY.getMin()).x;
		int xAxisHeight = this.getSize().height - margin;
		for (int i = startPos; i < endPos; i = i + dashStep) {
			// Draw the dash
			g.drawLine(	i, (xAxisHeight + dashlen),
						i, (xAxisHeight - dashlen));
			// exclude label for the pre-last dash
			if (i <= endPos-dashStep) {
				// draw the textual number
				g.drawString("" + df.format( transPlotToData((double)i,0).getX() ), 
							i - numbershift, 
							(xAxisHeight + dashlen + numberoffset)
						);
			}
		}
		// Draw the final dash
		g.drawLine(	endPos, (xAxisHeight + dashlen),
				endPos, (xAxisHeight - dashlen));
		// draw the final textual number
		g.drawString("" + df.format( plotAreaX.getMax() ), 
				endPos - numbershift, 
				(xAxisHeight + dashlen + numberoffset)
				);
		

		// Draw the vertical axis dashes according to the maximal plot area boundaries
		
		int height = this.getHeight()-margin-margin;
		int y0 = this.getHeight()-margin;
		
		for (int i = 0; i*dashStep < height; i++) {
			// Draw the dash
			g.drawLine((int) (margin + dashlen), (int) (y0 - i*dashStep),
					(int) (margin - dashlen), (int) (y0 - i*dashStep));
			// draw the textual number
			g.drawString("" + df.format(((double)(i * dashStep)/(double)(height)*plotAreaY.getRange()) + plotAreaY.getMin()),
						(int) (margin + dashlen) - numberoffset * 2,
						(int) (y0 - i*dashStep) + numbershift
					);
			
		}
	}

	/**
	 * Function to paint all available profiles in the profile set.
	 * 
	 * @param g2d
	 *            Reference for plotting to the plot area
	 */
	private void paintCurves(Graphics2D g2d) {
		// Over all available curves
		for (int i = 0; i < curves.size(); i++) {
			/* Ensure that the selected curve wont be painted in this loop
			 * (will be painted after that to be foreground)
			 */
			if (i != selectCurveIndex) {
				paintCurve(g2d, i);
			}
		}
		// Paint the selected curve at the end
		if (selectCurveIndex != -1) {
			paintCurve(g2d, selectCurveIndex);
		}
	}
	

	/**
	 * Function to paint a complete profile to the plot area
	 * 
	 * @param g
	 *            The reference for painting on the plot area
	 * @param profileIndex
	 *            The index of the profile for plotting
	 */
	private void paintCurve(Graphics2D g, int profileIndex) {
		/**
		 * Retrieve the color profile data object from the index.
		 */
		ColoredAnnotatedCurve cpd = curves.get(profileIndex);
		/**
		 * Retrieve the profile data for data point and reference point access
		 */
		AnnotatedCurve curve = cpd.getCurve();
		
		// get first coordinate index within range
		int startIndex = Arrays.binarySearch( curve.getX(), plotAreaX.getMin());
		if (startIndex < 0) {
			// take next higher index as start index (will be >= 0)
			startIndex = -1*(startIndex+1);
		}
		
		// get last coordinate index within range
		int endIndex = Arrays.binarySearch( curve.getX(), plotAreaX.getMax());
		if (endIndex < 0) {
			endIndex = -1*(endIndex+2);
		}
		// ensure we stay within curve data
		endIndex = FastMath.min(curve.size()-1, endIndex);
		
		// Check if the current profile is selected and set the selection flag
		boolean select = (profileIndex == selectCurveIndex);
		
		List<CurveAnnotation> ref = curve.getFilteredAnnotations();
		
		// Over all data points from the profile within the zoom interval
		for (int i = startIndex; i <= endIndex; i++) {

			if (curve.getX()[i] > plotAreaX.getMax()) {
				continue;
			}
			
			// draw initial segment up to first coordinate within plot-x-range
			if (i==startIndex && i>0 && curve.getX()[i] > plotAreaX.getMin()) {
				// set profile color
				g.setColor(cpd.getColor());
				// draw line
				paintLine(g, 
						plotAreaX.getMin(), curve.getY(plotAreaX.getMin()), 
						curve.getX()[i], curve.getY()[i], 
						select);
			}

			if (i < endIndex) {
				
				if (curve.getX()[i+1] <= plotAreaX.getMax()) {
					// profile color
					g.setColor(cpd.getColor());
					// draw line
					paintLine(g, 
							curve.getX()[i], curve.getY()[i], 
							curve.getX()[i+1], curve.getY()[i+1], 
							select);
				}
			} else {
				// if not end of curve:
				// draw final segment up to last coordinate within plot-x-range
				if (endIndex+1 < curve.size()) {
					// profile color
					g.setColor(cpd.getColor());
					// draw line
					paintLine(g, 
							curve.getX()[i], curve.getY()[i], 
							plotAreaX.getMax(), curve.getY(plotAreaX.getMax()), 
							select);
				}
			}
			// Check if the current data point is a reference point
			CurveAnnotation.Type pt = curve.getAnnotation()[i].isIntervalBoundary() ? curve.getAnnotation()[i] : Type.IS_POINT;
			if (showAnnotations) {
				for (CurveAnnotation r : ref) {
					if (r.getIndex() == i) {
						pt = r.getType();
						break;
					}
				}
			}
			// Search for a split point in the profile
			LinkedList<Integer> split = cpd.getSplitPoints();
			for (Integer splitIndex : split) {
				/**
				 * If the profile contains a split point set the point type to
				 * null
				 */
				if (i == splitIndex) {
					pt = null;
					break;
				}
			}

			/**
			 * Draw a point only if the profile is selected or the point is a
			 * reference point
			 */
			if (select || pt != Type.IS_POINT) {
				final boolean refPointIsCustom = false;
				/**
				 * If reference point is custom than invert the reference point
				 * color (background / foreground)
				 */
				paintPoint(g, curve.getX()[i], curve.getY()[i], pt,
						mouseHoverCurveIndex == i,
						mouseSelectionClickX == i,
						select, !refPointIsCustom);
			}

		}
	}

	/**
	 * Function to paint a data point or a reference point to the plot area.
	 * This function translates the data point to the plot point. Also this
	 * function takes care about the point coloring and the reference point
	 * shapes.
	 * 
	 * @param g
	 *            Reference for drawing on the plot area.
	 * @param x
	 *            Data point x value.
	 * @param y
	 *            Data point y value.
	 * @param t
	 *            Reference point type. If this is set to null an ordinary data
	 *            point will be printed.
	 * @param currentlyHovered 
	 * 			  whether or not the mouse currently hoveres over this point
	 * @param currentlySelected 
	 * 			  whether or not this point is currently selected
	 * @param profileInForeground
	 *            Indicates the selection of the overlaying profile. Only if the
	 *            profile is selected the point hover selection or click
	 *            selection will be displayed in orange or red. The selection
	 *            means the profile is in foreground.
	 * @param invertRefPointColor
	 *            If this is set to true the reference point will be painted in
	 *            inversive color. This means that the border is painted in the
	 *            inner color and the inner color is painted in the border color
	 */
	private void paintPoint(Graphics2D g, 
			double x, double y, 
			CurveAnnotation.Type t,
			boolean currentlyHovered,
			boolean currentlySelected,
			boolean profileInForeground, 
			boolean invertRefPointColor
			) 
	{
		/**
		 * Translate the point to the plot area
		 */
		Point2D dp = transDataToPlot(x, y);

		/**
		 * If the profile is in foreground plot the
		 */
		if (currentlyHovered && profileInForeground) {
			/**
			 * Paint the selected point orange
			 */
			g.setColor(Color.ORANGE);
		} else {
			/**
			 * If no selection was made set the color to default gray.
			 */
			g.setColor(Color.GRAY);
		}

		/**
		 * Check if a point was clicked by the user and indicate this red. If
		 * the user clicks on a data point this overwrites all available colors
		 * and the point is always red.
		 */
		if (currentlySelected && profileInForeground) {
			g.setColor(Color.RED);
		}

		/**
		 * Set the radius according to foreground and background.
		 */
		int radius = 0;
		if (profileInForeground) {
			radius = foregrRadius;
		} else {
			radius = backgrRadius;
		}
		
		// check if split point
		if (t == null || t == Type.IS_SPLIT) {
			/**
			 * No point type means split point
			 */
			Color fill = g.getColor();
			if (g.getColor() == Color.GRAY)
				fill = Color.CYAN;
			/**
			 * Differentiate between big brackets and thin line (foreground <->
			 * background) according to the select attribute
			 */
			paintSplitPointLine(g, dp, (radius + refpointRadiusIncrease) * 2,
					fill, profileInForeground);
		} else 
			if (t == Type.IS_POINT) {
			/**
			 * Ordinary point type, paint the circle. This point will be painted
			 * smaller than the reference points
			 */
			paintDataPointCircle(g, dp, radius - 1, g.getColor(), Color.WHITE);
			
		} else {
			// get fill color and min/max annotation
			Color fill = g.getColor();
			boolean isMaximum = false;
			switch(t) {
			case IS_START :
				isMaximum = true;
			case IS_END :
				fill = Color.cyan; 
				break;
			case IS_MAXIMUM_MAN :
			case IS_MAXIMUM_AUTO : 
				isMaximum = true;
			case IS_MINIMUM_MAN :
			case IS_MINIMUM_AUTO : 
				fill = Color.blue; 
				break;
			case IS_INFLECTION_ASCENDING_MAN :
			case IS_INFLECTION_ASCENDING_AUTO : 
				isMaximum = true;
			case IS_INFLECTION_DESCENDING_MAN :
			case IS_INFLECTION_DESCENDING_AUTO : 
				fill = Color.green; 
				break;
			default : break;
			}
			if (g.getColor() != Color.GRAY) {
				fill = g.getColor();
			}
			Color border = Color.WHITE;
			if (invertRefPointColor) {
				border = fill;
				fill = Color.WHITE;
			}
			// draw min/max triangle
			if (isMaximum) {
				paintDataPointTriangleUp(g, dp, radius + refpointRadiusIncrease,
						fill, border);
			} else {
				paintDataPointTriangleDown(g, dp, radius + refpointRadiusIncrease,
						fill, border);
			}
		}
	}

	/**
	 * Function to paint a triangle with top up to the plot area.
	 * 
	 * @param g
	 *            The reference for painting
	 * @param dp
	 *            The plot area point.
	 */
	private void paintDataPointTriangleDown(Graphics2D g, Point2D dp,
			int radius, Color innerColor, Color borderColor) {
		/**
		 * Set the inner color
		 */
		g.setColor(innerColor);
		/**
		 * Draw the shape
		 */
		Polygon poly = new Polygon();
		poly.addPoint((int) dp.getX() - radius, (int) dp.getY() + radius
				- radius * 2);
		poly.addPoint((int) dp.getX() - radius + radius, (int) dp.getY()
				+ radius);
		poly.addPoint((int) dp.getX() - radius + radius * 2, (int) dp.getY()
				+ radius - radius * 2);
		g.fillPolygon(poly);
		/**
		 * Draw a thin border around the polygon
		 */
		g.setStroke(new BasicStroke(1));
		g.setColor(borderColor);
		g.drawPolygon(poly);
	}

	/**
	 * Function to paint a triangle with top down to the plot area.
	 * 
	 * @param g
	 *            The reference for painting
	 * @param dp
	 *            The plot area point.
	 */
	private void paintDataPointTriangleUp(Graphics2D g, Point2D dp, int radius,
			Color innerColor, Color borderColor) {
		/**
		 * Set the inner color
		 */
		g.setColor(innerColor);
		/**
		 * Draw the shape
		 */
		Polygon poly = new Polygon();
		poly.addPoint((int) dp.getX() - radius, (int) dp.getY() - radius
				+ radius * 2);
		poly.addPoint((int) dp.getX() - radius + radius, (int) dp.getY()
				- radius);
		poly.addPoint((int) dp.getX() - radius + radius * 2, (int) dp.getY()
				- radius + radius * 2);
		g.fillPolygon(poly);
		/**
		 * Draw a thin border around the polygon
		 */
		g.setStroke(new BasicStroke(1));
		g.setColor(borderColor);
		g.drawPolygon(poly);
	}

	/**
	 * Function to paint a circle to the plot area.
	 * 
	 * @param g
	 *            The reference for painting
	 * @param dp
	 *            The plot area point.
	 */
	private void paintDataPointCircle(Graphics2D g, Point2D dp, int radius,
			Color innerColor, Color borderColor) {
		/**
		 * Set the inner color
		 */
		g.setColor(innerColor);
		/**
		 * Draw the shape
		 */
		g.fillOval((int) dp.getX() - radius, (int) dp.getY() - radius,
				radius * 2, radius * 2);
		/**
		 * Draw a thin border around the polygon
		 */
		g.setStroke(new BasicStroke(1));
		g.setColor(borderColor);
		g.drawOval((int) dp.getX() - radius, (int) dp.getY() - radius,
				radius * 2, radius * 2);
	}

	/**
	 * Function to paint a split bracket lines to the plot area.
	 * 
	 * @param g
	 *            Reference for painting
	 * @param dp
	 *            The plot area point
	 * @param radius
	 *            The radius of the point
	 * @param large
	 *            Enables painting a big bracket split symbol or a small line
	 *            for split point indication.
	 */
	private void paintSplitPointLine(Graphics2D g, Point2D dp, int radius,
			Color color, boolean large) {
		/**
		 * Defines the gap between the two split lines
		 */
		final int gap = 2;
		/**
		 * Defines the length of the bracket ends
		 */
		final int bracket = 6;
		/**
		 * Set the stroke to thin with the desired color
		 */
		g.setStroke(new BasicStroke(2));
		g.setColor(color);
		/**
		 * Differentiate between two shapes (brackets and thin line)
		 */
		if (large) {
			/**
			 * Draw the left of the split brackets
			 */
			g.drawLine((int) dp.getX() - gap - bracket, (int) dp.getY()
					+ radius + gap, (int) dp.getX() - gap, (int) dp.getY()
					+ radius);
			g.drawLine((int) dp.getX() - gap - bracket, (int) dp.getY()
					- radius - gap, (int) dp.getX() - gap, (int) dp.getY()
					- radius);
			g.drawLine((int) dp.getX() - gap, (int) dp.getY() + radius,
					(int) dp.getX() - gap, (int) dp.getY() - radius);
			/**
			 * Draw the right of the split brackets
			 */
			g.drawLine((int) dp.getX() + gap + bracket, (int) dp.getY()
					+ radius + gap, (int) dp.getX() + gap, (int) dp.getY()
					+ radius);
			g.drawLine((int) dp.getX() + gap + bracket, (int) dp.getY()
					- radius - gap, (int) dp.getX() + gap, (int) dp.getY()
					- radius);
			g.drawLine((int) dp.getX() + gap, (int) dp.getY() + radius,
					(int) dp.getX() + gap, (int) dp.getY() - radius);
		} else {
			/**
			 * Line case
			 */
			g.drawLine((int) dp.getX(), (int) dp.getY() + radius * 2 / 3,
					(int) dp.getX(), (int) dp.getY() - radius * 2 / 3);
		}
	}

	/**
	 * Function to draw a line between two data points in the plot area. This
	 * function translates the data points automatically to the plot area point
	 * values.
	 * 
	 * @param g
	 *            Reference for plotting the the line
	 * @param x1
	 *            Start point for drawing the line
	 * @param y1
	 *            Start point for drawing the line
	 * @param x2
	 *            End point for drawing the line
	 * @param y2
	 *            End point for drawing the line
	 * @param selected
	 *            Flag which indices the selection of a profile. If the profile
	 *            is selected the line is painted darker and thicker.
	 */
	private void paintLine(Graphics2D g, double x1, double y1, double x2,
			double y2, boolean selected) {
		/**
		 * Translate the point to the plot area
		 */
		Point2D p1 = transDataToPlot(x1, y1);
		Point2D p2 = transDataToPlot(x2, y2);
		/**
		 * Set the line settings
		 */
		if (selected) {
			g.setStroke(new BasicStroke(5));
		} else {
			g.setStroke(new BasicStroke(2));
		}
		/**
		 * Draw the line between the two points
		 */
		g.drawLine((int) p1.getX(), (int) p1.getY(), (int) p2.getX(),
				(int) p2.getY());

	}

	@Override
	public void paintComponent(Graphics g) {
		/**
		 * Cast the graphics object reference to a graphics 2d reference.
		 */
		Graphics2D g2d = (Graphics2D) g;
		/**
		 * Print the axis according to the window/panel dimensions
		 */
		paintAxis(g2d);
		/**
		 * Check if there is any data in the profile set for plotting. If the
		 * set is not empty scale the axes according to the profile set
		 * boundaries. Also plot all available profiles.
		 */
		if (!curves.isEmpty()) {
			paintAxisDashes(g2d);
			paintPlotLegend(g2d);
			paintCurves(g2d);
			/**
			 * Display user information if plot is in split mode
			 */
			if (splitInsertMode)
				paintSplitInfoMsg(g2d);
			/**
			 * If zoom active, display the message
			 */
			if (zoomRange != zoomRangeBound.getMax())
				paintZoomInfoMsg(g2d);
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// Get the curve x-position of the mouse position
		Point2D dpoint = transPlotToData( e.getX(), e.getY() );
		// Point mouse hover indication
		if (!curves.isEmpty()) {
			// check if outside of plot area
			if ( selectCurveIndex < 0
				|| curves.isEmpty()
				|| dpoint.getX() < plotAreaX.getMin()
				|| dpoint.getX() > plotAreaX.getMax() ) 
			{
				// outside -> disable highlight
				mouseHoverCurveIndex = -1;
			} else {
				// get next-best point within the curve for highlighting
				mouseHoverCurveIndex = curves.get(selectCurveIndex).getCurve().getClosestPoint( dpoint.getX() );
			}
			repaint();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		/**
		 * Slide up the plot
		 */
		slideUpThePlot();
		
		/**
		 * Now take care about the mouse event
		 */
		if (SwingUtilities.isLeftMouseButton(e)) {
			
			// stop if no curve selected
			if (selectCurveIndex < 0) {
				return;
			}
			
			Point2D dpoint = transPlotToData( e.getX(), e.getY() );
			if (splitInsertMode == false) {
				// Set the indicator for mouse click
				if (!curves.isEmpty()) {
					// get selected point
					mouseSelectionClickX = curves.get(selectCurveIndex).getCurve().getClosestPoint( dpoint.getX() );
					repaint();
					// inform listener on selection
					for (ISelectedPlotPointInfoListener l : pointSelectionListener) {
						l.selectedPlotPointChanged(curves.get(selectCurveIndex).getCurve().getX()[mouseSelectionClickX], 
								curves.get(selectCurveIndex).getCurve().getY()[mouseSelectionClickX], 
								curves.get(selectCurveIndex).getCurve().getAnnotation()[mouseSelectionClickX]);
					}
				}
				
			} else /* if(splitInsertMode == true) */
			{
				if (curves.isEmpty()) {
					throw new RuntimeException("curves empty while in split point insertion mode");
				}
				
				ColoredAnnotatedCurve p = curves.get(selectCurveIndex);
				// get selected point
				int splitIndex = p.getCurve().getClosestPoint( dpoint.getX() );
				
				// Check if the split point is WITHIN an interval and not already selected as a split point
				boolean splitOK = ! p.getCurve().getAnnotation()[splitIndex].isIntervalBoundary()
						&& ! p.getSplitPoints().contains(splitIndex);
				
				/**
				 * Insert and notify only if this is a new split point
				 */
				if (splitOK == true) {
					// Add the split point
					p.addSplitPoint(splitIndex);
					// Disable split mode and notify the listeners
					splitInsertMode = false;
					// Inform the split point listener
					for ( ISplitInsertionListener l : splitListener) {
						l.splitCompleted();
					}
				} else {
					String message = "The split point at position "
							+ p.getCurve().getX()[splitIndex]
							+ " can not be placed, since it is already an interval boundary."
							+ "\nPlease select another split point."
							;
					String title = "Invalid split point selection";
					JOptionPane.setDefaultLocale(Locale.ENGLISH);
					JOptionPane.showMessageDialog(null, message, title,
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} else if (SwingUtilities.isMiddleMouseButton(e)) {
			
			// Middle mouse button disables zoom and resets the zoom radius
			if (zoomRange != zoomRangeBound.getMax()) {
				resetZoom();
				repaint();
			}
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		
		// check if CTRL is pressed = ZOOM CHANGE
		if (e.getModifiersEx() == MouseWheelEvent.CTRL_DOWN_MASK) {
			
			// Check the mouse wheel direction for zoom radius
			int zoomChange = (e.getWheelRotation() < 0) ? -1 : +1 ;
			
			// update zoom information if needed
			if ( setZoomRange(zoomRange + zoomChange) ) {
				
				// update zoom anchor point
				zoomAnchorX = plotAreaX.getMin()+plotAreaX.getRange()/2d;
				
				// update plot area boundaries
				updatePlotArea();
				repaint();
			}
		} else {
			
			// Check the mouse wheel direction for zoom radius
			int scrollChangeInPixel = 5;
			double scrollChange = (transPlotToData( scrollChangeInPixel ,0 ).getX() - plotAreaX.getMin() ) * ((e.getWheelRotation() < 0) ? -1d : +1d );
			double newValue = FastMath.max( curvesX.getMin()+ (plotAreaX.getRange()/2d), FastMath.min( plotAreaX.getMin() + (plotAreaX.getRange()/2d) + scrollChange, curvesX.getMax()- (plotAreaX.getRange()/2d)) );
			// update plot if zoomAnchor was changed
			if (newValue != zoomAnchorX) {
				zoomAnchorX = newValue;
				updatePlotArea();
				repaint();
			}
			
		}

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		/**
		 * Allow only left mouse button for dragging the zoom window
		 */
		if (SwingUtilities.isLeftMouseButton(e)) {
			/**
			 * Determine the first position for drag movement
			 */
			if (Double.isNaN(mouseDragX)) {
				mouseDragX = transPlotToData( e.getX(), e.getY() ).getX();
			} else {
				/**
				 * Determine the second position for drag movement
				 */
				double mouseDragPosNew = transPlotToData( e.getX(), e.getY() ).getX();
				/**
				 * Calculate the movement direction
				 */
				double diff = mouseDragX - mouseDragPosNew;
				/**
				 * If no direction reset the second point measurement for
				 * another try
				 */
				if (diff != 0) {
					/**
					 * Movement and direction available, check if the plot is in
					 * zoom mode
					 */
					if (zoomRange != zoomRangeBound.getMax()) {
						
						double newValue = FastMath.max( curvesX.getMin()+ (plotAreaX.getRange()/2d), FastMath.min( plotAreaX.getMin() + (plotAreaX.getRange()/2d) + diff, curvesX.getMax()- (plotAreaX.getRange()/2d)) );
						// update plot if zoomAnchor was changed
						if (newValue != plotAreaX.getMin() + (plotAreaX.getRange()/2d)) {
							zoomAnchorX = newValue;
							updatePlotArea();
							repaint();
						}
						/**
						 * Replace last mouse point
						 */
						mouseDragX = mouseDragPosNew;
					}
				}
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Reset the mouse drag positions after the mouse button is released
		if (SwingUtilities.isLeftMouseButton(e)) {
			mouseDragX = Double.NaN;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Legend position enumeration. This enumeration is used to define the
	 * position of the legend box in the plot area
	 */
	public enum LegendPos {
		/**
		 * LEFT is the upper left position in the plot, CENTER is the upper
		 * middle position on the plot, RIGHT is the upper right position in the
		 * plot. HIDDEN means that the box will not be visible in the plot
		 */
		LEFT, CENTER, RIGHT, HIDDEN
	}

	@Override
	public void update(Observable o, Object arg) {
		// repaint if filter value changed
		if (o instanceof CurveAnnotationFilter) {
			this.repaint();
		}
	}
	
	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		this.repaint();
	}

}
