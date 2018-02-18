package de.uni_freiburg.bioinf.mica.view;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import de.uni_freiburg.bioinf.mica.model.ImportExport;
import de.uni_freiburg.bioinf.mica.model.ImportExport.OutType;

/**
 * View to select what data is to be exported to CSV.
 * 
 * @author mbeck, mmann
 * 
 */
public class ViewCsvExpSettings extends JDialog 
	implements ActionListener 
{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Represents and enables the selection of an OutType
	 * 
	 * @author Mmann
	 *
	 */
	protected class OutTypeSelection extends JPanel {
		
		private static final long serialVersionUID = 1L;
		
		//! button to select OutNone
		private JRadioButton outNone = new JRadioButton("none");
		//! button to select OutX
		private JRadioButton outX = new JRadioButton("X");
		//! button to select OutY
		private JRadioButton outY = new JRadioButton("Y");
		//! button to select OutXY
		private JRadioButton outXY = new JRadioButton("X+Y");
		//! grouping of the radiobuttons
		private ButtonGroup outTypeButtons = new ButtonGroup();
		
		/**
		 * Constructs buttons to select an OutType and selects for the 
		 * given initial setting.
		 * @param initialSelection the OutType to select initially
		 */
		public OutTypeSelection( ImportExport.OutType initialSelection ) {
			// new panel with layout
			super( new GridLayout(1,4) );
			
			// setup layout
			this.add(outX);
			this.add(outY);
			this.add(outXY);
			this.add(outNone);
			
			// setup radiobuttons
			outTypeButtons.add(outX);
			outTypeButtons.add(outY);
			outTypeButtons.add(outXY);
			outTypeButtons.add(outNone);
			
			// initial selection
			setOutTypeSelection(initialSelection);
		}
		
		/**
		 * Selects the button for the given OutType.
		 * @param selection the OutType to select 
		 */
		public void setOutTypeSelection(OutType selection) {
                    // select the according button
                    switch (selection) {
                        case OutNone:   outNone.setSelected(true);  break;
                        case OutX:	outX.setSelected(true);     break;
                        case OutXY:	outXY.setSelected(true);    break;
                        case OutY:	outY.setSelected(true);     break;
                        default: 	outNone.setSelected(true);  break;
                    }
		}
		
		/**
		 * Provides information what OutType was selected.
		 * @return the OutType for the selected button 
		 */
		public
		OutType
		getOutTypeSelection () {
			// check which button was selected
			if (outX.isSelected()) { return OutType.OutX; }
			if (outY.isSelected()) { return OutType.OutY; }
			if (outXY.isSelected()) { return OutType.OutXY; }
			return OutType.OutNone;
		}
		
		/**
		 * Dis-/enables also the displayed buttons.
		 */
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			outX.setEnabled(enabled);
			outY.setEnabled(enabled);
			outXY.setEnabled(enabled);
			outNone.setEnabled(enabled);
		}
	}
	
	/**
	 * JTextField to input a single character that is no whitespace.
	 * 
	 * @author Mmann
	 *
	 */
	public class DelimiterField extends JTextField {
		
		private static final long serialVersionUID = 1L;

		/**
		 * Empty constructor that initializes the delimiter with ';'.
		 */
		public DelimiterField() {
			super(";", 2);
		}

		/**
		 * Constructor with initial delimiter
		 * @param initialDelimiter the initial delimiter to show
		 */
	    public DelimiterField( char initialDelimiter ) {
	        super(String.valueOf(initialDelimiter));
	    }

	    @Override
	    protected Document createDefaultModel() {
	        return new DelimiterDocument();
	    }

	    private class DelimiterDocument extends PlainDocument {

			private static final long serialVersionUID = 1L;

			@Override
	        public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
	        	
	        	// ignore empty strings
	            if (str == null) return;
	            // ignore whitespaces
	            if (str.matches("^\\s$")) return;
	            // ignore too long inputs
	            if ((getLength() + str.length()) <= 1) {
	                super.insertString(offset, str, attr);
	            }
	        }       

	    }

	}
	
	
	//! OutType selection buttons for input curves
	protected OutTypeSelection inputOutTypeSelection = new OutTypeSelection( OutType.OutY );
	//! OutType selection buttons for the alignment's curves
	protected OutTypeSelection alignmentOutTypeSelection = new OutTypeSelection( OutType.OutX );
	//! OutType selection buttons for the alignment's consensus curve
	protected OutTypeSelection alignmentConsensusOutTypeSelection = new OutTypeSelection( OutType.OutXY );
	
	//! Input field for delimiter value
	private DelimiterField textfieldDelimiter = new DelimiterField();

        
	//! Button to confirm the export
	private JButton buttonExport = new JButton("Export CSV file");
	//! Button to abort the export
	private JButton buttonAbort = new JButton("Cancel");
	
	//! whether or not the export dialog was aborted or normally closed
	private boolean exportAborted = false;

	/**
	 * Constructor for this view which expects the parent view and the set of
	 * profiles.
	 * 
	 * @param parent
	 *            The parent view
	 * @param alignmentAvailable
	 *            whether or not alignment data is available
	 * @param colDelim column delimiter to be used as default
	 */
	public ViewCsvExpSettings(Frame parent, boolean alignmentAvailable, char colDelim ) {
		//! dialog title
		super(parent, "CSV export setup",
				ModalityType.DOCUMENT_MODAL);
		
		// disable alignment data output if not available
		if ( ! alignmentAvailable ) {
			alignmentOutTypeSelection.setOutTypeSelection( OutType.OutNone );
			alignmentOutTypeSelection.setEnabled( false );
			alignmentConsensusOutTypeSelection.setOutTypeSelection( OutType.OutNone );
			alignmentConsensusOutTypeSelection.setEnabled( false );
		}
		
		// set delimiter
		textfieldDelimiter.setText(String.valueOf(colDelim));

		// register button listener
		buttonExport.addActionListener(this);
		buttonAbort.addActionListener(this);
		
		// layout the dialog
		initForm();
	}


	/**
	 * Initializes the dialog layouting
	 */
	private void initForm() {
		
		// Set the properties of this frame.
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setMinimumSize(new Dimension(400, 200));
                this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		// Add listener if the user closes the window with the [x]
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// store that the export was aborted
				exportAborted = true;
			}
		});

		// set layout
		Container c = this.getContentPane();
		c.setLayout(new GridBagLayout());
		
		JPanel curPanel = new JPanel( new GridBagLayout() );
		// layout setup panel 
		GridBagConstraints gbcLabel = new GridBagConstraints(
				GridBagConstraints.RELATIVE
				, GridBagConstraints.RELATIVE
				, 1
				, 1
				, 0.3, 0.0
				, GridBagConstraints.CENTER
				, GridBagConstraints.HORIZONTAL
				, new Insets(0, 0, 0, 0), 0, 0);
                
		GridBagConstraints gbcInput = new GridBagConstraints(
				GridBagConstraints.RELATIVE
				, GridBagConstraints.RELATIVE
				, GridBagConstraints.REMAINDER
				, 1
				, 0.7, 0.0
				, GridBagConstraints.WEST
				, GridBagConstraints.NONE
				, new Insets(5, 10, 0, 10), 0, 0);
             
		// delimiter
		curPanel.add( new JLabel("Column delimiter:", JLabel.RIGHT), gbcLabel );
		curPanel.add( textfieldDelimiter, gbcInput );
		
		// output type selection
		curPanel.add( new JLabel("Input curves:", JLabel.RIGHT), gbcLabel );
		curPanel.add( inputOutTypeSelection, gbcInput );
		curPanel.add( new JLabel("Alignment curves:", JLabel.RIGHT), gbcLabel );
		curPanel.add( alignmentOutTypeSelection, gbcInput );
		curPanel.add( new JLabel("Alignment consensus:", JLabel.RIGHT), gbcLabel );
		curPanel.add( alignmentConsensusOutTypeSelection, gbcInput );
		
		// add panel to dialog
		c.add( curPanel, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0
								, GridBagConstraints.CENTER
								, GridBagConstraints.BOTH
								, new Insets(0, 0, 0, 0), 0, 0) );
		
		// layout button panel 
		curPanel = new JPanel( new GridBagLayout() );
		// buttons 
		curPanel.add( buttonExport );
		curPanel.add( buttonAbort );
		// add panel to dialog
		c.add( curPanel, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
								, GridBagConstraints.CENTER
								, GridBagConstraints.HORIZONTAL
								, new Insets(0, 0, 10, 0), 10, 10) );
		
		// trigger layouting of components
		this.validate();
		
	}
	
	/**
	 * Whether or not the dialog was aborted
	 * 
	 * @return true if the dialog was aborted; false otherwise (= export data ok)
	 */
	public boolean isAborted() {
		return exportAborted;
	}

	/**
	 * Access to the OutType selection for the input curves
	 * @return OutType for the input curves
	 */
	public OutType getOutTypeInput() {
		return inputOutTypeSelection.getOutTypeSelection();
	}

	/**
	 * Access to the OutType selection for the input curves
	 * @return OutType for the input curves
	 */
	public OutType getOutTypeAlignment() {
		return alignmentOutTypeSelection.getOutTypeSelection();
	}
	
	/**
	 * Access to the OutType selection for the input curves
	 * @return OutType for the input curves
	 */
	public OutType getOutTypeAlignmentConsensus() {
		return alignmentConsensusOutTypeSelection.getOutTypeSelection();
	}
	
	/**
	 * Function to get the delimiter specification by the user, if the user
	 * enters an empty delimiter in the text field this function returns the
	 * default delimiter ;
	 * 
	 * @return ; or depending on the user input
	 */
	public char getDelimiter() {
		String textFieldDeli = textfieldDelimiter.getText();
		if (textFieldDeli.isEmpty()) {
			textFieldDeli = ";";
		}
		return textFieldDeli.charAt(0);
	}


	/**
	 * Action performed listener implementation
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == buttonExport) {
			this.setVisible(false);
			exportAborted = false;
		} else if (arg0.getSource() == buttonAbort) {
			this.setVisible(false);
			exportAborted = true;
		}
	}

}
