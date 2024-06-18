import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GeometricDescriptors_ implements PlugIn {

    private ImagePlus referenceImage;

    @Override
    public void run(String arg) {
        try {
            // Abrir imagem de referência
            openReferenceImage();

            if (referenceImage == null) {
                IJ.showMessage("Error", "Nenhuma imagem foi aberta.");
                return;
            }

            // Abrir diretório com imagens de busca
            String searchImagesDir = openSearchImagesDirectory();
            if (searchImagesDir == null) {
                IJ.error("Error", "Failed to open search images directory");
                return;
            }

            // Extrair descritores da imagem de referência
            double[] refDescriptors = extractReferenceDescriptors();

            // Extrair descritores e caminhos das imagens para cada imagem no diretório
            List<double[]> featureVectors = new ArrayList<>();
            List<String> imagePaths = new ArrayList<>();
            extractSearchImageDescriptors(searchImagesDir, featureVectors, imagePaths);

            // Salvar descritores em um arquivo
            saveDescriptorsToFile(refDescriptors, featureVectors, imagePaths);

            // Obter valor de k e métrica de distância do usuário
            int k = Integer.parseInt(IJ.getString("Enter value of k", "3"));
            String distanceMetric = IJ.getString("Enter distance metric (euclidean/manhattan)", "euclidean");

            // Executar busca pelos k-vizinhos mais próximos
            KNNFinder.Neighbor[] neighbors = KNNFinder.findKNearestNeighbors(refDescriptors, featureVectors, k,
                    distanceMetric);

            // Exibir resultados
            displayResults(refDescriptors, neighbors, imagePaths);

        } catch (Exception e) {
            IJ.error("Error", "An error occurred: " + e.getMessage());
        }
    }

    private void openReferenceImage() {
        referenceImage = WindowManager.getCurrentImage();
    }

    private String openSearchImagesDirectory() {
        DirectoryChooser dc = new DirectoryChooser("Select directory with search images");
        return dc.getDirectory();
    }

    private double[] extractReferenceDescriptors() {
        return ImageDescriptorExtractor.extractDescriptors(referenceImage);
    }

    private void extractSearchImageDescriptors(String searchImagesDir, List<double[]> featureVectors, List<String> imagePaths) {
        for (String path : Objects.requireNonNull(new java.io.File(searchImagesDir).list())) {
            ImagePlus image = IJ.openImage(searchImagesDir + path);
            if (image != null) {
                try {
                    double[] descriptors = ImageDescriptorExtractor.extractDescriptors(image);
                    featureVectors.add(descriptors);
                    imagePaths.add(path); // Armazenar o caminho da imagem
                } catch (IllegalArgumentException e) {
                    IJ.log("Skipped image " + path + " due to: " + e.getMessage());
                }
            }
        }
    }

    private void saveDescriptorsToFile(double[] refDescriptors, List<double[]> featureVectors, List<String> imagePaths) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("descriptors.txt"))) {
            writer.write("Reference Image:\n");
            writer.write(Arrays.toString(refDescriptors) + "\n");

            writer.write("Search Images:\n");
            for (int i = 0; i < featureVectors.size(); i++) {
                writer.write(imagePaths.get(i) + ": " + Arrays.toString(featureVectors.get(i)) + "\n");
            }
        }
    }

    private void displayResults(double[] refDescriptors, KNNFinder.Neighbor[] neighbors, List<String> imagePaths) {
        StringBuilder result = new StringBuilder("Reference Vector: " + Arrays.toString(refDescriptors) + "\n");
        result.append("K Nearest Neighbors:\n");
        for (KNNFinder.Neighbor neighbor : neighbors) {
            int index = neighbor.getIndex(); // Obter índice do vizinho na lista de vetores de características
            String imageName = imagePaths.get(index); // Obter nome real da imagem correspondente ao vizinho
            double distance = neighbor.getDistance(); // Obter distância do vizinho
            result.append("Image: ").append(imageName).append(", Distance: ").append(distance).append("\n");
        }

        IJ.showMessage("K-Nearest Neighbors", result.toString());
    }
}
