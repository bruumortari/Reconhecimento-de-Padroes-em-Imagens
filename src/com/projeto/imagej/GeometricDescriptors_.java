import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;

public class GeometricDescriptors_ implements PlugIn {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Override
    public void run(String arg) {
        // Carregar a imagem usando ImageJ
        ImagePlus imp = IJ.openImage("path/to/your/urso.png");
        if (imp == null) {
            IJ.showMessage("Erro", "Não foi possível abrir a imagem.");
            return;
        }
        ImageProcessor ip = imp.getProcessor();

        // Converter ImageProcessor para Mat
        ImageProcessorToMatConverter converter = new ImageProcessorToMatConverter();
        Mat image = converter.convert(ip);

        // Processar a imagem e calcular descritores geométricos
        GeometricDescriptorsCalculator calculator = new GeometricDescriptorsCalculator(image);
        calculator.calculate();

        // Exibir os resultados
        calculator.displayResults();
    }
}
