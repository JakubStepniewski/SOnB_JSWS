package logic;

import java.util.List;

public class RAID_1 {
    private final List<Disk> disks;
    public RAID_1(List<Disk> disks)
    {
        if(disks.size() != 4)
        {
            throw new IllegalArgumentException("4 disks required!");
        }
        this.disks = disks;
    }
    public void write(int sector){}



}