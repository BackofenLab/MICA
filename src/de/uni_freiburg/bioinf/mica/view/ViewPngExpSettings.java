package de.uni_freiburg.bioinf.mica.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

/**
 * This object shows an user dialog to specify a desired picture resolution and
 * a full path on the hard disk to store a picture file.
 * 
 * @author mbeck
 * 
 */
public class ViewPngExpSettings extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	/**
	 * Set with all available radios buttons. The buttons will be generated in
	 * the constructor.
	 */
	private LinkedList<JRadioButton> setRadioButtons = null;
	/**
	 * Set with dimension objects. The available dimensions will be generated in
	 * the constructor. Additional to that the last dimension is a user defined
	 * dimension which may changes.
	 */
	private LinkedList<Dimension> setDimensions = null;
	/**
	 * String which will be placed in front of the custom radio button.
	 */
	private String stringCustomRB = null;
	/**
	 * Custom radio button for an user defined resolution specification.
	 */
	private JRadioButton radiobuttonCustom = null;
	/**
	 * Button for selecting a filename for saving a picture.
	 */
	private JButton buttonSelectFile = null;
	/**
	 * Attribute which contains the result dimension.
	 */
	private Dimension resultDimension = null;
	/**
	 * Attribute which contains the result file name.
	 */
	private File resultFile = null;
	/**
	 * Radio buttons for different export types
	 */
	private JRadioButton radioButtonInput = null;
	private JRadioButton radioButtonAlignment = null;
	private JRadioButton radioButtonAlignCons = null;

	/**
	 * Constructor of this dialog. This constructor creates a set of default
	 * dimensions automatically.
	 * 
	 * @param parent
	 */
	public ViewPngExpSettings(Frame parent) {
		/**
		 * Mode less dialog
		 */
		super(parent, "PNG export", ModalityType.DOCUMENT_MODAL);

		/**
		 * Create a default set of allowed picture sizes
		 */
		setDimensions = new LinkedList<Dimension>();
		setDimensions.add(new Dimension(320, 240));
		setDimensions.add(new Dimension(600, 480));
		setDimensions.add(new Dimension(800, 600));
		setDimensions.add(new Dimension(1280, 720));
		setDimensions.add(new Dimension(1600, 1200));
		setDimensions.add(new Dimension(1920, 1080));
		setDimensions.add(new Dimension(2560, 1600));

		/**
		 * Create instances of the buttons on this view.
		 */
		buttonSelectFile = new JButton("Save file");

		/**
		 * Buttons for the source specification. Add them to an own group and
		 * select alignment and consensus for export by default.
		 */
		radioButtonInput = new JRadioButton("Input");
		radioButtonAlignment = new JRadioButton("Alignment");
		radioButtonAlignCons = new JRadioButton("Alignment + Consensus");
		ButtonGroup bgroupSource = new ButtonGroup();
		bgroupSource.add(radioButtonInput);
		bgroupSource.add(radioButtonAlignment);
		bgroupSource.add(radioButtonAlignCons);
		radioButtonAlignCons.setSelected(true);

		/**
		 * Create a button group for the radio buttons
		 */
		ButtonGroup buttongroup = new ButtonGroup();
		/**
		 * Create a set of radio buttons
		 */
		setRadioButtons = new LinkedList<JRadioButton>();
		/**
		 * Create instances of radio buttons from the dimensions set and add to
		 * the button group.
		 */
		for (Dimension d : setDimensions) {
			JRadioButton rb = new JRadioButton((int) d.getWidth() + " x "
					+ (int) d.getHeight());
			/**
			 * Select the first dimension by default.
			 */
			if (setRadioButtons.isEmpty()) {
				rb.setSelected(true);
			}
			setRadioButtons.add(rb);
			buttongroup.add(rb);
		}

		/**
		 * Create a custom dimension and add to set
		 */
		Dimension customDim = new Dimension(100, 100);
		setDimensions.add(customDim);
		/**
		 * Create a radio button for the custom dimension and add to the set
		 */
		stringCustomRB = new String("click for adjust ");
		radiobuttonCustom = new JRadioButton(stringCustomRB
				+ (int) customDim.getHeight() + " x "
				+ (int) customDim.getWidth());
		setRadioButtons.add(radiobuttonCustom);
		buttongroup.add(radiobuttonCustom);

		/**
		 * Init the dialog form with all components
		 */
		initForm();

		/**
		 * Add action listeners to some view components.
		 */
		addActionListeners();

	}

	/**
	 * Function to init the dialog
	 */
	private void initForm() {
		/**
		 * Set the properties of this dialog
		 */
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setMinimumSize(new Dimension(400, 300));
		this.setLocationRelativeTo(null);

		/**
		 * Create the constraint object for the view.
		 */
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		/**
		 * Create panel for the resolution radio buttons and add them to the
		 * dialog
		 */
		JPanel panelPicSize = new JPanel(new GridLayout(
				setRadioButtons.size() / 2, 2));
		panelPicSize.setBorder(new TitledBorder(new EtchedBorder(),
				"Picture size selection"));
		for (JRadioButton rb : setRadioButtons) {
			panelPicSize.add(rb);
		}
		JScrollPane scrpanePicSize = new JScrollPane(panelPicSize);
		scrpanePicSize
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		JPanel panelSource = new JPanel(new GridLayout(3, 1));
		panelSource.setBorder(new TitledBorder(new EtchedBorder(),
				"Plot source selection"));
		panelSource.add(radioButtonInput);
		panelSource.add(radioButtonAlignment);
		panelSource.add(radioButtonAlignCons);

		/**
		 * Create panel for the button or file name selection
		 */
		JPanel panelFileName = new JPanel(new GridLayout(1, 1));
		panelFileName.setBorder(new TitledBorder(new EtchedBorder(),
				"Select file and save"));
		panelFileName.add(buttonSelectFile);

		/**
		 * Add the panels to the frame with constraints
		 */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1;
		gbc.weighty = 0.8;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(scrpanePicSize, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelSource, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 0.1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		c.add(panelFileName, gbc);
	}

	/**
	 * Function to add all needed action listeners to the view components
	 */
	private void addActionListeners() {
		buttonSelectFile.addActionListener(this);
		radiobuttonCustom.addActionListener(this);
	}

	/**
	 * Function which can be invoked after this dialog closes. If the dialog was
	 * closed with a defined picture dimension the dimension will be returned.
	 * If the dialog was closed by the window x (cancel operation) this function
	 * will return null.
	 * 
	 * @return The dimension of the picture resolution or null.
	 */
	public Dimension getPictureExportSize() {
		return resultDimension;
	}

	/**
	 * Function which can be invoked after this dialog closes. If the dialog was
	 * closed with a defined picture file name the file name will be returned.
	 * If the dialog was closed by the window x this function will return null.
	 * 
	 * @return The file name of the picture export or null.
	 */
	public File getPictureExportFilepath() {
		return resultFile;
	}

	/**
	 * Function to get the selected source type information
	 * 
	 * @return Is the source type enumeration
	 * 
	 */
	public SourceType getSourceTypeSelection() {
		if (radioButtonInput.isSelected()) {
			return SourceType.INPUT;
		} else if (radioButtonAlignment.isSelected()) {
			return SourceType.ALIGN;
		} else if (radioButtonAlignCons.isSelected()) {
			return SourceType.ALIGNCONS;
		} else {
			return SourceType.NONE;
		}
	}

	/**
	 * Returns the selected source type as string short cut. This is internally
	 * used for default file name creation
	 * 
	 * @return The short cut string
	 */
	private String getSelectedSourceTypeString() {
		return getSourceTypeSelection().toString();
	}

	/**
	 * Overwritten function to handle the action performed events
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonSelectFile) {
			/**
			 * Ask the user for a filename
			 */
			FileDialog fd = new FileDialog(new Frame(),
					"Export solution to PNG", FileDialog.SAVE);
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
			fd.setFile(getSelectedSourceTypeString() + "-plot_"
					+ df.format(Calendar.getInstance().getTime()) + ".png");
			fd.setVisible(true);
			/**
			 * Valid file name was returned
			 */
			if (fd.getDirectory() != null && fd.getFile() != null) {
				/**
				 * Append the png file extension if missing.
				 */
				String file = fd.getDirectory() + fd.getFile();
				if (!file.endsWith(".png")) {
					file += ".png";
				}
				/**
				 * Create the result file object.
				 */
				resultFile = new File(file);
			}
			/**
			 * Check which radio button is selected and set the result dimension
			 */
			for (int i = 0; i < setRadioButtons.size(); i++) {
				if (setRadioButtons.get(i).isSelected()) {
					resultDimension = setDimensions.get(i);
					break;
				}
			}
			/**
			 * Check if some of the return values is null, then show error
			 * message.
			 */
			if (resultFile == null || resultDimension == null) {
				JOptionPane.setDefaultLocale(Locale.ENGLISH);
				JOptionPane.showMessageDialog(null,
						"Please define a file for saving the picture.",
						"Missing picture file name", JOptionPane.ERROR_MESSAGE);
			} else {
				/**
				 * In the other case close the dialog
				 */
				this.setVisible(false);
			}
		} else if (e.getSource() == radiobuttonCustom) {
			/**
			 * Radio button was customization was pressed. Show dialog for user
			 * defined resolution input.
			 */
			String dialogRet = (String) JOptionPane
					.showInputDialog(null, "Please enter a valid resolution.",
							"Customized resolution",
							JOptionPane.INFORMATION_MESSAGE, null, null,
							(int) setDimensions.getLast().getHeight() + " x "
									+ (int) setDimensions.getLast().getHeight());
			
			// check if dialog was aborted or nothing entered
			if (dialogRet != null && ! dialogRet.trim().isEmpty()) {
				/**
				 * Remove all white spaces from the user input
				 */
				dialogRet = dialogRet.replaceAll("\\s", "");
				/**
				 * User the x symbol as delimiter to split the height and with
				 * value.
				 */
				String[] resolution = dialogRet.split("x");
				/**
				 * We expect at this point exactly two input values
				 */
				if (resolution.length == 2) {
					int w = -1;
					int h = -1;
					/**
					 * Try to parse the input to integer values.
					 */
					try {
						w = Integer.parseInt(resolution[0]);
						h = Integer.parseInt(resolution[1]);
					} catch (NumberFormatException ex) {
						// ex.printStackTrace();
					}
					/**
					 * If the input height and with is in a valid range (larger than
					 * 0) adapt the radio button and the dimension object which is
					 * responsible for customization
					 */
					if (w >= 1 && h >= 1) {
						setDimensions.getLast().setSize(w, h);
						radiobuttonCustom.setText(stringCustomRB + "" + w + " x "
								+ h);
					} else {
						/**
						 * The input is not valid, show error message and keep the
						 * previous costomization values.
						 */
						JOptionPane.setDefaultLocale(Locale.ENGLISH);
						JOptionPane.showMessageDialog(null,
								"Corrupt resolution input.", "Error",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

	/**
	 * Enumeration which defines the input source plot
	 * 
	 * @author mbeck
	 * 
	 */
	public enum SourceType {
		/**
		 * INPUT defines a plot containing only the input profiles
		 * 
		 * ALIG defines a plot containing only the result warped profiles
		 * 
		 * ALIGNCONS defines a plot containing the result with the consensus
		 * 
		 * NONE no source type is selected
		 */
		INPUT, ALIGN, ALIGNCONS, NONE
	}
}
