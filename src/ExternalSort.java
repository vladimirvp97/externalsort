import java.io.*;
import java.util.*;

public class ExternalSort {

    private static String headerRow;

    public static void main(String[] args) throws IOException {
        String inputFile = "src/resources/data/largefile.csv";
        String outputFile = "src/resources/data/sorted_largefile.csv";
        String tempFileDirectory = "src/resources/tempFiles/";
        int chunkSize = 10; // количество строк, которое помещается в память

        List<File> tempFiles = splitAndSortTempFiles(inputFile, tempFileDirectory, chunkSize);

        mergeSortedFiles(tempFiles, outputFile);
    }

    private static List<File> splitAndSortTempFiles(String inputFile, String tempFileDirectory, int chunkSize) throws IOException {
        List<File> tempFiles = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            List<String> lines = new ArrayList<>();
            headerRow = reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
                if (lines.size() == chunkSize) {
                    tempFiles.add(sortAndSave(lines, tempFileDirectory));
                    lines.clear();
                }
            }
            if (!lines.isEmpty()) {
                tempFiles.add(sortAndSave(lines, tempFileDirectory));
            }
        }
        return tempFiles;
    }

    private static File sortAndSave(List<String> lines, String tempFileDirectory) throws IOException {
        lines.sort(Comparator.comparingInt(line -> Integer.parseInt(line.split(",")[0])));
        File tempFile = File.createTempFile("sorted_", ".csv", new File(tempFileDirectory));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {

            for (int i = 0; i < lines.size(); ++i) {
                writer.write(lines.get(i));
                if(i == lines.size() - 1) break;
                writer.newLine();
            }
        }
        return tempFile;
    }

    private static void mergeSortedFiles(List<File> tempFiles, String outputFile) throws IOException {
        PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparingInt(entry -> Integer.parseInt(entry.line.split(",")[0])));

        for (File tempFile : tempFiles) {
            BufferedReader reader = new BufferedReader(new FileReader(tempFile));
            String line = reader.readLine();
            if (line != null) {
                queue.add(new Entry(reader, line));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(headerRow);
            writer.newLine();

            while (!queue.isEmpty()) {
                Entry minEntry = queue.poll();
                writer.write(minEntry.line);
                writer.newLine();

                String nextLine = minEntry.reader.readLine();
                if (nextLine != null) {
                    queue.add(new Entry(minEntry.reader, nextLine));
                } else {
                    minEntry.reader.close();
                }
            }
        }
    }

    private static class Entry {
        BufferedReader reader;
        String line;

        Entry(BufferedReader reader, String line) {
            this.reader = reader;
            this.line = line;
        }
    }



}
