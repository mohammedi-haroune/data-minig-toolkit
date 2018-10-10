package com.usthb.dmtk.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

public class InstanceController {
    @FXML
    TableView<Instance> instances;
    @FXML
    TableView<Attribute> attributes;

    @FXML
    TableColumn<Attribute, String> name, type, hasMissingValues;

    @FXML
    Label relationName, numAttributes, numInstances;

    private Home home;

    @FXML
    public void initialize() {
        name.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().name()));
        type.setCellValueFactory(param -> {
            String type = "missing";
            switch (param.getValue().type()) {
                case Attribute.DATE:
                    type = Attribute.ARFF_ATTRIBUTE_DATE;
                    break;
                case Attribute.STRING:
                    type = Attribute.ARFF_ATTRIBUTE_STRING;
                    break;
                case Attribute.NUMERIC:
                    type = Attribute.ARFF_ATTRIBUTE_NUMERIC;
                    break;
                case Attribute.NOMINAL:
                    type = "nominal";
            }
            return new SimpleStringProperty(type);
        });
    }

    @FXML
    public void loadInstance() throws Exception {
        instances.getColumns().clear();
        instances.getItems().clear();
        attributes.getItems().clear();

        InputStream inputStream = Files.newInputStream(Paths.get(home.path.getValue()));
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
        Instances instance = dataSource.getDataSet();

        Enumeration<Attribute> attributeEnumeration = instance.enumerateAttributes();
        Enumeration<Instance> i = instance.enumerateInstances();

        relationName.setText("relation name: " + instance.relationName());
        numAttributes.setText("#attributes: " + instance.numAttributes());
        numInstances.setText("#instances: " + instance.numInstances());


        while (attributeEnumeration.hasMoreElements()) {
            Attribute attribute = attributeEnumeration.nextElement();

            TableColumn<Instance, Object> column = new TableColumn<>(attribute.name());

            instances.getColumns().add(column);

            column.setCellValueFactory(param -> {
                        Object value = null;
                        Instance ist = param.getValue();
                        switch (attribute.type()) {
                            case Attribute.DATE:
                                value = attribute.formatDate(ist.value(attribute));
                                break;
                            case Attribute.STRING:
                                value = ist.stringValue(attribute);
                                break;
                            case Attribute.NUMERIC:
                                value = ist.value(attribute);
                                break;
                        }
                        return new SimpleObjectProperty<>(value);
                    }
            );
            attributes.getItems().add(attribute);
        }

        while (i.hasMoreElements()) {
            instances.getItems().add(i.nextElement());
        }
    }

    public void setHome(Home home) {
        this.home = home;
        home.instanceController = this;
    }
}
