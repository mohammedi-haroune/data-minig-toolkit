package com.usthb.dmtk.algorithms.dbscan;


import weka.core.Attribute;
import weka.core.Instance;

import java.util.*;

public class Point implements Comparable<Point> {

    private Instance point;
    static TreeMap<String, BitSet> codifs = new TreeMap<>();

    public Point(Instance instance) {
        this.point = instance;
    }

    public Point(Point p) {
        this.point = (Instance) p.getPoint().copy();
    }

    public ArrayList<Attribute> infos()
    {
        Enumeration a = point.enumerateAttributes();
        if(a == null)
            return null;
        ArrayList<Attribute> res = new ArrayList<>();
        while (a.hasMoreElements())
        {
            res.add((Attribute) a.nextElement());
        }
        return res;
    }


    public static double distance(Point p1, Point p2, double q) {
        double sumNumeric = 0;
        double sumNominal = 0;
        for (int i = 0; i < p1.point.numAttributes(); i++) {
            if (p1.point.attribute(i).isNumeric() && p1.point.classIndex() != i) {
                sumNumeric += Math.pow(p1.point.value(i) - p2.point.value(i), q);
            }
            if (p1.point.attribute(i).isNominal() && p1.point.classIndex() != i) {
                String key_a = p1.point.attribute(i).name().toLowerCase() + " : " + p1.point.stringValue(i).toLowerCase();
                String key_b = p1.point.attribute(i).name().toLowerCase() + " : " + p2.point.stringValue(i).toLowerCase();
                if (Point.codifs.get(key_a) == null)
                    System.out.println("key_a = " + key_a);
                sumNominal += Math.pow((key_a.equals(key_b)) ? 0 : 1, q);
                //Math.pow(distance(codifs.get(key_a),codifs.get(key_b)),q);
            }
        }
        return Math.pow(sumNumeric + sumNominal, (double) 1 / q);
    }


    private static double distance(BitSet a, BitSet b) {
        BitSet tmp;
        if (a == null || b == null) {
            System.out.println("a = " + a);
            System.out.println("b = " + b);
        }
        tmp = (BitSet) a.clone();
        tmp.xor(b);
        return (double) tmp.cardinality();
    }

    @Override
    public int compareTo(Point point) {
        return (int) distance(this, point, 1);
    }

    public Instance getPoint() {
        return point;
    }

    @Override
    public String toString() {
        return point.toString();
    }
}