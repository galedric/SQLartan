package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.Sqlartan;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;
import java.sql.SQLException;

public class DatabaseTreeItem extends CustomTreeItem {

	public DatabaseTreeItem(String name, SqlartanController controller) {
		super(name, controller);

	}

	@Override
	public ContextMenu getMenu() {
		MenuItem vacuum = new MenuItem("Vacuum");
		MenuItem addTable = new MenuItem("Add table");

		vacuum.setOnAction(event -> {
			Sqlartan.getInstance().getController().getDB().vacuum();
			Popup.information("Vacuum", "The database " + Sqlartan.getInstance().getController().getDB().name() + " get vacuumed");
		});
		addTable.setOnAction(event -> {
			Popup.input("Add table", "Name : ", "").ifPresent(name -> {
				if (name.length() > 0) {
					Sqlartan.getInstance().getController().addTable(Sqlartan.getInstance().getController().getDB(), name);
				}
			});
			//controller.addColumn();
		});

		return new ContextMenu(vacuum, addTable);

	}
	@Override
	public Type type() {
		return Type.DATABASE;
	}
}
