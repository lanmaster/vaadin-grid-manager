package org.vaadin.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.dom.Element;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Grid manager, that provide functionality save/load columns widths and orders.
 * When adding component to column header there is possibility to set column's name manually.
 * We prepare column creation instaces that provide ability for re-add columns when it needed.
 */
@Slf4j
@CssImport(value = "./styles/vaadincomponents/vaadin-grid-columns-manager/vaadin-grid-columns-manager.css")
@CssImport(value = "./styles/vaadincomponents/vaadin-grid-columns-manager/vaadin-grid-columns-manager-shadow.css", themeFor = "vaadin-context-menu-list-box")
@CssImport(value = "./styles/vaadincomponents/vaadin-grid-columns-manager/vaadin-grid-columns-manager-shadow.css", themeFor = "vaadin-checkbox")
public class VaadinGridColumnsManager04<T> {

    private static final String OPERATOR_SETTINGS_FOLDER = "ui_params";

    // Hidden column's name, for menu activating purpose
    private static final String COLUMNS_MANAGER = "#COLUMNS_MANAGER#";
    private ContextMenu managerContextMenu;

    // Создание папки для хранения настроек
    static {
        Path path = Paths.get(OPERATOR_SETTINGS_FOLDER);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                log.error("Error while creating grids settings folder.", e);
            }
        }
    }


    private final String gridSettingsFileName;
    private final Grid<T> tGrid;

    // Registered columns. Map KEY => Column+Params
    private final Map<String, ColumnParams<T>> registeredColumnsMap = new LinkedHashMap<>();
    // Currently sorted columns map.
    private final Map<String, ColumnParams<T>> actualColumnsMap = Collections.synchronizedMap(new LinkedHashMap<>());
    // Flag that settings is loaded from file
    private boolean parametersLoaded = false;


    /**
     * Constructor
     *
     * @param tGrid - grid to manage
     * @param ids   - String id collection to create filename
     */
    public VaadinGridColumnsManager04(Grid<T> tGrid, Object... ids) {
        String join = Arrays.stream(ids).map(Object::toString).collect(Collectors.joining("_"));
        this.gridSettingsFileName = "v4_" + join;
        this.tGrid = tGrid;

        if (tGrid.isAttached()) {
            createListeners();
        } else {
            // Активируем сервис только при подключении компонента к UI
            tGrid.addAttachListener(attachEvent -> {
                if (attachEvent.isInitialAttach()) {
                    attachEvent.unregisterListener();
                    createListeners();
                }

            });
        }
        createColumnsManagerMenu();
    }


    /**
     * Adding listeners to grid
     */
    private void createListeners() {
        tGrid.addColumnReorderListener(columnReorderEvent -> {
            if (columnReorderEvent.isFromClient()) {
                // Column keys for columns that already connected to grid (sorted)
                List<String> columnKeys = new LinkedList<>();
                columnReorderEvent.getColumns().forEach(tColumn -> columnKeys.add(tColumn.getKey()));
                // Disconnected columns added at tail
                registeredColumnsMap.keySet().forEach(columnKey -> {
                    if (!columnKeys.contains(columnKey)) columnKeys.add(columnKey);
                });
                actualColumnsMap.clear();
                columnKeys.forEach(columnKey -> actualColumnsMap.put(columnKey, registeredColumnsMap.get(columnKey)));
                saveColumnsToFile();
                reconstructManagerContextMenu();
            }
        });

        tGrid.addColumnResizeListener(columnResizeEvent -> {
            Grid.Column<T> resizedColumn = columnResizeEvent.getResizedColumn();
            actualColumnsMap.get(resizedColumn.getKey()).setWidth(resizedColumn.getWidth());
            saveColumnsToFile();
        });
    }


    /**
     * Connect context menu for show manager cell. It is header of most left column.
     */
    private void createColumnsManagerMenu() {
        Button columnsManagerIcon = new Button();
        columnsManagerIcon.setSizeFull();
        columnsManagerIcon.getElement().setAttribute("style", "" +
                "width: 20px;" +
                "background-color: #81d781;" +
                "margin-left: -20px;" +
                "");

        registerColumn(COLUMNS_MANAGER, columnsManagerIcon,
                () -> tGrid.addColumn(t -> "")
                        .setWidth("20px").setSortable(false).setResizable(false).setFrozen(true)
        );

        managerContextMenu = new ContextMenu(columnsManagerIcon);
        managerContextMenu.getElement().setAttribute("theme", "grid-columns-manager");
        managerContextMenu.setOpenOnClick(true);
        managerContextMenu.addOpenedChangeListener(openedChangeEvent -> {
            // Save columns settings on close
            if (!openedChangeEvent.isOpened()) {
                refreshColumns();
                reconstructManagerContextMenu();
                saveColumnsToFile();
            }
        });
        // Header
        Span headerSpan = new Span("Columns visibility");
        headerSpan.setClassName("grid-columns-manager-header");
        // Buttons
        Button buttonOne = new Button("One");
        buttonOne.getElement().setAttribute("style", "margin: 0 3px; width: 100%;");
        Button buttonAll = new Button("All");
        buttonAll.getElement().setAttribute("style", "margin: 0 3px; width: 100%;");
        HorizontalLayout hlButtons = new HorizontalLayout(buttonOne, buttonAll);
        buttonOne.addClickListener(event -> {
            actualColumnsMap.entrySet().stream().findFirst().ifPresent(entry -> entry.getValue().getCheckbox().setValue(true));
            actualColumnsMap.entrySet().stream().skip(2).forEach(entry -> entry.getValue().getCheckbox().setValue(false));
        });
        buttonAll.addClickListener(event -> actualColumnsMap.forEach((key, value) -> value.getCheckbox().setValue(true)));
        managerContextMenu.add(headerSpan);
        managerContextMenu.add(hlButtons);
    }


    /**
     * Saving order and width of columns
     */
    private void saveColumnsToFile() {
        refreshColumns();
        if (!parametersLoaded) {
            Notification.show("Grid settings didn't loaded. Check configuration: " + gridSettingsFileName);
            return;
        }
        Notification.show("Grid settings saved...");
        LinkedList<String> columnKeyList = actualColumnsMap.entrySet().stream()
                .map(columnParamsEntry -> {
                    String columnKey = columnParamsEntry.getKey();
                    boolean visible = columnParamsEntry.getValue().getCheckbox().getValue();
                    String width = columnParamsEntry.getValue().getWidth();
                    // Header adding is only for debug purposes
                    String header = Optional.ofNullable(columnParamsEntry.getValue().getHeader())
                            .map(Component::getElement)
                            .map(Element::getText).orElse("");
                    return columnKey + "/" + visible + "/" + width + "/" + header;
                })
                .collect(Collectors.toCollection(LinkedList::new));
        String fileName = getFileName(gridSettingsFileName);
        Path path = Paths.get(fileName);
        try {
            Files.write(path, columnKeyList);
        } catch (IOException e) {
            log.error("Error while saving grid settings parameters. Setting file name={}", gridSettingsFileName, e);
        }
    }


    /**
     * Initialization.
     * 1. Loading columns orders, visibility and width from file
     * 2. Recreating context menu checkboxes
     * 3. Recreating columns in grid with loaded order
     */
    public void initialize() {
        this.parametersLoaded = true;
        // Split all lines with delimiter "/" to array
        Path path = Paths.get(getFileName(gridSettingsFileName));
        if (Files.exists(path)) {
            try (Stream<String> lines = Files.lines(path)) {
                actualColumnsMap.clear();
                // Load colums settings
                lines
                        .map(s -> s.split("/"))
                        .filter(array -> array.length > 2)
                        .filter(array -> registeredColumnsMap.containsKey(array[0]))
                        .forEach(array -> {
                            ColumnParams<T> columnParams = registeredColumnsMap.get(array[0]);
                            columnParams.getCheckbox().setValue(Boolean.parseBoolean(array[1]));
                            columnParams.setWidth(array[2]);
                            actualColumnsMap.put(array[0], columnParams);
                        });
                // If you add some columns after already saved config - it must be added now.
                registeredColumnsMap.forEach((key, columnParams) -> {
                    if (!actualColumnsMap.containsKey(key)) actualColumnsMap.put(key, columnParams);
                });
                registeredColumnsMap.forEach((key, tColumnParams) -> actualColumnsMap.computeIfAbsent(key, registeredColumnsMap::get));
            } catch (IOException e) {
                log.error("Error while loading grid settings parameters. Setting file name={}", gridSettingsFileName, e);
            }

        } else {
            registeredColumnsMap.forEach((key, tColumnParams) -> actualColumnsMap.computeIfAbsent(key, registeredColumnsMap::get));
            saveColumnsToFile();
        }
        reconstructManagerContextMenu();
        refreshColumns();
    }


    /**
     * Recreate checkboxes in context menu
     */
    private void reconstructManagerContextMenu() {
        actualColumnsMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(COLUMNS_MANAGER))
                .forEach(entry -> {
                    ColumnParams<T> tColumnParams = entry.getValue();
                    Checkbox checkbox = tColumnParams.getCheckbox();
                    managerContextMenu.add(checkbox);
                });
    }


    /**
     * Grid columns reconstruct
     */
    private void refreshColumns() {
        // Only connected columns
        List<Grid.Column<T>> addedColumns = tGrid.getColumns();
        // Column "Setting" must be first all the time
        Map.Entry<String, ColumnParams<T>> columnsManagerEntry = actualColumnsMap.entrySet().stream()
                .filter(entry -> entry.getKey().equals(COLUMNS_MANAGER))
                .findFirst()
                .orElse(null);
        if (Objects.isNull(columnsManagerEntry)) return;
        // Next is "freezed" columns
        LinkedHashMap<String, ColumnParams<T>> sortedByFrozen = actualColumnsMap.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(COLUMNS_MANAGER))
                .sorted((o1, o2) -> Boolean.compare(o2.getValue().getColumnInstance().isFrozen(), o1.getValue().getColumnInstance().isFrozen()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
        // Mapping building
        actualColumnsMap.clear();
        actualColumnsMap.put(columnsManagerEntry.getKey(), columnsManagerEntry.getValue());
        actualColumnsMap.putAll(sortedByFrozen);
        // Removing/adding columns
        actualColumnsMap.forEach((key, tColumnParams) -> {
            if (tColumnParams.getCheckbox().getValue()) {
                // For visible column
                boolean present = addedColumns.stream().anyMatch(addedColumn -> addedColumn.getKey().equals(key));
                if (!present) {
                    createAddColumn(key, tColumnParams);
                }
            } else {
                // For invisible column
                addedColumns.stream()
                        .filter(addedColumn -> addedColumn.getKey().equals(key))
                        .findAny()
                        .ifPresent(tColumn -> tGrid.removeColumnByKey(key));
            }
        });

        // Setting columns order
        LinkedList<Grid.Column<T>> collect = actualColumnsMap.values().stream()
                .filter(tColumnParams -> tColumnParams.getCheckbox().getValue())
                .map(ColumnParams::getColumnInstance)
                .collect(Collectors.toCollection(LinkedList::new));
        tGrid.setColumnOrder(collect);

        // Setting widths
        actualColumnsMap.values().stream()
                .filter(tColumnParams -> tColumnParams.getCheckbox().getValue())
                .forEach(tColumnParams -> tColumnParams.getColumnInstance().setWidth(tColumnParams.getWidth()));
    }


    private String getFileName(String gridName) {
        return OPERATOR_SETTINGS_FOLDER + "/" + gridName;
    }


    /**
     * Registering column with String header
     */
    public void registerColumn(String columnKey, String columnHeader, Supplier<Grid.Column<T>> columnInstanceSupplier) {
        registerColumn(columnKey, new Text(columnHeader), true, true, columnInstanceSupplier);
    }

    /**
     * Registering column with Component header
     */
    public void registerColumn(String columnKey, Component columnHeader, Supplier<Grid.Column<T>> columnInstanceSupplier) {
        registerColumn(columnKey, columnHeader, true, true, columnInstanceSupplier);
    }

    /**
     * Registering column with Component header, full version
     *
     * @param columnKey              - column key
     * @param columnHeader           - component for column header
     * @param populateKey            - is it needed to apply column key (or it was already added in column's creation code)
     * @param populateHeader         - is it needed to apply column header (or it was already added in column's creation code)
     * @param columnInstanceSupplier - column creation code supplier
     */
    public void registerColumn(String columnKey, Component columnHeader, boolean populateKey, boolean populateHeader, Supplier<Grid.Column<T>> columnInstanceSupplier) {
        Grid.Column<T> columnInstance = columnInstanceSupplier.get();
        // Trying to get header text from "colman-checkbox-name" or text attribute
        String checkboxName = Optional
                .ofNullable(columnHeader.getElement().getAttribute("colman-checkbox-name"))
                .orElse(columnHeader.getElement().getText())
                + (columnInstance.isFrozen() ? "(*)" : "");
        Checkbox checkbox = new Checkbox(checkboxName, true);

        registeredColumnsMap.put(columnKey, new ColumnParams<>(
                columnInstance.getWidth(),
                columnHeader,
                columnInstanceSupplier,
                columnInstance,
                populateKey,
                populateHeader,
                checkbox
        ));
        if (populateKey) columnInstance.setKey(columnKey);
        if (populateHeader) columnInstance.setHeader(columnHeader);
    }


    /**
     * Adding column to grid
     */
    private void createAddColumn(String key, ColumnParams<T> tColumnParams) {
        Grid.Column<T> columnInstance = tColumnParams.getColumnInstanceSupplier().get();
        if (tColumnParams.isPopulateKey()) columnInstance.setKey(key);
        if (tColumnParams.isPopulateHeader()) columnInstance.setHeader(tColumnParams.getHeader());
        columnInstance.setWidth(tColumnParams.getWidth());
        tColumnParams.setColumnInstance(columnInstance);
    }


    public Map<String, ColumnParams<T>> getRegisteredColumnsMap() {
        return registeredColumnsMap;
    }

    public Map<String, ColumnParams<T>> getActualColumnsMap() {
        return actualColumnsMap;
    }


    /**
     * Static class for mapping values
     */
    @Getter
    @Setter
    @AllArgsConstructor
    public static class ColumnParams<T> {
        private String width;
        private Component header;
        private Supplier<Grid.Column<T>> columnInstanceSupplier;
        private Grid.Column<T> columnInstance;
        private boolean populateKey;
        private boolean populateHeader;
        private Checkbox checkbox;
    }
}
