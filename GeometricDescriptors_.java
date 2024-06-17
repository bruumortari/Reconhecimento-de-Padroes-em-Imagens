import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class GeometricDescriptors_ implements PlugInFilter {

    public int setup(String arg, ImagePlus imp) {
        return DOES_ALL;
    }

    public void run(ImageProcessor ip) {
        GeometricDescriptorsCalculator calculator = new GeometricDescriptorsCalculator();
        calculator.calculate(ip);
        calculator.displayResults();
    }

    public static void main(String[] args) {
        // Carregar uma imagem de exemplo
        ImagePlus imp = IJ.openImage("imgs forma\\apple-1.gif.jpg"); // Altere para o caminho da sua imagem
        if (imp == null) {
            System.err.println("Erro ao abrir a imagem.");
            return;
        }

        // Processar a imagem
        imp.show();
        GeometricDescriptors_ plugin = new GeometricDescriptors_();
        plugin.setup("", imp);
        plugin.run(imp.getProcessor());
    }
}
