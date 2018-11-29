package com.usthb.dmtk.controllers;

import com.usthb.dmtk.algorithms.knn.KNN;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.w3c.dom.html.HTMLButtonElement;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
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
import java.util.*;
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

    @FXML
    BorderPane borderPane;

    @FXML
    VBox first, second;

    @FXML
    Slider k, q, ratio;

    @FXML
    TextArea results;

    @FXML
    ComboBox<Attribute> target;
    AprioriController aprioriController;
    @FXML
    private VBox resultsVBox;

    @FXML
    private Button runButton;

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
                loadInstance(getInstances(), instancesTable);

                Runnable thread = () -> {
                    //generate statistics
                    String script = getClass().getResource("/scripts/statistics.py").getFile();
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

        target.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            getInstances().setClass(newValue);
        });
    }

    public void loadInstance(Instances instances, TableView<Instance> table) {
        table.getColumns().clear();
        table.getItems().clear();
        attributes.getItems().clear();
        frequencies.clear();
        statistics.clear();

        Enumeration<Attribute> attributeEnumeration = instances.enumerateAttributes();
        Enumeration<Instance> i = instances.enumerateInstances();

        //relationName.setText(instances.relationName() + "");

        numAttributes.setText("(" + instances.numAttributes() + ")");
        numInstances.setText("(" + instances.numInstances() + ")");


        while (attributeEnumeration.hasMoreElements()) {
            Attribute attribute = attributeEnumeration.nextElement();
            System.out.println("attribute = " + attribute);
            if (attribute.isNominal())
                if (!target.getItems().contains(attribute))
                    target.getItems().add(attribute);

            TableColumn<Instance, Object> column = createColumnFor(attribute);
            table.getColumns().add(column);
        }
        if (instances.classIndex() != -1) {
            TableColumn<Instance, Object> column = createColumnFor(instances.classAttribute());
            table.getColumns().add(column);
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

    private TableColumn<Instance, Object> createColumnFor(Attribute attribute) {
        TableColumn<Instance, Object> column = new TableColumn<>(attribute.name());


        Frequency frequency = new Frequency();
        DescriptiveStatistics stats = new DescriptiveStatistics();

        //frequencies.put(attribute, frequency);
        //statistics.put(attribute, stats);

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
        return column;
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

    @FXML
    public void hachwa() throws IOException {
        FXMLLoader hachwaPane = new FXMLLoader(getClass().getResource("/views/apriori.fxml"));
        AprioriController hachwaController = new AprioriController();
        hachwaPane.setController(hachwaController);

        aprioriController = hachwaController;

        SplitPane mainPane = hachwaPane.load();
        this.borderPane.setCenter(mainPane);
    }

    @FXML
    public void loadApriori() throws IOException {
        aprioriController.loadApriori();
    }

    @FXML
    public void runApriori() {
        aprioriController.runApripor();
    }

    Instances train, test;
    TableView<Instance> trainTable = new TableView<>();
    TableView<Instance> testTable = new TableView<>();

    @FXML
    public void runKnn() {
        KNN knn = new KNN(getInstances(), train, test, (int) k.getValue(), ratio.getValue());

        TreeMap<Integer, String> map = knn.classifyTestSet(knn.test);
        String res = "";
        res += "Le nombre de voisins K : " + k.getValue() + "\n";
        res += "Le ratio de division de l'ensemble d'apprentissage est: " + ratio.getValue() + "\n";
        res += "La précision (accuracy) lors de l'évaluation est     " + knn.accuracy + "%\n";

        System.out.println(res);

        TableColumn<Instance, String> column = new TableColumn<>("Prediction");

        trainTable.getColumns().add(column);

        column.setCellValueFactory(param -> {
            Instance ist = param.getValue();
            return new SimpleStringProperty(knn.classify(ist));
        });

        TableColumn<Instance, String> column2 = new TableColumn<>("Prediction");

        testTable.getColumns().add(column2);

        column2.setCellValueFactory(param -> {
            Instance ist = param.getValue();
            return new SimpleStringProperty(knn.classify(ist));
        });
    }

    @FXML
    public void showStats() {
        first.getChildren().clear();
        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setHgap(10.0);
        gridPane.setVgap(10.0);
        gridPane.addRow(0, new Label("Predicted\\Correct"), new Label("Positive"), new Label ("Negative"));
        gridPane.addRow(1, new Label("Positive"), new Label("TP = 100"), new Label ("FN = 45"));
        gridPane.addRow(2, new Label("Negative"), new Label("FP = 14"), new Label ("TN = 100"));
        first.getChildren().add(gridPane);
    }

    @FXML
    public void split() {
        Instances all = getInstances();

        all.randomize(new java.util.Random(0));
        int trainSize = (int) Math.round(all.numInstances() * ratio.getValue());

        int testSize = all.numInstances() - trainSize;

        train = new Instances(all, 0, trainSize);
        test = new Instances(all, trainSize, testSize);

        runButton.setDisable(false);

        second.getChildren().clear();

        Label trainLabel = new Label("Train Set");
        loadInstance(train, trainTable);
        VBox trainVBox = new VBox(trainLabel, trainTable);
        trainTable.getItems().addAll(train);
        second.getChildren().add(trainVBox);

        Label testLabel = new Label("Test Set");
        loadInstance(test, testTable);
        testTable.getItems().addAll(test);
        VBox testVBox = new VBox(testLabel, testTable);
        second.getChildren().add(testVBox);
    }
}
