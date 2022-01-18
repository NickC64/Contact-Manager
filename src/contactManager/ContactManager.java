/*
 * This program has all the features from Contact Manager v2, except plotted on a GUI.
 * Notable additional features include the ability to select and delete multiple contacts, and a live search filter.
 * 
 *  Author: Minghan Chen, 2022
 */

package contactManager;

import java.awt.*;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.util.Arrays;

/**
 * Implements the GUI of the program
 * 
 * @version 2022/01/17
 * 
 * @author Minghan Chen
 */

public class ContactManager extends JFrame {
	
	// JComponents representing sections of the frame
	JMenuBar menuBar;
	JPanel topPanel;
	JButton editButton;
	JTextField searchField;
	JScrollPane tablePane;
	JTable table;
	ContactTableModel tableModel;
	TableRowSorter<ContactTableModel> tableSorter;
	JPanel bottomPanel;
	JButton deleteButton;
	JButton addButton;
	
	// Other variables
	int filterColumn;
	static final Vector<String> tableColumnNames = new Vector<String>(Arrays.asList(new String[] {"First Name", "Last Name", "Phone Number", "Email", "Address"}));
	static final String FILE_SAVE_PATH = "SavedContacts.txt";
	 
	public static void main(String[] args) {
		new ContactManager();
	}
	
	/* Initiates GUI components */
	public ContactManager() {
		
		/* Menu Bar */
		
		String[][] menuOptions = {
				{"File", "Save", "Load", "Clear Table", "Quit"}, 
				{"Options", "Draggable Columns", "Set Background Colour"}
		};
		
		menuBar = new JMenuBar();
		
		for (int i = 0; i < menuOptions.length; i++) {
			
			JMenu menu = new JMenu(menuOptions[i][0]);
			
			// Generate menu item given information from array, then adds action listener and adds to the menu
			for (int j = 1; j < menuOptions[i].length; j++) {
				
				JMenuItem menuItem = new JMenuItem(menuOptions[i][j]);
				
				// Add toggle checkmark to "Draggable Columns"
				if (i == 1 && j == 1) {
					
					menuItem = new JCheckBoxMenuItem(menuOptions[i][j], true);
				}
				
				menuItem.addActionListener(new MenuListener());
				menu.add(menuItem);
			}
			
			menuBar.add(menu);
		}
		
		setJMenuBar(menuBar);
		
		/* Table/Center Panel */ 
		
		// Instantiate table with column names and no rows
		tableModel = new ContactTableModel(tableColumnNames, 0);
		table = new JTable(tableModel);
		
		// Create listener for table
		ListSelectionModel tableSelectionModel = table.getSelectionModel();
		tableSelectionModel.addListSelectionListener(new TableSelectionListener());
		
		// Create default sorting and filtration system
		tableSorter = new TableRowSorter<ContactTableModel>(tableModel);
		table.setRowSorter(tableSorter);
		
		// Automatically manage table layout and scrolling
		tablePane = new JScrollPane(table);
		add(BorderLayout.CENTER, tablePane);
		table.setFillsViewportHeight(true);
		
		// Configure table header size and attempt to change cursor icon on hover (to indicate sorting functionality)
		JTableHeader tableHeader = table.getTableHeader();
		try {
			
			Image cursorImage = ImageIO.read(this.getClass().getClassLoader().getResourceAsStream("rsc/CursorIcon.png"));	// Required to access image in a JAR
			tableHeader.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0,0), null));
		} catch (Exception e) {
			
			System.out.println("Unable to load custom cursor");
			e.printStackTrace();
		}
		tableHeader.setToolTipText("Sort alphabetically");
		tableHeader.setPreferredSize(new Dimension(tablePane.getWidth(), 25));
		tableHeader.setFont(new Font(tableHeader.getFont().getName() , Font.PLAIN, 13));
		
		/* Top Panel */
		
		topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
		
		// Attempt to load icon for edit button
		try {
			
			Image editImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream("rsc/EditIcon.png"));
			ImageIcon editIcon = new ImageIcon(editImage);
			editButton = new JButton("Edit", editIcon);
		} catch (Exception e) {
			
			// If not successful, create edit bnutton without icon
			editButton = new JButton("Edit");
			System.out.println("Unable to load edit button");
			e.printStackTrace();
		}
		editButton.addActionListener(new EditListener());
		editButton.setFont(new Font(editButton.getFont().getName(), Font.PLAIN, 14));
		editButton.setEnabled(false);
		editButton.setPreferredSize(new Dimension(75, 30));
		topPanel.add(editButton);
		
		topPanel.add(Box.createRigidArea(new Dimension(15, 0)));
		
		JLabel searchByLabel = new JLabel("Search By:");
		topPanel.add(searchByLabel);
		
		// Sets search column to first name by default
		filterColumn = 0;
		JComboBox<String> searchByComboBox = new JComboBox<String>(tableColumnNames);
		searchByComboBox.setPreferredSize(new Dimension(130, 30));
		searchByComboBox.addActionListener(new SearchByListener());
		topPanel.add(searchByComboBox);
		
		JLabel searchLabel = new JLabel("Search:");
		topPanel.add(searchLabel);
		
		searchField = new JTextField();
		searchField.setPreferredSize(new Dimension(170, 30));
		searchField.getDocument().addDocumentListener(new SearchListener());
		topPanel.add(searchField);
		
		add(BorderLayout.NORTH, topPanel);
		
		/* Bottom Panel */
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		
		deleteButton = new JButton("Delete Contact");
		deleteButton.addActionListener(new DeleteListener());
		deleteButton.setPreferredSize(new Dimension(200, 35));
		deleteButton.setEnabled(false);
		bottomPanel.add(deleteButton);
		
		addButton = new JButton("Add Contact");
		addButton.addActionListener(new AddListener());
		addButton.setPreferredSize(new Dimension(200, 35));
		bottomPanel.add(addButton);
		
		add(bottomPanel, BorderLayout.SOUTH);
		
		/* Overall Frame */
		
		setTitle("Contact Manager v.3");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		setMinimumSize(new Dimension(600, 300));
		setVisible(true);
	}
	
	/* ActionListener for menu bar containing some options. Uses serialization to save/load table data */
	
	class MenuListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			String event = e.getActionCommand();
			
			if (event.equals("Save")) {
				
				try {
						
					ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(FILE_SAVE_PATH));
					os.writeObject(tableModel.getDataVector());
					JOptionPane.showMessageDialog(null, table.getRowCount() + " contacts saved successfully", null, JOptionPane.INFORMATION_MESSAGE);
					os.close();
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "Unable to save contacts", null, JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} 
				
			} else if (event.equals("Load")) {
				
				/* Attempt to load save file, informs user if save file is not found or data is unreachable */
				
				try {
					
					ObjectInputStream is = new ObjectInputStream(new FileInputStream(FILE_SAVE_PATH));
					Vector<Vector<String>> loadedVector = (Vector<Vector<String>>) is.readObject();
					tableModel.setDataVector(loadedVector, tableColumnNames);
					JOptionPane.showMessageDialog(null, "Saved contact book loaded successfully", null, JOptionPane.INFORMATION_MESSAGE);
					is.close();
				} catch (FileNotFoundException e1) {
					
					JOptionPane.showMessageDialog(null, "No contact book has been saved", null, JOptionPane.INFORMATION_MESSAGE);
				} catch (IOException e1) {
					
					JOptionPane.showMessageDialog(null, "The contact data has been corrupted", null, JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} catch (Exception e1) {
					
					JOptionPane.showMessageDialog(null, "Unable to load contacts", null, JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				} 
				
			} else if (event.equals("Clear Table")) {
				
				int rowCount = table.getRowCount();
				if (rowCount > 0) {
					
					int result = JOptionPane.showConfirmDialog(null, "You are about to delete all visible contacts, continue?", null, JOptionPane.YES_NO_OPTION);
					if (result == JOptionPane.YES_OPTION) {
						
						for (int i = 0; i < rowCount; i++) {
							
							tableModel.removeRow(0);
						}
					}
				}
			} else if (event.equals("Quit")) {
				
				System.exit(0);
			}	else if (event.equals("Draggable Columns")) {
				
				JTableHeader header = table.getTableHeader();
				header.setReorderingAllowed(!header.getReorderingAllowed());
			} else if (event.equals("Set Background Colour")) {
				
				Color newColour = JColorChooser.showDialog(null, null, getBackground());
				
				if (newColour != null) {
					
					topPanel.setBackground(newColour);
					bottomPanel.setBackground(newColour);
					repaint();
				}
			}
		}
		
	}
	
	/* ActionListener for edit button, allows user to edit a contact */
	
	class EditListener implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			int selectedRow = table.getSelectedRow();
			String[] rowData = new String[5];
			
			for (int i = 0; i < rowData.length; i++) {
				
				rowData[i] = (String) table.getValueAt(selectedRow, i);
			}
			
			ContactDialogue dialogue = new ContactDialogue(rowData);
			if (dialogue.getSuccess()) {
				   
				rowData = dialogue.getUserInput();
				for (int i = 0; i < rowData.length; i++) {
					
					table.setValueAt(rowData[i], selectedRow, i);
				}
				
				tableSorter.sort();				// Re-invoke sorting method to correct position of edited contact
			}
		}
		
	}
	

	/* Listener for combo box that changes the column that the search filter will look into */
	class SearchByListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			JComboBox<String> box = (JComboBox<String>) e.getSource();
			String event = (String) box.getSelectedItem();
			
			filterColumn = tableColumnNames.indexOf(event);
			
			// Manually executes method that is executed when the user inputs text in search bar
			 
		}
	}
	
	/* Listener for search bar that conducts live searches on the table for what the user inputs */
	class SearchListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			
			onChange();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {

			onChange();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {

			onChange();
		}

		public void onChange() {
			
			// Executes case insensitive filtering method with search input and the column to filter by, specified by the user with the combo box
			tableSorter.setRowFilter(RowFilter.regexFilter("(?i)" + searchField.getText(), filterColumn));
		}
	}
	
	/* Listener for the selections that the user makes on the table, grays out text and changes button text depending on how many rows are selected */
	
	class TableSelectionListener implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent e) {
			
			// Execute switch statement only once the user has stopped selecting
			if (!e.getValueIsAdjusting()) {
				
				int numRowsSelected = table.getSelectedRowCount();
				switch (numRowsSelected) {
				case 0:
					
					deleteButton.setEnabled(false);
					deleteButton.setText("Delete Contact");
					editButton.setEnabled(false);
					break;
				case 1:
					
					deleteButton.setEnabled(true);
					deleteButton.setText("Delete Contact");
					editButton.setEnabled(true);
					break;
				default:
					
					deleteButton.setEnabled(true);
					deleteButton.setText("Delete Contacts");
					editButton.setEnabled(false);
					break;
				}
			}
		}
		
	}
	
	/* Listener for delete button, behavior changes depending on number of table items selected by the user */
	
	class DeleteListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			int selectedRowCount = table.getSelectedRowCount();
			int firstSelectedRowView = table.getSelectedRow();
			int[] selectedRowsView = table.getSelectedRows();
			
			int[] selectedRowsModel = new int[selectedRowCount];								// Accommodate for difference between table model and table view using a conversion method because removeRow() acts on model; delete the correct row in underlying model
			for (int i = 0; i < selectedRowCount; i++) {
				selectedRowsModel[i] = table.convertRowIndexToModel(selectedRowsView[i]);
			}
			Arrays.sort(selectedRowsModel);														// Required to remove entries correctly
			
			if (selectedRowCount > 1) {
				
				int result = JOptionPane.showConfirmDialog(null, "You are about to delete " + selectedRowCount + " contacts, continue?", null, JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					
					return;
				}
			}
			
			for (int i = selectedRowCount - 1; i >= 0; i--) {
				
				tableModel.removeRow(selectedRowsModel[i]);
			}
			
			// As long as there are contacts left automatically select the contact before the first contact that was deleted (or the next contact if the first contact in the list was deleted)
			if (firstSelectedRowView > 0) {
				
				table.setRowSelectionInterval(firstSelectedRowView - 1, firstSelectedRowView - 1);
			} else if (table.getRowCount() > 0) {
				
				table.setRowSelectionInterval(0, 0);
			}
		}
		
	}
	
	/* Listener for add button, creates dialogue pane and adds user input to table if valid */
	
	class AddListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			ContactDialogue dialogue = new ContactDialogue();
			
			// If valid input successfully obtained, add row to table and automatically select that row
			if (dialogue.getSuccess()) {
				  
				tableModel.addRow(dialogue.getUserInput());
				int addedRowIndexView = table.convertRowIndexToView(table.getRowCount() - 1);	// Put selection on added contact as it appears on the table because setRowSelectionInterval acts on view
				table.setRowSelectionInterval(addedRowIndexView, addedRowIndexView);
			}	
		}
		
	}
	
}
