package org.vaadin.example;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.vaadin.example.countriesgrid.ProvincesGrid;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        ProvincesGrid provincesGrid = new ProvincesGrid();
        add(provincesGrid.getComponent());
        setSizeFull();
    }
}