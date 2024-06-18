import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class GeometricDescriptorsCalculator {
    private List<String> results;

    public GeometricDescriptorsCalculator() {
        this.results = new ArrayList<>();
    }

    public void calculate(ImageProcessor ip) {
        if (ip == null) {
            System.out.println("ImageProcessor is null.");
            return;
        }
    
        List<int[]> contours = getContours(ip);
        for (int[] contour : contours) {
            if (contour.length > 5000) {
                System.out.println("Contorno muito grande, ignorando.");
                continue;
            }
    
            IJ.log("+");
            results.add(String.format("Circularidade: %.2f", calculateCircularity(contour)));
            results.add(String.format("Diametro efetivo: %.2f", calculateEffectiveDiameter(contour)));
            results.add(String.format("Arredondamento: %.2f", calculateRoundness(contour)));
            results.add(String.format("Razao de raio: %.2f", calculateAspectRatio(contour)));
    
            System.out.println("Contorno processado: " + contour.length + " pontos");
        }
    
        System.out.println("Total de contornos processados: " + contours.size());
    }    

    private List<int[]> getContours(ImageProcessor ip) {
        List<int[]> contours = new ArrayList<>();
        int width = ip.getWidth();
        int height = ip.getHeight();
        int[] pixels = (int[]) ip.convertToRGB().getPixels();

        boolean[] visited = new boolean[width * height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int index = y * width + x;
                if (!visited[index] && (pixels[index] & 0xff) == 255) { // Verifica se o pixel é branco (255)
                    List<Integer> contour = new ArrayList<>();
                    traceContour(x, y, width, height, pixels, visited, contour);
                    // Verifica se o contorno é válido antes de adicioná-lo
                    if (!contour.isEmpty()) {
                        contours.add(contour.stream().mapToInt(Integer::intValue).toArray());
                    }
                }
            }
        }
        return contours;
    }

    private void traceContour(int startX, int startY, int width, int height, int[] pixels, boolean[] visited,
            List<Integer> contour) {
        Stack<int[]> stack = new Stack<>();
        stack.push(new int[] { startX, startY });

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int x = point[0];
            int y = point[1];
            int index = y * width + x;

            if (x < 0 || x >= width || y < 0 || y >= height || visited[index] || (pixels[index] & 0xff) != 255) {
                continue;
            }

            visited[index] = true;
            contour.add(x);
            contour.add(y);

            stack.push(new int[] { x + 1, y });
            stack.push(new int[] { x - 1, y });
            stack.push(new int[] { x, y + 1 });
            stack.push(new int[] { x, y - 1 });
        }
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

    private double calculateCircularity(int[] contour) {
        double perimeter = calculatePerimeter(contour);
        double area = calculateArea(contour);

        if (perimeter > 0) {
            return (4 * Math.PI * area) / (perimeter * perimeter);
        } else {
            return 0;
        }
    }

    private double calculateEffectiveDiameter(int[] contour) {
        double area = calculateArea(contour);

        if (area > 0) {
            return 2 * Math.sqrt(area / Math.PI);
        } else {
            return 0;
        }
    }

    private double calculateRoundness(int[] contour) {
        double area = calculateArea(contour);
        double majorAxis = calculateMajorAxis(contour);
    
        if (majorAxis > 0) {
            return (4 * area) / (Math.PI * majorAxis * majorAxis);
        } else {
            return 0;
        }
    }

    private double calculateAspectRatio(int[] contour) {
        double majorAxis = calculateMajorAxis(contour);
        double minorAxis = calculateMinorAxis(contour);
    
        if (minorAxis > 0) {
            return majorAxis / minorAxis;
        } else {
            return 0; // Evita divisão por zero
        }
    }
    
    private double calculateMajorAxis(int[] contour) {
        double maxDistance = 0.0;
        for (int i = 0; i < contour.length; i += 2) {
            for (int j = i + 2; j < contour.length; j += 2) {
                double dx = contour[j] - contour[i];
                double dy = contour[j + 1] - contour[i + 1];
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        return maxDistance;
    }
    
    private double calculateMinorAxis(int[] contour) {
        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < contour.length; i += 2) {
            for (int j = i + 2; j < contour.length; j += 2) {
                double dx = contour[j] - contour[i];
                double dy = contour[j + 1] - contour[i + 1];
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < minDistance && distance > 0) {
                    minDistance = distance;
                }
            }
        }
        return minDistance;
    }    

    public List<String> getResults() {
        return results;
    }

    public void displayResults() {
        for (String result : results) {
            System.out.println(result);
        }
    }

    public static void main(String[] args) {
        // Obter a imagem aberta no ImageJ
        ImagePlus image = IJ.getImage();

        // Obter o processador de imagem da imagem
        ImageProcessor ip = image.getProcessor();

        // Criar uma instância do calculador de descritores geométricos
        GeometricDescriptorsCalculator calculator = new GeometricDescriptorsCalculator();

        // Calcular os descritores geométricos para a imagem
        calculator.calculate(ip);

        // Exibir os resultados
        calculator.displayResults();
    }
}
