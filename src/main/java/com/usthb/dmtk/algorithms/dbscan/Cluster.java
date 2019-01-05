package com.usthb.dmtk.algorithms.dbscan;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Cluster implements Comparable<Cluster> {

    private List<Point> objects;
    public Cluster() {
        this.objects = new ArrayList<>();
    }

    public void addToCluster(Point p) {
        if(objects.contains(p))
            return;
        this.objects.add(p);
    }


    @Override
    public int compareTo(Cluster cluster) {
        return this.objects.size() - cluster.objects.size();
    }

    @Override
    public String toString() {
        return objects.toString();
    }


    public List<Point> getObjects() {
        return objects;
    }


    public double score() {

        Point G = center();
        if(G==null) {
            return -1;
        }
        double sum = 0;
        for (Point p : objects) {
            sum+=Math.pow(Point.distance(p,G,2),2);
        }
        return sum/ objects.size();
    }

    public Point center() {
        if (objects.size() == 0)
            return null;

        Instances data = new Instances(this.toString(), objects.get(0).infos(), objects.size());
        InstancesStats sr = new InstancesStats(data);
        data.addAll(objects.stream().map(Point::getPoint).collect(Collectors.toList()));

        Point G = new Point(objects.get(0));
        for (int i = 0; i < G.getPoint().numAttributes() - 1; i++) {
            if (G.getPoint().attribute(i).isNumeric()) {

                G.getPoint().attribute(i).setStringValue(String.valueOf(sr.mean(i)));
            } else {

                G.getPoint().attribute(i).setStringValue(sr.mode(i));
            }
        }

        return G;
    }

}