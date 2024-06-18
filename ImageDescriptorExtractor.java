import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.AutoThresholder;
import ij.process.ImageProcessor;

public class ImageDescriptorExtractor {

    public static double[] extractDescriptors(ImagePlus image) {
        ImageProcessor ip = image.getProcessor();

        // Cria uma instância de AutoThresholder
        AutoThresholder autoThresholder = new AutoThresholder();

        // Aplica thresholding usando método de Otsu
        AutoThresholder.Method method = AutoThresholder.Method.Otsu;
        int[] histogram = ip.getHistogram();
        int thresholdValue = autoThresholder.getThreshold(method, histogram);

        ip.threshold(thresholdValue);

        ResultsTable rt = new ResultsTable();
        ParticleAnalyzer pa = new ParticleAnalyzer(ParticleAnalyzer.SHOW_RESULTS, ParticleAnalyzer.ALL_STATS, rt, 0,
                Double.MAX_VALUE);
        pa.analyze(image);

        if (rt.getCounter() == 0) {
            throw new IllegalArgumentException("No particles found in the image.");
        }

        double[] descriptors = new double[4];
        double area = rt.getValue("Area", 0);
        double perimeter = rt.getValue("Perim.", 0);
        double major = rt.getValue("Major", 0);
        double minor = rt.getValue("Minor", 0);

        // Circularidade
        descriptors[0] = 4 * Math.PI * area / (perimeter * perimeter);
        // Arredondamento
        descriptors[1] = 4 * area / (Math.PI * major * major);
        // Diâmetro Efetivo
        descriptors[2] = Math.sqrt(4 * area / Math.PI);
        // Razão de Raio
        descriptors[3] = major / minor;

        return descriptors;
    }
}
