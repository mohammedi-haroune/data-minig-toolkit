package com.usthb.dmtk.algorithms.knn;

import weka.core.Instance;
import weka.core.Instances;

import java.util.*;
import java.util.function.BinaryOperator;

public class KNN {

    public Instances dataset;
    public Instances train;
    public Instances test;

    public double accuracy = 0;
    private static int q = 3;
    int K = 3;
    TreeMap<String,BitSet> valueToCode = new TreeMap<>();


    public KNN(Instances dataset, Instances train, Instances test, int K, double ratio)
    {
        if (dataset.classIndex() == -1)
            throw new weka.core.UnassignedClassException("Dabar rassek! select a class Attribute");
        this.dataset = dataset;
        this.K = K;

        this.train = train;
        this.test = test;

        for (int i = 0; i < dataset.numAttributes(); i++) {
            if(dataset.attribute(i).isNominal())
            {
                Enumeration a = dataset.attribute(i).enumerateValues();
                int codif = 0 ;
                while (a.hasMoreElements())
                {
                    valueToCode.put(dataset.attribute(i).name()+" : " +a.nextElement().toString().toLowerCase(),BitSet.valueOf(new long[]{codif++}));
                }

            }
        }
    }


    private double hamming(BitSet a, BitSet b)
    {
        BitSet tmp;
        if(a == null)
            throw new IllegalArgumentException("a should not be null");
        if( b == null)
            throw new IllegalArgumentException("b should not be null");

        tmp = (BitSet) a.clone();
        tmp.xor(b);
        return (double) tmp.cardinality();
    }

    private double distance(Instance A , Instance B)
    {
        double sumNumeric = 0;
        double sumNominal = 0;
        for (int i = 0; i < A.numAttributes(); i++) {
            if(A.attribute(i).isNumeric())
            {
                sumNumeric+=Math.pow(A.value(i)-B.value(i),q);
            }
            if(A.attribute(i).isNominal())
            {
                String key_a = dataset.attribute(i).name()+" : " + A.stringValue(i).toLowerCase();
                String key_b = dataset.attribute(i).name()+" : " + B.stringValue(i).toLowerCase();

                sumNominal+=Math.pow(hamming(valueToCode.get(key_a), valueToCode.get(key_b)),2);
            }
        }
//        sumNumeric = Math.sqrt(sumNumeric);
//        return (double) (sumNominal+sumNumeric)/dataset.numAttributes();
        return Math.pow(sumNumeric+sumNominal,(double)1/q);
//        return Math.sqrt(sum);
    }

    public String classify(Instance instance)
    {

        TreeMap<Integer,Double> distances = new TreeMap<>();



        for (int i = 0; i < dataset.numInstances(); i++) {
            double dst = distance(instance, dataset.instance(i));
            distances.put(i,dst);
        }


        TreeMap<Integer,Double> sorted  = new TreeMap<>(new ItemComaparator(distances));
        sorted.putAll(distances);

        String[] knns = new String[this.K];


        int cpt = 0;
        for (Integer k: sorted.keySet()) {
            if(cpt>= this.K){
                break;
            }
            knns[cpt] = dataset.instance(k).stringValue(dataset.classIndex());
            cpt++;
        }
        return getMostFrequentClass(knns);
    }


    public TreeMap<Integer,String> classifyTestSet(Instances test)
    {
        TreeMap<Integer ,String> results = new TreeMap<>();
        int correctlyClassified = 0;
        for (int i = 0; i < test.numInstances(); i++) {
            String predicted = classify(test.instance(i));
            results.put(i,test.instance(i).stringValue(dataset.classIndex())+","+predicted);
            if(test.instance(i).stringValue(dataset.classIndex()).toLowerCase().equals(predicted.toLowerCase()))
                correctlyClassified++;

        }
        this.accuracy = test.numInstances()>0?correctlyClassified*100/test.numInstances():0;
        return results;
    }


    private String getMostFrequentClass(String[] cls)
    {
        List<String> list = Arrays.asList(cls);
        String mostFrequented = list.stream().reduce(BinaryOperator.maxBy(Comparator.comparingInt(o -> Collections.frequency(list, o)))).orElse(null);
        return mostFrequented;
    }


    private class ItemComaparator implements Comparator<Integer> {
        TreeMap<Integer,Double> map;
        public ItemComaparator(Map<Integer, Double> map) {
            this.map = (TreeMap<Integer, Double>) map;
        }
        @Override
        public int compare(Integer a, Integer b) {
            return map.get(a).compareTo(map.get(b));
        }

    }

    private class ItemComaparator2 implements Comparator<String>{
        TreeMap<String,Integer> map;
        public ItemComaparator2(Map<String, Integer> map) {
            this.map = (TreeMap<String, Integer>) map;
        }
        @Override
        public int compare(String a, String b) {
            return -map.get(a).compareTo(map.get(b));
        }
    }
}