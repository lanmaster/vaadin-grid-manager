package org.vaadin.example.countriesgrid;

import com.helger.commons.codec.IDecoder;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.function.ValueProvider;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.example.VaadinGridColumnsManager04;

import java.util.*;
import java.util.function.Function;

public class ProvincesGridColumns {

    private final Map<TextField, SerializablePredicate<ProvinceEntity>> predicateCollection = new HashMap<>();
    private final Grid<ProvinceEntity> component;
    private final ListDataProvider<ProvinceEntity> dataProvider;


    public ProvincesGridColumns(ProvincesGrid provincesGrid) {
        component = provincesGrid.getComponent();
        dataProvider = provincesGrid.getDataProvider();

        VaadinGridColumnsManager04<ProvinceEntity> columnsManager = new VaadinGridColumnsManager04<>(
                provincesGrid.getComponent(),
                "ProvincesGrid"
        );

        // Province or Special Region
        ValueProvider<ProvinceEntity, String> provinceName = ProvinceEntity::getProvinceName;
        columnsManager.registerColumn("PROVINCE_NAME", "Province or Special Region",
                () -> component.addColumn(provinceName)
                        .setWidth("60px").setSortable(true).setResizable(true).setFrozen(true)
                        .setComparator(compareNullable(provinceName)));

        // Province or Special Region / with filter component
        ValueProvider<ProvinceEntity, String> provinceName1 = ProvinceEntity::getProvinceName;
        columnsManager.registerColumn("PROVINCE_NAME_FILTER", createFilterTextField("Province or Special Region / F", provinceName1),
                () -> component.addColumn(provinceName1)
                        .setWidth("60px").setSortable(true).setResizable(true).setFrozen(true)
                        .setComparator(compareNullable(provinceName1)));

        // Capital
        ValueProvider<ProvinceEntity, String> capital = ProvinceEntity::getCapital;
        columnsManager.registerColumn("CAPITAL", "Capital",
                () -> component.addColumn(capital)
                        .setWidth("60px").setSortable(true).setResizable(true).setFrozen(true)
                        .setComparator(compareNullable(capital)));

        // Area
        ValueProvider<ProvinceEntity, Double> area = ProvinceEntity::getArea;
        columnsManager.registerColumn("AREA", "Area",
                () -> component.addColumn(area)
                        .setWidth("60px").setSortable(true).setResizable(true).setFrozen(true)
                        .setComparator(compareNullable(area)));

        // AreaPercent
        ValueProvider<ProvinceEntity, Double> areaPercent = ProvinceEntity::getAreaPercent;
        columnsManager.registerColumn("AREA_PERCENT", "Area %",
                () -> component.addColumn(areaPercent)
                        .setWidth("60px").setSortable(true).setResizable(true)
                        .setComparator(compareNullable(areaPercent)));

        // Population 2000
        ValueProvider<ProvinceEntity, Integer> population2000 = ProvinceEntity::getPopulation2000;
        columnsManager.registerColumn("POPULATION_2000", "Population census 2000",
                () -> component.addColumn(population2000)
                        .setWidth("60px").setSortable(true).setResizable(true)
                        .setComparator(compareNullable(population2000)));

        // Population 2010
        ValueProvider<ProvinceEntity, Integer> population2010 = ProvinceEntity::getPopulation2010;
        columnsManager.registerColumn("POPULATION_2010", "Population census 2010",
                () -> component.addColumn(population2010)
                        .setWidth("60px").setSortable(true).setResizable(true)
                        .setComparator(compareNullable(population2010)));

        // Population 2020
        ValueProvider<ProvinceEntity, Integer> getPopulation2020 = ProvinceEntity::getPopulation2020;
        columnsManager.registerColumn("POPULATION_2020", "Population census 2020",
                () -> component.addColumn(getPopulation2020)
                        .setWidth("60px").setSortable(true).setResizable(true)
                        .setComparator(compareNullable(getPopulation2020)));

        // Population estimate mid 2021
        ValueProvider<ProvinceEntity, Integer> populationEstimate2021 = ProvinceEntity::getPopulationEstimate2021;
        columnsManager.registerColumn("POPULATION_ESTIMATE_2021", "Population estimate mid 2021",
                () -> component.addColumn(populationEstimate2021)
                        .setWidth("60px").setSortable(true).setResizable(true)
                        .setComparator(compareNullable(populationEstimate2021)));

        // Population density mid 2021
        ValueProvider<ProvinceEntity, Integer> populationDensity2021 = ProvinceEntity::getPopulationDensity2021;
        columnsManager.registerColumn("POPULATION_DENSITY_2021", "Population density mid 2021",
                () -> component.addColumn(populationDensity2021)
                        .setWidth("60px").setSortable(true).setResizable(true)
                        .setComparator(compareNullable(populationDensity2021)));

        columnsManager.initialize();

    }

    public static <S, T extends Comparable<T>> Comparator<S> compareNullable(Function<? super S, ? extends T> keyExtractor) {
        return Comparator.comparing(keyExtractor, Comparator.nullsFirst(Comparator.naturalOrder()));
    }


    /**
     * Method for creation header TextField for filtering
     */
    private TextField createFilterTextField(String headerText, ValueProvider<ProvinceEntity, ? extends Comparable<?>> valueProvider) {
        TextField textField = new TextField();
        textField.getElement().setAttribute("colman-checkbox-name", headerText);
        textField.getElement().setAttribute("style", "" +
                "color: black;" +
                "width: 100%;" +
                "font-size: 12px;" +
                "");
        textField.setPlaceholder(headerText + ": ");
        textField.setClearButtonVisible(true);
        textField.setValueChangeTimeout(200);
        textField.setValueChangeMode(ValueChangeMode.LAZY);
        textField.addValueChangeListener(event -> {
            String value = event.getValue();
            if (StringUtils.isNotBlank(value)) {
                predicateCollection.put(textField, (SerializablePredicate<ProvinceEntity>) provinceEntity -> {
                    String s = Optional.ofNullable(valueProvider.apply(provinceEntity)).map(Objects::toString).orElse("");
                    return StringUtils.startsWithIgnoreCase(s, value);
                });
            } else {
                predicateCollection.remove(textField);
            }
            dataProvider.clearFilters();
            predicateCollection.forEach((tf, predicate) -> dataProvider.addFilter(predicate));
        });
        return textField;
    }


}
