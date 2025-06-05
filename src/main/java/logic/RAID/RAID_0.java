package logic.RAID;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import logic.Disk;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RAID_0 {
    private final List<Disk> diskList;

    public RAID_0(List<Disk> disks) {
        if (disks.size() < 2) {
            throw new IllegalArgumentException("RAID 0 requires at least 2 disks.");
        }
        this.diskList = disks;
    }

    public void writeData() {
        Stage dialogWindow = new Stage();
        dialogWindow.initModality(Modality.APPLICATION_MODAL);
        dialogWindow.setTitle("Write Data");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(10));

        TextArea textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setPromptText("Enter multi-line data here...");

        Button btnSave = new Button("Save");
        btnSave.setOnAction(event -> {
            writeDataInternal(textArea.getText());
            dialogWindow.close();
        });

        dialogVBox.getChildren().addAll(textArea, btnSave);
        Scene scene = new Scene(dialogVBox, 400, 250);
        dialogWindow.setScene(scene);
        dialogWindow.show();
    }

    private synchronized void writeDataInternal(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        int sectorSize = diskList.get(0).getSectorSize();

        List<Disk> activeDisks = diskList.stream()
                .filter(Disk::isActive)
                .toList();

        if (activeDisks.size() < 2) {
            System.out.println("Not enough active disks for RAID 0 write.");
            return;
        }

        long startTime = System.nanoTime();
        int i = 0;
        while (i < dataBytes.length) {
            for (Disk disk : activeDisks) {
                if (i >= dataBytes.length) break;

                int remaining = dataBytes.length - i;
                int blockSize = Math.min(sectorSize, remaining);
                byte[] chunk = new byte[blockSize];
                System.arraycopy(dataBytes, i, chunk, 0, blockSize);

                disk.write(chunk);
                i += blockSize;
            }
        }
        long endTime = System.nanoTime();
        double elapsedMillis = (endTime - startTime) / 1_000_000.0;
        System.out.printf("RAID 0 – Write time: %.3f ms%n", elapsedMillis);
    }

    public synchronized void readData() {
        StringBuilder result = new StringBuilder();
        int sectorSize = diskList.get(0).getSectorSize();
        int maxLength = 0;

        byte[][] allData = new byte[diskList.size()][];
        for (int i = 0; i < diskList.size(); i++) {
            allData[i] = diskList.get(i).read();
            maxLength = Math.max(maxLength, allData[i].length);
        }

        for (int i = 0; ; i++) {
            boolean anyData = false;
            for (byte[] diskData : allData) {
                int start = i * sectorSize;
                if (start + sectorSize <= diskData.length) {
                    result.append(new String(diskData, start, sectorSize, StandardCharsets.UTF_8));
                    anyData = true;
                }
            }
            if (!anyData) break;
        }

        showTextDialog("RAID 0 – Read Data", result.toString());
    }

    public void simulateLoadMultithreaded(int threads, int repetitionsPerThread, int dataSize) {
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        Random random = new Random();
        long startTime = System.nanoTime();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                for (int i = 0; i < repetitionsPerThread; i++) {
                    byte[] data = new byte[dataSize];
                    random.nextBytes(data);
                    String input = new String(data, StandardCharsets.UTF_8);

                    synchronized (this) {
                        writeDataInternal(input);
                        readData();
                        resetDisks();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        double totalMs = (endTime - startTime) / 1_000_000.0;
        showTextDialog("RAID 0 – Load Simulation",
                String.format("Threads: %d\nRepetitions/thread: %d\nData size: %d bytes\n\nTotal time: %.2f ms",
                        threads, repetitionsPerThread, dataSize, totalMs));
    }

    private void resetDisks() {
        for (Disk disk : diskList) {
            disk.reset();
        }
    }

    private void showTextDialog(String title, String content) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);

        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setEditable(false);

        Button btnClose = new Button("Close");
        btnClose.setOnAction(e -> dialog.close());

        VBox layout = new VBox(10, textArea, btnClose);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 500, 300);
        dialog.setScene(scene);
        dialog.show();
    }
}