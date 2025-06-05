package logic.RAID;

import javafx.scene.control.TextArea;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;

import logic.Disk;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RAID_3 {
    private final List<Disk> diskList;
    private final int parityIndex;
    private final int sectorSize;

    public RAID_3(List<Disk> disks) {
        if (disks.size() < 3) {
            throw new IllegalArgumentException("RAID 3 requires at least 3 disks (2 data + 1 parity).");
        }
        this.diskList = disks;
        this.parityIndex = disks.size() - 1;
        this.sectorSize = disks.get(0).getSectorSize();
    }

    public void writeData() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Write Data");

        TextArea textArea = new TextArea();
        textArea.setPromptText("Enter the data to write...");
        textArea.setWrapText(true);

        Button btnSave = new Button("Save");
        btnSave.setOnAction(e -> {
            writeDataInternal(textArea.getText());
            dialog.close();
        });

        VBox layout = new VBox(10, textArea, btnSave);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 250);
        dialog.setScene(scene);
        dialog.show();
    }

    private synchronized void writeDataInternal(String data) {
        byte[] dataBytes = data.getBytes();
        int dataDisks = diskList.size() - 1;
        int i = 0;
        long startTime = System.nanoTime();

        while (i < dataBytes.length) {
            byte[][] chunks = new byte[dataDisks][sectorSize];
            byte[] parityChunk = new byte[sectorSize];

            for (int j = 0; j < dataDisks; j++) {
                int remaining = dataBytes.length - i;
                int blockSize = Math.min(sectorSize, remaining);

                if (blockSize > 0) {
                    System.arraycopy(dataBytes, i, chunks[j], 0, blockSize);
                    i += blockSize;

                    for (int b = 0; b < sectorSize; b++) {
                        parityChunk[b] ^= chunks[j][b];
                    }

                    diskList.get(j).write(chunks[j]);
                }
            }

            diskList.get(parityIndex).write(parityChunk);
        }

        long endTime = System.nanoTime();
        double elapsedMillis = (endTime - startTime) / 1_000_000.0;
        System.out.printf("RAID 3 – Write time: %.3f ms%n", elapsedMillis);
    }

    public synchronized void readData() {
        int dataDisks = diskList.size() - 1;
        int numSectors = diskList.get(0).getNumSectors();
        StringBuilder result = new StringBuilder();

        for (int s = 0; s < numSectors; s++) {
            int missingIndex = -1;
            byte[][] sectorData = new byte[diskList.size()][];

            for (int d = 0; d < diskList.size(); d++) {
                Disk disk = diskList.get(d);
                if (!disk.isActive() || disk.isSectorBad(s)) {
                    if (d == parityIndex) continue;
                    if (missingIndex == -1) {
                        missingIndex = d;
                    } else {
                        showTextDialog("RAID 3 ERROR", "More than one failed data disk at sector " + s + " – cannot recover.");
                        return;
                    }
                } else {
                    sectorData[d] = disk.readSector(s);
                }
            }

            if (missingIndex != -1) {
                byte[] reconstructed = new byte[sectorSize];
                for (int i = 0; i < sectorSize; i++) {
                    byte xor = 0;
                    for (int d = 0; d < diskList.size(); d++) {
                        if (d == parityIndex || d == missingIndex || sectorData[d] == null) continue;
                        xor ^= sectorData[d][i];
                    }
                    xor ^= diskList.get(parityIndex).readSector(s)[i];
                    reconstructed[i] = xor;
                }
                sectorData[missingIndex] = reconstructed;
                diskList.get(missingIndex).writeSector(s, reconstructed);
            } else {
                byte[] parityCheck = new byte[sectorSize];
                for (int d = 0; d < dataDisks; d++) {
                    for (int i = 0; i < sectorSize; i++) {
                        parityCheck[i] ^= sectorData[d][i];
                    }
                }

                byte[] actualParity = diskList.get(parityIndex).readSector(s);
                for (int i = 0; i < sectorSize; i++) {
                    if (parityCheck[i] != actualParity[i]) {
                        for (int suspect = 0; suspect < dataDisks; suspect++) {
                            byte[] reconstructed = new byte[sectorSize];
                            for (int j = 0; j < sectorSize; j++) {
                                byte xor = actualParity[j];
                                for (int d = 0; d < dataDisks; d++) {
                                    if (d != suspect) {
                                        xor ^= sectorData[d][j];
                                    }
                                }
                                reconstructed[j] = xor;
                            }
                            sectorData[suspect] = reconstructed;
                            diskList.get(suspect).writeSector(s, reconstructed);
                            break;
                        }
                        break;
                    }
                }
            }

            for (int d = 0; d < dataDisks; d++) {
                if (sectorData[d] != null) {
                    result.append(new String(sectorData[d], StandardCharsets.UTF_8));
                }
            }
        }

        showTextDialog("RAID 3 – Read Data", result.toString());
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
                        resetDisks(); // dodajemy czyszczenie po każdej operacji
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
        showTextDialog("RAID 3 – Load Simulation",
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


