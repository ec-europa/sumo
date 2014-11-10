/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.image;

import java.util.HashMap;

 



/**
 *
 * @author thoorfr
 */
public class CacheManager {
    private static HashMap<String, Cache> cacheManager=null;
    
    private CacheManager() {}
    
    
    public static Cache getCacheInstance(String fileName) {
    	Cache cache=null;
    	if(cacheManager==null){
    		cacheManager=new HashMap<String, Cache>();
    		cache=new Cache(fileName);
    		cacheManager.put(fileName,cache);
    	}else{
    		cache=cacheManager.get(fileName);
    		if(cache==null){
    			cache=new Cache(fileName);
    			cacheManager.put(fileName,cache);
    		}	
    	}
    	return cache;
    }
    public static Cache getRootCacheInstance() {
    	Cache cache=new Cache();
    	return cache;
    }
    
    
}
