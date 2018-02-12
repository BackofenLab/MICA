package de.uni_freiburg.bioinf.mica.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.controller.GuiController;
import de.uni_freiburg.bioinf.mica.controller.Debug;
import de.uni_freiburg.bioinf.mica.model.FileFormatCsv;
import de.uni_freiburg.bioinf.mica.model.ImportExport;

/**
 * View object which helps the user to set up the parameters for the parsing
 * action for the import of profiles.
 * 
 * @author mbeck
 * 
 */
public class ViewParse extends JDialog implements ActionListener, KeyListener,
		ISelectedProfilesListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Internal attributes including file name, profile set, selection set for
	 * the parsed profiles the import and export object and the default
	 * delimiter.
	 */
	private String file = null;
	private LinkedList<Curve> parsedProfiles = null;
	private LinkedList<Boolean> selectedProfiles = null;
	private ImportExport ie = null;
	private String defaultDelimiter = null;
	private GuiController controller = null;
	private boolean enableAccessToImpProfile = false;
	private final int minRequiredDataPoints;
	/**
	 * Labels, text field and buttons for the left part of the view. This
	 * controls the parsing and import.
	 */
	private JLabel labelEmpty = null;
	private JLabel labelDelimiter = null;
	private JLabel labelContainsHdr = null;
	private JLabel labelDetectProfiles = null;
	private JLabel labelSelectProfiles = null;
	private JLabel labelValueDetectProfiles = null;
	private JLabel labelValueSelectProfiles = null;
	private JLabel labelLengthCorrecton = null;
	private JTextField textfieldDelimSymbol = null;
	private JTextField textfieldLenCorrection = null;
	private JCheckBox cbContainsHeader = null;
	private JLabel labelEnableLenCorrect = null;
	private JCheckBox cbEnableLenCorrect = null;
	private JButton buttonImport = null;
	private JButton buttonCancel = null;
	/**
	 * Selection table object, which provides the possibility to select profiles
	 * and change the profile names.
	 */
	private SelectionTable tableSelectionPD = null;
	/**
	 * Message label which contains user feedback text
	 */
	private JLabel labelDelimiterInfoMessage = null;
	/**
	 * Panel which holds the table or the delimiter check message
	 */
	private JPanel panelTable = null;

	/**
	 * Constructor which needs the file name for importing the profiles.
	 * 
	 * @param parent
	 *            Is the Frame of the parent view. This is needed for the
	 *            modality settings.
	 * @param filename
	 *            Filename to the data on the hard disk.
	 * @param c
	 *            Reference to the controller object to perform the profile name
	 *            check.
	 * @param minimalNumberDataPoints
	 *            Is the minimal length of datapoints the user is allowed to
	 *            import a profile
	 * @param parseHeader whether or not to enable header parsing
	 */
	public ViewParse(Frame parent, String filename, GuiController c,
			final int minimalNumberDataPoints,
			final boolean parseHeader ) {
		/**
		 * Set the filename to the window title.
		 */
		super(parent, "Import from file: " + filename,
				ModalityType.DOCUMENT_MODAL);

		/**
		 * Set the default delimiter 
		 */
		defaultDelimiter = ";";
		/**
		 * Store the filename and create instances of the internal objects.
		 */
		controller = c;
		file = filename;
		parsedProfiles = new LinkedList<Curve>();
		selectedProfiles = new LinkedList<Boolean>();
		ie = new ImportExport();
		minRequiredDataPoints = minimalNumberDataPoints;
		/**
		 * Create instances of the view components.
		 */
		labelEmpty = new JLabel();
		labelDelimiter = new JLabel("Delimiter: ");
		labelContainsHdr = new JLabel("Contains header: ");
		labelDetectProfiles = new JLabel("Detected profiles: ");
		labelSelectProfiles = new JLabel("Selected profiles: ");
		labelValueDetectProfiles = new JLabel("0");
		labelValueSelectProfiles = new JLabel("0");
		labelLengthCorrecton = new JLabel("Length correction: ");
		textfieldDelimSymbol = new JTextField(2);
		textfieldDelimSymbol.setText(defaultDelimiter);
		textfieldLenCorrection = new JTextField("100");
		textfieldLenCorrection
				.setInputVerifier(new TextFieldIntegerValueInputVerifier(
						minRequiredDataPoints, 10000));
		labelEnableLenCorrect = new JLabel("Enable len. corr.:");
		cbEnableLenCorrect = new JCheckBox();
		cbEnableLenCorrect.setSelected(false);
		cbContainsHeader = new JCheckBox();
		cbContainsHeader.setSelected(parseHeader);
		buttonImport = new JButton("Import");
		buttonCancel = new JButton("Cancel");
		tableSelectionPD = new SelectionTable();

		/**
		 * Message label which is used to display user feedback if no parsing
		 * results are available
		 */
		labelDelimiterInfoMessage = new JLabel(
				"No parsing results. Please check the delimiter.");

		/**
		 * Init the table panel which contains the parsed table or the delimiter
		 * check message
		 */
		panelTable = new JPanel(new GridLayout(1, 1));
		panelTable
				.setBorder(new TitledBorder(new EtchedBorder(), "Data points"));

		/**
		 * Init the view
		 */
		initForm();
		/**
		 * And add all action listeners.
		 */
		addActionListeners();
		/**
		 * Start the parsing step
		 */
		parseAction();
	}

	/**
	 * Function to retrieve the scaled profiles from the parse and import view.
	 * 
	 * @return The set of profiles from the import step
	 */
	public LinkedList<Curve> getSelectedProfiles() {
		LinkedList<Curve> retList = new LinkedList<>();
		/**
		 * Enable access to the imported profiles only if the import button was
		 * pressed.
		 */
		if (enableAccessToImpProfile) {
			String infoMessage = null;
			int len = Integer.parseInt(textfieldLenCorrection.getText());
			for (Curve p : parsedProfiles) {
				if (selectedProfiles.get(parsedProfiles.indexOf(p)) == true) {
					/**
					 * Check if length correction is enabled
					 */
					if (cbEnableLenCorrect.isSelected()) {
						/**
						 * Add the profiles according to the length correction
						 * definition
						 */
						retList.add(new Curve(p.getName(), p.getYequiX( len ) ));
					} else {
						/**
						 * If no length correction is enabled check against the
						 * minimum number of required data points and correct
						 * automatically if smaller.
						 */
						if (p.size() < minRequiredDataPoints) {
							// interpolate to minimal point number
							retList.add(new Curve(p.getName(),
									p.getYequiX(minRequiredDataPoints)
									));
							if (infoMessage == null)
								infoMessage = new String();
							
							infoMessage += "> Profile " + p.getName()
									+ " = " + p.size()
									+ " data points\n";
						} else {
							retList.add(p);
						}
					}
				}
			}

			/**
			 * Show user information if automatically length correction was
			 * performed
			 */
			if (infoMessage != null) {
				String additionalInfo = "The following profiles had less than "
						+ minRequiredDataPoints
						+ " data points.\n"
						+ "These profiles were interpolated.\n\n";
				String title = "Length correction information";
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				JOptionPane.showMessageDialog(null, additionalInfo
						+ infoMessage, title, JOptionPane.INFORMATION_MESSAGE);
			}
		}
		return retList;
	}

	
	/**
	 * Function which represents the import action. In this step the view will
	 * be set to invisible only if the imported profiles names are globally
	 * unique.
	 */
	private void importButtonAction() {
		/**
		 * Sync the profile names from the selection table.
		 */
		syncProfileNames();
		/**
		 * Sync the profile selection from the selection table.
		 */
		syncProfileSelection();
		/**
		 * Check if the length correction is bigger than 2
		 */
		int lenCorr = Integer.parseInt(textfieldLenCorrection.getText());
		if (lenCorr < minRequiredDataPoints) {
			String message = "The length correction \"" + lenCorr
					+ "\" has to be "
					+ "a positive \nnumber and larger or equals to "
					+ minRequiredDataPoints + ".";
			String title = "Invalid length correction";
			JOptionPane.setDefaultLocale(Locale.ENGLISH);
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		/**
		 * Check the uniqueness of the profile names.
		 */
		if (!checkProfileNameUniqueness(true)) {
			this.setVisible(false);
			enableAccessToImpProfile = true;
		} else {
			if (!checkProfileNameUniqueness(false)) {
				this.setVisible(false);
				enableAccessToImpProfile = true;
			} else {
				String message = "There are still profile names that occur"
						+ " repeatedly.\nPlease ensure that the profile"
						+ " names are globally unique.";
				String title = "Duplicate profile names";
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Function to propagate the information about the profile selection from
	 * the selection table object to the data structure for return.
	 */
	private void syncProfileSelection() {
		for (int i = 0; i < selectedProfiles.size(); i++) {
			selectedProfiles.set(i, tableSelectionPD.isProfileSelected(i));
		}
	}

	/**
	 * Function to propagate the information about the updated profile names
	 * from the selection table object to the data structure for return.
	 */
	private void syncProfileNames() {
		for (int i = 0; i < parsedProfiles.size(); i++) {
			parsedProfiles.get(i).setName(tableSelectionPD.getProfileName(i));
		}
	}

	/**
	 * Function to check if the profile names in this parsing step are unique.
	 * This is relevant if there are no profiles available in previous. In this
	 * case we import the first time profiles and then these profiles has to be
	 * unique locally.
	 * 
	 * @return True if there occurs a profile name twice. If the return value is
	 *         false every profile has a unique profile name.
	 */
	private boolean checkProfileNameUniquessLocal() {
		for (int i = 0; i < parsedProfiles.size(); i++) {
			for (int j = i + 1; j < parsedProfiles.size(); j++) {
				if (parsedProfiles.get(i).getName()
						.compareTo(parsedProfiles.get(j).getName()) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Function to check the profile names against the existing profile names.
	 * If there is a duplicate profile name the user has the possibility to
	 * change the name of the profile.
	 * 
	 * @param userDialog
	 *            If this flag is true the user has the possibility to directly
	 *            change the duplicate profile name. If this flag is set to
	 *            false the function only indicates if there is a duplicate
	 *            profile name or not.
	 * @return True if a duplicate profile name was found, otherwise false.
	 */
	private boolean checkProfileNameUniqueness(boolean userDialog) {
		boolean duplicateNameOccours = false;
		/**
		 * Over the new imported profiles
		 */
		for (int i = 0; i < parsedProfiles.size(); i++) {
			Curve p = parsedProfiles.get(i);
			/**
			 * Handle the case that the profile name is not unique
			 */
			if (!controller.isProfileNameGlobalUnique(p.getName())
					&& selectedProfiles.get(i)) {
				/**
				 * Indicate duplicate profile names
				 */
				duplicateNameOccours = true;
				/**
				 * Provide a user interface to change the profile name
				 */
				if (userDialog) {
					/**
					 * In the other case ask the user for a different name
					 */
					String dialogTitle = "Duplicate profile name";
					String dialogMsg = "The profile name \"" + p.getName()
							+ "\" already exists globally.\n"
							+ "Please enter a globally unique"
							+ " profile name.";
					String dialogRet = JOptionPane.showInputDialog(null,
							dialogMsg, dialogTitle, JOptionPane.ERROR_MESSAGE);
					/**
					 * Only store the new name if not empty
					 */
					if (dialogRet != null) {
						if (!dialogRet.isEmpty()) {
							p.setName(dialogRet);
						}
					}
				}
			}
		}

		/**
		 * Check also if there exists local uniqueness if the comparison against
		 * the global names found no error.
		 */
		if (duplicateNameOccours == false) {
			duplicateNameOccours = checkProfileNameUniquessLocal();
		}

		return duplicateNameOccours;
	}

	/**
	 * Function to init the view.
	 */
	private void initForm() {
		/**
		 * Set the properties of this frame.
		 */
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new GridLayout(1, 2));
		this.setMinimumSize(new Dimension(800, 700));
		this.setLocationRelativeTo(null);
		/**
		 * Create the constraint object for the view.
		 */
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		/**
		 * Create panels for the top.
		 */
		JPanel panelParse = new JPanel(new GridLayout(3, 2));
		panelParse.setBorder(new TitledBorder(new EtchedBorder(), "Parse"));
		panelParse.add(labelDelimiter);
		panelParse.add(textfieldDelimSymbol);
		panelParse.add(labelContainsHdr);
		panelParse.add(cbContainsHeader);
		panelParse.add(labelEmpty);
		panelParse.add(labelEmpty);
		JPanel panelImport = new JPanel(new GridLayout(4, 2));
		panelImport.setBorder(new TitledBorder(new EtchedBorder(), "Import"));
		panelImport.add(labelDetectProfiles);
		panelImport.add(labelValueDetectProfiles);
		panelImport.add(labelSelectProfiles);
		panelImport.add(labelValueSelectProfiles);
		panelImport.add(labelLengthCorrecton);
		panelImport.add(textfieldLenCorrection);
		panelImport.add(labelEnableLenCorrect);
		panelImport.add(cbEnableLenCorrect);
		JPanel panelControl = new JPanel(new GridLayout(2, 1));
		panelControl.setBorder(new TitledBorder(new EtchedBorder(), "Control"));
		panelControl.add(buttonImport);
		panelControl.add(buttonCancel);
		JPanel panelTop = new JPanel(new GridLayout(1, 3));
		panelTop.add(panelParse);
		panelTop.add(panelImport);
		panelTop.add(panelControl);
		/**
		 * Create panel for the table at the bottom
		 */
		panelTable.add(tableSelectionPD);
		/**
		 * Add the panels to the frame with constraints
		 */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0.01;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelTop, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.99;
		gbc.anchor = GridBagConstraints.LAST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelTable, gbc);
	}

	/**
	 * Function to trigger a parse action.
	 */
	private void parseAction() {
		/**
		 * Empty the sets from a possible previous parsing step.
		 */
		parsedProfiles.clear();
		selectedProfiles.clear();

		/**
		 * Perform only if the delimiter symbol input is not empty
		 */
		if (!textfieldDelimSymbol.getText().isEmpty()) {
			/**
			 * Parse performed enable the import button.
			 */
			ie.setFileFormat(new FileFormatCsv(textfieldDelimSymbol.getText()));
			int numCol = ie.getNumberOfColumns(file);
			for (int i = 0; i < numCol; i++) {
				Curve p = ie.load(file, i, cbContainsHeader.isSelected());
				if (p != null) {
					/**
					 * If no file name is available generate default from
					 * filename and index
					 */
					if (p.getName() == null) {
						String defaultFilename = new File(file).getName() + "-"
								+ i;
						p.setName(defaultFilename);
					}
					/**
					 * And to the internal sets.
					 */
					parsedProfiles.add(p);
					selectedProfiles.add(true);
				} else {
					/**
				 * 
				 */
					break;
				}
			}
		}
		labelValueDetectProfiles.setText("" + parsedProfiles.size());

		/**
		 * Display the table content
		 */
		tableSelectionPD.initModel(parsedProfiles);

		/**
		 * According to the parsing results display the table with content or
		 * show the user information to check the delimiter settings.
		 */
		if (parsedProfiles.size() == 0) {
			buttonImport.setEnabled(false);
			panelTable.setLayout(new FlowLayout());
			panelTable.remove(tableSelectionPD);
			panelTable.add(labelDelimiterInfoMessage);
		} else {
			buttonImport.setEnabled(true);
			panelTable.setLayout(new GridLayout(1, 1));
			panelTable.remove(labelDelimiterInfoMessage);
			panelTable.add(tableSelectionPD);
		}
	}

	/**
	 * Function for adding the necessary action listeners.
	 */
	private void addActionListeners() {
		buttonImport.addActionListener(this);
		buttonCancel.addActionListener(this);
		cbContainsHeader.addActionListener(this);
		cbEnableLenCorrect.addActionListener(this);
		textfieldDelimSymbol.addKeyListener(this);
		textfieldLenCorrection.addKeyListener(this);
		tableSelectionPD.registerSelectedProfilesListener(this);
	}

	/**
	 * Action performed method for execute the actions initiated by the view
	 * components.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonImport) {
			/**
			 * Close this dialog if the profile names are globally unique.
			 */
			importButtonAction();
		} else if (e.getSource() == buttonCancel) {
			/**
			 * Nothing will be used from this import view. Set all available
			 * profiles to null.
			 */
			parsedProfiles.clear();
			/**
			 * consume an empty set of profiles.
			 */
			this.setVisible(false);
		} else if (e.getSource() == cbContainsHeader) {
			/**
			 * Skip header option changed. The same actions are needed as in the
			 * previous delimiter case.
			 */
			parseAction();
		} else if (e.getSource() == cbEnableLenCorrect) {
			if (cbEnableLenCorrect.isSelected()) {
				labelLengthCorrecton.setEnabled(false);
				textfieldLenCorrection.setEnabled(true);
			} else {
				labelLengthCorrecton.setEnabled(true);
				textfieldLenCorrection.setEnabled(false);
			}
		} else {
			/**
			 * Default output if a action event occurs and no case covers the
			 * action.
			 */
			Debug.out
					.println("Undefined source in video data selection actionPerformed() ...");
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == textfieldDelimSymbol) {
			/**
			 * Analog handling to the key released case. But the key typed case
			 * only takes care about the backspace which deletes characters from
			 * the input delimiter text field.
			 * 
			 * Is case is responsible for deleting a character and refresh the
			 * output.
			 * 
			 * This leads to a better performance but not solves the problem
			 * (press backspace and symbol simultaneously)
			 */
			if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
				parseAction();
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// Nothing to do here
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getSource() == textfieldDelimSymbol) {
			/**
			 * Don't start parsing by non printable key events.
			 */
			if (!e.isActionKey() && (e.getKeyChar() != KeyEvent.VK_ENTER)
					&& (e.getKeyChar() != KeyEvent.VK_DELETE)
					&& (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED)
					&& (e.getKeyChar() != KeyEvent.VK_ESCAPE)
					&& ((e.getKeyChar() != KeyEvent.VK_BACK_SPACE))) {
				/**
				 * Consider this key released case wont accept the backspace,
				 * because this leads to an overrun with forces the lost of
				 * event listener on the table check boxes. The backspace key is
				 * moved to the key typed case. This solves the problem.
				 * 
				 * In other words this case is responsible for adding an
				 * character and refresh the output.
				 * 
				 * This leads to a better performance but not solves the problem
				 * (press backspace and symbol simultaneously)
				 */
				parseAction();
			}
		} else if (e.getSource() == textfieldLenCorrection) {
			/**
			 * Set the focus from the text field to an empty label to trigger
			 * the input verifier. In the next step set the focus back to the
			 * text field.
			 */
			labelEmpty.requestFocus();
			textfieldLenCorrection.requestFocus();
		}
	}

	/**
	 * Internal class for verifying the user input in the text field.
	 * 
	 * @author mbeck
	 * 
	 */
	private class TextFieldIntegerValueInputVerifier extends InputVerifier {
		/**
		 * Attributes for knowing the boundaries of the allowed integer
		 * interval.
		 */
		private int min = 0;
		private int max = 0;

		/**
		 * Constructor which expects the minimal and maximal allowed input.
		 * 
		 * @param minimum
		 *            The minimal allowed number.
		 * @param maximum
		 *            The maximal allowed number.
		 */
		public TextFieldIntegerValueInputVerifier(int minimum, int maximum) {
			min = minimum;
			max = maximum;
		}

		/**
		 * Verification function
		 */
		@Override
		public boolean verify(JComponent input) {
			/**
			 * Retrieve the text from the field.
			 */
			String text = ((JTextField) input).getText();
			boolean retval = false;
			try {
				/**
				 * Try to convert to integer
				 */
				int value = Integer.parseInt(text);
				if (value >= min && value <= max) {
					retval = true;
				}
			} catch (NumberFormatException e) {
				// Nothing to do here
			}
			/**
			 * If the input is not in the expected boundaries show the error
			 * message.
			 */
			if (retval == false) {
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				JOptionPane.showMessageDialog(null, "User input \""
						+ textfieldLenCorrection.getText()
						+ "\" is not a valid integer number\n"
						+ "or out of the allowed range [" + min + ", " + max
						+ "].", "Invalid input", JOptionPane.ERROR_MESSAGE);
			}
			return retval;
		}
	}

	/**
	 * Function from the interface INumberSelectedProfilesListener to update the
	 * view component to display the number of selected profiles.
	 * 
	 * This function will be invoked by the selection table object.
	 * 
	 * @param number
	 *            The new number of selected profiles.
	 */
	@Override
	public void numberSelectedProfilesChanged(int number) {
		labelValueSelectProfiles.setText("" + number);
	}
}
