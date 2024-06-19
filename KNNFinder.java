import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class KNNFinder {

    public static class Neighbor {
        public final double distance;
        public final String imageName;
        public final int index;

        public Neighbor(double distance, String imageName, int index) {
            this.distance = distance;
            this.imageName = imageName;
            this.index = index;
        }
        public int getIndex() {
            return index;
        }

        public double getDistance() {
            return distance;
        }
    }

    public static Neighbor[] findKNearestNeighbors(double[] refVector, List<double[]> featureVectors, int k, String distanceMetric) {
        PriorityQueue<Neighbor> queue = new PriorityQueue<>(Comparator.comparingDouble(n -> n.distance));

        for (int i = 0; i < featureVectors.size(); i++) {
            double[] featureVector = featureVectors.get(i);
            double distance = calculateDistance(refVector, featureVector, distanceMetric);
            queue.add(new Neighbor(distance, "Image " + (i + 1), i));
        }

        Neighbor[] neighbors = new Neighbor[k];
        for (int i = 0; i < k; i++) {
            neighbors[i] = queue.poll();
        }

        return neighbors;
    }

    private static double calculateDistance(double[] v1, double[] v2, String distanceMetric) {
        if ("euclidean".equals(distanceMetric)) {
            return euclideanDistance(v1, v2);
        } else if ("manhattan".equals(distanceMetric)) {
            return manhattanDistance(v1, v2);
        } else {
            throw new IllegalArgumentException("Unknown distance metric: " + distanceMetric);
        }
    }

    private static double euclideanDistance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.pow(v1[i] - v2[i], 2);
        }
        return Math.sqrt(sum);
    }

    private static double manhattanDistance(double[] v1, double[] v2) {
        double sum = 0.0;
        for (int i = 0; i < v1.length; i++) {
            sum += Math.abs(v1[i] - v2[i]);
        }
        return sum;
    }
}
