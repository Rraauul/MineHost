package com.example.APIGateWay.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_world")
public class World {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_world")
    private Integer id;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "template", nullable = false, length = 45)
    private String template;

    @Column(name = "ram", nullable = false)
    private Integer ram;

    @Column(name = "size", nullable = false)
    private float size;

    @Column(name = "status", nullable = false, length = 45)
    private String status;

    @Column(name = "addressNgrok", nullable = true, length = 255)
    private String addressNgrok;
    @Column(name = "localPort", nullable = false, length = 45)
    private long localPort;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "FK_user", nullable = false)
    private Integer fkUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_server", nullable = false)
    private Server FkServer;

    public void setName(String name) {
        this.name = name;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setAddressNgrok(String addressNgrok) {
        this.addressNgrok = addressNgrok;
    }

    public void setLocalPort(long localPort) {
        this.localPort = localPort;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public void setFkServer(Server fkServer) {
        this.FkServer = fkServer;
    }

    public void setFkUser(Integer fkUser) {
        this.fkUser = fkUser;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTemplate() {
        return template;
    }

    public Integer getRam() {
        return ram;
    }

    public float getSize() {
        return size;
    }

    public String getStatus() {
        return status;
    }

    public String getAddressNgrok() {
        return addressNgrok;
    }

    public long getLocalPort() {
        return localPort;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public Integer getFkUser() {
        return fkUser;
    }

    public Server getFkServer() {
        return FkServer;
    }

}
