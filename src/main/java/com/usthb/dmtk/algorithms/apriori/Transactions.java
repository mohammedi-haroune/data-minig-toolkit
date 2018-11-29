package com.usthb.dmtk.algorithms.apriori;

import java.lang.reflect.Array;
import java.util.*;

public class Transactions {


    public ArrayList<TreeSet<String>> transactions ;
    public ArrayList<TreeSet<String>> labeledTransactions;

    public Transactions()
    {
        transactions = new ArrayList<>();
        labeledTransactions = new ArrayList<>();

    }

    public void addTransaction(int id, String[] items)
    {
        TreeSet<String> tmp = new TreeSet<>();
        for (int i = 0; i < items.length; i++) {
            tmp.add(items[i]);
        }
        transactions.add(id,tmp);
    }
    public void addLabeledTransaction(int id, String[] items)
    {
        TreeSet<String> tmp = new TreeSet<>();
        for (int i = 0; i < items.length; i++) {
            tmp.add(items[i]);
        }
        labeledTransactions.add(id,tmp);
    }

    @Override
    public String toString()
    {
        String res = "";
        for (int i = 0; i < transactions.size(); i++) {
            res+="T_id "+i+" :  ";
            for(String key : transactions.get(i))
            {
                res += key +" , ";
            }
            res+="\n";
        }
        return res;
    }

    public void printTransactions()
    {
        for(TreeSet<String> key : transactions)
        {
            System.out.println(key);
        }
    }
}