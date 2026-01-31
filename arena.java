package com.frostnw.duels.models;

import org.bukkit.Location;

public class Arena {
    
    private String name;
    private Location spawn1;
    private Location spawn2;
    private Location pos1; // Region pos1
    private Location pos2; // Region pos2
    private boolean available = true;
    
    public Arena(String name) {
        this.name = name;
    }
    
    // Getter ve Setter'lar
    public String getName() { return name; }
    public Location getSpawn1() { return spawn1; }
    public Location getSpawn2() { return spawn2; }
    public boolean isAvailable() { return available; }
    
    public void setSpawn1(Location loc) { this.spawn1 = loc; }
    public void setSpawn2(Location loc) { this.spawn2 = loc; }
    public void setAvailable(boolean available) { this.available = available; }
}
