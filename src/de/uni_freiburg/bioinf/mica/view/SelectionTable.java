package de.uni_freiburg.bioinf.mica.view;

import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import de.uni_freiburg.bioinf.mica.algorithm.Curve;
import de.uni_freiburg.bioinf.mica.controller.Debug;

/**
 * Class SelectionTable which is an ordinary JPanel. This panel contains
 * automatically an JScrollPane and an JTable with an implemented model. The
 * table uses an special table header renderer to provide a header vector with
 * check boxes. Additional this object registers an mouse listener to the header
 * fields to provide check box selection and header string changing.
 * 
 * @author mbeck
 * 
 */
public class SelectionTable extends JPanel implements MouseListener,
		ChangeListener {
	private static final long serialVersionUID = 1L;

	/**
	 * Instances of the Table, the model and the renderer
	 */
	private JTable t = null;
	private TModel tm = null;
	private LinkedList<ChechBoxTableHdrRenderer> hdrRenderer = null;
	private JScrollPane sp = null;
	private ISelectedProfilesListener selectionListener = null;
	
	//! the set of curves currently visualized
	protected LinkedList<Curve> visualizedProfileSet = null;

	/**
	 * Constructor which init the layout to grid layout with one element.
	 */
	public SelectionTable() {
		super(new GridLayout(1, 1));
	}

	/**
	 * Function to register a listener to get the number of selected profiles.
	 * 
	 * @param l
	 *            The listener which receives the number of selected profiles.
	 */
	public void registerSelectedProfilesListener(ISelectedProfilesListener l) {
		if (selectionListener != null)
			Debug.out
					.println("Already registered selection listener detected!");
		selectionListener = l;
	}

	/**
	 * High level model initialization. This function needs a set of profiles
	 * and translates the set into a static table content.
	 * 
	 * @param profileSet
	 *            The set of profiles
	 */
	public void initModel(LinkedList<Curve> profileSet) {
		
		//! store set for later access
		visualizedProfileSet = profileSet;
		
		/**
		 * Determine the maximum number of needed rows.
		 */
		int maxRows = 0;
		for (Curve p : profileSet) {
			if (p.size() > maxRows) {
				maxRows = p.size();
			}
		}
		/**
		 * Create static table content.
		 */
		Object[][] data = new Object[maxRows][profileSet.size()];
		Object[] hdr = new Object[profileSet.size()];
		/**
		 * Fill static table content
		 */
		for (int i = 0; i < profileSet.size(); i++) {
			Curve tmp = profileSet.get(i);
			hdr[i] = tmp.getName();
			for (int j = 0; j < tmp.size(); j++) {
				data[j][i] = tmp.getY()[j];
			}
		}

		initModel(hdr, data);
	}

	/**
	 * Access to the currently visualized set of curves
	 * @return the visualized set of curves
	 */
	public LinkedList<Curve> getVisualizedProfileSet() {
		return visualizedProfileSet;
	}

	/**
	 * Function to set or overwrite the table data. Therefore the header vector
	 * and the data matrix is needed.
	 * 
	 * @param h
	 *            The header vector which normally contains string information.
	 * @param d
	 *            The data matrix of the profiles which are normally double
	 *            values.
	 */
	private void initModel(Object h[], Object d[][]) {
		/**
		 * Create instances of the local objects.
		 */
		t = new JTable();
		tm = new TModel(h, d);
		t.setModel(tm);
		t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		/**
		 * Add the mouse listener to the table header.
		 */
		t.getTableHeader().addMouseListener(this);
		/**
		 * Create a set of table header renderer objects
		 */
		hdrRenderer = new LinkedList<ChechBoxTableHdrRenderer>();
		/**
		 * Create for each column in the table an own table header renderer
		 * object. Also register for each of the check box in the header a
		 * change listener.
		 */
		for (int i = 0; i < tm.getColumnCount(); i++) {
			ChechBoxTableHdrRenderer cb = new ChechBoxTableHdrRenderer();
			/**
			 * Change listener needed, because if the user moves the mouse and
			 * clicks during the movement at some check box in the header, the
			 * internal check box state changed but the
			 * ISelectedProfilesListener can't be informed about the change.
			 */
			cb.addChangeListener(this);
			hdrRenderer.add(cb);
		}
		/**
		 * Update the selection label value to the initial number of selected
		 * profiles.
		 */
		informSelectedProfilesListener();
		/**
		 * Register the created table header renderer
		 */
		registerHeaderRenderer();
		/**
		 * Check if a scroll pane already exists. Then remove from panel. Add
		 * the table to a scroll pane
		 */
		if (sp != null) {
			remove(sp);
		}
		sp = new JScrollPane(t);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(sp);

		/**
		 * Use the implementation of the mouse motion adapter, because using the
		 * function getTableHeader().setReorderingAllowed(false) disables the
		 * mouse clicked events.
		 */
		t.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				if (t.getTableHeader().getDraggedColumn() != null) {
					/**
					 * Set the drag action to null.
					 */
					t.getTableHeader().setDraggedColumn(null);

					// Only one action possible, because MouseMotionAdapter
					// doesn't support mouse button differentiation.
					int colIndex = t.columnAtPoint(e.getPoint());
					ChechBoxTableHdrRenderer cbthr = (ChechBoxTableHdrRenderer) t
							.getColumnModel().getColumn(colIndex)
							.getHeaderRenderer();
					cbthr.setSelected(!cbthr.isSelected());

				}
			}
		});
	}

	/**
	 * Function to register or re register the JChechBoxTableHeaderRenderer
	 * components to the table.
	 */
	public void registerHeaderRenderer() {
		for (int i = 0; i < hdrRenderer.size(); i++) {
			t.getColumnModel().getColumn(i)
					.setHeaderRenderer(hdrRenderer.get(i));
		}
	}

	/**
	 * Function to return the profile selection by the user.
	 * 
	 * @param profileIndex
	 *            Index of the profile in the table column.
	 * @return True if the profile is selected by the user, otherwise false.
	 */
	public boolean isProfileSelected(int profileIndex) {
		/**
		 * Retrieve the header renderer component.
		 */
		return hdrRenderer.get(profileIndex).isSelected();
	}

	/**
	 * Function to inform all registered listeners with the actual number of
	 * selected profiles.
	 */
	private void informSelectedProfilesListener() {
		/**
		 * Determine the number of selected profiles
		 */
		int selected = 0;
		for (ChechBoxTableHdrRenderer cb : hdrRenderer) {
			if (cb.isSelected()) {
				selected++;
			}
		}
		/**
		 * Inform the registered listener.
		 */
		if (selectionListener != null)
			selectionListener.numberSelectedProfilesChanged(selected);
	}

	/**
	 * Function to return the name of a profile which may be changed by user
	 * interaction.
	 * 
	 * @param profileIndex
	 *            Is the index of the selected profile in the table column.
	 * @return Is the name of the profile from the table.
	 */
	public String getProfileName(int profileIndex) {
		return tm.getColumnName(profileIndex);
	}

	/**
	 * Inform the registered ISelectedProfilesListener if the state of the check
	 * box changes.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		// Information of the listeners moved from the mouse clicked event
		// (MouseEvent.BUTTON1) to the change listener event to cover also the
		// click event during a mouse motion.
		informSelectedProfilesListener();
	}

	/**
	 * Function to react to the user action on the table header. Supported
	 * actions are left mouse click to select the check box in the header and
	 * right click to change the name of the header cell.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {

		/**
		 * get the position of the clicked column
		 */
		int colIndex = t.columnAtPoint(e.getPoint());
		/**
		 * Retrieve the header renderer component.
		 */
		ChechBoxTableHdrRenderer cbthr = (ChechBoxTableHdrRenderer) t
				.getColumnModel().getColumn(colIndex).getHeaderRenderer();

		if (e.getButton() == MouseEvent.BUTTON1) {
			/**
			 * Left mouse button action. Toggle the selection of the check box.
			 */
			cbthr.setSelected(!cbthr.isSelected());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			/**
			 * Right mouse button action. Ask the user for a new name for the
			 * column.
			 */
			String dialogTitle = "Profile name change";
			String dialogMsg = "Please enter a new profile name.";
			String dialogRet = (String) JOptionPane.showInputDialog(null,
					dialogMsg, dialogTitle, JOptionPane.INFORMATION_MESSAGE,
					null, null, cbthr.getText());
			/**
			 * Only store the new name if not empty
			 */
			if (dialogRet != null) {
				if (!dialogRet.isEmpty()) {
					/**
					 * Update the column name in the selected cell.
					 */
					tm.changeColName(colIndex, dialogRet);
					/**
					 * Fire a table content changed event.
					 */
					tm.fireTableStructureChanged();
					/**
					 * Register the header renderer again, because of the
					 * structured changed firing event.
					 */
					registerHeaderRenderer();
				}
			}
		}
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

	private class TModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		/**
		 * Table content, header and data
		 */
		private Object[] hdr = null;
		private Object[][] data = null;

		/**
		 * Constructor for storing the table content to the model.
		 * 
		 * @param h
		 * @param d
		 */
		public TModel(Object[] h, Object[][] d) {
			hdr = h;
			data = d;
		}

		/**
		 * Function to update a header name. If the index is out of range of the
		 * header noting will happens.
		 * 
		 * @param col
		 *            The index of the header entry to change.
		 * @param name
		 *            The new name of the header entry.
		 */
		public void changeColName(int col, String name) {
			if (col < hdr.length || col >= 0) {
				hdr[col] = name;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			return (String) hdr[columnIndex];
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return hdr.length;
		}

		/**
		 * Function returns null if no data value can be selected in the table.
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Object o = null;
			try {
				/**
				 * Try to access the data cell
				 */
				o = data[rowIndex][columnIndex];
			} catch (Exception e) {
				// nothing to do here.
			}
			return o;
		}

		/**
		 * This functions disables the editable of the table cells.
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}
}
