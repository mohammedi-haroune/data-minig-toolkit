package com.usthb.dmtk.controllers;

import com.usthb.dmtk.algorithms.apriori.Miner;
import com.usthb.dmtk.algorithms.apriori.Reader;
import com.usthb.dmtk.algorithms.apriori.Transactions;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class AprioriController {

    //Apriori

    @FXML
    TableView<TreeSet<String>> aInstancesTable;

    Transactions transactions;

    @FXML
    TextArea resultsItems, resultsRules;

    @FXML
    Slider minSupport;
    @FXML
    Slider minConfiance;

    @FXML
    public void loadApriori() throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File("data"));

        File file = chooser.showOpenDialog(aInstancesTable.getScene().getWindow());

        transactions = Reader.loadInstance(file);


        aInstancesTable.getColumns().clear();
        aInstancesTable.getItems().clear();

        List<String> attributes = transactions.labeledTransactions.get(0).stream().map(s -> s.split("= ")[0]).collect(Collectors.toList());

        List<TreeSet<String>> instances = transactions.labeledTransactions;

        for (String attribute: attributes) {


            TableColumn<TreeSet<String>, String> column = new TableColumn<>(attribute);

            aInstancesTable.getColumns().add(column);

            column.setCellValueFactory(param -> {
                        TreeSet<String> ist = param.getValue();
                        String x = "";
                        for (String s: ist)
                        {
                            if (s.split("= ")[0].equals(attribute))
                                x = s.split("= ")[1];
                        }

                        return new SimpleStringProperty(x);
                    }
            );
        }

        for (TreeSet<String> ist: instances) {
            this.aInstancesTable.getItems().add(ist);
        }
    }

    @FXML
    public void runApripor() {
        int su = 175;
        double ms = 0.75;
        // (int) minSupport.getValue() * 100 / transactions.transactions.size();

        Miner sr = new Miner(transactions, su, ms);

        sr.search();
        //FreqSets :
        TreeMap<String,Integer> freqSets = sr.Ls;
        TreeMap<String,Double> rules = sr.rules;


        for (Map.Entry<String, Integer> freq: freqSets.entrySet()) {
            resultsItems.appendText(freq.getKey() + "a un support = " + freq.getValue() + "\n");
        }


        for (Map.Entry<String, Double> freq: rules.entrySet()) {
            resultsRules.appendText(freq.getKey() + "a un conflit = " + freq.getValue() + "\n");
        }
    }
}
