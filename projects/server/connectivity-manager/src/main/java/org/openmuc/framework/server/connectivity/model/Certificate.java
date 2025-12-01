package org.openmuc.framework.server.connectivity.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a certificate used for VPN authentication.
 */
public class Certificate {

    private String id;
    private String name;
    private String type; // "cert", "key", "ca"
    private String subject;
    private List<String> identities;

    public Certificate() {
        this.identities = new ArrayList<>();
    }

    public Certificate(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.identities = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getIdentities() {
        return identities;
    }

    public void setIdentities(List<String> identities) {
        this.identities = identities;
    }

    public void addIdentity(String identity) {
        if (this.identities == null) {
            this.identities = new ArrayList<>();
        }
        this.identities.add(identity);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Certificate that = (Certificate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Certificate{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
