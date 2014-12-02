/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author thoorfr
 */
@Entity
@Table(name = "PLUGINS")
public class Plugins implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "JAR_URL")
    private String jarUrl;
    @Id
    @Basic(optional = false)
    @Column(name = "CLASS_NAME")
    private String className;
    @Column(name = "ACTIVE")
    private Boolean active;
    
    /*
    @Column(name = "NEED_PARAMS")
    private boolean needParams=false;
    @Column(name = "PARAMS")
    private Object[] params=null; 
    
    public boolean isNeedParams() {
		return needParams;
	}

	public void setNeedParams(boolean needParams) {
		this.needParams = needParams;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}*/

	public Plugins() {
    }

    public Plugins(String className) {
        this.className = className;
    }

    public String getJarUrl() {
        return jarUrl;
    }

    public void setJarUrl(String jarUrl) {
        this.jarUrl = jarUrl;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Boolean isActive() {
        return active==null?Boolean.FALSE:active;
    }

    public void setActive(Boolean active) {
        this.active = active==null?Boolean.FALSE:active;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (className != null ? className.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Plugins)) {
            return false;
        }
        Plugins other = (Plugins) object;
        if ((this.className == null && other.className != null) || (this.className != null && !this.className.equals(other.className))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return className;
    }

}
