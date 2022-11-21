package org.vaadin.example.countriesgrid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProvinceEntity {

    private String provinceName;
    private String capital;
    private Double area;
    private Double areaPercent;
    private Integer population2000;
    private Integer population2010;
    private Integer population2020;
    private Integer populationEstimate2021;
    private Integer populationDensity2021;

}
