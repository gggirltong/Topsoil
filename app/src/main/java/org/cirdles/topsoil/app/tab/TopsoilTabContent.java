package org.cirdles.topsoil.app.tab;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;

import org.cirdles.commons.util.ResourceExtractor;
import org.cirdles.topsoil.app.MainWindow;
import org.cirdles.topsoil.app.plot.variable.format.VariableFormat;
import org.cirdles.topsoil.app.plot.variable.format.VariableFormats;
import org.cirdles.topsoil.app.table.TopsoilDataTable;
import org.cirdles.topsoil.app.table.TopsoilTableCell;
import org.cirdles.topsoil.app.table.command.DeleteRowCommand;
import org.cirdles.topsoil.app.table.command.InsertRowCommand;
import org.cirdles.topsoil.app.dataset.entry.TopsoilDataEntry;
import org.cirdles.topsoil.app.plot.PlotPropertiesPanelController;

import java.io.IOException;
import java.util.*;

/**
 * This is the primary view for a {@link TopsoilDataTable}. It contains the {@link TableView} that the data is loaded
 * into, the {@link PlotPropertiesPanelController} that controls the attributes of any plots for the data, and any
 * other visual controls in the {@link TopsoilTab}
 *
 * @author Jake Marotta
 * @see PlotPropertiesPanelController
 * @see Tab
 * @see TopsoilTabPane
 */
public class TopsoilTabContent extends SplitPane {

    //***********************
    // Attributes
    //***********************

    /**
     * A {@code GridPane} that contains the {@code Label}s and {@code ChoiceBox}es above the {@code TableView}.
     */
    @FXML private GridPane labelGridPane;

    /**
     * A {@code Label} denoting a {@code TableView} column position as the 'X' column.
     */
    @FXML private Label xLabel;

    /**
     * A {@code Label} denoting a {@code TableView} column position as the 'Y' column.
     */
    @FXML private Label yLabel;

    /**
     * A {@code Label} denoting a {@code TableView} column position as the 'σX' column.
     */
    @FXML private Label xUncertaintyLabel;

    /**
     * A {@code Label} denoting a {@code TableView} column position as the 'σY' column.
     */
    @FXML private Label yUncertaintyLabel;

    /**
     * A {@code Label} denoting a {@code TableView} column position as the 'Corr Coef' column.
     */
    @FXML private Label corrCoefLabel;

    /**
     * A {@code ChoiceBox} for selecting the X Uncertainty {@link VariableFormat}.
     */
    @FXML private ChoiceBox<String> xUncertaintyChoiceBox;

    /**
     * A {@code ChoiceBox} for selecting the Y Uncertainty {@link VariableFormat}.
     */
    @FXML private ChoiceBox<String> yUncertaintyChoiceBox;

    /**
     * A {@code TableView} that displays the table data.
     */
    @FXML private TableView<TopsoilDataEntry> tableView;

    /**
     * A copy of the data contained in the corresponding {@link TopsoilDataTable}.
     */
    private ObservableList<TopsoilDataEntry> data;

    /**
     * A {@code Button} that, when pressed, adds an empty row at the end of the {@code TableView}.
     */
    @FXML private Button addRowButton;

    /**
     * A {@code Button} that, when pressed, removes a row at the end of the {@code TableView}.
     */
    @FXML private Button removeRowButton;

    /**
     * An {@code AnchorPane} that contains the {@link PlotPropertiesPanelController} for this tab.
     */
    @FXML private AnchorPane plotPropertiesAnchorPane;

    /**
     * The {@code PlotPropertiesPanelController} for this tab.
     */
    private PlotPropertiesPanelController plotPropertiesPanelController;

    /**
     * The {@code String} path to the {@code .fxml} file for the {@link PlotPropertiesPanelController}.
     */
    private final String PROPERTIES_PANEL_FXML_PATH = "plot-properties-panel.fxml";

    /**
     * A {@code Map} of {@code String}s to {@code VariableFormat}s, used for getting values from the two uncertainty 
     * {@code ChoiceBox}es.
     */
    private static Map<String, VariableFormat<Number>> STRING_TO_VARIABLE_FORMAT_MAP;

    /**
     * A {@code Map} of  {@code VariableFormat}s to {@code String}s, used for selecting values in the two uncertainty
     * {@code ChoiceBox}es.
     */
    private static Map<VariableFormat<Number>, String> VARIABLE_FORMAT_TO_STRING_MAP;
    static {
        // Map VariableFormat names to the formats for displaying and selecting from the uncertainty ChoiceBoxes.
        STRING_TO_VARIABLE_FORMAT_MAP = new LinkedHashMap<>();
        VARIABLE_FORMAT_TO_STRING_MAP = new LinkedHashMap<>();
        for (VariableFormat<Number> format : VariableFormats.UNCERTAINTY_FORMATS) {
            STRING_TO_VARIABLE_FORMAT_MAP.put(format.getName(), format);
            VARIABLE_FORMAT_TO_STRING_MAP.put(format, format.getName());
        }
    }

    /**
     * A {@code ResourceExtractor} for extracting necessary resources. Used by CIRDLES projects.
     */
    private final ResourceExtractor RESOURCE_EXTRACTOR = new ResourceExtractor(TopsoilTabContent.class);

    //***********************
    // Methods
    //***********************
    
    /** {@inheritDoc}
     */
    @FXML public void initialize() {
        assert labelGridPane != null : "fx:id=\"labelGridPane\" was not injected: check your FXML file " +
                                       "'topsoil-tab.fxml'.";
        assert xLabel != null : "fx:id=\"xLabel\" was not injected: check your FXML file 'topsoil-tab.fxml'.";
        assert yLabel != null : "fx:id=\"yLabel\" was not injected: check your FXML file 'topsoil-tab.fxml'.";
        assert xUncertaintyLabel != null : "fx:id=\"xUncertaintyLabel\" was not injected: check your FXML file " +
                                           "'topsoil-tab.fxml'.";
        assert yUncertaintyLabel != null : "fx:id=\"yUncertaintyLabel\" was not injected: check your FXML file " +
                                           "'topsoil-tab.fxml'.";
        assert corrCoefLabel != null : "fx:id=\"corrCoefLabel\" was not injected: check your FXML file " +
                                       "'topsoil-tab.fxml'.";

        assert xUncertaintyChoiceBox != null : "fx:id=\"xUncertaintyChoiceBox\" was not injected: check your FXML " +
                                               "file 'topsoil-tab.fxml'.";
        assert yUncertaintyChoiceBox != null : "fx:id=\"yUncertaintyChoiceBox\" was not injected: check your FXML " +
                                               "file 'topsoil-tab.fxml'.";

        assert tableView != null : "fx:id=\"tableView\" was not injected: check your FXML file 'topsoil-tab.fxml'.";
        assert addRowButton != null : "fx:id=\"addRowButton\" was not injected: check your FXML file 'topsoil-tab.fxml'.";

        // Add VariableFormats to uncertainty ChoiceBoxes and select TWO_SIGMA_PERCENT by default
        xUncertaintyChoiceBox.setItems(FXCollections.observableArrayList(STRING_TO_VARIABLE_FORMAT_MAP.keySet()));
        xUncertaintyChoiceBox.getSelectionModel().select(VariableFormats.TWO_SIGMA_PERCENT.getName());
        yUncertaintyChoiceBox.setItems(FXCollections.observableArrayList(STRING_TO_VARIABLE_FORMAT_MAP.keySet()));
        yUncertaintyChoiceBox.getSelectionModel().select(VariableFormats.TWO_SIGMA_PERCENT.getName());

        // Handle Keyboard Events
        tableView.setOnKeyPressed(keyEvent -> handleTableViewKeyEvent(keyEvent));

        // Set initial state of remove button.

        tableView.itemsProperty().addListener(c -> {
            if (tableView.getItems() != null) {
                tableView.getItems().addListener((ListChangeListener<TopsoilDataEntry>) d -> {
                    if (tableView.getItems().isEmpty()) {
                        removeRowButton.setDisable(true);
                    } else {
                        removeRowButton.setDisable(false);
                    }
                });
            }
        });

        configureColumns();
        resetIds();

        initializePlotPropertiesPanel();

    }

    /**
     * Loads and initializes the {@code PlotPropertiesPanelController} from FXML.
     */
    private void initializePlotPropertiesPanel() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(RESOURCE_EXTRACTOR.extractResourceAsPath(PROPERTIES_PANEL_FXML_PATH).toUri().toURL());
            Node panel = fxmlLoader.load();
            plotPropertiesPanelController = fxmlLoader.getController();
            AnchorPane.setTopAnchor(panel, 0.0);
            AnchorPane.setRightAnchor(panel, 0.0);
            AnchorPane.setBottomAnchor(panel, 0.0);
            AnchorPane.setLeftAnchor(panel, 0.0);
            plotPropertiesAnchorPane.getChildren().add(panel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles keyboard events in the {@code TableView}.
     *
     * @param keyEvent  a KeyEvent
     */
    private void handleTableViewKeyEvent(KeyEvent keyEvent) {

        List<TableColumn<TopsoilDataEntry, ?>> columns = tableView.getColumns();
        TableView.TableViewSelectionModel<TopsoilDataEntry> selectionModel = tableView.getSelectionModel();

        // Tab focuses right cell
        // Shift + Tab focuses left cell
        if (keyEvent.getCode().equals(KeyCode.TAB)) {
            if (keyEvent.isShiftDown()) {
                if (selectionModel.getSelectedCells().get(0).getColumn() == 0 &&
                    selectionModel.getSelectedIndex() != 0) {
                    selectionModel.select(selectionModel.getSelectedIndex() - 1, this.tableView.getColumns().get
                            (columns.size() - 1));
                } else {
                    selectionModel.selectLeftCell();
                }
            } else {
                if (selectionModel.getSelectedCells().get(0).getColumn() ==
                    columns.size() - 1 && selectionModel.getSelectedIndex() != tableView.getItems().size() - 1) {
                    selectionModel.select(selectionModel.getSelectedIndex() + 1, this.tableView.getColumns().get(0));
                } else {
                    selectionModel.selectRightCell();
                }
            }

            keyEvent.consume();

            // Enter moves down or creates new empty row
            // Shift + Enter moved up a row
        } else if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            if (keyEvent.isShiftDown()) {
                selectionModel.selectAboveCell();
            } else {
                // if on last row
                if (selectionModel.getSelectedIndex() == tableView.getItems().size() - 1) {
                    InsertRowCommand insertRowCommand = new InsertRowCommand(this.tableView);
                    insertRowCommand.execute();
                    ((TopsoilTabPane) this.tableView.getScene().lookup("#TopsoilTabPane")).getSelectedTab()
                                                                                          .addUndo(insertRowCommand);
                }
                selectionModel.selectBelowCell();
            }
            keyEvent.consume();
        }
    }

    /**
     * Configures the {@code TableColumn}s in the {@code TableView}.
     */
    private void configureColumns() {
        List<TableColumn<TopsoilDataEntry, ?>> columns = tableView.getColumns();

        TableColumn<TopsoilDataEntry, Double> newColumn;

        for (int i = 0; i < columns.size(); i++) {

            final int columnIndex = i;
            newColumn = new TableColumn<>(columns.get(i).getText());

            // override cell value factory to accept the i'th index of a data entry for the i'th column
            newColumn.setCellValueFactory(param -> {
                if (param.getValue().getProperties().size() == 0) {
                    return (ObservableValue) new SimpleDoubleProperty(0.0);
                } else {
                    // If data was incomplete i.e. length of line is too short for number of columns.
                    if (param.getValue().getProperties().size() < columnIndex + 1) {
                        SimpleDoubleProperty newProperty = new SimpleDoubleProperty(Double.NaN);
                        param.getValue().getProperties().add(newProperty);
                        return (ObservableValue) newProperty;
                    } else {
                        return (ObservableValue) param.getValue().getProperties().get(columnIndex);
                    }
                }
            });

            // override cell factory to custom editable cells
            newColumn.setCellFactory(value -> new TopsoilTableCell());

            // disable column sorting
            newColumn.setSortable(false);

//            newColumn.setId(Integer.toString(i + 1));

            // add functional column to the array of columns
            columns.set(i, newColumn);
        }
    }

    /**
     * Returns the {@code TableView} from this {@code TopsoilTabContent}.
     *
     * @return TableView
     */
    public TableView<TopsoilDataEntry> getTableView() {
        return tableView;
    }

    /**
     * Returns the {@code PlotPropertiesPanelController} from this {@code TopsoilTabContent}.
     *
     * @return  PlotPropertiesPanelController
     */
    public PlotPropertiesPanelController getPlotPropertiesPanelController() {
        return plotPropertiesPanelController;
    }

    /**
     * Sets the data to be displayed.
     *
     * @param dataEntries   an ObservableList of TopsoilDataEntries
     */
    public void setData(ObservableList<TopsoilDataEntry> dataEntries) {
        this.data = dataEntries;
        tableView.setItems(data);
    }

    /**
     * Returns the X Uncertainty {@code VariableFormat} as selected in xUncertaintyChoiceBox.
     *
     * @return  the selected VariableFormat
     */
    public VariableFormat<Number> getXUncertainty() {
        return STRING_TO_VARIABLE_FORMAT_MAP.get(xUncertaintyChoiceBox.getValue());
    }

    /**
     * Sets the selected X Uncertainty {@code VariableFormat} in the xUncertaintyChoiceBox.
     *
     * @param format    the VariableFormat to select
     */
    public void setXUncertainty(VariableFormat<Number> format) {
        String o = VARIABLE_FORMAT_TO_STRING_MAP.get(format);
        // Find and select the specific item that matches the format.
        for (String s : xUncertaintyChoiceBox.getItems()) {
            if (s.equals(o)) {
                xUncertaintyChoiceBox.getSelectionModel().select(VARIABLE_FORMAT_TO_STRING_MAP.get(format));
                break;
            }
        }
    }

    /**
     * Returns the Y Uncertainty {@code VariableFormat} as selected in yUncertaintyChoiceBox.
     *
     * @return  the selected VariableFormat
     */
    public VariableFormat<Number> getYUncertainty() {
        return STRING_TO_VARIABLE_FORMAT_MAP.get(yUncertaintyChoiceBox.getValue());
    }

    /**
     * Sets the selected X Uncertainty {@code VariableFormat} in the xUncertaintyChoiceBox.
     *
     * @param format    the VariableFormat to select
     */
    public void setYUncertainty(VariableFormat<Number> format) {
        String o = VARIABLE_FORMAT_TO_STRING_MAP.get(format);
        // Find and select the specific item that matches the format.
        for (String s : yUncertaintyChoiceBox.getItems()) {
            if (s.equals(o)) {
                yUncertaintyChoiceBox.getSelectionModel().select(VARIABLE_FORMAT_TO_STRING_MAP.get(format));
                break;
            }
        }
    }

    /**
     * Resets the {@code String} ids associated with each {@code TableColumn} in the {@code TableView}.
     * <p>Each {@code TableColumn} has an associated String id assigned to it, increasing numerically from 1, left to
     * right. This is to keep track of the order of the columns before and after they are re-ordered due to clicking and
     * dragging.
     * </p>
     */
    public void resetIds() {
        int id = 0;
        for (TableColumn<TopsoilDataEntry, ?> column : this.tableView.getColumns()) {
            column.setId(Integer.toString(id));
            id++;
        }
    }

    /**
     * Appends an empty {@code TopsoilDataEntry} to the end of the {@code TableView}.
     */
    @FXML private void addRowButtonAction() {
        if (tableView.getItems() != null) {
            InsertRowCommand insertRowCommand = new InsertRowCommand(tableView);
            insertRowCommand.execute();
            ((TopsoilTabPane) this.tableView.getScene().lookup("#TopsoilTabPane")).getSelectedTab().addUndo
                    (insertRowCommand);
        }
    }

    /**
     * Removes a {@code TopsoilDataEntry} from the end of the {@code TableView}.
     */
    @FXML private void removeRowButtonAction() {
        if (tableView.getItems() != null && !tableView.getItems().isEmpty()) {
            DeleteRowCommand deleteRowCommand = new DeleteRowCommand(tableView);
            deleteRowCommand.execute();
            ((TopsoilTabPane) this.tableView.getScene().lookup("#TopsoilTabPane")).getSelectedTab().addUndo
                    (deleteRowCommand);
        }
    }
}
