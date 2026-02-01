package com.example.APIGateWay.dto;

import java.time.LocalDateTime;

import com.example.RestAPI1.model.World;

public class WorldDTO {
    private Integer id;
    private String name;
    private String status;
    private Integer ram;
    private Float size;
    private String template;
    private LocalDateTime dateCreation;
    private String addressNgrok;

    public WorldDTO(Integer id, String name, String status, Integer ram, Float size, String template,
            LocalDateTime dateCreation) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.ram = ram;
        this.size = size;
        this.template = template;
        this.dateCreation = dateCreation;
    }

    public WorldDTO(Integer id, String name, String status, Integer ram, Float size, String template,
            LocalDateTime dateCreation, String addressNgrok) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.ram = ram;
        this.size = size;
        this.template = template;
        this.dateCreation = dateCreation;
        this.addressNgrok = addressNgrok;
    }

    // Getters et setters
    public String getAddressNgrok() {
        return addressNgrok;
    }

    public void setAddressNgrok(String addressNgrok) {
        this.addressNgrok = addressNgrok;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRam() {
        return ram;
    }

    public void setRam(Integer ram) {
        this.ram = ram;
    }

    public Float getSize() {
        return size;
    }

    public void setSize(Float size) {
        this.size = size;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

}
