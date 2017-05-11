package de.uni_freiburg.bioinf.mica.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

public class DoubleParameterPanel extends JPanel implements ChangeListener<Number>, javax.swing.event.ChangeListener  {

	private static final long serialVersionUID = 1L;
	
	private DoubleParameter parameter;
	
	private JLabel label = new JLabel();
	
	private JSpinner doubleInput = new JSpinner();
	
	public DoubleParameterPanel( DoubleParameter parameter ) {
		super();
		
		this.parameter = parameter;
		
		// register as change listener
		this.parameter.addListener(this);
		
		String toolTip = "<html>" 
				+ parameter.getDescription()
				+ "<br>"
				+ "The default value is " + String.valueOf(parameter.get())
				+ "</html>";
		
		// setup label
		label.setText( parameter.getName() + ":" );
		label.setToolTipText( toolTip );
		
		// setup number field
		
		// register as listener
		doubleInput.addChangeListener(this);
		doubleInput.setModel( new SpinnerNumberModel( parameter.get(), parameter.getRange().getMin(), parameter.getRange().getMax(), parameter.getStepWidth()));
		((JSpinner.DefaultEditor)doubleInput.getEditor()).getTextField().setColumns(6);
		doubleInput.setToolTipText( toolTip );
		
		// setup layout
		this.setLayout( new GridBagLayout() );
		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.weighty = 0;
		this.add( label, c );
		
		c.gridx = 1;
		c.weightx = 0;
		this.add( doubleInput, c );
	}


	@Override
	public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
		// check source of change and that a change happened
		if ( observable.equals(parameter) && !oldValue.equals(newValue) ) {
			doubleInput.setValue( (Double)newValue );
		}
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		parameter.set( (Double)doubleInput.getValue());
	}

}