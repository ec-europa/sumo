/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 * A class that deals with environment variables, they are stored on a local h2 database
 * @author gabbaan + leforth + thoorfr
 */
public class PreferencesDB {
    EntityManagerFactory emf;

    public PreferencesDB() {
        emf=Persistence.createEntityManagerFactory("GeoImageViewerPU");
    }

    //for each name it is possible to save only one value
    public boolean updateRow(String name, String value) {
        EntityManager em=emf.createEntityManager();
        em.getTransaction().begin();
        Preferences p=new Preferences(name);
        p.setValue(value);
        try{
            em.persist(em.merge(p));
        }catch(Exception e){
            em.getTransaction().rollback();
            em.close();
            return false;
        }
        em.getTransaction().commit();
        em.close();
        return true;
    }

    public String readRow(String name) {
        EntityManager em=emf.createEntityManager();
        Query q=em.createNamedQuery("Preferences.findByName");
        q.setParameter("name", name);
        List<Preferences> l=q.getResultList();
        if(l.size()==0) return null;
        Preferences p=l.get(0);
        em.close();
        return p.getValue();

    }

    public boolean insertIfNotExistRow(String name, String value) {
        if(readRow(name)!=null){
            return false;
        }
        EntityManager em=emf.createEntityManager();
        em.getTransaction().begin();
        Preferences p=new Preferences(name);
        p.setValue(value);
        try{
            em.persist(p);
        }catch(Exception e){
            em.getTransaction().rollback();
            em.close();
            return false;
        }
        em.getTransaction().commit();
        em.close();
        return true;
    }
}
