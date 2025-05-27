package logic.RAID;

import logic.Disk;
import javafx.scene.control.TextInputDialog;
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

public class RAID_1 {
    private final List<Disk> diskList;

    public RAID_1(List<Disk> disks) {
        if (disks.size() < 4) {
            throw new IllegalArgumentException("RAID 1 requires at least 4 disks.");
        }
        this.diskList = disks;
    }

    public void writeData() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Write Data");
        dialog.setHeaderText("Enter the data to write:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(data -> {
            byte[] dataBytes = data.getBytes();

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
        });
    }

    public void readData() {
        System.out.println("RAID 1 – Reading with sector-level recovery");

        int numSectors = diskList.get(0).getNumSectors();
        int sectorSize = diskList.get(0).getSectorSize();

        // Rekonstrukcja sektorów
        for (int i = 0; i < diskList.size(); i += 2) {
            Disk a = diskList.get(i);
            Disk b = diskList.get(i + 1);

            for (int s = 0; s < numSectors; s++) {
                boolean aBad = a.isSectorBad(s);
                boolean bBad = b.isSectorBad(s);
                boolean aFull = a.isSectorFull(s);
                boolean bFull = b.isSectorFull(s);

                if (aBad && bFull && !bBad) {
                    byte[] data = b.readSector(s);
                    a.writeSector(s, data);
                    System.out.println("Reconstructed sector " + s + " on disk " + i);
                } else if (bBad && aFull && !aBad) {
                    byte[] data = a.readSector(s);
                    b.writeSector(s, data);
                    System.out.println("Reconstructed sector " + s + " on disk " + (i + 1));
                }
            }
        }

        // Odczyt danych z pierwszego dostępnego, aktywnego dysku
        for (Disk d : diskList) {
            if (d.isActive()) {
                byte[] data = d.read(); // teraz powinno być naprawione
                if (data.length > 0 && !isEmptyOrZero(data)) {
                    showTextDialog("RAID 1 – Read Data", new String(data).trim());
                    return;
                }
            }
        }

        showTextDialog("RAID 1 – Read Data", "[no data found]");
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
