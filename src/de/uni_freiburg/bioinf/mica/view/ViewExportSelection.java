package de.uni_freiburg.bioinf.mica.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;

/**
 * View for selecting profiles for export. This view uses the same profile
 * selection table as in the parse view. Over the table the user can select or
 * de select profiles. Also the user has the possibility to change the name of
 * the profile.
 * 
 * @author mbeck
 * 
 */
public class ViewExportSelection extends JDialog implements
		ISelectedProfilesListener, ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * Input set of available profiles for export
	 */
	private LinkedList<Curve> pset_input = null;
	private LinkedList<Curve> pset_align = null;
	private Curve p_cons = null;
	/**
	 * Button for confirm the export
	 */
	private JButton buttonExport = null;
	/**
	 * Labels for displaying information about the current selection
	 */
	private JLabel labelAllProfiles = null;
	private JLabel labelSelProfiles = null;
	/**
	 * Table which represents the profiles
	 */
	private SelectionTable selTable = null;
	/**
	 * Checkboxes for initial group selection of the export
	 */
	private JCheckBox checkboxInput = null;
	private JCheckBox checkboxAlign = null;
	private JCheckBox checkboxCons = null;
	/**
	 * Delimiter view components
	 */
	private JLabel labelDelimiter = null;
	private JTextField textfieldDelimiter = null;

	/**
	 * Constructor for this view which expects the parent view and the set of
	 * profiles.
	 * 
	 * @param parent
	 *            The parent view
	 * @param input
	 *            The set of input profiles
	 * @param align
	 *            The set of alignment result profiles, expect the consensus
	 *            itself
	 * @param cons
	 *            Is the consensus profile for export.
	 * @param colDelim column delimiter to be used as default
	 */
	public ViewExportSelection(Frame parent, LinkedList<Curve> input,
			LinkedList<Curve> align, Curve cons, String colDelim) {
		/**
		 * Set the filename to the window title.
		 */
		super(parent, "Profile selection for export",
				ModalityType.DOCUMENT_MODAL);
		/**
		 * Store the profile set
		 */
		pset_input = input;
		pset_align = align;
		p_cons = cons;
		/**
		 * Create an instance of the selection table
		 */
		selTable = new SelectionTable();
		/**
		 * Create instances of the view components
		 */
		buttonExport = new JButton("Export");
		/**
		 * Instances for the check boxes
		 */
		checkboxInput = new JCheckBox("Input");
		checkboxAlign = new JCheckBox("Alignment");
		checkboxCons = new JCheckBox("Consensus");
		/**
		 * Instances for the delimiter
		 */
		labelDelimiter = new JLabel("Delimiter: ");
		/**
		 * Set the default delimiter 
		 */
		textfieldDelimiter = new JTextField(colDelim);
		/**
		 * Instances for the labels
		 */
		labelAllProfiles = new JLabel("Number available profiles: " + 0);
		labelSelProfiles = new JLabel("Number selected profiles: " + 0);
		/**
		 * Disable the check boxes if the corresponding input is not available
		 */
		if (pset_input == null)
			checkboxInput.setEnabled(false);
		if (pset_align == null)
			checkboxAlign.setEnabled(false);
		if (p_cons == null)
			checkboxCons.setEnabled(false);
		/**
		 * Enable all per default
		 */
		if (pset_input != null)
			checkboxInput.setSelected(true);
		if (pset_align != null)
			checkboxAlign.setSelected(true);
		if (p_cons != null)
			checkboxCons.setSelected(true);

		/**
		 * Add listeners to the view components
		 */
		addListeners();
		/**
		 * Init the view
		 */
		initForm();
		/**
		 * Display the selection in the table
		 */
		displayInitialSelection();
	}

	/**
	 * Function displays the initial selection from the check boxes
	 */
	private void displayInitialSelection() {
		/**
		 * Get the profiles from the check box group selection for the table
		 */
		LinkedList<Curve> set = getProfilesForTable();
		/**
		 * Update the number of profiles
		 */
		labelAllProfiles.setText("Number available profiles: " + set.size());
		/**
		 * Init the table model with the profiles set
		 */
		selTable.initModel(set);
	}

	/**
	 * Function which collects according to the check box selection (input,
	 * alignment, consensus) all required profiles and store them into a set for
	 * the table
	 * 
	 * @return The set of the collected profiles
	 */
	private LinkedList<Curve> getProfilesForTable() {
		LinkedList<Curve> set = new LinkedList<Curve>();
		if (checkboxInput.isSelected())
			set.addAll(pset_input);
		if (checkboxAlign.isSelected())
			set.addAll(pset_align);
		if (checkboxCons.isSelected())
			set.add(p_cons);
		return set;
	}

	/**
	 * Add all needed listeners to the view components
	 */
	private void addListeners() {
		selTable.registerSelectedProfilesListener(this);
		buttonExport.addActionListener(this);
		checkboxAlign.addActionListener(this);
		checkboxCons.addActionListener(this);
		checkboxInput.addActionListener(this);
	}

	/**
	 * Function to init the view form
	 */
	private void initForm() {
		/**
		 * Set the properties of this frame.
		 */
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setMinimumSize(new Dimension(600, 500));
		this.setLocationRelativeTo(null);
		/**
		 * Add listener if the user closes the window with the x
		 */
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				/**
				 * Set the export attributes to null
				 */
				pset_input = null;
				pset_align = null;
				p_cons = null;
			}
		});

		/**
		 * Create the constraint object for the view.
		 */
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		JPanel panelTable = new JPanel(new GridLayout(1, 1));
		panelTable.add(selTable);

		JPanel panelCheckBox = new JPanel(new GridLayout(1, 3));
		panelCheckBox.setBorder(new TitledBorder(new EtchedBorder(),
				"Group selection"));
		panelCheckBox.add(checkboxInput);
		panelCheckBox.add(checkboxAlign);
		panelCheckBox.add(checkboxCons);

		JPanel panelDelimiter = new JPanel(new GridLayout(1, 2));
		panelDelimiter.setBorder(new TitledBorder(new EtchedBorder(),
				"Delimiter"));
		panelDelimiter.add(labelDelimiter);
		panelDelimiter.add(textfieldDelimiter);

		JPanel panelCheckDeli = new JPanel(new GridLayout(1, 2));
		panelCheckDeli.add(panelCheckBox);
		panelCheckDeli.add(panelDelimiter);

		JPanel panelControl = new JPanel(new GridLayout(1, 1));
		panelControl.setBorder(new TitledBorder(new EtchedBorder(), "Control"));
		panelControl.add(buttonExport);

		JPanel panelInfo = new JPanel(new GridLayout(2, 1));
		panelInfo.setBorder(new TitledBorder(new EtchedBorder(), "Info"));
		panelInfo.add(labelAllProfiles);
		panelInfo.add(labelSelProfiles);

		JPanel panelBottom = new JPanel(new GridLayout(1, 2));
		panelBottom.add(panelControl);
		panelBottom.add(panelInfo);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelTable, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.LAST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelCheckDeli, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.LAST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelBottom, gbc);
	}

	/**
	 * Function to get the selected profiles for export.
	 * 
	 * @return This set contains the profiles which are selected for export in
	 *         the same order as in the input profile set
	 */
	public LinkedList<Curve> getSelectedProfiles() {
		/**
		 * Create instance of the set for export
		 */
		LinkedList<Curve> selectedProfiles = new LinkedList<Curve>();
		LinkedList<Curve> pset = getProfilesForTable();
		/**
		 * Over all available profiles
		 */
		for (int i = 0; i < pset.size(); i++) {
			/**
			 * Check if the users selects for export
			 */
			if (selTable.isProfileSelected(i)) {
				Curve p = pset.get(i);
				
				/**
				 * Create a copy of the profile with the new updated profiles
				 * name. The data can be used 1:1 because these data wont be
				 * changed.
				 */
				selectedProfiles.add(new Curve(selTable.getProfileName(i), p.getY()));
			}
		}
		/**
		 * Return the set for export
		 */
		return selectedProfiles;
	}

	/**
	 * Function to get the delimiter specification by the user, if the user
	 * enters an empty delimiter in the text field this function returns the
	 * system default delimiter ; or ,
	 * 
	 * @return ; or , depending on the language of the system or the user input
	 */
	public String getDelimiter() {
		String textFieldDeli = textfieldDelimiter.getText();
		if (textFieldDeli.isEmpty()) {
			if (System.getProperty("user.language").equalsIgnoreCase("de")) {
				textFieldDeli = ";";
			} else {
				textFieldDeli = ",";
			}
		}
		return textFieldDeli;
	}

	/**
	 * Listener implementation if the user changes the profile selection
	 */
	@Override
	public void numberSelectedProfilesChanged(int number) {
		labelSelProfiles.setText("Number selected profiles: " + number);
	}

	/**
	 * Action performed listener implementation
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttonExport) {
			this.setVisible(false);
		} else if (arg0.getSource() == checkboxAlign
				|| arg0.getSource() == checkboxCons
				|| arg0.getSource() == checkboxInput) {
			displayInitialSelection();
		}
	}

}
