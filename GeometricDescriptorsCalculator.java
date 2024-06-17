import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import java.util.ArrayList;
import java.util.List;

public class GeometricDescriptorsCalculator {
    private List<String> results;

    public GeometricDescriptorsCalculator() {
        this.results = new ArrayList<>();
    }

    public void calculate(ImageProcessor ip) {
        List<int[]> contours = getContours(ip);

        for (int[] contour : contours) {
            double perimeter = calculatePerimeter(contour);
            double area = calculateArea(contour);
            double[] boundingBox = calculateBoundingBox(contour);

            results.add(String.format("Perimeter: %.2f", perimeter));
            results.add(String.format("Area: %.2f", area));
            results.add(String.format("BoundingBox: %.2f, %.2f, %.2f, %.2f", boundingBox[0], boundingBox[1], boundingBox[2], boundingBox[3]));
        }
    }

    private List<int[]> getContours(ImageProcessor ip) {
        List<int[]> contours = new ArrayList<>();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[] pixels = (int[]) ip.getPixels();
        boolean[] visited = new boolean[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = y * width + x;
                if (!visited[index] && pixels[index] == 255) {
                    List<Integer> contour = new ArrayList<>();
                    traceContour(x, y, width, height, pixels, visited, contour);
                    contours.add(contour.stream().mapToInt(Integer::intValue).toArray());
                }
            }
        }
        return contours;
    }

    private void traceContour(int x, int y, int width, int height, int[] pixels, boolean[] visited, List<Integer> contour) {
        int index = y * width + x;
        if (x < 0 || x >= width || y < 0 || y >= height || visited[index] || pixels[index] != 255) {
            return;
        }
        visited[index] = true;
        contour.add(x);
        contour.add(y);
        traceContour(x + 1, y, width, height, pixels, visited, contour);
        traceContour(x - 1, y, width, height, pixels, visited, contour);
        traceContour(x, y + 1, width, height, pixels, visited, contour);
        traceContour(x, y - 1, width, height, pixels, visited, contour);
    }

    private double calculatePerimeter(int[] contour) {
        double perimeter = 0.0;
        for (int i = 0; i < contour.length; i += 2) {
            int x1 = contour[i];
            int y1 = contour[i + 1];
            int x2 = contour[(i + 2) % contour.length];
            int y2 = contour[(i + 3) % contour.length];
            perimeter += Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
        return perimeter;
    }

    private double calculateArea(int[] contour) {
        double area = 0.0;
        for (int i = 0; i < contour.length; i += 2) {
            int x1 = contour[i];
            int y1 = contour[i + 1];
            int x2 = contour[(i + 2) % contour.length];
            int y2 = contour[(i + 3) % contour.length];
            area += (x1 * y2 - x2 * y1);
        }
        return Math.abs(area / 2.0);
    }

    private double[] calculateBoundingBox(int[] contour) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < contour.length; i += 2) {
            double x = contour[i];
            double y = contour[i + 1];
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        return new double[]{minX, minY, maxX, maxY};
    }

    public void displayResults() {
        for (String result : results) {
            System.out.println(result);
        }
    }

    public static void main(String[] args) {
        GeometricDescriptorsCalculator calculator = new GeometricDescriptorsCalculator();
        calculator.calculate(ip);
        calculator.displayResults();
    }
}
