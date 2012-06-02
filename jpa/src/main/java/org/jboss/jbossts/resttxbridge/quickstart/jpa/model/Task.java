package org.jboss.jbossts.resttxbridge.quickstart.jpa.model;

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * User's task entity which is marked up with JPA annotations and JAXB for serializing XML
 * 
 * @author Gytis Trikleris
 * @author Oliver Kiss and others
 */
@SuppressWarnings("serial")
@Entity
public class Task implements Serializable {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne
    private User owner;

    private String title;

    public Task() {
    }

    public Task(String title) {
        super();
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return owner.getUsername();
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Task other = (Task) obj;
        if (owner == null) {
            if (other.owner != null) {
                return false;
            }
        } else if (!owner.equals(other.owner)) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else if (!title.equals(other.title)) {
            return false;
        }
        return true;
    }

    /**
     * Returns JSON representation of task object.
     * 
     * TODO it would be much easier to use JAXB to convert to JSON. However postprocess interceptor of the bridge is called
     * before JAXB mapper.
     * 
     * @return
     */
    public String toJson() {
        JSONObject json = new JSONObject();

        try {
            json.putOpt("id", id);
            json.putOpt("owner", owner.getUsername());
            json.putOpt("title", title);
            return json.toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "{}";
    }

}
