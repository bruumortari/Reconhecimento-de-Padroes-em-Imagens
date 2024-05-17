import java.util.ArrayList;
import java.util.List;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class GeometricDescriptors {
    public static void main(String[] args) {
        // Carregar a imagem
        Mat image = Imgcodecs.imread("urso.png");

        // Converter a imagem para escala de cinza
        Mat grayImage = new Mat();
        Imgproc.cvtColor(image, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Binarizar a imagem (opcional, dependendo do que você quer)
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
            // Área
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

            // Exibir os resultados
            System.out.println("Perímetro: " + perimeter);
            System.out.println("Área: " + area);
            System.out.println("Menor eixo: " + minorAxis);
            System.out.println("Maior eixo: " + majorAxis);
            System.out.println("Diâmetro efetivo: " + effectiveDiameter);
            System.out.println("Circularidade: " + circularity);
            System.out.println("Arredondamento: " + roundness);
            System.out.println("Razão de raio: " + aspectRatio);
        }
    }
}
