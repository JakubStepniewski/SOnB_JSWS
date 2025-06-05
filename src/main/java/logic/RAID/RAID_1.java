package logic.RAID;

import logic.Disk;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class RAID_1 {
    private final List<Disk> diskList;

    public RAID_1(List<Disk> disks) {
        if (disks.size() < 4) {
            throw new IllegalArgumentException("RAID 1 requires at least 4 disks.");
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
            String data = textArea.getText();
            writeDataInternal(data);
            dialogWindow.close();
        });

        dialogVBox.getChildren().addAll(textArea, btnSave);
        Scene scene = new Scene(dialogVBox, 400, 250);
        dialogWindow.setScene(scene);
        dialogWindow.show();
    }

    private synchronized void writeDataInternal(String data) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i < diskList.size(); i++) {
            Disk disk = diskList.get(i);
            if (disk.isActive()) {
                disk.write(dataBytes);
                System.out.println("Written to disk " + i);
            } else {
                System.out.println("Skipped disk " + i + " (inactive)");
            }
        }

        System.out.println("RAID 1 write completed (partial if some disks were inactive).");
    }

    public synchronized void readData() {
        System.out.println("RAID 1 – Reading with sector-level recovery");

        int numSectors = diskList.get(0).getNumSectors();
        int sectorSize = diskList.get(0).getSectorSize();

        for (int i = 0; i < diskList.size(); i++) {
            Disk target = diskList.get(i);
            if (!target.isActive()) continue;

            for (int s = 0; s < numSectors; s++) {
                if (!target.isSectorFull(s) || target.isSectorBad(s)) {
                    for (int j = 0; j < diskList.size(); j++) {
                        if (i == j) continue;
                        Disk source = diskList.get(j);

                        if (source.isActive() && source.isSectorFull(s) && !source.isSectorBad(s)) {
                            byte[] data = source.readSector(s);
                            target.writeSector(s, data);
                            System.out.println("Reconstructed sector " + s + " on disk " + i + " from disk " + j);
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < diskList.size(); i++) {
            Disk d = diskList.get(i);
            if (d.isActive()) {
                byte[] data = d.read();
                if (data.length > 0 && !isEmptyOrZero(data)) {
                    showTextDialog("RAID 1 – Read Data (Disk " + i + ")", new String(data, StandardCharsets.UTF_8).trim());
                    return;
                }
            }
        }

        showTextDialog("RAID 1 – Read Data", "[no data found]");
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
        showTextDialog("RAID 1 – Load Simulation",
                String.format("Threads: %d\nRepetitions/thread: %d\nData size: %d bytes\n\nTotal time: %.2f ms",
                        threads, repetitionsPerThread, dataSize, totalMs));
    }

    private void resetDisks() {
        for (Disk disk : diskList) {
            disk.reset();
        }
    }

    private boolean isEmptyOrZero(byte[] data) {
        if (data == null || data.length == 0) return true;
        for (byte b : data) {
            if (b != 0) return false;
        }
        return true;
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
