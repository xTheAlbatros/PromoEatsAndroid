package com.example.promoeatsandroid.models;

import java.io.Serializable;

public class Images implements Serializable {
    private Integer id;
    private String name;
    private String path;

    // Konstruktor bez argumentów
    public Images() {
    }

    // Konstruktor z argumentami
    public Images(Integer id, String name, String path) {
        this.id = id;
        this.name = name;
        this.path = path;
    }

    // Gettery i Settery
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Images{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
