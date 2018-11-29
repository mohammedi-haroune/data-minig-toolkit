package com.usthb.dmtk.algorithms.apriori;

import java.io.*;

public class Reader {

    File location;
    Transactions instance;
    public Reader(File path){
        this.instance = new Transactions();
        this.location = path;
    }


    public static Transactions loadInstance(File path) throws IOException {
        Reader ir = new Reader(path);
        BufferedReader br = new BufferedReader(new FileReader(ir.location));
        String line = "";
        int id = 0;
        String[] header = br.readLine().split(",");

        while ((line = br.readLine()) != null) {
            String[] items = line.split(",");
            ir.instance.addTransaction(id,items);

            for (int i = 0; i < items.length; i++) {
                items[i] =header[i]+" = "+items[i];
            }
            ir.instance.addLabeledTransaction(id,items);
            id++;

        }
        return ir.instance;
    }

}
