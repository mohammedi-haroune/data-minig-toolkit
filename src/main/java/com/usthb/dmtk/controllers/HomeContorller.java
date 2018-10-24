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
import weka.core.pmml.jaxbbindings.Output;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Standardize;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeContorller {
    public StringProperty instancePath = new SimpleStringProperty(null);
    public StringProperty path = new SimpleStringProperty(null);
    public SimpleObjectProperty<Instances> instancesProperty = new SimpleObjectProperty<>(null);
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

    @FXML
    Button plotSelectedBoxPlots, plotSelectedHistograms;

    public Instances getInstances() {
        return instancesProperty.get();
    }

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

        attributes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println(newValue);
            if (newValue == null) {
                plotSelectedBoxPlots.setDisable(true);
                plotSelectedHistograms.setDisable(true);
            } else {
                plotSelectedHistograms.setDisable(false);
                plotSelectedBoxPlots.setDisable(false);
            }
        });


        path.addListener((observable, oldValue, newValue) -> {
            try {
                InputStream inputStream = Files.newInputStream(Paths.get(path.getValue()));
                ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
                instancesProperty.set(dataSource.getDataSet());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        instancesProperty.addListener((observable, oldValue, newValue) -> {
            try {
                loadInstance();

                Runnable thread = () -> {
                    //generate statistics
                    String script =  getClass().getResource("/scripts/statistics.py").getFile();
                    String cmd[] = {"python3", script, path.get()};
                    System.out.println("cmd =  " + Arrays.toString(cmd));

                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec(cmd);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Scanner err = new Scanner(process.getErrorStream());
                    while (err.hasNext()) {
                        System.out.println("ERROR: " + err.nextLine());
                    }

                    Scanner out = new Scanner(process.getInputStream());
                    while (out.hasNext()) {
                        System.out.println("DEBUG: " + out.nextLine());
                    }

                    try {
                        process.waitFor();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    process.destroy();
                };


                ExecutorService executorService
                        = Executors.newFixedThreadPool(1);
                executorService.execute(thread);
                executorService.shutdown();


            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void loadInstance() {
        instancesTable.getColumns().clear();
        instancesTable.getItems().clear();
        attributes.getItems().clear();
        frequencies.clear();
        statistics.clear();

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
                        break;
                    case Attribute.NOMINAL:
                        value = ist.stringValue(a);
                        break;
                    default:
                        throw new IllegalArgumentException("can't get value for a " + a.name() + " missing type " + a.type());
                }
                if (!ist.isMissing(a)) {
                    stats.addValue(ist.value(a));
                    frequency.addValue(value);
                }
            }
        }
        for (Attribute a : frequencies.keySet()) {
            attributes.getItems().add(a);
        }
    }




    public void plot(String plotPath) {
        System.out.println("plotting: " + plotPath);

        Stage stage = new Stage();
        stage.setTitle(plotPath);
        Image back = new
                Image(
                "file:" + plotPath, 800, 800, true, true);
        AnchorPane root = new AnchorPane(new ImageView(back));
        stage.setScene(new Scene(root, 800, 600));
        stage.show();
    }

    @FXML
    public void normalize() throws Exception {
        Normalize normalize = new Normalize();
        normalize.setInputFormat(getInstances());
        Instances normalized = Filter.useFilter(getInstances(), normalize);
        saveInstances(normalized, "normalized");
        //instancesProperty.set(normalized);
        //instancesTable.getItems().setAll(normalized);
    }

    @FXML
    public void standardise() throws Exception {
        Standardize standardize = new Standardize();
        standardize.setInputFormat(getInstances());
        Instances standarized = Filter.useFilter(getInstances(), standardize);
        saveInstances(standarized, "standarized");
        //instancesProperty.set(standarized);
        //instancesTable.getItems().setAll(standarized);
    }

    @FXML
    public void replaceMissingValues() throws Exception {
        ReplaceMissingValues replaceMissingValues = new ReplaceMissingValues();
        replaceMissingValues.setInputFormat(getInstances());
        Instances missingValuesReplaced = Filter.useFilter(getInstances(), replaceMissingValues);
        saveInstances(missingValuesReplaced, "missing");
        //instancesProperty.set(missingValuesReplaced);
        //instancesTable.getItems().setAll(missingValuesReplaced);
    }

    @FXML
    public void resetDataset() throws Exception {
        path.setValue(instancePath.getValue());
        /*
        InputStream inputStream = Files.newInputStream(Paths.get(path.getValue()));
        ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(inputStream);
        instancesProperty.set(dataSource.getDataSet());
        */
    }

    @FXML
    public void plotSelectedBoxPlots() {
        String filename = Paths.get(path.get()).getFileName().toString();
        String attribute = attributes.getSelectionModel().getSelectedItem().name();
        plot("data/boxplots/" + filename + "/" + attribute + ".png");
    }


    @FXML
    public void plotAllBoxPlots() {
        String filename = Paths.get(path.get()).getFileName().toString();
        plot("data/boxplots/" + filename + "/" + "all" + ".png");
    }

    @FXML
    public void plotSelectedHistograms() {
        String filename = Paths.get(path.get()).getFileName().toString();
        String attribute = attributes.getSelectionModel().getSelectedItem().name();
        plot("data/hists/" + filename + "/" + attribute + ".png");
    }

    @FXML
    public void plotAllHistograms() {
        String filename = Paths.get(path.get()).getFileName().toString();
        String attribute = attributes.getSelectionModel().getSelectedItem().name();
        plot("data/hists/" + filename + "/" + attribute + ".png");
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
            instancePath.setValue(file.getPath());
        }
    }

    public void saveInstances(Instances instances, String suffix) throws Exception {
        Path p = Paths.get(path.getValue() + suffix + ".arff");
        OutputStream outputStream = Files.newOutputStream(p);
        ConverterUtils.DataSink dataSink = new ConverterUtils.DataSink(outputStream);
        dataSink.write(instances);
        path.setValue(p.toString());
    }
}
