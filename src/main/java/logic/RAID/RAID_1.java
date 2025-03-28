package logic.RAID;

import logic.Disk;
import javafx.scene.control.TextInputDialog;
import java.util.List;
import java.util.Optional;

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

            // Zapisywanie na Dysk 1 i Dysk 2 (pierwsza kopia)
            diskList.get(0).write(dataBytes);
            diskList.get(1).write(dataBytes);

            // Zapisywanie na Dysk 3 i Dysk 4 (druga kopia)
            diskList.get(2).write(dataBytes);
            diskList.get(3).write(dataBytes);

            System.out.println("Data successfully written using RAID 1.");
        });
    }

    public void readData() {
        // Odczytujemy dane z pierwszej pary (Dysk 1)
        byte[] data = diskList.get(0).read();
        if (data != null) {
            System.out.println("Data read from RAID 1: " + new String(data));
        } else {
            System.out.println("No data found on RAID 1.");
        }
    }
}