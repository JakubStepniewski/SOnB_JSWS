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
        int parityIndex = diskList.size() - 1;
        int sectorSize = diskList.get(0).getSectorSize();

        // Zbieraj dane z dysków
        byte[][] data = new byte[diskList.size()][];
        int missingIndex = -1;

        for (int i = 0; i < diskList.size(); i++) {
            data[i] = diskList.get(i).read();
            if (i != parityIndex && data[i].length == 0 && missingIndex == -1) {
                missingIndex = i;
            } else if (i != parityIndex && data[i].length == 0 && missingIndex != -1) {
                showTextDialog("RAID 3 ERROR", "Too many failed disks! Cannot recover.");
                return;
            }
        }

        // Rekonstrukcja
        if (missingIndex != -1) {
            System.out.println("Auto-reconstructing disk " + missingIndex + " using XOR.");

            int blockCount = data[parityIndex].length;
            byte[] recovered = new byte[blockCount];

            for (int i = 0; i < blockCount; i++) {
                byte xor = data[parityIndex][i];
                for (int j = 0; j < diskList.size(); j++) {
                    if (j != parityIndex && j != missingIndex && i < data[j].length) {
                        xor ^= data[j][i];
                    }
                }
                recovered[i] = xor;
            }

            data[missingIndex] = recovered;
            diskList.get(missingIndex).write(recovered);
            System.out.println("Disk " + missingIndex + " reconstructed.");
        }

        // Sklej dane z dysków danych
        StringBuilder result = new StringBuilder();
        int i = 0;
        boolean dataLeft = true;

        while (dataLeft) {
            dataLeft = false;
            for (int d = 0; d < diskList.size(); d++) {
                if (d == parityIndex) continue;
                byte[] dData = data[d];
                if (i + sectorSize <= dData.length) {
                    result.append(new String(dData, i, sectorSize));
                    dataLeft = true;
                }
            }
            i += sectorSize;
        }

        showTextDialog("RAID 3 – Read Data", result.toString().trim());
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
