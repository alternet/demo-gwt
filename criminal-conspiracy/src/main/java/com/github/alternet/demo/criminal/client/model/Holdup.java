package com.github.alternet.demo.criminal.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Holdup implements Serializable {

    private static final long serialVersionUID = 7714913268515791786L;

    private int id;
    private String target;
    private List<Gangster> gangsters = new ArrayList<Gangster>();

    private static int COUNTER = 0;

    public Holdup() {
        this.id = COUNTER++;
    }

    public Holdup(String target) {
        this();
        this.target = target;
    }

    public Holdup(String target, List<Gangster> gangsters) {
        this(target);
        this.gangsters = gangsters;
    }

    public int getId() {
        return id;
    }

    public String getTarget() {
        return target;
    }

    public List<Gangster> getGangsters() {
        return gangsters;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setGangsters(List<Gangster> gangsters) {
        this.gangsters = gangsters;
    }

    @Override
    public String toString() {
        return this.id + " : " + this.gangsters;
    }

}
