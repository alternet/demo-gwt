package com.github.alternet.demo.criminal.client.model;

import java.io.Serializable;

public class Gangster implements Serializable {

    private static final long serialVersionUID = -4286528583264610122L;

    private int id;
    private Aptitude aptitude;
    private String name;

    private static int COUNTER = 0;

    public Gangster(Aptitude aptitude, String name) {
        this(COUNTER++, aptitude, name);
    }

    public Gangster(int id, Aptitude aptitude, String name) {
        this.id = id;
        this.aptitude = aptitude;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Aptitude getAptitude() {
        return aptitude;
    }

    public void setAptitude(Aptitude aptitude) {
        this.aptitude = aptitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Gangster) && ((Gangster) obj).id == this.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

}
