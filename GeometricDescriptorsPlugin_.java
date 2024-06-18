import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.WindowManager;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

class ImageDescriptor {
    private List<String> descriptors;
    private File imageFile;

    public ImageDescriptor(List<String> descriptors, File imageFile) {
        this.descriptors = descriptors;
        this.imageFile = imageFile;
    }

    public List<String> getDescriptors() {
        return descriptors;
    }

    public File getImageFile() {
        return imageFile;
    }
}

public class GeometricDescriptorsPlugin_ implements PlugIn {
    private ImagePlus referenceImage;
    private List<String> allDescriptors = new ArrayList<>(); // Lista para armazenar todos os descritores

    @Override
    public void run(String arg) {
        referenceImage = WindowManager.getCurrentImage();
        if (referenceImage == null) {
            IJ.showMessage("Error", "Nenhuma imagem foi aberta.");
            return;
        }

        File[] searchImages = openSearchImagesDirectory();
        if (referenceImage != null && searchImages != null) {
            int k = getKValue();
            String distanceFunction = getDistanceFunction();
            if (k > 0 && distanceFunction != null) {
                executeExtractionAndSearch(searchImages, k, distanceFunction);
            }
        }
    }

    private File[] openSearchImagesDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = chooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File directory = chooser.getSelectedFile();
            if (directory != null && directory.isDirectory()) {
                // Filtrar apenas arquivos
                return directory.listFiles(File::isFile);
            } else {
                System.err.println("Diretório selecionado inválido.");
            }
        } else {
            System.err.println("Nenhum diretório foi selecionado.");
        }
        return null;
    }

    private int getKValue() {
        String kValue = JOptionPane.showInputDialog("Digite o valor de k:");
        try {
            return Integer.parseInt(kValue);
        } catch (NumberFormatException e) {
            IJ.showMessage("Error", "Valor inválido para k.");
            return -1;
        }
    }

    private String getDistanceFunction() {
        Object[] options = { "Euclidiana", "Manhattan" };
        int choice = JOptionPane.showOptionDialog(null, "Escolha a função de distância", "Função de Distância",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (choice >= 0) {
            return options[choice].toString();
        } else {
            IJ.showMessage("Error", "Nenhuma função de distância foi selecionada.");
            return null;
        }
    }

    private void executeExtractionAndSearch(File[] searchImages, int k, String distanceFunction) {
        GeometricDescriptorsCalculator calculator = new GeometricDescriptorsCalculator();
        List<ImageDescriptor> allSearchDescriptors = new ArrayList<>();

        for (File imageFile : searchImages) {
            ImagePlus searchImage = new ImagePlus(imageFile.getAbsolutePath());
            ImageProcessor searchProcessor = searchImage.getProcessor();
            calculator.calculate(searchProcessor);
            List<String> searchDescriptors = calculator.getResults();

            allSearchDescriptors.add(new ImageDescriptor(searchDescriptors, imageFile));
            allDescriptors.addAll(searchDescriptors);

            logDescriptors("Descritores da imagem " + imageFile.getName() + ":", searchDescriptors);
        }

        ImageProcessor refProcessor = referenceImage.getProcessor();
        calculator.calculate(refProcessor);
        List<String> refDescriptors = calculator.getResults();
        allDescriptors.addAll(refDescriptors);

        List<File> nearestNeighbors = findNearestNeighbors(refDescriptors, allSearchDescriptors, k, distanceFunction);
        displayNearestNeighbors("Resultados de Busca", nearestNeighbors);

        saveAllDescriptorsToFile("all_descriptors.txt");
    }

    private void saveDescriptorsToFile(String fileName, List<String> descriptors) {
        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file)) {
            for (String descriptor : descriptors) {
                writer.write(descriptor + "\n");
            }
            writer.close();
            IJ.log("Descritores salvos com sucesso em: " + file.getAbsolutePath());
        } catch (IOException e) {
            IJ.error("Erro ao salvar os descritores em arquivo: " + e.getMessage());
        }
    }

    private void logDescriptors(String title, List<String> descriptors) {
        System.out.println(title);
        for (String descriptor : descriptors) {
            System.out.println(descriptor);
        }
        System.out.println();
    }

    private void saveAllDescriptorsToFile(String fileName) {
        File file = new File(fileName);
        try (FileWriter writer = new FileWriter(file)) {
            for (String descriptor : allDescriptors) {
                writer.write(descriptor + "\n");
            }
            writer.close();
            IJ.log("Todos os descritores foram salvos com sucesso em: " + file.getAbsolutePath());
        } catch (IOException e) {
            IJ.error("Erro ao salvar os descritores em arquivo: " + e.getMessage());
        }
    }

    private List<File> findNearestNeighbors(List<String> refDescriptors, List<ImageDescriptor> allSearchDescriptors,
            int k, String distanceFunction) {
        List<File> nearestNeighbors = new ArrayList<>();

        if (refDescriptors.isEmpty() || allSearchDescriptors.isEmpty()) {
            System.out.println("Não há descritores suficientes para comparar.");
            return nearestNeighbors;
        }

        PriorityQueue<AbstractMap.SimpleEntry<Double, File>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(AbstractMap.SimpleEntry::getKey));

        NumberFormat format = NumberFormat.getInstance();
        for (ImageDescriptor searchDesc : allSearchDescriptors) {
            double totalDistance = 0;
            try {
                for (int i = 0; i < 4; i++) {
                    double searchValue = format.parse(searchDesc.getDescriptors().get(i).split(":")[1].trim()).doubleValue();
                    double refValue = format.parse(refDescriptors.get(i).split(":")[1].trim()).doubleValue();
                    double distance;
                    if (distanceFunction.equals("Euclidiana")) {
                        distance = euclideanDistance(refValue, searchValue);
                    } else if (distanceFunction.equals("Manhattan")) {
                        distance = manhattanDistance(refValue, searchValue);
                    } else {
                        continue;
                    }
                    totalDistance += distance;
                }

                double averageDistance = totalDistance / refDescriptors.size();
                pq.offer(new AbstractMap.SimpleEntry<>(averageDistance, searchDesc.getImageFile()));
                if (pq.size() > k) {
                    pq.poll();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        while (!pq.isEmpty()) {
            nearestNeighbors.add(pq.poll().getValue());
        }

        Collections.reverse(nearestNeighbors);
        return nearestNeighbors;
    }

    private List<Double> calculateDistances(List<String> refDescriptors, List<String> searchDescriptors,
            String distanceFunction) {
        List<Double> distances = new ArrayList<>();

        NumberFormat format = NumberFormat.getInstance();

        for (String refDesc : refDescriptors) {
            try {
                double refValue = format.parse(refDesc.split(":")[1].trim()).doubleValue();

                for (String searchDesc : searchDescriptors) {
                    double searchValue = format.parse(searchDesc.split(":")[1].trim()).doubleValue();

                    // Calcula a distância conforme a função selecionada
                    if (distanceFunction.equals("Euclidiana")) {
                        distances.add(euclideanDistance(refValue, searchValue));
                    } else if (distanceFunction.equals("Manhattan")) {
                        distances.add(manhattanDistance(refValue, searchValue));
                    }
                }
            } catch (ParseException e) {
                // Trate qualquer exceção de parsing aqui, se necessário
                e.printStackTrace();
            }
        }

        return distances;
    }

    private double euclideanDistance(double value1, double value2) {
        return Math.sqrt(Math.pow(value2 - value1, 2));
    }

    private double manhattanDistance(double value1, double value2) {
        return Math.abs(value2 - value1);
    }

    private List<Integer> findKNearestIndices(List<Double> distances, int k) {
        // Cria uma lista de índices
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < distances.size(); i++) {
            indices.add(i);
        }

        // Ordena os índices com base nas distâncias
        indices.sort(Comparator.comparingDouble(distances::get));

        // Retorna apenas os k primeiros índices
        return indices.subList(0, Math.min(k, indices.size()));
    }

    private void displayNearestNeighbors(String imageName, List<File> nearestNeighbors) {
        StringBuilder sb = new StringBuilder();
        sb.append("K-vizinhos mais próximos para: ").append(imageName).append("\n");
        if (nearestNeighbors.isEmpty()) {
            sb.append("Nenhum vizinho encontrado.");
        } else {
            for (File neighbor : nearestNeighbors) {
                sb.append(neighbor.getName()).append("\n");
            }
        }
        IJ.showMessage("Resultados", sb.toString());
    }
}
