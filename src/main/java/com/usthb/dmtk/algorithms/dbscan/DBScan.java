package com.usthb.dmtk.algorithms.dbscan;


import weka.core.Instances;

import java.util.*;

public class DBScan {

    private final double epsilon;
    private final double minPts;

    private enum PointStatus {
        NOISE,
        CLUSTERED
    }


    private TreeMap<String, PointStatus> visitedPoints = new TreeMap<>();
    private List<Cluster> clusters = new ArrayList<>();
    private Collection<Point> allPoints = new ArrayList<>();

    private TreeMap<String, BitSet> mapAttributeValueToCodification = new TreeMap<>();

    public DBScan(double epsilon, double minPts, Instances instances) {
        this.epsilon = epsilon;
        this.minPts = minPts;


        for (int i = 0; i < instances.numAttributes() - 1; i++) {
            if (instances.attribute(i).isNominal()) {
                Enumeration a = instances.attribute(i).enumerateValues();
                int codif = 0;
                if (a == null)
                    return;
                while (a.hasMoreElements()) {
                    mapAttributeValueToCodification.put(
                            instances.attribute(i).name().toLowerCase() + " : " + a.nextElement().toString().toLowerCase(),
                            BitSet.valueOf(new long[]{codif++}));
                }

            }
        }

        Point.codifs = mapAttributeValueToCodification;

        for (int i = 0; i < instances.numInstances(); i++) {
            this.allPoints.add(new Point(instances.instance(i)));
        }

    }


    public void start() {
        int itter = 0;

        for (Point point : allPoints) {
            if (visitedPoints.get(point.toString()) != null) {
                continue;
            }
            List<Point> neighbors = getDensityReachableNeighbors(point, allPoints);
            if (neighbors.size() >= minPts) {
                Cluster cluster = new Cluster();
                clusters.add(exploreNeighbors(cluster, point, neighbors, allPoints, visitedPoints));
            } else {
                visitedPoints.put(point.toString(), PointStatus.NOISE);
            }
        }

    }


    private Cluster exploreNeighbors(Cluster cluster,
                                     Point point,
                                     List<Point> neighbors,
                                     Collection<Point> allPoints,
                                     Map<String, PointStatus> visited) {
        cluster.addToCluster(point);
        visited.put(point.toString(), PointStatus.CLUSTERED);

        List<Point> startingNeighbors = new ArrayList<>(neighbors);
        int index = 0;
        while (index < startingNeighbors.size()) {
            Point current = startingNeighbors.get(index);
            PointStatus pointStatus = visited.get(current.toString());
            if (pointStatus == null) {
                List<Point> currentNeighbors = getDensityReachableNeighbors(current, allPoints);
                if (currentNeighbors.size() >= minPts) {
                    startingNeighbors = fusion(startingNeighbors, currentNeighbors);
                }
            }

            if (pointStatus != PointStatus.CLUSTERED) {
                visited.put(current.toString(), PointStatus.CLUSTERED);
                cluster.addToCluster(current);
            }
            index++;
        }
        System.out.println("Centroid = " + cluster.center());
        return cluster;
    }


    private List<Point> fusion(List<Point> one, List<Point> two) {

        final Set<Point> oneSet = new HashSet<>(one);
        for (Point item : two) {
            if (!oneSet.contains(item)) {
                one.add(item);
            }
        }
        return one;
    }


    private List<Point> getDensityReachableNeighbors(Point point, Collection<Point> points) {
        List<Point> neighbors = new ArrayList<>();
        for (Point neighbor : points) {
            double dst = Point.distance(point, neighbor, 2);
            if (point != neighbor && dst <= epsilon) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }


    public double intra() {
        if (clusters.size() == 0)
            return -1;
        double sum = 0;
        for (Cluster cl : clusters) {
            sum += cl.score();
        }
        return sum;
    }

    public double inter() {
        if (clusters == null)
            return -1;

        Cluster allGs = new Cluster();
        for (Cluster cl : clusters) {
            allGs.addToCluster(cl.center());
        }

        return allGs.score();

    }

    public List<Cluster> clusters() {
        return clusters;
    }
}