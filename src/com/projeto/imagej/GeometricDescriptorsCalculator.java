import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;
import ij.IJ;

public class GeometricDescriptorsCalculator {
    private Mat image;
    private List<String> results;

    public GeometricDescriptorsCalculator(Mat image) {
        this.image = image;
        this.results = new ArrayList<>();
    }

    public void calculate() {
        // Converter a imagem para escala de cinza
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Binarizar a imagem
        Mat binaryImage = new Mat();
        Imgproc.threshold(grayImage, binaryImage, 0, 255, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // Encontrar contornos na imagem binarizada
        Mat contoursImage = binaryImage.clone();
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(contoursImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Para cada contorno encontrado, calcular os descritores geométricos
        for (MatOfPoint contour : contours) {
            // Perímetro
            double perimeter = Imgproc.arcLength(new MatOfPoint2f(contour.toArray()), true);
            // Area
            double area = Imgproc.contourArea(contour);
            // Retângulo delimitador
            RotatedRect boundingBox = Imgproc.minAreaRect(new MatOfPoint2f(contour.toArray()));
            double minorAxis = Math.min(boundingBox.size.width, boundingBox.size.height);
            double majorAxis = Math.max(boundingBox.size.width, boundingBox.size.height);
            // Diâmetro efetivo
            double effectiveDiameter = Math.sqrt(4 * area / Math.PI);
            // Circularidade
            double circularity = (4 * Math.PI * area) / Math.pow(perimeter, 2);
            // Arredondamento
            double roundness = 4 * area / (Math.PI * Math.pow(majorAxis, 2));
            // Razão de raio
            double aspectRatio = majorAxis / minorAxis;

            // Adicionar resultados à lista
            results.add(String.format("Perimetro: %.2f", perimeter));
            results.add(String.format("Area: %.2f", area));
            results.add(String.format("Menor eixo: %.2f", minorAxis));
            results.add(String.format("Maior eixo: %.2f", majorAxis));
            results.add(String.format("Diametro efetivo: %.2f", effectiveDiameter));
            results.add(String.format("Circularidade: %.2f", circularity));
            results.add(String.format("Arredondamento: %.2f", roundness));
            results.add(String.format("Razao de raio: %.2f", aspectRatio));
        }
    }

    public void displayResults() {
        for (String result : results) {
            IJ.log(result);
        }
    }
}
