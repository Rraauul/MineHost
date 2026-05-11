

package mineHost.dto;

import java.time.LocalDateTime;

public class ServerDTO {
    private Integer id;
    private String name;
    private String ipAddress;
    private Integer ram;
    private Float disk_size;
    private Float disk_available;

    public ServerDTO(Integer id, String name, String ipAddress, Integer ram, Float disk_size, Float disk_available) {

        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.ram = ram;
        this.disk_size = disk_size;
        this.disk_available = disk_available;
    }

    // Getters et setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Float getDisk_size() {
        return disk_size;
    }

    public void setDisk_size(Float disk_size) {
        this.disk_size = disk_size;
    }

    public Float getDisk_available() {
        return disk_available;
    }

    public void setDisk_available(Float disk_available) {
        this.disk_available = disk_available;
    }

}
