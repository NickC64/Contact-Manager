package contactManager;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 * Extension of default table model that disables double click editing
 * 
 * @version 2022/01/16
 * 
 * @author Minghan Chen
 */

public class ContactTableModel extends DefaultTableModel {
	
	public ContactTableModel(Vector<String> columnNames, int rowCount) {
		super(columnNames, rowCount);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		
		return false;
	}
}