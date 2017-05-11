package de.uni_freiburg.bioinf.mica.view;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Header renderer which is constructed by the JCheckBox blueprint. Additional
 * to that the table cell renderer is implemented at this object.
 * 
 * @author mbeck
 * 
 */
public class ChechBoxTableHdrRenderer extends JCheckBox implements
		TableCellRenderer {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor which sets the check box selection to true.
	 */
	public ChechBoxTableHdrRenderer() {
		/**
		 * Set the check box by default to selected.
		 */
		setSelected(true);
	}

	/**
	 * Overwritten function from the table cell renderer interface. This
	 * function sets the profile name from the table header vector and a tool
	 * tip message to the table header cell.
	 */
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int rowIndex, int vColIndex) {
		setToolTipText("<html>" + "<b>Left mouse button</b>" + "<br>"
				+ "<i>Selects or deselects a profile.</i>" + "<br>"
				+ "<b>Right mouse button</b>" + "<br>"
				+ "<i>Opens dialog to change the profile name.</i>" + "</html>");
		/**
		 * Set only the check box text from the table header.
		 */
		setText(value.toString());
		return this;
	}
}
