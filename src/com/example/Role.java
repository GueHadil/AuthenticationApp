package com.example;

/**
 * Role entity representing user roles in the system
 */
public class Role {
    private int id;
    private String name;

    // Role constants
    public static final String ADMIN = "ADMIN";
    public static final String GESTIONNAIRE = "GESTIONNAIRE";
    public static final String TRAVAILLEUR = "TRAVAILLEUR";
    public static final String ENSEIGNANT = "ENSEIGNANT";
    public static final String ETUDIANT = "ETUDIANT";

    public Role() {}

    public Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "'}";
    }
}

