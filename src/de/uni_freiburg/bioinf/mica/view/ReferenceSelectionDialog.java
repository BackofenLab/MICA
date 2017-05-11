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

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * View for reference profile selection for RMICA/PRMICA execution
 * 
 * @author mbeck
 * 
 */
public class ReferenceSelectionDialog extends JDialog implements
		ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * Button for use the selected reference profile
	 */
	private JButton buttonProceed = null;
	/**
	 * Set which contains the selection possibilities
	 */
	private LinkedList<AbstractButton> selectionSet = null;

	/**
	 * Constructor of the reference profile selection view
	 * 
	 * @param parent
	 *            Reference to the parent frame for setting the correct view
	 *            modality type.
	 * @param profiles
	 *            Set of profile names, all entries in this set are used to init
	 *            the radio buttons for reference profile selection
	 * @param multiple
	 *            If true more than one an be selected, if false only one can be
	 *            selected.
	 */
	public ReferenceSelectionDialog(Frame parent,
			LinkedList<String> profiles, boolean multiple) {
		super(parent, "Select reference profile", ModalityType.DOCUMENT_MODAL);

		/**
		 * Create a set of radio buttons according to the input profile name set
		 */
		selectionSet = new LinkedList<AbstractButton>();
		ButtonGroup bg = new ButtonGroup();
		for (String s : profiles) {
			AbstractButton ab = null;
			if (multiple) {
				ab = new JCheckBox(s);
			} else {
				ab = new JRadioButton(s);
			}
			selectionSet.add(ab);
			if (!multiple) {
				bg.add(ab);
			}
		}
		/**
		 * Select the first entry in the set
		 */
		if (!selectionSet.isEmpty()) {
			selectionSet.getFirst().setSelected(true);
		}
		/**
		 * Button for proceed with the current reference profile selection
		 */
		buttonProceed = new JButton("Proceed");

		/**
		 * Add listeners to view components.
		 */
		addListeners();
		/**
		 * Init the view form
		 */
		initForm();
	}

	/**
	 * If a profile was selected by the user as reference profile the return
	 * value will be in the range of 0 until the number of available profiles.
	 * If the user selects no profile the value will be null.
	 * 
	 * @return null if no selection was performed, otherwise the index/indices
	 *         of the profile/s in the profile set.
	 */
	public LinkedList<Integer> getSelectedReferenceProfileIndices() {
		LinkedList<Integer> selection = null;
		/**
		 * Search for the selected radio button and return the index
		 */
		for (int i = 0; i < selectionSet.size(); i++) {
			if (selectionSet.get(i).isSelected()) {
				/**
				 * Check if the return set is null, than create the return set
				 */
				if (selection == null)
					selection = new LinkedList<Integer>();
				selection.add(i);
			}
		}
		/**
		 * Return null if nothing is selected or the selection set with indices
		 */
		return selection;
	}

	/**
	 * Sets every radio button to deselect
	 */
	private void deselectAll() {
		for (AbstractButton jrb : selectionSet) {
			jrb.setSelected(false);
		}
	}

	/**
	 * Function to add the necessary action listeners to the view components
	 */
	private void addListeners() {
		/**
		 * Add window listener to restore the backup filter value if the window
		 * dispose action was called (Dialog x in the upper right corner)
		 */
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				deselectAll();
			}
		});
		/**
		 * Action listener for proceed button
		 */
		buttonProceed.addActionListener(this);
	}

	/**
	 * Function to init the view form
	 */
	private void initForm() {
		/**
		 * Settings of the window frame
		 */
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLayout(new GridLayout(2, 1));
		this.setMinimumSize(new Dimension(500, Math.max(300,
				selectionSet.size() * 10)));
		this.setLocationRelativeTo(null);

		/**
		 * Create the constraint object for the view.
		 */
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		/**
		 * Create three parts of sub panels with radio buttons. determine the
		 * number of radio buttons in each panel
		 */
		final int parts = 3;
		int divider = selectionSet.size() / parts;
		/**
		 * Check if we need to enhance the divider value to store all radio
		 * buttons in the available cells
		 */
		if (divider * parts != selectionSet.size()) {
			divider++;
		}

		/**
		 * Panel which contains the selection radio buttons
		 */
		JPanel panelRadio = new JPanel(new GridLayout(1, parts));
		panelRadio.setBorder(new TitledBorder(new EtchedBorder(),
				"Reference profile selection"));
		/**
		 * Create instances of the sub panels
		 */
		LinkedList<JPanel> panelParts = new LinkedList<JPanel>();
		for (int i = 0; i < parts; i++) {
			panelParts.add(new JPanel(new GridLayout(divider, 1)));
		}
		/**
		 * Attributes for the remaining radio buttons of the current sub panel,
		 * and the current panel selection.
		 */
		int remain = divider;
		int currPart = 0;
		/**
		 * Add the radio buttons to the panels
		 */
		for (int i = 0; i < selectionSet.size(); i++) {
			/**
			 * Add the radio button to the current selected sub panel
			 */
			panelParts.get(currPart).add(selectionSet.get(i));
			/**
			 * Update the remain value
			 */
			remain--;
			/**
			 * Jump to the next sub panel
			 */
			if (remain == 0) {
				remain = divider;
				currPart++;
			}
		}
		/**
		 * All sub panels to the radio main panel
		 */
		for (int i = 0; i < parts; i++) {
			panelRadio.add(panelParts.get(i));
		}

		/**
		 * Add the main radio panel and the button to the view
		 */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0.9;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelRadio, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(buttonProceed, gbc);
	}

	/**
	 * Function to react on action performed events
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttonProceed) {
			/**
			 * Disable the view if proceed button is pressed and keep the
			 * selection in storage
			 */
			this.setVisible(false);
		}
	}

}
