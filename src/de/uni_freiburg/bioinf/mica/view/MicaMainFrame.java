package de.uni_freiburg.bioinf.mica.view;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.uni_freiburg.bioinf.mica.algorithm.AnnotatedCurve;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotation;
import de.uni_freiburg.bioinf.mica.algorithm.CurveAnnotationFilter;
import de.uni_freiburg.bioinf.mica.algorithm.IntervalDecomposition;
import de.uni_freiburg.bioinf.mica.algorithm.MICA;
import de.uni_freiburg.bioinf.mica.algorithm.MicaRunner;
import de.uni_freiburg.bioinf.mica.controller.Debug;
import de.uni_freiburg.bioinf.mica.controller.GuiController;
import de.uni_freiburg.bioinf.mica.controller.MicaController;
import de.uni_freiburg.bioinf.mica.model.Model;
import de.uni_freiburg.bioinf.mica.view.ColoredAnnotatedCurvePlot.LegendPos;

/**
 * Main view of the MICA algorithm. This view represents the initial view. Here
 * the user can import curves which are plotted. Also the user has the
 * possibility to setup annotation filters. 
 * 
 * @author mbeck
 * 
 */
public class MicaMainFrame extends JFrame implements ActionListener,
		MouseListener, KeyListener, ISelectedPlotPointInfoListener,
		ISplitInsertionListener, PopupMenuListener,
		Observer 
{
	private static final long serialVersionUID = 1L;

	/**
	 * Model objects for the list components on the view.
	 */
	private DefaultListModel<String> listProfileModel = null;
	/**
	 * Reference to the controller object for communication.
	 */
	private GuiController controller = null;
	/**
	 * Main menu bar
	 */
	private JMenuBar menubar = null;
	private JMenu menuQuestionSymbol = null;
	private JMenu menuFile = null;
	private JMenu menuInput = null;
	private JMenu menuPlot = null;
	private JMenuItem menuitemAddSplit = null;
	private JMenuItem menuitemRemoveSplit = null;
	private JMenuItem menuitemLegBoxLeft = null;
	private JMenuItem menuitemLegBoxCenter = null;
	private JMenuItem menuitemLegBoxRight = null;
	private JMenuItem menuitemLegBoxHidden = null;
	private JMenuItem menuitemBackColorPlot = null;
	private JMenuItem menuitemAbout = null;
	private JMenuItem menuitemImportProfile = null;
	private JMenuItem menuitemExportCsv = null;
	private JMenuItem menuitemExportPng = null;
	private JMenuItem menuitemExit = null;
	
	private MicaProgressDialog micaProgressDialog = new MicaProgressDialog(this);
	
	
	/**
	 * View components for the curve representation.
	 */
	private JList<String> listProfile = null;
	private JButton buttonImportProfile = null;
	private JButton buttonRemoveProfile = null;
	/**
	 * View components for the curve information representation.
	 */
	private JLabel labelPInfName = null;
	private JTextField textfieldPInfName = null;
	
	private JLabel labelPInfColor = null;
	private JButton buttonPInfColor = null;
	// number of data points
	private JLabel labelPInfNumDP = null;
	private JTextField textfieldPInfNumDP = null;
	// number of reference points / curve annotations after filtering
	private JLabel labelPInfNumRP = null;
	private JTextField textfieldPInfNumRP = null;
	// number of split points
	private JLabel labelPInfNumSP = null;
	private JTextField textfieldPInfNumSP = null;
	/**
	 * View components for the program start or exit mechanism.
	 */
	private JButton buttonStartMICA = null;

	/**
	 * Combo box for selecting the foreground curve in the plot
	 */
	private JComboBox<String> comboboxPlotset = null;
	/**
	 * Text field contains information about the selected point
	 */
	private JLabel plotPointInfoLabel = null;
	/**
	 * Attribute which indicates the current curve index the user has to
	 * insert a split point. If the value is -1 the split point insertion is not
	 * active.
	 */
	private int profileIndexForSplit = -1;
	/**
	 * Displays the rmsd of the consensus
	 */
	private JLabel micaDistanceOutput = null;
	/**
	 * Displays the algorithm duration
	 */
	private JLabel micaRuntimeOutput = null;
	/**
	 * Split pane for dividing the input and output plot
	 */
	private JSplitPane panelSplit = null;
	/**
	 * Attribute for setting the stop of the split panel movement in percent.
	 */
	private final int splitStop = 85;
	/**
	 * Timer for sliding the split pane up and down smoothly
	 */
	private Timer timerSlideUp = null;
	private Timer timerSlideDown = null;
	/**
	 * View component for displaying the plot of the curve on the view.
	 */
	private ColoredAnnotatedCurvePlot profilePlotIn = null;
	private ColoredAnnotatedCurvePlot profilePlotOut = null;
	/**
	 * Radio buttons for selecting the distance strategy
	 */
	private JRadioButton radioSlopeDistance = null;
	private JRadioButton radioDataDistance = null;
	/**
	 * Radio buttons for selecting the MICA algorithm type
	 */
	private JRadioButton radioReferenceMica = null;
	private JRadioButton radioProgressiveMica = null;
	
	/**
	 * shows/hides the consensus curve within the input plot
	 */
	private JCheckBox showConsensusInput = new JCheckBox("Show consensus curve", true);
	
	/**
	 * shows/hides the consensus curve within the output plot
	 */
	private JCheckBox showConsensusOutput = new JCheckBox("Show consensus curve", true);

	/** max warping factor input */
	private DoubleParameterPanel maxWarpingFactorPanel;
	
	/** minimal interval length input */
	private DoubleParameterPanel minIvalDecompPanel;
	
	/** maximal relative shift input */
	private DoubleParameterPanel paramMaxRelXShiftPanel;
	
	/** extrema filter value input */
	private DoubleParameterPanel filterExtremaValuePanel;
	
	/** inflection filter value input */
	private DoubleParameterPanel filterInflectionValuePanel;
	
	/** inflection filter value input */
	private DoubleParameterPanel distanceSamplesPanel;

	/**
	 * Constructor of the main view.
	 * 
	 * @param c
	 *            Is the reference of the controller component which is needed
	 *            for communication.
	 */
	public MicaMainFrame(GuiController c) {
		/**
		 * Set the window title
		 */
		super("MICA - Multiple Interval-based Curve Alignment");
		
		// Set the controller reference
		controller = c;
		
		// list of profiles
		listProfileModel = new DefaultListModel<String>();

		listProfile = new JList<String>();
		// Allow selecting more than one entry in the profile list.
		listProfile.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		listProfile.setModel(listProfileModel);
		
		
		buttonImportProfile = new JButton("Import curves");
		buttonRemoveProfile = new JButton("Delete curves");

		labelPInfName = new JLabel("Name: ");
		textfieldPInfName = new JTextField("");
		textfieldPInfName.setEnabled(false);
		
		labelPInfColor = new JLabel("Color: ");
		buttonPInfColor = new JButton("Change color");
		Font f = buttonPInfColor.getFont();
		Font newf = new Font(f.getName(), Font.BOLD, f.getSize());
		buttonPInfColor.setFont(newf);
		buttonPInfColor.setBackground(Color.LIGHT_GRAY);
		buttonPInfColor.setForeground(Color.LIGHT_GRAY);
		
		labelPInfNumDP = new JLabel("Num data points: ");
		textfieldPInfNumDP = new JTextField("");
		textfieldPInfNumDP.setEnabled(false);
		
		labelPInfNumRP = new JLabel("Num ref points: ");
		textfieldPInfNumRP = new JTextField("");
		textfieldPInfNumRP.setEnabled(false);
		
		labelPInfNumSP = new JLabel("Num split points: ");
		textfieldPInfNumSP = new JTextField("");
		
		// setup parameter input fields
		maxWarpingFactorPanel = new DoubleParameterPanel( controller.getParamMaxWarpFactor() );
		minIvalDecompPanel = new DoubleParameterPanel( controller.getParamMinIvalDecomp() );
		paramMaxRelXShiftPanel = new DoubleParameterPanel( controller.getParamMaxRelXShift() );
		
		filterExtremaValuePanel = new DoubleParameterPanel( controller.getParamFilterExtremaValue() );
		filterInflectionValuePanel = new DoubleParameterPanel( controller.getParamFilterInflectionValue() );
		
		distanceSamplesPanel = new DoubleParameterPanel( controller.getParamDistanceSamples() );

		buttonStartMICA = new JButton("Start MICA");

		micaDistanceOutput = new JLabel(" Distance: ");
		micaDistanceOutput.setBorder( BorderFactory.createEmptyBorder(0,10,0,10));
		micaDistanceOutput.setPreferredSize(new Dimension(150,micaDistanceOutput.getMinimumSize().height));
		micaRuntimeOutput = new JLabel(" Computation time: ");
		micaRuntimeOutput.setBorder( BorderFactory.createEmptyBorder(0,10,0,10));
		micaRuntimeOutput.setPreferredSize(new Dimension(200,micaDistanceOutput.getMinimumSize().height));
		
		showConsensusInput.setBorder( BorderFactory.createEmptyBorder(0,10,0,10));
		showConsensusOutput.setBorder( BorderFactory.createEmptyBorder(0,10,0,10));

		/**
		 * Create timer instance and add action listener
		 */
		timerSlideUp = new Timer(20, this);
		timerSlideDown = new Timer(20, this);

		profilePlotIn = new ColoredAnnotatedCurvePlot( true );
		controller.registerFilterChangeListener( profilePlotIn );
		controller.getParamFilterExtremaValue().addListener( profilePlotIn );
		controller.getParamFilterInflectionValue().addListener( profilePlotIn );
		profilePlotOut = new ColoredAnnotatedCurvePlot( false );
		controller.registerFilterChangeListener( profilePlotOut );
		/**
		 * Register slide timers
		 */
		profilePlotIn.registerSlideTimer(timerSlideUp, timerSlideDown);
		profilePlotOut.registerSlideTimer(timerSlideDown, timerSlideUp);

		comboboxPlotset = new JComboBox<String>();
		plotPointInfoLabel = new JLabel(" ");
		plotPointInfoLabel.setBorder( BorderFactory.createEmptyBorder(0,10,0,10));
		plotPointInfoLabel.setPreferredSize(new Dimension(300,plotPointInfoLabel.getMinimumSize().height));

		panelSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		/**
		 * Setting up the menu bar with items
		 */
		menubar = new JMenuBar();
		menuQuestionSymbol = new JMenu("?");
		menuInput = new JMenu("Manual point alignment");
		menuPlot = new JMenu("Plot");
		menuFile = new JMenu("File");

		menubar.add(menuFile);
		menubar.add(menuInput);
		menubar.add(menuPlot);
		menubar.add(menuQuestionSymbol);

		menuitemAddSplit = new JMenuItem("Add manual point alignment");
		menuitemRemoveSplit = new JMenuItem("Remove all manual alignments");
		menuInput.add(menuitemAddSplit);
		menuInput.add(menuitemRemoveSplit);

		menuitemLegBoxHidden = new JMenuItem("Legend box hidden");
		menuitemLegBoxLeft = new JMenuItem("Legend box left");
		menuitemLegBoxCenter = new JMenuItem("Legend box center");
		menuitemLegBoxRight = new JMenuItem("Legend box right");
		menuitemLegBoxRight.setEnabled(false); // default position is right atstart up
		menuitemBackColorPlot = new JMenuItem("Background color");
		
		menuPlot.add(menuitemLegBoxLeft);
		menuPlot.add(menuitemLegBoxCenter);
		menuPlot.add(menuitemLegBoxRight);
		menuPlot.add(menuitemLegBoxHidden);
		menuPlot.addSeparator();
		menuPlot.add(menuitemBackColorPlot);

		menuitemAbout = new JMenuItem("About");
		menuQuestionSymbol.add(menuitemAbout);

		menuitemImportProfile = new JMenuItem("Import curve");
		menuitemExportCsv = new JMenuItem("Export CSV");
		menuitemExportPng = new JMenuItem("Export PNG");
		menuitemExit = new JMenuItem("Exit");
		menuFile.add(menuitemImportProfile);
		menuFile.addSeparator();
		menuFile.add(menuitemExportCsv);
		menuFile.add(menuitemExportPng);
		menuFile.addSeparator();
		menuFile.add(menuitemExit);

		addToolTipToMenuItems();

		/**
		 * Instances of the radio buttons
		 */
		ButtonGroup distanceButtons = new ButtonGroup();
		radioSlopeDistance = new JRadioButton("Slope", true);
		radioSlopeDistance.setSelected(controller.isDistanceSlopeBased());
		distanceButtons.add(radioSlopeDistance);
		radioDataDistance = new JRadioButton("Y-value", false);
		radioDataDistance.setSelected(!controller.isDistanceSlopeBased());
		distanceButtons.add(radioDataDistance);
		
		addTollTipToDataBasis();
		
		/**
		 * Instances of the algorithm type radio buttons
		 */
		ButtonGroup alignmentButtons = new ButtonGroup();
		radioProgressiveMica = new JRadioButton("Progressive (standard)", true);
		alignmentButtons.add(radioProgressiveMica);
		radioReferenceMica = new JRadioButton("Reference-based", false);
		alignmentButtons.add(radioReferenceMica);

		addToolTipToAlignmentTypes();

		/**
		 * Set the default editable properties to the view components.
		 */
		buttonPInfColor.setEnabled(false);
		buttonStartMICA.setEnabled(false);
		buttonRemoveProfile.setEnabled(false);
		comboboxPlotset.setEnabled(false);
		textfieldPInfNumSP.setEnabled(false);
		menuitemAddSplit.setEnabled(false);
		menuitemRemoveSplit.setEnabled(false);
		

		/**
		 * Add all needed action listeners and init the form.
		 */
		addListeners();
		initForm();
	}

	/**
	 * Function to add tool tips to the data basis type selectors
	 */
	private void addTollTipToDataBasis() {
		String slope = "<html>"
				+ "The distance is computed based<br>"
				+ "on mapped <b>slope</b> values."
				+ "</html>";
		radioSlopeDistance.setToolTipText(slope);

		String data = "<html>"
				+ "The distance is computed based<br>"
				+ "on mapped <b>y-values</b>."
				+ "</html>";
		radioDataDistance.setToolTipText(data);
	}

	/**
	 * Function to add tool tips to the alignment type radio buttons
	 */
	private void addToolTipToAlignmentTypes() {
		String prog = "<html>"
				+ "Alignment type <b>progressive</b> enables the progressive<br>"
				+ "alignment mode. All imported curves will be alignment<br>"
				+ "according to the iterations steps best distance evaluation<br>"
				+ "between two curves." + "</html>";
		radioProgressiveMica.setToolTipText(prog);

		String ref = "<html>"
				+ "Alignment type <b>reference</b> enables the reference<br>"
				+ "curve based alignment mode. All imported curves will be<br>"
				+ "alignment against the defined reference curve but without<br>"
				+ "any weights at the curves expect the reference curve."
				+ "</html>";
		radioReferenceMica.setToolTipText(ref);
	}

	/**
	 * Function to add tool tip messages to the menu items in the menu bar.
	 */
	private void addToolTipToMenuItems() {

		String splitToolTip = "<html>"
				+ "<b>Manual alignment</b><br>"
				+ "<i>Enables the manual alignment of individual curve points.<br>"
				+ "The manual alignment is to be done for <em>all</em> curves<br>"
				+ "and can be aborted using the <b>ESC</b> key.<br>"
				+ "The automated MICA alignment will only align the remaining<br>"
				+ "curve intervals between manually aligned points.<br>"
				+ "</html>";
		menuInput.setToolTipText(splitToolTip);
		String splitToolTipAdd = "<html>"
				+ "<b>Add manual alignment</b> allows the user to define<br>"
				+ "manually one point for each curve to be aligned.<br>"
				+ "</html>";
		menuitemAddSplit.setToolTipText(splitToolTipAdd);
		String splitToolTipRem = "<html>"
				+ "<b>Remove all manual</b> allows the user to discard all<br>"
				+ "manually defined alignment points from all curves.<br>"
				+ "</html>";
		menuitemRemoveSplit.setToolTipText(splitToolTipRem);

		String plotToolTipLeft = "<html>"
				+ "<b>Position left</b> sets the legend pox to the upper left<br>"
				+ "corner of the curve plot.<br>" + "</html>";
		menuitemLegBoxLeft.setToolTipText(plotToolTipLeft);
		String plotToolTipMiddle = "<html>"
				+ "<b>Position center</b> sets the legend box to the upper<br>"
				+ "middle position of the curve plot.<br>" + "</html>";
		menuitemLegBoxCenter.setToolTipText(plotToolTipMiddle);
		String plotToolTipRight = "<html>"
				+ "<b>Position right</b> sets the legend pox to the upper<br>"
				+ "right corner of the curve plot.<br>" + "</html>";
		menuitemLegBoxRight.setToolTipText(plotToolTipRight);
		String plotToolTipNone = "<html>"
				+ "<b>Position hidden</b> hides the legend box.<br></i>"
				+ "</html>";
		menuitemLegBoxHidden.setToolTipText(plotToolTipNone);

		String fileToolTipImport = "<html>"
				+ "Imports new curves into the data set"
				+ "</html>";
		menuitemImportProfile.setToolTipText(fileToolTipImport);
		String fileToolTipEPic = "<html>"
				+ "Creates an image of the shown curve plots in PNG format"
				+ "</html>";
		menuitemExportPng.setToolTipText(fileToolTipEPic);
		String fileToolTipECsv = "<html>"
				+ "Export the data in CSV format"
				+ "</html>";
		menuitemExportCsv.setToolTipText(fileToolTipECsv);
		String fileToolTipExit = "<html>"
				+ "Terminates the application" + "</html>";
		menuitemExit.setToolTipText(fileToolTipExit);
	}

	/**
	 * Function to add action listeners to the view components.
	 */
	private void addListeners() {
		/**
		 * For the buttons
		 */
		buttonPInfColor.addActionListener(this);
		buttonImportProfile.addActionListener(this);
		buttonRemoveProfile.addActionListener(this);
		buttonStartMICA.addActionListener(this);
		menuitemExportCsv.addActionListener(this);
		menuitemExportPng.addActionListener(this);
		menuitemAddSplit.addActionListener(this);
		menuitemRemoveSplit.addActionListener(this);
		menuitemLegBoxLeft.addActionListener(this);
		menuitemLegBoxCenter.addActionListener(this);
		menuitemLegBoxRight.addActionListener(this);
		menuitemLegBoxHidden.addActionListener(this);
		menuitemImportProfile.addActionListener(this);
		menuitemBackColorPlot.addActionListener(this);
		
		radioDataDistance.addActionListener(this);
		radioSlopeDistance.addActionListener(this);
		
		showConsensusInput.addActionListener(this);
		showConsensusOutput.addActionListener(this);
		
		/**
		 * For the text fields
		 */
		textfieldPInfName.addActionListener(this);
		/**
		 * For the lists
		 */
		listProfile.addMouseListener(this);
		listProfile.addKeyListener(this);
		/**
		 * For the menu items
		 */
		menuitemAbout.addActionListener(this);
		menuitemExit.addActionListener(this);
		/**
		 * For the combo box
		 */
		comboboxPlotset.addActionListener(this);
		/**
		 * For the point info
		 */
		profilePlotIn.registerPointSelectionListener(this);
		profilePlotIn.registerSplitListener(this);
		/**
		 * For the split pane
		 */
		panelSplit.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				if (panelSplit.getDividerLocation() > panelSplit.getHeight()
						* splitStop / 100) {
					panelSplit.setDividerLocation(panelSplit.getHeight()
							* splitStop / 100);
				} else if (panelSplit.getDividerLocation() < panelSplit
						.getHeight() * (100 - splitStop) / 100) {
					panelSplit.setDividerLocation(panelSplit.getHeight()
							* (100 - splitStop) / 100);
				}
			}
		});

		/**
		 * Add component listener to set the split panel divider to half of the
		 * window height if the window resizes
		 */
		this.addComponentListener(new ComponentListener() {

			@Override
			public void componentShown(ComponentEvent e) {
			}

			@Override
			public void componentResized(ComponentEvent e) {
				/**
				 * Set to half of the current window height
				 */
				panelSplit.setDividerLocation(panelSplit.getHeight() / 2);
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});
	}

	/**
	 * Function for initialize the view components.
	 */
	private void initForm() {
		/**
		 * Main frame settings. Disable the default closing operation, because
		 * of the window adapter implementation.
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		/**
		 * Implement the window closing event.
		 */
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				windowClosingDialog();
			}
		});
		this.setLayout(new GridLayout(1, 2));
		this.setMinimumSize(new Dimension(1280, 800));
		this.setExtendedState(Frame.MAXIMIZED_BOTH);
		this.setLocationRelativeTo(null);
		this.setJMenuBar(menubar);
		
		/**
		 * Create the button view.
		 */
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		/////////// curve selection / import
		
		JPanel panelProfileButtons = new JPanel(new GridLayout(1, 2));
		panelProfileButtons.add(buttonImportProfile);
		panelProfileButtons.add(buttonRemoveProfile);
		JPanel panelProfile = new JPanel(new GridBagLayout());
		panelProfile.setPreferredSize(new Dimension(100, 200));
		panelProfile.setBorder(new TitledBorder(new EtchedBorder(),
				"Curve selection"));
		JScrollPane scrollProfileList = new JScrollPane(listProfile);
		scrollProfileList.setPreferredSize(new Dimension(500, 500));
		scrollProfileList
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0.9;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		panelProfile.add(scrollProfileList, gbc);
		gbc.gridy = 1;
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.LAST_LINE_START;
		panelProfile.add(panelProfileButtons, gbc);
		
		/////////// curve info

		JPanel panelProfileInf = new JPanel(new GridLayout(5, 2));
		panelProfileInf.setBorder(new TitledBorder(new EtchedBorder(),
				"Curve info"));
		panelProfileInf.add(labelPInfName);
		panelProfileInf.add(textfieldPInfName);
		panelProfileInf.add(labelPInfColor);
		panelProfileInf.add(buttonPInfColor);
		panelProfileInf.add(labelPInfNumDP);
		panelProfileInf.add(textfieldPInfNumDP);
		panelProfileInf.add(labelPInfNumRP);
		panelProfileInf.add(textfieldPInfNumRP);
		panelProfileInf.add(labelPInfNumSP);
		panelProfileInf.add(textfieldPInfNumSP);
		
		///////  filter buttons
		
		JPanel panelFilter = new JPanel(new GridBagLayout());
		panelFilter.setBorder(new TitledBorder(new EtchedBorder(),
				"Landmark filter"));
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelFilter.add(filterExtremaValuePanel, gbc);
		panelFilter.add(filterInflectionValuePanel, gbc);

		///////  alignment buttons
		
		// distance panel
		JPanel panelDistanceType = new JPanel(new GridLayout(1, 3));
		panelDistanceType.add(new JLabel("Based on:"));
		panelDistanceType.add(radioSlopeDistance);
		panelDistanceType.add(radioDataDistance);
		JPanel panelDistanceSetup = new JPanel(new GridLayout(2, 1));
		panelDistanceSetup.setBorder(new TitledBorder(new EtchedBorder(),
				"Distance function"));
		panelDistanceSetup.add(panelDistanceType);
		panelDistanceSetup.add(distanceSamplesPanel);
		
		// alignment constraints panel
		JPanel panelAlignmentConstraints = new JPanel(new GridLayout(3, 1));
		panelAlignmentConstraints.setBorder(new TitledBorder(new EtchedBorder(),
				"Alignment constraints"));
		panelAlignmentConstraints.add(minIvalDecompPanel, gbc);
		panelAlignmentConstraints.add(maxWarpingFactorPanel, gbc);
		panelAlignmentConstraints.add(paramMaxRelXShiftPanel, gbc);
		
		// alignment type panel
		JPanel panelAlignmentType = new JPanel(new GridLayout(2, 1));
		panelAlignmentType.setBorder(new TitledBorder(new EtchedBorder(),
				"Alignment type"));
		panelAlignmentType.add(radioProgressiveMica);
		panelAlignmentType.add(radioReferenceMica);
		
		JPanel panelProgCtl = new JPanel(new GridBagLayout());
		panelProgCtl.setBorder(new TitledBorder(new EtchedBorder(),
				"Alignment setup"));
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelProgCtl.add(panelDistanceSetup, gbc);
		panelProgCtl.add(panelAlignmentConstraints, gbc);
		panelProgCtl.add(panelAlignmentType, gbc);
		panelProgCtl.add(buttonStartMICA, gbc);

		/**
		 * Add the left panels also with constraints
		 */
		JPanel panelLeft = new JPanel(new GridBagLayout());
		gbc.gridx = GridBagConstraints.REMAINDER;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.weightx = 1;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelLeft.add(panelProfile, gbc);
		panelLeft.add(panelProfileInf, gbc);
		panelLeft.add(panelFilter, gbc);
		panelLeft.add(panelProgCtl, gbc);
		
		
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		panelLeft.add(new JLabel(), gbc);

		JPanel panelRight1Buttons = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		JLabel highlightLabel = new JLabel("Highlighted curve:");
		highlightLabel.setBorder( BorderFactory.createEmptyBorder(0,10,0,10));
		panelRight1Buttons.add(highlightLabel, gbc);
		panelRight1Buttons.add(comboboxPlotset, gbc);
		panelRight1Buttons.add(plotPointInfoLabel, gbc);
		panelRight1Buttons.add(showConsensusInput, gbc);
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelRight1Buttons.add(new JLabel(), gbc);

		JPanel panelRight1Plot = new JPanel(new GridLayout(1, 1));
		panelRight1Plot.setBorder(new TitledBorder(new EtchedBorder(),
				"Input curve plot"));
		panelRight1Plot.add(profilePlotIn);

		JPanel panelRight2Buttons = new JPanel(new GridBagLayout());
		gbc = new GridBagConstraints();
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.NONE;
		panelRight2Buttons.add(micaDistanceOutput, gbc);
		panelRight2Buttons.add(micaRuntimeOutput, gbc);
		panelRight2Buttons.add(showConsensusOutput, gbc);
		gbc.weightx = 0.6;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		panelRight2Buttons.add(new JLabel(), gbc);

		JPanel panelRight2Plot = new JPanel(new GridLayout(1, 1));
		panelRight2Plot.setBorder(new TitledBorder(new EtchedBorder(),
				"Alignment plot"));
		panelRight2Plot.add(profilePlotOut);

		/**
		 * Add the export buttons with constraints to the profile plot panel
		 */
		JPanel panelRight1 = new JPanel(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH; // only fill vertically
		panelRight1.add(panelRight1Buttons, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		panelRight1.add(panelRight1Plot, gbc);

		/**
		 * Add the export buttons with constraints to the alignment plot panel
		 */
		JPanel panelRight2 = new JPanel(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0.01;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH; // only fill vertically
		panelRight2.add(panelRight2Buttons, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.99;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		panelRight2.add(panelRight2Plot, gbc);

		panelSplit.setLeftComponent(panelRight1);
		panelSplit.setRightComponent(panelRight2);
		panelSplit.setDividerLocation(panelSplit.getHeight() / 2);

		/**
		 * Add the panels to the frame with constraints
		 */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelLeft, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelSplit, gbc);
		
	}

	/**
	 * Function to refresh the pot of the current selected profile
	 */
	private void refreshPlot() {
		LinkedList<Integer> selSet = getSelectedIndicesProfileList();
		LinkedList<ColoredAnnotatedCurve> plotSet = new LinkedList<>();

		comboboxPlotset.removeAllItems();
		
		// add option to select no curve
		comboboxPlotset.addItem("No selection");
		// add all curve names selected in the list
		for (Integer i : selSet) {
			ColoredAnnotatedCurve pd = controller.getProfileDataByIndex(i);
			plotSet.add(pd);
			comboboxPlotset.addItem(pd.getCurve().getName());
		}
		
		// generate simple mean data + consensus
		if (plotSet.size() > 1 && showConsensusInput.isSelected()) {
			double meanLength = plotSet.stream().mapToDouble( c -> c.getCurve().length() ).sum() / (double)plotSet.size();
			double meanStart = plotSet.stream().mapToDouble( c -> c.getCurve().getXmin() ).sum() / (double)plotSet.size();
			LinkedList<IntervalDecomposition> decomp = new LinkedList<>();
			for ( int p=0; p<plotSet.size(); p++ ) {
				// get current curve
				ColoredAnnotatedCurve c = plotSet.get(p);
				// construct new x-coordinates
				double[] newX = new double[c.getCurve().size()];
				// newX = (oldX-oldStart)/oldLength*newLength + newStart
				IntStream.range(0, newX.length).forEach( i -> newX[i] = (c.getCurve().getX()[i] - c.getCurve().getXmin())/c.getCurve().length()*meanLength + meanStart );
				// copy former curve
				ColoredAnnotatedCurve curveCopy = new ColoredAnnotatedCurve( new AnnotatedCurve(c.getCurve().getName(), newX, c.getCurve().getY(), c.getCurve().getAnnotation()), c.getColor());
				// add filters
				c.getCurve().getAnnotationFilter().stream().forEachOrdered( f -> curveCopy.getCurve().addAnnotationFilter(f) );
				// store
				plotSet.set(p, curveCopy );
				// add as an initial decomposition
				decomp.add( new IntervalDecomposition( plotSet.get(p).getCurve() ));
			}
			// get consensus
			AnnotatedCurve cons = MICA.getConsensusCurve( decomp ).getCurveOriginal();
			// overwrite annotations
			for (int i=1; i+1<cons.size(); i++) {
				cons.getAnnotation()[i] = CurveAnnotation.Type.IS_POINT;
			}
			cons.resetFilteredAnnotations();
			cons.setName("consensus");
			// store consensus for plotting
			plotSet.add( new ColoredAnnotatedCurve( cons, Color.black ) );
		}
		
		profilePlotIn.clearPlot();
		// set selected profile
		profilePlotIn.plotProfiles(plotSet);
		if (!selSet.isEmpty()) {
			comboboxPlotset.setSelectedIndex(1);
			comboboxPlotset.setEnabled(true);
			timerSlideUp.stop();
			timerSlideDown.start();
		} else {
			comboboxPlotset.setSelectedIndex(0);
			comboboxPlotset.setEnabled(false);
			panelSplit.setDividerLocation(panelSplit.getHeight() / 2);
		}
		
		if (plotSet.size() > 1 && showConsensusInput.isSelected()) {
			// select nothing
			profilePlotIn.setSelectedCurve(0);
		} else {
			profilePlotIn.setSelectedCurve(comboboxPlotset.getSelectedIndex()-1);
		}
	}

	/**
	 * Function to display the resulting profile plot
	 * 
	 * @param plotSet
	 *            the profile which contains the data for graph representation.
	 * @param consensus the consensus curve of the alignment
	 * @param alignmentDistance the final distance value for the alignment
	 * @param alignmentDuration the runtime used to compute the alignment
	 */
	public void displayResultPlot(
			LinkedList<ColoredAnnotatedCurve> plotSet,
			ColoredAnnotatedCurve consensus,
			double alignmentDistance, 
			long alignmentDuration) 
	{
		
		// clear plot
		profilePlotOut.clearPlot();
		
		if (plotSet.isEmpty()) {
			micaDistanceOutput.setText(" Distance: ");
			micaRuntimeOutput.setText(" Computation time: ");
			return;
		} else {
			DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
			dfs.setDecimalSeparator('.');
			DecimalFormat f = new DecimalFormat("#0.000000000", dfs);
			micaDistanceOutput.setText(" Distance: " + f.format(alignmentDistance));
			micaRuntimeOutput.setText(" Computation time: " + Model.generateTimeString(alignmentDuration));
		}
		
		// prepare curve list to be plotted
		LinkedList<ColoredAnnotatedCurve> toBePlotted = new LinkedList<>( plotSet );
		// check whether or not to plot the consensus
		if (showConsensusOutput.isSelected()) {
			toBePlotted.add(consensus);
		}
		
		// set curves for plotting
		profilePlotOut.plotProfiles(toBePlotted);
		
		// consensus selection if present
		profilePlotOut.setSelectedCurve( showConsensusOutput.isSelected() ? toBePlotted.size()-1 : -1);

		// make output upwards
		timerSlideDown.stop();
		timerSlideUp.start();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	/**
	 * Overwritten action handler for key released.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		/**
		 * Check if the ESC key is pressed
		 */
		if (e.getKeyChar() == KeyEvent.VK_ESCAPE) {
			/**
			 * If ESC check is split point insertion is active
			 */
			if (profileIndexForSplit != -1) {
				/**
				 * Ask the user if he is sure to abort the current split
				 * insertion
				 */
				int value = JOptionPane.showConfirmDialog(null,
						"Do you want to abort the current split insertion?\n"
								+ "This rejects all insterted "
								+ "split points in this step!",
						"Abort split point insertion",
						JOptionPane.YES_NO_OPTION);
				if (value == 0) {
					/**
					 * Abort current split insertion and restore the previous
					 * state
					 */
					abortCurrentSplitInsertionAndRestore();
				}
			}
		} else if (e.getKeyChar() == KeyEvent.VK_DELETE) {
			/**
			 * Keyboard key DELETE performs a removal of the selected curves
			 * or filters in the view.
			 */
			if (e.getSource() == listProfile) {
				removeSelectedProfile();
			}
		} else {
			/**
			 * Other key is pressed check which source it was, curve list or
			 * filter list
			 */
			if (e.getSource() == listProfile) {
				userActionProfileSelection();
			}
		}
	}

	/**
	 * Function which will be executed if the user selects a curve.
	 */
	private void userActionProfileSelection() {
		LinkedList<Integer> selSet = getSelectedIndicesProfileList();
		if (selSet.size() == 0) {
			/**
			 * If no curve is selected
			 */
			buttonRemoveProfile.setEnabled(false);

		} else {
			/**
			 * If at least one curve is selected
			 */
			buttonRemoveProfile.setEnabled(true);
		}
		/**
		 * Update plot and curve info
		 */
		refreshPlot();
		refreshProfileInfo();
	}


	/**
	 * Function for refresh the curve info. This function takes care about the
	 * curve information according to the number of selected curves
	 */
	public void refreshProfileInfo() {
		LinkedList<Integer> sel = getSelectedIndicesProfileList();
		if (sel.size() == 0) {
			/**
			 * If no curve was selected
			 */
			resetProfileInfo();
		} else if (sel.size() == 1) {
			/**
			 * If exactly one curve was selected
			 */
			refreshProfileInfoFor(sel.getFirst());
		} else {
			/**
			 * If more than one curve was selected. Now it is required to
			 * determine equal and different values.
			 */
			buttonPInfColor.setEnabled(false);
			buttonPInfColor.setBackground(Color.LIGHT_GRAY);
			buttonPInfColor.setForeground(Color.LIGHT_GRAY);
			textfieldPInfName.setText("*");
			textfieldPInfName.setEnabled(false);
			/**
			 * Check which attributes of the selected set is identically, expect
			 * the color
			 */
			ColoredAnnotatedCurve lastPD = null;
			boolean dpIdentical = true;
			boolean rpIdentical = true;
			boolean spIdentical = true;
			for (Integer i : sel) {
				if (lastPD != null) {
					ColoredAnnotatedCurve pd = controller.getProfileDataByIndex(i);
					dpIdentical = dpIdentical && (pd.getCurve().size() == lastPD.getCurve().size());
					rpIdentical = rpIdentical && (pd.getCurve().getFilteredAnnotations().size() == lastPD.getCurve().getFilteredAnnotations().size());
					spIdentical = spIdentical&& (pd.getSplitPoints().size() == lastPD.getSplitPoints().size());
				}
				lastPD = controller.getProfileDataByIndex(i);
			}

			if (dpIdentical) {
				textfieldPInfNumDP.setText(""
						+ lastPD.getCurve().size());
			} else {
				textfieldPInfNumDP.setText("*");
			}

			if (rpIdentical) {
				textfieldPInfNumRP.setText(""
						+ lastPD.getCurve().getFilteredAnnotations().size());
			} else {
				textfieldPInfNumRP.setText("*");
			}


			if (spIdentical) {
				textfieldPInfNumSP.setText("" + lastPD.getSplitPoints().size());
			} else {
				textfieldPInfNumSP.setText("*");
				Debug.out
						.println("refreshProfileInfo(): The number of split points has to be indentically over all curves!");
			}
		}
	}

	private void refreshProfileInfoFor(int selIndex) {
		ColoredAnnotatedCurve pd = controller.getProfileDataByIndex(selIndex);
		buttonPInfColor.setEnabled(true);
		buttonPInfColor.setBackground(pd.getColor());
		buttonPInfColor.setForeground(pd.getColor());
		textfieldPInfName.setText(pd.getCurve().getName());
		textfieldPInfName.setEnabled(true);
		textfieldPInfNumDP.setText(""
				+ pd.getCurve().size());
		textfieldPInfNumRP.setText(""
				+ pd.getCurve().getFilteredAnnotations().size());
		textfieldPInfNumSP.setText(""
				+ pd.getSplitPoints().size());
	}

	private void resetProfileInfo() {
		buttonPInfColor.setEnabled(false);
		buttonPInfColor.setBackground(Color.LIGHT_GRAY);
		buttonPInfColor.setForeground(Color.LIGHT_GRAY);
		textfieldPInfName.setText("");
		textfieldPInfName.setEnabled(false);
		textfieldPInfNumDP.setText("");
		textfieldPInfNumRP.setText("");
		textfieldPInfNumSP.setText("");
	}

	/**
	 * Overwritten function from the mouse clicked listener.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == listProfile) {
			userActionProfileSelection();
		}
	}

	/**
	 * Function to get a set of all selected indices in the curve selection
	 * list. If nothing is selected by the user this function automatically adds
	 * all entries to the selection.
	 * 
	 * @return Set of selected indices in the curve list
	 */
	public LinkedList<Integer> getSelectedIndicesProfileList() {
		LinkedList<Integer> retSet = new LinkedList<Integer>();
		ListSelectionModel lsm = listProfile.getSelectionModel();
		for (int i = 0; i < listProfileModel.size(); i++) {
			/**
			 * Check if the index is selected in the selection model
			 */
			if (lsm.isSelectedIndex(i)) {
				retSet.add(i);
			}
		}
		/**
		 * If the selection set is empty add all to selection
		 */
		if (retSet.isEmpty()) {
			lsm.setSelectionInterval(0, listProfileModel.size());
			for (int i = 0; i < listProfileModel.size(); i++) {
				retSet.add(i);
			}
		}

		return retSet;
	}

	/**
	 * Function to set the selected curves in the view list. This function is
	 * mainly used after the controller loads the program setup. With this
	 * function the controller can restore the curve selection.
	 * 
	 * @param set
	 *            Is the set of indices the curves are selected in the list
	 */
	public void setSelectedIndicesProfileList(LinkedList<Integer> set) {
		int arraySet[] = new int[set.size()];
		for (int i = 0; i < set.size(); i++) {
			arraySet[i] = set.get(i);
		}
		listProfile.setSelectedIndices(arraySet);
		userActionProfileSelection();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void popupMenuCanceled(PopupMenuEvent arg0) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
	}

	/**
	 * Overwritten action performed method from the action event listener.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonImportProfile
				|| e.getSource() == menuitemImportProfile) {
			/**
			 * Store the number of curves
			 */
			int numProfiles = listProfileModel.size();
			/**
			 * Import new curves
			 */
			parseAndImport();
			/**
			 * If the number of curves has changed during the import step ->
			 * clear the output plot
			 */
			if (numProfiles != listProfileModel.size()) {
				clearOutput();
			}
			/**
			 * Clear the previous selection and select the new imported curves
			 */
			listProfile.clearSelection();
			listProfile.setSelectionInterval(numProfiles,
					listProfileModel.size());
			userActionProfileSelection();
			/**
			 * Check for button activation
			 */
			checkStartMicaButtonActivation();
		} else if (e.getSource() == buttonRemoveProfile) {
			removeSelectedProfile();
		} else if (e.getSource() == showConsensusOutput) {
			controller.distributeSolution();
		} else if (e.getSource() == showConsensusInput) {
			this.refreshPlot();
		} else if (e.getSource() == buttonStartMICA) {

			/**
			 * Apply the alignment algorithm to all imported curves.
			 */
			if (listProfileModel.size() > 1) {
				/**
				 * Select all input curves for visualization in the input
				 * plot. This improves the comparison against the output for the
				 * user.
				 */
				listProfile.setSelectionInterval(0, listProfileModel.size());
				userActionProfileSelection();
				/**
				 * Disable start mica button and other components
				 */
//				enableDisableViewComponents(false);
				/**
				 * Execute the algorithm with the selected strategy
				 */
				if (radioProgressiveMica.isSelected()) {
					// run mica without reference selection
					controller.startMica(-1);
				} else if (radioReferenceMica.isSelected()) {
					
					// show dialog for reference selection
					ReferenceSelectionDialog refSelectionDialog = new ReferenceSelectionDialog(
							this, controller.getProfileNameSet(), false);
					refSelectionDialog.setVisible(true);
					LinkedList<Integer> selection = refSelectionDialog
							.getSelectedReferenceProfileIndices();
					

					if (selection != null && selection.size() == 1) {
						controller.startMica(selection.getFirst());
					} else {
						JOptionPane.showMessageDialog(null,
								"Reference MICA computation aborted,"
										+ " because reference curve"
										+ " selection is missing.",
								"No reference curve selected",
								JOptionPane.ERROR_MESSAGE);
					}
				} else /* if(radioRefProgMica.isSelected()) */{
					Debug.out.println("ViewImporFilterExecute.actionPerformed() : should not happen ... neither reference-based nor normal alignment selected!!");
				}
			}
		} else if (e.getSource() == menuitemExportCsv) {

			// trigger CSV export
			controller.exportCsv();
			
		} else if (e.getSource() == menuitemBackColorPlot) {
			/**
			 * Change the color of the background of the input plot
			 */
			Color newcolor = JColorChooser.showDialog(this,
					"Choose background color",
					profilePlotIn.getBackgroundColor());
			if (newcolor != null && newcolor != profilePlotIn.getBackgroundColor()) {
				profilePlotIn.setBackgroundColor(newcolor);
				profilePlotOut.setBackgroundColor(newcolor);
			}
		} else if (e.getSource() == menuitemExportPng) {
			
			// get position of legend within plots
			LegendPos lpos = LegendPos.LEFT;
			if (menuitemLegBoxLeft.isSelected()) { lpos = LegendPos.LEFT; }
			if (menuitemLegBoxCenter.isSelected()) { lpos = LegendPos.CENTER; }
			if (menuitemLegBoxRight.isSelected()) { lpos = LegendPos.RIGHT; }
			if (menuitemLegBoxHidden.isSelected()) { lpos = LegendPos.HIDDEN; }
			
			// trigger PNG export
			controller.exportPng( lpos );
			
		} else if (e.getSource() == buttonPInfColor) {
			/**
			 * Retrieve the color from the dialog
			 */
			Color newcolor = JColorChooser.showDialog(this,
					"Choose curve color",
					buttonPInfColor.getBackground());
			if (newcolor != null) {
				/**
				 * Set the new color to the color selection button
				 */
				buttonPInfColor.setBackground(newcolor);
				buttonPInfColor.setForeground(newcolor);
				/**
				 * Update the new color into the color curve data object. This
				 * set only can contain one element, because of the button
				 * enabling (zero or multiple selection deactivates the button).
				 */
				controller.getProfileDataByIndex(
						getSelectedIndicesProfileList().getFirst()).setColor(
						newcolor);
				/**
				 * Refresh the plot
				 */
				profilePlotIn.repaint();
				/**
				 * Update the color in the out plot
				 */
				profilePlotOut.setProfileColor(
						getSelectedIndicesProfileList().getFirst(), newcolor);
			}
		} else if (e.getSource() == textfieldPInfName) {
			/**
			 * Retrieve the selected curve. This set contains only one entry,
			 * because in all other cases the text field will be disabled.
			 */
			LinkedList<Integer> set = getSelectedIndicesProfileList();
			AnnotatedCurve p = controller.getProfileDataByIndex(set.getFirst()).getCurve();
			/**
			 * Update the curve name if unique
			 */
			String newPName = textfieldPInfName.getText();
			if (controller.isProfileNameGlobalUnique(newPName)) {
				/**
				 * Set the new curve name and update the view with the help of
				 * the controller.
				 */
				p.setName(newPName);
				/**
				 * Update the curve plot with the new name.
				 */
				profilePlotIn.repaint();
				/**
				 * Update also the curve name in the output plot
				 */
				profilePlotOut.setProfileName(set.getFirst(), "warp("
						+ newPName + ")");
				/**
				 * Update the curve list in the main view
				 */
				controller.updateViewProfileList();
				/**
				 * Restore the index of the selected curve in the list
				 */
				listProfile.setSelectedIndex(set.getFirst());
			} else {
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				JOptionPane.showMessageDialog(null, "User input \"" + newPName
						+ "\" is not global unique.", "Invalid input",
						JOptionPane.ERROR_MESSAGE);

				textfieldPInfName.setText(p.getName());
			}
		} else if (e.getSource() == menuitemAbout) {
			String aboutText = "Multiple Interval-based Curve Alignment (MICA)\n"
					+ "\n"
					+ "University of Freiburg\n"
					+ "- Bioinformatics - http://www.bioinf.uni-freiburg.de\n"
					+ "- Forest Growth - http://www.iww.uni-freiburg.de\n"
					+ "\n"
					+ "Contributors:\n"
					+ "- Matthias Beck\n"
					+ "- Martin Mann\n"
					+ "\n"
					+ "Version: "+MicaController.version+"\n"
					;
			JOptionPane.setDefaultLocale(Locale.ENGLISH);
			JOptionPane.showMessageDialog(null, aboutText, "About",
					JOptionPane.INFORMATION_MESSAGE);
		} else if (e.getSource() == menuitemExit) {
			windowClosingDialog();
		} else if (e.getSource() == timerSlideUp) {
			/**
			 * Ensure the other time is stopped
			 */
			timerSlideDown.stop();
			/**
			 * Move the split divider until 0,3 of the window height
			 */
			if (panelSplit.getDividerLocation() > panelSplit.getHeight()
					* (100 - splitStop) / 100) {
				/**
				 * Step movement 5% of the frame height
				 */
				panelSplit.setDividerLocation(panelSplit.getDividerLocation()
						- (int) Math.round((double) getContentPane()
								.getHeight() * 0.05));
			} else {
				/**
				 * Then stop the timer
				 */
				timerSlideUp.stop();
			}
		} else if (e.getSource() == timerSlideDown) {
			/**
			 * Ensure the other time is stopped
			 */
			timerSlideUp.stop();
			/**
			 * Move the split divider until 0,7 of the window height
			 */
			if (panelSplit.getDividerLocation() < panelSplit.getHeight()
					* splitStop / 100) {
				/**
				 * Step movement 5% of the frame height
				 */
				panelSplit.setDividerLocation(panelSplit.getDividerLocation()
						+ (int) Math.round((double) getContentPane()
								.getHeight() * 0.05));
			} else {
				/**
				 * Then stop the timer
				 */
				timerSlideDown.stop();
			}
		} else if (e.getSource() == comboboxPlotset) {
			/**
			 * Select the corresponding curve in plot
			 */
			profilePlotIn.setSelectedCurve(comboboxPlotset.getSelectedIndex()-1);
//			/**
//			 * Receive the profile and update the displaying view components
//			 */
//			LinkedList<Integer> selSet = getSelectedIndicesProfileList();
//			int subSelection = comboboxPlotset.getSelectedIndex();
//			if (subSelection >= 0) {
//				int profileIndex = selSet.get(subSelection);
//				ColoredAnnotatedCurve cpd = controller.getProfileDataByIndex(profileIndex);
//				AnnotatedCurve pdar = cpd.getCurve();
//				int numSelectionFilters = pdar.getNumCustomReferencePoints() - 2;
//				if (numSelectionFilters > 0) {
//					buttonResetTypeChanges.setEnabled(true);
//					buttonResetTypeChanges.setText("Reset "
//							+ numSelectionFilters + " changes");
//				} else {
//					buttonResetTypeChanges.setEnabled(false);
//					buttonResetTypeChanges.setText("Reset changes");
//				}
//			}

		} else if (e.getSource() == menuitemLegBoxLeft) {
			/**
			 * Update menu item enabling
			 */
			menuitemLegBoxCenter.setEnabled(true);
			menuitemLegBoxRight.setEnabled(true);
			menuitemLegBoxLeft.setEnabled(false);
			menuitemLegBoxHidden.setEnabled(true);
			/**
			 * Update the new position of the legend box in the plots
			 */
			profilePlotIn
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.LEFT);
			profilePlotOut
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.LEFT);
		} else if (e.getSource() == menuitemLegBoxCenter) {
			/**
			 * Update menu item enabling
			 */
			menuitemLegBoxCenter.setEnabled(false);
			menuitemLegBoxRight.setEnabled(true);
			menuitemLegBoxLeft.setEnabled(true);
			menuitemLegBoxHidden.setEnabled(true);
			/**
			 * Update the new position of the legend box in the plots
			 */
			profilePlotIn
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.CENTER);
			profilePlotOut
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.CENTER);
		} else if (e.getSource() == menuitemLegBoxRight) {
			/**
			 * Update menu item enabling
			 */
			menuitemLegBoxCenter.setEnabled(true);
			menuitemLegBoxRight.setEnabled(false);
			menuitemLegBoxLeft.setEnabled(true);
			menuitemLegBoxHidden.setEnabled(true);
			/**
			 * Update the new position of the legend box in the plots
			 */
			profilePlotIn
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.RIGHT);
			profilePlotOut
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.RIGHT);
		} else if (e.getSource() == menuitemLegBoxHidden) {
			/**
			 * Update menu item enabling
			 */
			menuitemLegBoxCenter.setEnabled(true);
			menuitemLegBoxRight.setEnabled(true);
			menuitemLegBoxLeft.setEnabled(true);
			menuitemLegBoxHidden.setEnabled(false);
			/**
			 * Update the new position of the legend box in the plots
			 */
			profilePlotIn
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.HIDDEN);
			profilePlotOut
					.setLegendPosition(ColoredAnnotatedCurvePlot.LegendPos.HIDDEN);
		} else if (e.getSource() == menuitemAddSplit) {
			/**
			 * Check if the user can add split points to all profiles
			 */
			if (splitPossibleForAllProfiles()) {
				/**
				 * First time split insertion show information message about the
				 * curve import disabling.
				 */
				if (controller.getProfileDataByIndex(0).getSplitPoints().isEmpty()) {
					JOptionPane.setDefaultLocale(Locale.ENGLISH);
					JOptionPane
							.showMessageDialog(
									null,
									"<html>Import of new curves will be disabled until<br> all manual alignments are removed.</html>",
									"Manual alignment",
									JOptionPane.INFORMATION_MESSAGE);
				}
				/**
				 * Disable the import button, because the already imported
				 * curves have from now on more split points than a newly
				 * imported curve
				 */
				enableDisableImportProfileComponents(false);
				/**
				 * Start requesting the split point insertion starting with the
				 * first curve
				 */
				profileIndexForSplit = 0;
				/**
				 * Sliding down the input plot area
				 */
				timerSlideUp.stop();
				timerSlideDown.start();
				/**
				 * Also clear the output plot
				 */
				clearOutput();
				/**
				 * Request for split insertion
				 */
				requestSplitForProfile();
			} else {
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				JOptionPane.showMessageDialog(null,
						"Split point insertion is not possible, because"
								+ " at least one\n" + "curve can not"
								+ " retrieve an additional new split point.",
						"Split info", JOptionPane.INFORMATION_MESSAGE);
			}
		} else if (e.getSource() == menuitemRemoveSplit) {
			/**
			 * Remove all split points from the profiles
			 */
			controller.removeAllSplitPoints();
			/**
			 * Repaint the plot
			 */
			profilePlotIn.repaint();
			/**
			 * Also clear the output plot
			 */
			clearOutput();
			/**
			 * Disable the remove button
			 */
			menuitemRemoveSplit.setEnabled(false);
			/**
			 * Import button can be enabled, because no split points exists
			 * anymore
			 */
			enableDisableImportProfileComponents(true);
			
		} else if (e.getSource() == radioDataDistance || e.getSource() == radioSlopeDistance) {
			// set distance computation data
			controller.setDistanceSlopeBased( radioSlopeDistance.isSelected() );
		}
	}
	
	/**
	 * Sets the distance base selection according to the given value
	 * @param slopeBased whether or not to use the slope for distance computation
	 */
	public void setDistanceSlopeBased( boolean slopeBased ) {
		if (slopeBased)
			radioSlopeDistance.setSelected(true);
		else
			radioDataDistance.setSelected(true);
	}

	/**
	 * Function to remove the selected curves from the curve list
	 */
	private void removeSelectedProfile() {
		LinkedList<Integer> sel = getSelectedIndicesProfileList();
		/**
		 * Remove selected curves from model. Removal starting with the
		 * highest index to avoid using a deletion index offset.
		 */
		for (int i = sel.size() - 1; i >= 0; i--) {
			controller.removeProfileByIndex(sel.get(i));
		}
		buttonRemoveProfile.setEnabled(false);
		refreshPlot();
		refreshProfileInfo();

		clearOutput();
		/**
		 * Enable the mica button only if at least two curves are imported.
		 */
		checkStartMicaButtonActivation();
		/**
		 * Ensures the enabling of the delete button until no curve is in the
		 * list
		 */
		userActionProfileSelection();
	}

	/**
	 * Function to check if an insertion of split points is possible in all
	 * curves. If at least one curve can not get an additional split point
	 * (because of the interval ranges) this function indicate this.
	 * 
	 * @return True if all curves can retrieve an additional split points,
	 *         false otherwise
	 */
	private boolean splitPossibleForAllProfiles() {
		
		// check if any data available
		if (listProfileModel.isEmpty()) {
			return false;
		}
		
		for (int i = 0; i < listProfileModel.getSize(); i++) {
			
			ColoredAnnotatedCurve curProfile = controller.getProfileDataByIndex(i);
			
			// check if number of split points exceeds available points between start and end
			if (curProfile.getSplitPoints().size() >= curProfile.getCurve().size()-2) {
				// no split possible
				return false;
			}
		}
		// seems to be fine
		return true;
	}

	/**
	 * Function which takes care about the enabling of the import curve
	 * components. If at least one curve contains a split point the import of
	 * new curves is disabled.
	 * 
	 * @param enable
	 */
	private void enableDisableImportProfileComponents(boolean enable) {
		/**
		 * If at least one of the curves contains a split point overwrite the
		 * import curve components with disabled
		 */
		boolean importProfileFlag = true;
		if (controller.getNumberProfiles() > 0) {
			if (controller.getProfileDataByIndex(0)
					.getSplitPoints().size() > 0) {
				importProfileFlag = false;
			}
		}
		buttonImportProfile.setEnabled(enable && importProfileFlag);
		menuitemImportProfile.setEnabled(enable && importProfileFlag);
	}

	/**
	 * Function to enable or disable some view components.
	 * 
	 * @param enable
	 *            True enabled and false disables the view components
	 */
	private void enableDisableViewComponents(boolean enable) {
		/**
		 * enable or disable
		 */
		menuitemAddSplit.setEnabled(enable);
		menuitemRemoveSplit.setEnabled(enable);
		buttonRemoveProfile.setEnabled(enable);
		buttonPInfColor.setEnabled(enable);
		buttonStartMICA.setEnabled(enable);
		textfieldPInfName.setEnabled(enable);
		listProfile.setEnabled(enable);
		radioDataDistance.setEnabled(enable);
		radioSlopeDistance.setEnabled(enable);
		radioProgressiveMica.setEnabled(enable);
		radioReferenceMica.setEnabled(enable);
		maxWarpingFactorPanel.setEnabled(enable);
		minIvalDecompPanel.setEnabled(enable);
		paramMaxRelXShiftPanel.setEnabled(enable);
		comboboxPlotset.setEnabled(enable);
		/**
		 * Also remove the listeners from the list curve
		 */
		if (enable == false) {
			listProfile.removeMouseListener(this);
			listProfile.removeKeyListener(this);
		} else {
			listProfile.addMouseListener(this);
			listProfile.addKeyListener(this);
		}
	}

	/**
	 * Function which displays the curve with the index profileIndexForSplit
	 * in the plot. The plot object is set to split insertion mode. After the
	 * user inserts the split point successfully the plot informs this object.
	 */
	private void requestSplitForProfile() {
		/**
		 * Check if the index is in a valid range
		 */
		if (profileIndexForSplit >= 0
				&& profileIndexForSplit < listProfileModel.getSize()) {
			/**
			 * Disable some view components until split process is finished
			 */
			enableDisableViewComponents(false);
			/**
			 * Retrieve the curve via the controller
			 */
			ColoredAnnotatedCurve c = controller.getProfileDataByIndex(profileIndexForSplit);
			/**
			 * Plot the curve and wait for the split completed callback if the
			 * user inserts a split point
			 */
			profilePlotIn.plotProfileForSplit(c);
		} else {
			Debug.out
					.println("requestSplitForProfile(): Curve selection index out of range!");
			profileIndexForSplit = -1;
		}
	}


	/**
	 * Function which aborts the current split point insertion and restore the
	 * split points before inserting the current set.
	 */
	private void abortCurrentSplitInsertionAndRestore() {
		/**
		 * Ensure to remove the rejected split points in this set. The last
		 * curves number of split points provides the number of split points
		 * for all curves
		 */
		int numSplitOfLastProfile = controller
				.getProfileDataByIndex(controller.getNumberProfiles() - 1)
				.getSplitPoints().size();
		/**
		 * Over all curves ensure that all curves have the same number of
		 * split points
		 */
		for (int i = 0; i < controller.getNumberProfiles(); i++) {
			ColoredAnnotatedCurve pd = controller.getProfileDataByIndex(i);
			/**
			 * Delete the last split point until the number of split points is
			 * equal to the desired number
			 */
			while (pd.getSplitPoints().size() > numSplitOfLastProfile)
				pd.removeLastSplitPoint();
		}
		/**
		 * Disable split insertion
		 */
		enableDisableViewComponents(true);
		profileIndexForSplit = -1;
		/**
		 * Clear the selection if the curves and repaint
		 */
		listProfile.clearSelection();
		userActionProfileSelection();
	}

	/**
	 * Function is called through the curve plot object if the user inserts a
	 * split point to the current curve.
	 */
	@Override
	public void splitCompleted() {
		/**
		 * select the next curve for split insertion
		 */
		profileIndexForSplit++;
		if (profileIndexForSplit >= listProfileModel.getSize()) {
			/**
			 * Enable the previous disabled (in function requestSplitForProfile)
			 * view components because the split is completed
			 */
			enableDisableViewComponents(true);
			profileIndexForSplit = -1;
			userActionProfileSelection();
		} else {
			requestSplitForProfile();
		}
	}

	/**
	 * Function to check if the mica start button can be enabled. This depends
	 * on the number of imported curves. If the number of imported curves is
	 * larger or equals to 2 the button will be enabled.
	 */
	public void checkStartMicaButtonActivation() {
		buttonStartMICA.setEnabled(false);
		if (listProfileModel.size() >= 2) {
			buttonStartMICA.setEnabled(true);
		}
	}

	/**
	 * Function to clear the output plot
	 */
	private void clearOutputPlot() {
		controller.discardAlignment();
		profilePlotOut.clearPlot();
		if (getSelectedIndicesProfileList().size() > 0) {
			timerSlideUp.stop();
			timerSlideDown.start();
		} else {
			panelSplit.setDividerLocation(panelSplit.getHeight() / 2);
		}
	}

	/**
	 * Function to clear the result info (distance, duration, progress)
	 */
	private void clearResultInfo() {
		micaRuntimeOutput.setText(" Computation time: ");
		micaDistanceOutput.setText(" Distance: ");
//		updateProgressIndication( Status.NOT_STARTED );
	}

	/**
	 * Function to clear the output plot and the result info
	 */
	public void clearOutput() {
		clearOutputPlot();
		clearResultInfo();
	}

	/**
	 * Function to get the mica mode selection in the main view. This function
	 * is used to export the information in the program setup save file.
	 * 
	 * @return The AlignmentType specification
	 */
	public MicaMainFrame.AlignmentType getSelectedMicaMode() {
		if (radioReferenceMica.isSelected()) {
			return MicaMainFrame.AlignmentType.RMICA;
		}
//		if (radioProgressiveMica.isSelected())
		return MicaMainFrame.AlignmentType.MICA;
	}

	/**
	 * Function to set the mica mode radio button in the main view. This
	 * function is used by the controller to restore the mica mode selection
	 * from a setup load file.
	 * 
	 * @param at
	 *            Is the AlignmentType enumeration specification
	 */
	public void setSelectedMicaMode(MicaMainFrame.AlignmentType at) {
		radioProgressiveMica.setSelected( at == MicaMainFrame.AlignmentType.MICA ); 
		radioReferenceMica.setSelected( at == MicaMainFrame.AlignmentType.RMICA ); 
	}

	/**
	 * Function which creates a dialog before application termination. If the
	 * user Answers the dialog with no the program keep alive.
	 */
	private void windowClosingDialog() {
		JOptionPane.setDefaultLocale(Locale.ENGLISH);
		int value = JOptionPane.showConfirmDialog(null, "Do you want to exit?",
				"Exit", JOptionPane.YES_NO_OPTION);
		if (value == 0) {
			System.exit(0);
		}
	}


	/**
	 * Function to init the parse and import step. Therefore a file selection
	 * dialog appears. The returns path to the filename from the dialog is used
	 * to parse and import via the controller.
	 */
	private void parseAndImport() {
		/**
		 * Create file dialog to retrieve the file name for import
		 */
		FileDialog fd = new FileDialog(new Frame(),
				"Select curve data file ...", FileDialog.LOAD);
		fd.setVisible(true);
		/**
		 * Valid file name was returned
		 */
		if (fd.getDirectory() != null && fd.getFile() != null) {
			/**
			 * create the parse and import view through the controller.
			 */
			controller.startParseAndImport(fd.getDirectory() + fd.getFile());
		}
	}

	/**
	 * Function to update the curve list.
	 * 
	 * @param pNames
	 *            The set of names in the curve list. These list has so be in
	 *            the same order as the curves are in the model stored.
	 */
	public void updateProfileList(LinkedList<String> pNames) {
		/**
		 * Erase old content.
		 */
		listProfileModel.clear();
		for (String s : pNames) {
			listProfileModel.addElement(s);
		}
		enableDisableImportProfileComponents(true);

		/**
		 * enable or disable the split button
		 */
		if (!pNames.isEmpty()) {
			menuitemAddSplit.setEnabled(true);
		} else {
			menuitemAddSplit.setEnabled(false);
		}
	}

	/**
	 * Callback function to update the information about the currently selected
	 * point in the plot
	 */
	@Override
	public void selectedPlotPointChanged(double x, double y, CurveAnnotation.Type t) {
		/**
		 * Needed to format the double values to a representable length for the
		 * view components
		 */
		DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance();
		dfs.setDecimalSeparator('.');
		DecimalFormat f = new DecimalFormat("#0.00", dfs);
		
		// nothing selected
		if (Double.isNaN(x)) {
			// nothing selected
			plotPointInfoLabel.setText(" ");
		} else {
			// Point selected -> update the view components
			plotPointInfoLabel.setText("Point ("+f.format(x) + "," + f.format(y)+ ") = "+t);
		}
	}



	@Override
	public void update(Observable o, Object arg) {
		
		// repaint curve information if filter value changed
		if (o instanceof CurveAnnotationFilter) {
			this.refreshProfileInfo();
		}
	}
	
	
	public static class MicaProgressDialog extends JDialog implements ActionListener, IProgressIndicator {
		
		private static final long serialVersionUID = 1L;
		/**
		 * Progress bar for showing the computation progress
		 */
		private JProgressBar progressbar = new JProgressBar( JProgressBar.HORIZONTAL, 0, 1);
		
		/**
		 * button to abort computation
		 */
		private JButton abortButton = new JButton( "Abort computation", UIManager.getIcon("OptionPane.errorIcon"));
		
		/**
		 * The mica thread currently running and observed
		 */
		private Thread runningMica = null;
		
		private MicaMainFrame mainView;

		/**
		 * Creates a dialog
		 * @param owner
		 */
		public MicaProgressDialog( MicaMainFrame owner ) {
			super( owner, "Alignment computation", true );
			
			mainView = owner;
			
			progressbar.setStringPainted(true);
			progressbar.setString(" running .. please wait ");
			
			JPanel content = new JPanel( new GridBagLayout() );
			
			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.anchor = GridBagConstraints.CENTER;
			
			// add progress bar
			c.fill = GridBagConstraints.HORIZONTAL;
			content.add( progressbar, c );
			
			// add abortion button
			c.fill = GridBagConstraints.NONE;
			abortButton.addActionListener(this);
			content.add( abortButton, c );
			
			this.setContentPane(content);
			this.setPreferredSize( new Dimension(200, 150) );
			this.pack();
			
			switchToFront(false);
			
			this.validate();
			
			this.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
		}
		
		/*
		 * handles computation abort request
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			// interrupt the computation
			if (runningMica != null) {
				runningMica.interrupt();
			}
			switchToFront(false);
			mainView.clearOutput();
		}

		@Override
		public void updateProgressIndication(Status status) {
			switch (status) {
			case NOT_STARTED:
			case RUNNING:
				progressbar.setIndeterminate( status == Status.RUNNING );
				progressbar.setValue(0);
				break;
				
			case FINISHED:
				progressbar.setIndeterminate( false );
				progressbar.setValue(1);
				switchToFront(false);
				break;
			}
		}

		@Override
		public void startMica(MicaRunner micaRunner) {
			
			
			runningMica = new Thread(micaRunner);
			
			runningMica.start();
			
			switchToFront(true);
		}
		
		private void switchToFront( boolean toFront ) {
			
			// en/disable main panel
			this.getParent().setEnabled( !toFront );
			
			// show/hide dialog
			this.setVisible(toFront);
			this.setAlwaysOnTop(toFront);
			this.setEnabled(toFront);
			this.setModal(toFront);
			this.setAutoRequestFocus(toFront);
			this.setLocationRelativeTo(this.getParent());
		}

	}
	
	/**
		 * Enumeration to specify the alignment type.
		 * 
		 * @author mbeck
		 * 
		 */
		public enum AlignmentType {
			/**
			 * MICA - Stands for the progressive standard alignment
			 * 
			 * RMICA - Stands for the reference based alignment
			 */
			MICA, RMICA
		}
	//		 * 
	//		 * PRMICA - Is the combination of reference and progressive alignment
	//		, PRMICA

	/**
	 * Access to the progress indicator to be used when MICA is started
	 * @return the progress indicator
	 */
	public IProgressIndicator getProgressIndicator() {
		return micaProgressDialog;
	}

}
