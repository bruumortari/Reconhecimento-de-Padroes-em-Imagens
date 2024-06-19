import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import java.util.ArrayList;

public class ImageDescriptorExtractor {

    public static double[] extractDescriptors(ImagePlus image) {
        ImageProcessor ip = image.getProcessor();

        // Pré-processamento da imagem (suavização, etc.)
        ip = preprocessImage(ip);

        // Aplica um filtro Sobel para detectar bordas
        ImageProcessor edgeIp = sobelEdgeDetection(ip);

        // Aplica um limiar adaptativo
        edgeIp = applyAdaptiveThreshold(edgeIp);

        // Identificação de contornos
        ArrayList<int[]> contours = findContours(edgeIp);

        double[] descriptors = new double[4];
        double area = calculateArea(contours);
        double perimeter = calculatePerimeter(contours);
        double[] majorMinor = calculateMajorMinorAxes(contours);

        // Diâmetro Efetivo
        descriptors[0] = 2 * Math.sqrt(area / Math.PI);
        // Circularidade
        descriptors[1] = 4 * Math.PI * area / (perimeter * perimeter);
        // Arredondamento
        descriptors[2] = 4 * area / (Math.PI * majorMinor[0] * majorMinor[0]);
        // Razão de Raio
        descriptors[3] = majorMinor[0] / majorMinor[1];

        return descriptors;
    }

    private static ImageProcessor preprocessImage(ImageProcessor ip) {
        // Exemplo de suavização antes da detecção de bordas
        ip.blurGaussian(2.0);
        return ip;
    }

    private static ImageProcessor sobelEdgeDetection(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        ByteProcessor result = new ByteProcessor(width, height);

        // Novos kernels Sobel
        int[] gx = { -1, 0, 1, -2, 0, 2, -1, 0, 1 };
        int[] gy = { -1, -2, -1, 0, 0, 0, 1, 2, 1 };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumX = 0, sumY = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = ip.getPixel(x + kx, y + ky);
                        sumX += pixel * gx[(ky + 1) * 3 + (kx + 1)];
                        sumY += pixel * gy[(ky + 1) * 3 + (kx + 1)];
                    }
                }
                int magnitude = (int) Math.min(255, Math.sqrt(sumX * sumX + sumY * sumY));
                result.putPixel(x, y, magnitude);
            }
        }
        return result;
    }

    private static ImageProcessor applyAdaptiveThreshold(ImageProcessor ip) {
        ip.autoThreshold();
        return ip;
    }

    private static ArrayList<int[]> findContours(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        boolean[][] visited = new boolean[height][width];
        ArrayList<int[]> contours = new ArrayList<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (ip.getPixel(x, y) > 0 && !visited[y][x]) {
                    ArrayList<int[]> contour = new ArrayList<>();
                    traceContour(ip, x, y, visited, contour);
                    if (!contour.isEmpty()) {
                        contours.add(contour.stream().flatMapToInt(arr -> java.util.Arrays.stream(arr)).toArray());
                    }
                }
            }
        }
        return contours;
    }

    private static void traceContour(ImageProcessor ip, int startX, int startY, boolean[][] visited,
            ArrayList<int[]> contour) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[] dirX = { 1, 1, 0, -1, -1, -1, 0, 1 };
        int[] dirY = { 0, -1, -1, -1, 0, 1, 1, 1 };
        int x = startX;
        int y = startY;
        int dir = 0;

        do {
            visited[y][x] = true;
            contour.add(new int[] { x, y });
            boolean found = false;
            for (int i = 0; i < 8; i++) {
                int newX = x + dirX[dir];
                int newY = y + dirY[dir];
                if (newX >= 0 && newX < width && newY >= 0 && newY < height && ip.getPixel(newX, newY) > 0
                        && !visited[newY][newX]) {
                    x = newX;
                    y = newY;
                    found = true;
                    break;
                }
                dir = (dir + 1) % 8;
            }
            if (!found) {
                break;
            }
        } while (x != startX || y != startY);
    }

    private static double calculateArea(ArrayList<int[]> contours) {
        double area = 0;
        for (int[] contour : contours) {
            int n = contour.length / 2;
            double contourArea = 0;
            for (int i = 0; i < n; i++) {
                int x1 = contour[2 * i];
                int y1 = contour[2 * i + 1];
                int x2 = contour[2 * ((i + 1) % n)];
                int y2 = contour[2 * ((i + 1) % n) + 1];
                contourArea += x1 * y2 - y1 * x2;
            }
            area += Math.abs(contourArea) / 2.0;
        }
        return area;
    }

    private static double calculatePerimeter(ArrayList<int[]> contours) {
        double perimeter = 0;
        for (int[] contour : contours) {
            int n = contour.length / 2;
            for (int i = 0; i < n; i++) {
                int x1 = contour[2 * i];
                int y1 = contour[2 * i + 1];
                int x2 = contour[2 * ((i + 1) % n)];
                int y2 = contour[2 * ((i + 1) % n) + 1];
                perimeter += Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            }
        }
        return perimeter;
    }

    private static double[] calculateMajorMinorAxes(ArrayList<int[]> contours) {
        double maxDist = 0;
        double minDist = Double.MAX_VALUE;
        for (int[] contour : contours) {
            int n = contour.length / 2;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    int x1 = contour[2 * i];
                    int y1 = contour[2 * i + 1];
                    int x2 = contour[2 * j];
                    int y2 = contour[2 * j + 1];
                    double dist = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                    if (dist > maxDist) {
                        maxDist = dist;
                    }
                    if (dist < minDist) {
                        minDist = dist;
                    }
                }
            }
        }
        return new double[] { maxDist, minDist };
    }
}
