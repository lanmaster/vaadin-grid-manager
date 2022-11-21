package org.vaadin.example.countriesgrid;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.ListDataProvider;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Data from:
 * <a href="https://en.wikipedia.org/wiki/Java">...</a>
 */
public class ProvincesGrid {

    private final Grid<ProvinceEntity> component;
    private final ListDataProvider<ProvinceEntity> dataProvider;
    private final List<ProvinceEntity> dataList = new ArrayList<>();

    public ProvincesGrid() {
        component = new Grid<>();
        component.setSizeFull();
        component.setSelectionMode(Grid.SelectionMode.SINGLE);
        component.setColumnReorderingAllowed(true);
        dataProvider = new ListDataProvider<>(dataList);
        dataList.addAll(createData());
        component.setDataProvider(dataProvider);
        //dataProvider.refreshAll();
        new ProvincesGridColumns(this);
    }

    private List<ProvinceEntity> createData() {
        return Arrays.asList(
                new ProvinceEntity("Banten", "Serang", 9_662.92, 7.1, 8_098_277, 10_632_166, 11_904_562, 12_061_475, 1_248),
                new ProvinceEntity("DKI Jakarta", "–", 664.01, 0.5, 8_361_079, 9_607_787, 10_562_088, 10_609_681, 15_978),
                new ProvinceEntity("West Java", "Bandung", 35_377.76, 27.1, 35_724_093, 43_053_732, 48_274_160, 48_782_402, 1_379),
                new ProvinceEntity("Western Java (3 areas above)", "", 45_704.69, 34.7,	52_183_449,	63_293_685,	70_740_810,	71_453_558,	1_563),
                new ProvinceEntity("Central Java", "Semarang", 32_800.69, 25.3, 31_223_258,	32_382_657,	36_516_035,	36_742_501,	1_120),
                new ProvinceEntity("Yogyakarta", "Yogyakarta", 3_133.15, 2.4, 3_121_045, 3_457_491,	3_668_719, 3_712_896, 1_185),
                new ProvinceEntity("Central Java Region (2 areas above)", "", 35_933.84, 27.7, 34_344_303, 35_840_148, 40_184_754, 40_455_397, 1_126),
                new ProvinceEntity("East Java",	"Surabaya",	47_799.75, 37.3, 34_765_993, 37_476_757, 40_665_696, 40_878_790, 855),
                new ProvinceEntity("Region Administered as Java", "Jakarta", 129_438.28, 100.0, 121_293_745, 136_610_590, 151_591_260, 152_787_745, 1_180),
                new ProvinceEntity("Madura Island of East Java", "–", 5_025.30,	3.3, 3_230_300,	3_622_763, 4_004_564, 4_031_060, 802),
                new ProvinceEntity("Java Island", "–", 124_412.98, 96.7, 118_063_445, 132_987_827, 147_586_696,	148_756_685, 1_196)
                );
    }

    public Grid<ProvinceEntity> getComponent() {
        return component;
    }

    public ListDataProvider<ProvinceEntity> getDataProvider() {
        return dataProvider;
    }
}
