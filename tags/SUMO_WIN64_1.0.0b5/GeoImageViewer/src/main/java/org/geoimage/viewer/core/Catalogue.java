/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author thoorfr
 */
@Entity
@Table(name = "CATALOGUE")
@NamedQueries({
    @NamedQuery(name = "Catalogue.findAll", query = "SELECT c FROM Catalogue c"),
    @NamedQuery(name = "Catalogue.findByImagename", query = "SELECT c FROM Catalogue c WHERE c.imagename = :imagename"),
    @NamedQuery(name = "Catalogue.findByImagetype", query = "SELECT c FROM Catalogue c WHERE c.imagetype = :imagetype"),
    @NamedQuery(name = "Catalogue.findByGeom", query = "SELECT c FROM Catalogue c WHERE c.geom = :geom"),
    @NamedQuery(name = "Catalogue.findByRepository", query = "SELECT c FROM Catalogue c WHERE c.repository = :repository"),
    @NamedQuery(name = "Catalogue.findByDateCreation", query = "SELECT c FROM Catalogue c WHERE c.dateCreation = :dateCreation")})
public class Catalogue implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "IMAGENAME")
    private String imagename;
    @Column(name = "IMAGETYPE")
    private String imagetype;
    @Column(name = "GEOM")
    private String geom;
    @Column(name = "REPOSITORY")
    private String repository;
    @Column(name = "DATE_CREATION")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    public Catalogue() {
    }

    public Catalogue(String imagename) {
        this.imagename = imagename;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getImagetype() {
        return imagetype;
    }

    public void setImagetype(String imagetype) {
        this.imagetype = imagetype;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (imagename != null ? imagename.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Catalogue)) {
            return false;
        }
        Catalogue other = (Catalogue) object;
        if ((this.imagename == null && other.imagename != null) || (this.imagename != null && !this.imagename.equals(other.imagename))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.geoimage.viewer.core.Catalogue[imagename=" + imagename + "]";
    }

}
