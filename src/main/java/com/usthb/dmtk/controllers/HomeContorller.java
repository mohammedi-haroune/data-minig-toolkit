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
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.Standardize;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

public class HomeContorller {
    public StringProperty path = new SimpleStringProperty(null);
    public SimpleObjectProperty<Instances> instancesProperty = new SimpleObjectProperty<Instances>(null);
    public HashMap<Attribute, Frequency> frequencies = new HashMap<>();
    public HashMap<Attribute, DescriptiveStatistics> statistics = new HashMap<>();

    DecimalFormat df = new DecimalFormat("0.00");

    @FXML
    ToggleButton newWindow;
    @FXML
    TableView<Instance> instancesTable;
    @FXML
    TableView<Attribute> attributes;
    @FXML
    TableColumn<Attribute, String> name, type;
    @FXML
    TableColumn<Attribute, String> min, max, Q1, mean, Q3, mode, median, variance;
    @FXML
    Label relationName, numAttributes, numInstances;

    public Instances getInstances() {
        return instancesProperty.get();
    }

    @FXML
    public void normalize() throws Exception {
        Normalize normalize = new Normalize();
        normalize.setInputFormat(getInstances());
        Instances normalized = Filter.useFilter(getInstances(), normalize);
        instancesTable.getItems().setAll(normalized);
    }

    @FXML
    public void standardise() throws Exception {
        Standardize standardize = new Standardize();
        standardize.setInputFormat(getInstances());
        Instances standarized = Filter.useFilter(getInstances(), standardize);
        instancesTable.getItems().setAll(standarized);
    }

    @FXML
    public void resetDataset() {
        instancesTable.getItems().setAll(getInstances());
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
                m = "" + df.format(statistics.get(attribute).getMin());
            return new SimpleStringProperty(m);
        });

        max.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + df.format(statistics.get(attribute).getMax());
            return new SimpleStringProperty(m);
        });

        mean.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + df.format(statistics.get(attribute).getMean());
            return new SimpleStringProperty(m);
        });

        Q1.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + df.format(statistics.get(attribute).getPercentile(25));
            return new SimpleStringProperty(m);
        });


        Q3.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + df.format(statistics.get(attribute).getPercentile(75));
            return new SimpleStringProperty(m);
        });

        mode.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            return new SimpleStringProperty(frequencies.get(attribute).getMode().get(0).toString());
        });

        median.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + df.format(statistics.get(attribute).getPercentile(50));
            return new SimpleStringProperty(m);
        });


        variance.setCellValueFactory(param -> {
            Attribute attribute = param.getValue();
            String m = "?";
            if (attribute.type() == Attribute.NUMERIC)
                m = "" + df.format(statistics.get(attribute).getVariance());
            return new SimpleStringProperty(m);
        });


        instancesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        attributes.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    @FXML
    public void loadInstance() throws Exception {
        instancesTable.getColumns().clear();
        instancesTable.getItems().clear();
        attributes.getItems().clear();
        frequencies.clear();
        statistics.clear();

        InputStream inputStream = Files.newInputStream(Paths.get(path.getValue()));
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
        instancesProperty.set(dataSource.getDataSet());

        Enumeration<Attribute> attributeEnumeration = getInstances().enumerateAttributes();
        Enumeration<Instance> i = getInstances().enumerateInstances();

        //relationName.setText(getInstances().relationName() + "");

        numAttributes.setText("(" + getInstances().numAttributes() + ")");
        numInstances.setText("(" + getInstances().numInstances() + ")");


        while (attributeEnumeration.hasMoreElements()) {
            Attribute attribute = attributeEnumeration.nextElement();

            System.out.println(attribute);

            TableColumn<Instance, Object> column = new TableColumn<>(attribute.name());

            instancesTable.getColumns().add(column);

            Frequency frequency = new Frequency();
            DescriptiveStatistics stats = new DescriptiveStatistics();

            frequencies.put(attribute, frequency);
            statistics.put(attribute, stats);

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
                                break;
                            case Attribute.NOMINAL:
                                value = ist.stringValue(attribute);
                                break;
                            default:
                                throw new IllegalArgumentException("can't get value for attribute " + attribute.name() + " missing type " + attribute.type());
                        }
                        return new SimpleObjectProperty<>(value);
                    }
            );
        }

        while (i.hasMoreElements()) {
            Instance ist = i.nextElement();
            this.instancesTable.getItems().add(ist);
            for (Attribute a : frequencies.keySet()) {
                Comparable value;
                DescriptiveStatistics stats = statistics.get(a);
                Frequency frequency = frequencies.get(a);
                switch (a.type()) {
                    case Attribute.DATE:
                        value = a.formatDate(ist.value(a));
                        break;
                    case Attribute.STRING:
                        value = ist.stringValue(a);
                        break;
                    case Attribute.NUMERIC:
                        value = ist.value(a);
                        stats.addValue(ist.value(a));
                        break;
                    case Attribute.NOMINAL:
                        value = ist.stringValue(a);
                        break;
                    default:
                        throw new IllegalArgumentException("can't get value for a " + a.name() + " missing type " + a.type());
                }
                frequency.addValue(value);
            }
        }
        for (Attribute a : frequencies.keySet())
            attributes.getItems().add(a);
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
