package org.geoimage.viewer.core;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.commons.io.FilenameUtils;
import org.geoimage.viewer.actions.AddGenericWorldLayerAction;
import org.jrc.sumo.core.api.iactions.IAction;
import org.jrc.sumo.util.files.ClassPathHacker;
import org.slf4j.LoggerFactory;

public class PluginsManager {
	private final EntityManagerFactory emf;
	private Map<String, Plugins> plugins;
	private String[] commands;
	private Map<String, IAction> actions;
	
	private static org.slf4j.Logger logger=LoggerFactory.getLogger(PluginsManager.class);

	 
	public PluginsManager() {
		plugins = new HashMap<String, Plugins>();
		actions = new HashMap<String, IAction>();
		
        emf = Persistence.createEntityManagerFactory("GeoImageViewerPU");

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("select p.className from Plugins p");
        List<String> dbPuginNames = q.getResultList();
        em.getTransaction().commit();
        populateDatabase(dbPuginNames);

        em.getTransaction().begin();
        
        Query q2 = em.createQuery("select p from Plugins p");
        List<Plugins> dbPlugins = q2.getResultList();
        dbPlugins = q2.getResultList();
        em.getTransaction().commit();

        em.close();
        List<IAction> landActions=getDynamicActionForLandmask();
        parseActions(dbPlugins);
        
        parseActionsLandMask(landActions);
	}
	
	/**
     * @return the actions
    */
    public Map<String, Plugins> getPlugins() {
        return plugins;
    }
	
    
    /**
     * 
     */
    private void populateDatabase(List<String> dbPlugins) {
	    String[] classes = java.util.ResourceBundle.getBundle("GeoImageViewer").getString("actions").split(",");
	    if(dbPlugins.size()==0||dbPlugins.size()!=classes.length){   
	        EntityManager em = emf.createEntityManager();
	        em.getTransaction().begin();
	        
	        for (int i = 0; i < classes.length; i++) {
	            try {
	            	if(!dbPlugins.contains(classes[i])){
		                Object temp = Class.forName(classes[i]).newInstance();
		                if (temp instanceof IAction) {
		                    Plugins p = new Plugins(classes[i]);
		                    p.setActive(Boolean.TRUE);
		                    p.setJarUrl(temp.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
		                    em.persist(p);
		                    IAction action = (IAction) temp;
		                    getPlugins().put(action.getName(), p);
		                    logger.info(temp.toString() + " added");
		                }
	            	}    
	            } catch (Exception ex) {
	            	logger.warn("Plugin not loaded:"+classes[i]);
	            }
	        }
	        em.getTransaction().commit();
	
	        em.close();
	        commands = getActions().keySet().toArray(new String[getActions().size()]);
    	}   
    }
    
    /**
     * 
     */
    public void reloadPlugins() {
    	actions.clear();
    	
    	EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q = em.createQuery("delete from Plugins p");
        int result=q.executeUpdate();
        em.getTransaction().commit();
        em.close();
        
        populateDatabase(new ArrayList<String>());
        
        em = emf.createEntityManager();
        em.getTransaction().begin();
        Query q2 = em.createQuery("select p from Plugins p");
        List<Plugins> dbPlugins = q2.getResultList();
        dbPlugins = q2.getResultList();
        em.getTransaction().commit();

        em.close();
        List<IAction> landActions=getDynamicActionForLandmask();

        parseActions(dbPlugins);
        parseActionsLandMask(landActions);
    }
    
    
    private void parseActions(List<Plugins> plugins) {
        for (Plugins p : plugins) {
            try {
                Object temp = instanciate(p);
                if (temp instanceof IAction) {
                    IAction action = (IAction) temp;
                    getActions().put(action.getName(), action);
                    getPlugins().put(action.getName(), p);
                    System.out.println(temp.toString() + " added");
                }
            } catch (Exception ex) {
                //Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        commands = getActions().keySet().toArray(new String[getActions().size()]);
    }
    
    private void parseActionsLandMask(List<IAction> acts) {
        for (IAction act : acts) {
            try {
            	Plugins p=new Plugins(act.getClass().getName());
				p.setActive(Boolean.TRUE);
                p.setJarUrl(act.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
                getPlugins().put(act.getName(), p);
                getActions().put(act.getName(), act);
                System.out.println(act.toString() + " added");
            } catch (Exception ex) {
                //Logger.getLogger(ConsoleLayer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        commands = getActions().keySet().toArray(new String[getActions().size()]);
    }
    
    /**
     * @return the actions
     */
    public Map<String, IAction> getActions() {
        return actions;
    }
    public String[] getCommands() {
		return commands;
	}

    
    private IAction instanciate(Plugins p) {
        try {
            ClassPathHacker.addFile(new File(new URI(p.getJarUrl())));
            return (IAction) Class.forName(p.getClassName()).newInstance();
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    public boolean isActive() {
        return true;
    }

    public void setActive(boolean active) {
    }

    
	 /**
     * Create and return new actions dinamically for the land mask import options  
     */
    private List<IAction> getDynamicActionForLandmask(){
    	List<IAction> actions=new ArrayList<IAction>();
    	String folder=SumoPlatform.getApplication().getConfiguration().getCoastlinesFolder();
    	
    	File folderShapes=new File(folder);
    	if(folderShapes.exists()&&folderShapes.isDirectory()){
    		//cerco nelle sottocartelle altre shape files
    		File[] childs=folderShapes.listFiles();
    		for(File f:childs){
    			//controllo se sono cartelle e se contengono uno shape file . In questo caso creo la nuova action 
    			if(f.isDirectory()){
    				File[] files=f.listFiles();
    				for(File ff:files){
    					String ext=FilenameUtils.getExtension(ff.getName());
    					if(ext.equals("shp")){
    						//creo la nuova azione da aggiungere
    						AddGenericWorldLayerAction action=new AddGenericWorldLayerAction(f.getName(),ff);
    						Plugins p=new Plugins(action.getClass().getName());
    						p.setActive(Boolean.TRUE);
    	                    p.setJarUrl(action.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
    						actions.add(action);
    						break;
    					}
    				}
    			}
    		}
    	}
    	return actions;
    }
    
    
    
}
