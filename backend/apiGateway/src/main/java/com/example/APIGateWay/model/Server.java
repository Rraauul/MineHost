package com.example.APIGateWay.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "t_server")
public class Server {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_server")
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "disk_size", nullable = false)
    private Float diskSize;
    @Column(name = "disk_available", nullable = false)
    private Float diskAvailable;

    @Column(name = "ram", nullable = false)
    private Integer ram;

    @OneToMany(mappedBy = "FkServer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<World> worlds = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setDiskSize(Float diskSize) {
        this.diskSize = diskSize;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Float getDiskSize() {
        return diskSize;
    }

    public Integer getId() {
        return id;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getName() {
        return name;
    }

    public Integer getRam() {
        return ram;
    }

    public List<World> getWorlds() {
        return new ArrayList<>(this.worlds);
    }

    public void setWorlds(List<World> worlds) {
        this.worlds = worlds;
    }

    public void setDiskAvailable(Float diskAvailable) {
        this.diskAvailable = diskAvailable;
    }

    public Float getDiskAvailable() {
        return diskAvailable;
    }
}