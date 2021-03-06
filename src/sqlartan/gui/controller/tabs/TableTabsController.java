package sqlartan.gui.controller.tabs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sqlartan.Sqlartan;
import sqlartan.core.InsertRow;
import sqlartan.core.Table;
import sqlartan.gui.controller.tabs.model.InsertRowModel;
import sqlartan.gui.controller.tabs.model.PersistentStructureModel;
import sqlartan.gui.controller.tabs.model.StructureModel;
import sqlartan.gui.util.Popup;
import java.io.IOException;
import static sqlartan.gui.util.ActionButtons.actionButton;

/**
 * Controller of TableTabs.fxml. Controller of the tabs of a table.
 */
public class TableTabsController extends PersistentStructureTabsController {

	@FXML
	protected Tab insertTab;
	@FXML
	private TableColumn<PersistentStructureModel, String> colRename;
	@FXML
	private TableColumn<PersistentStructureModel, String> colDelete;
	@FXML
	private TableColumn<InsertRowModel, String> insertColName;
	@FXML
	private TableColumn<InsertRowModel, String> insertColType;
	@FXML
	private TableColumn<InsertRowModel, CheckBox> insertNull;
	@FXML
	private TableColumn<InsertRowModel, TextField> insertColValue;
	@FXML
	private TableView<InsertRowModel> insertTable;

	private ObservableList<InsertRowModel> insertRows = FXCollections.observableArrayList();

	/**
	 * {@inheritDoc}
	 * Creates the button rename and drop, complete the structure tab
	 */
	@FXML
	protected void initialize() throws IOException {
		super.initialize();

		colRename.setCellFactory(actionButton("Rename", (self, event) -> {
			StructureModel tableStruct = self.getTableView().getItems().get(self.getIndex());
			Sqlartan.getInstance().getController().renameColumn((Table) structure, tableStruct.name.get());
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			StructureModel tableStruct = self.getTableView().getItems().get(self.getIndex());
			Sqlartan.getInstance().getController().dropColumn((Table) structure, tableStruct.name.get());
		}));

		insertColName.setCellValueFactory(param -> param.getValue().name);
		insertColType.setCellValueFactory(param -> param.getValue().type);

		insertColValue.setCellValueFactory(param -> {
			ObservableValue<TextField> tf = new SimpleObjectProperty<>(new TextField());
			param.getValue().value.bindBidirectional(tf.getValue().textProperty());
			return tf;
		});

		insertNull.setCellValueFactory(param -> {
			ObservableValue<CheckBox> cb = new SimpleObjectProperty<>(new CheckBox());
			param.getValue().nullable.bindBidirectional(cb.getValue().selectedProperty());
			return cb;
		});
	}

	/**
	 * Displays the insert tab.
	 */
	private void displayInsertTab() {
		insertRows.clear();
		insertRows.addAll(structure.columns().map(InsertRowModel::new).toList());
		insertTable.setItems(insertRows);
	}

	/**
	 * Inserts the data in the table.
	 */
	@FXML
	protected void submitNewData() {
		try {
			Object objects[] = InsertRowModel.toArray(insertTable.getItems());
			InsertRow insertRow = ((Table) structure).insert();

			insertRow.set(objects);

			insertRow.execute();
			Popup.information("Insertion in" + structure.name(), "The data are successfully inserted");
		} catch (Exception e) {
			Popup.error("Error while inserting data", e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refresh(Tab selected) {
		if (selected == structureTab) {
			displayStructure();
		} else if (selected == displayTab) {
			displayData();
		} else if (selected == insertTab) {
			displayInsertTab();
		}
	}
}
