package com.usthb.dmtk.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class HomeContorller {
    public StringProperty path = new SimpleStringProperty(null);
    public SimpleObjectProperty<Instances> instancesProperty = new SimpleObjectProperty<Instances>(null);
    public HashMap<String, Frequency> frequencies = new HashMap<>();
    public HashMap<String, DescriptiveStatistics> statistics = new HashMap<>();
    @FXML
    ToggleButton newWindow;
    @FXML
    TableView<Instance> instancesTable;
    @FXML
    TableView<Attribute> attributes;
    @FXML
    TableColumn<Attribute, String> name, type;
    @FXML
    TableColumn<Attribute, String> min, max, Q1, mean, Q3, mode;
    @FXML
    Label relationName, numAttributes, numInstances;

    public Instances getInstances() {
        return instancesProperty.get();
    }

    @FXML
    public void plotAllBoxPlots() {
        plot();
    }

    @FXML
    public void plotSelectedHistograms() {
        plot();
    }

    @FXML
    public void plotAllHistograms() {
        plot();
    }

    @FXML
    public void plotSelectedBoxPlots() {
        plot();
    }

    @FXML
    public void open() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("data"));
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Instances files", "*.arff");
        chooser.getExtensionFilters().add(filter);
        File file = chooser.showOpenDialog(instancesTable.getScene().getWindow());

        if (file != null) {
            path.setValue(file.getPath());
        }
    }

    @FXML
    public void initialize() {

        path.addListener((observable, oldValue, newValue) -> {
            try {
                loadInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

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
        min.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + getInstances().attributeStats(attribute.index()).numericStats.min;
            return new SimpleStringProperty(m);
        });

        max.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + getInstances().attributeStats(attribute.index()).numericStats.max;
            return new SimpleStringProperty(m);
        });

        mean.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + getInstances().attributeStats(attribute.index()).numericStats.mean;
            return new SimpleStringProperty(m);
        });

        Q1.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC) {
                List<Double> columnData = new ArrayList<>();

                for (Instance item : instancesTable.getItems()) {
                    columnData.add(item.value(attribute));
                }

                Collections.sort(columnData);

                int q1 = (int) Math.round(columnData.size() * 0.25);

                m = columnData.get(q1) + "";
            }
            return new SimpleStringProperty(m);
        });


        Q3.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC) {
                List<Double> columnData = new ArrayList<>();

                for (Instance item : instancesTable.getItems()) {
                    columnData.add(item.value(attribute));
                }
                Collections.sort(columnData);
                int q1 = (int) Math.round(columnData.size() * 0.75);
                m = columnData.get(q1) + "";
            }
            return new SimpleStringProperty(m);
        });

        mode.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            return new SimpleStringProperty(frequencies.get(attribute.name()).getMode().get(0).toString());
        });
        instancesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        attributes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    public void loadInstance() throws Exception {
        instancesTable.getColumns().clear();
        instancesTable.getItems().clear();
        attributes.getItems().clear();

        InputStream inputStream = Files.newInputStream(Paths.get(path.getValue()));
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
        instancesProperty.set(dataSource.getDataSet());

        Enumeration<Attribute> attributeEnumeration = getInstances().enumerateAttributes();
        Enumeration<Instance> i = getInstances().enumerateInstances();

        //relationName.setText("relation name: " + instancesTable.relationName());
        //numAttributes.setText("#attributes: " + instancesTable.numAttributes());
        //fnumInstances.setText("#instancesTable: " + instancesTable.numInstances());


        while (attributeEnumeration.hasMoreElements()) {
            Attribute attribute = attributeEnumeration.nextElement();

            System.out.println(attribute);

            TableColumn<Instance, Object> column = new TableColumn<>(attribute.name());

            instancesTable.getColumns().add(column);

            Frequency frequency = new Frequency();
            DescriptiveStatistics stats = new DescriptiveStatistics();

            frequencies.put(attribute.name(), frequency);
            statistics.put(attribute.name(), stats);

            column.setCellValueFactory(param -> {
                        Comparable value;
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
                                stats.addValue(ist.value(attribute));
                                break;
                            case Attribute.NOMINAL:
                                value = ist.stringValue(attribute);
                                break;
                            default:
                                throw new IllegalArgumentException("can't get value for attribute " + attribute.name() + " missing type " + attribute.type());
                        }
                        frequency.addValue(value);
                        return new SimpleObjectProperty<>(value);
                    }
            );
            attributes.getItems().add(attribute);
        }

        while (i.hasMoreElements()) {
            Instance ist = i.nextElement();
            this.instancesTable.getItems().add(ist);
        }
    }


    public void plot() {
        Stage stage = new Stage();
        stage.setTitle("My New Stage Title");
        Image back = new
                Image(
                "file:data/boxplots/iris.arff/all.png", 800, 800, true, true);
        //,
        //                300, 200, false, false
        AnchorPane root = new AnchorPane(new ImageView(back));
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }
}
