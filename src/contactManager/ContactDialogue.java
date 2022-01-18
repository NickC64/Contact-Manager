package contactManager;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Sets up JOption pane to request user to enter the details of a contact; used for both add and edit buttons
 * 
 * @version 2022/01/16
 * 
 * @author Minghan Chen
 */

public class ContactDialogue {
	
	private String[] userInput = new String[5];
	
	// Denotes whether not adding a contact was successful
	private boolean success = false;
	
	public String[] getUserInput() {
		
		return userInput;
	}
	
	public boolean getSuccess() {
		
		return success;
	}
	
	/* Constructor for option pane with a String array to hold any possible previous values of a contact (used for edit button) */
	
	public ContactDialogue(String[] fieldStrings) {
		
		Vector<String> textFieldLabels = ContactManager.tableColumnNames;
		JTextField[] textFields = new JTextField[5];
		
		JPanel addPanel = new JPanel();
		addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.PAGE_AXIS));
		
		for (int i = 0; i < 5; i++) {
			
			addPanel.add(new JLabel(textFieldLabels.get(i)));
			textFields[i] = new JTextField(10);
			textFields[i].setText(fieldStrings[i]);
			addPanel.add(textFields[i]);
		}
		
		// Simulates key press to get cursor focus on the first text field in the window
		try {
			
			new Robot().keyPress(KeyEvent.VK_TAB);
		} catch (AWTException e) {
			
			e.printStackTrace();
		}
		
	    int result = JOptionPane.showConfirmDialog(null, addPanel, null, JOptionPane.OK_CANCEL_OPTION);
	    
	    // Attempt to create a contact if user presses ok, completes the rest of the contact data if a first or last name is entered
	    if (result == JOptionPane.OK_OPTION) {
			
	    	for (int i = 0; i < 2; i++) {
				
				userInput[i] = textFields[i].getText() == null ? "" : textFields[i].getText().trim();
				if (!userInput[i].equals("")) {
					
					success = true;
				}
			}
	    	
	    	if (success) {
	    		
	    			for (int i = 2; i < 5; i++) {
					userInput[i] = textFields[i].getText() == null ? "" : textFields[i].getText().trim();
				}
	    	} else {
				
	    		JOptionPane.showMessageDialog(null, "A first or last name must be entered", null, JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	
	/* Constructor used for add contact button where there is no previous information */
	
	public ContactDialogue() {
		
		this(new String[5]);
	}
	
}

