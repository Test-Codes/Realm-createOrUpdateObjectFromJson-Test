package io.realm.examples.intro;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by TheFinestArtist on 5/27/15.
 */
public class Contact extends RealmObject {

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null)
            email = "";
        this.email = email;
    }

    public String getUriString() {
        return uriString;
    }

    public void setUriString(String uriString) {
        this.uriString = uriString;
    }

    private int id;
    private String name;
    private String phone;
    @PrimaryKey
    private String email = "";
    private String uriString;
}