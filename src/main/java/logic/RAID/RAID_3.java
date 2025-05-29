package logic.RAID;

import javafx.scene.control.TextInputDialog;
import logic.Disk;

import java.util.List;
import java.util.Optional;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;


public class RAID_3 {
    private final List<Disk> diskList;
    private final int parityIndex;
    private final int sectorSize;

    public RAID_3(List<Disk> disks) {
        if (disks.size() < 3) {
            throw new IllegalArgumentException("RAID 3 requires at least 3 disks (2 data + 1 parity).");
        }
        this.diskList = disks;
        this.parityIndex = disks.size() - 1; // ostatni dysk = parity
        this.sectorSize = disks.get(0).getSectorSize(); // zakładamy równe sektory
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
            String data = textArea.getText();
            byte[] dataBytes = data.getBytes();
            int dataDisks = diskList.size() - 1;
            int i = 0;

            long startTime = System.nanoTime(); // start pomiaru czasu

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

            long endTime = System.nanoTime(); // koniec pomiaru czasu
            double elapsedMillis = (endTime - startTime) / 1_000_000.0;
            System.out.printf("RAID 3 – Write time: %.3f ms%n", elapsedMillis);

            System.out.println("Data successfully written using RAID 3.");
            dialog.close();
        });

        VBox layout = new VBox(10, textArea, btnSave);
        layout.setPadding(new Insets(10));
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 400, 250);
        dialog.setScene(scene);
        dialog.show();
    }


    public void readData() {
        int dataDisks = diskList.size() - 1;
        int numSectors = diskList.get(0).getNumSectors();
        StringBuilder result = new StringBuilder();

        for (int s = 0; s < numSectors; s++) {
            int missingIndex = -1;
            byte[][] sectorData = new byte[diskList.size()][];

            // Zbieranie danych z sektorów
            for (int d = 0; d < diskList.size(); d++) {
                Disk disk = diskList.get(d);
                if (!disk.isActive() || disk.isSectorBad(s)) {
                    if (d == parityIndex) continue; // pomiń XOR
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

            // Rekonstrukcja brakującego sektora danych
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
            }

            // Sklej dane z dysków danych
            for (int d = 0; d < dataDisks; d++) {
                if (sectorData[d] != null) {
                    result.append(new String(sectorData[d], StandardCharsets.UTF_8));
                }
            }
        }

        showTextDialog("RAID 3 – Read Data", result.toString());
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
