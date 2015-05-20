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
public class MemoryCacheManager<E> {
    
    private int max=128;
    private long count=0;
    private String[] paths;
    private int index=0;
    private HashMap<String, E> map;

    public MemoryCacheManager(int max) {
        this.max=max;
        map=new HashMap<String, E>(max,1);
        paths= new String[max];
    }
    
    public void add(String path, E image){
        if(count>=max){
            //count=max-1;
            map.remove(paths[index]);
        }
        map.put(path, image);
        paths[index++]=path;
        if(index>=max) index=0;
        count++;
    }
    
    public boolean isCached(String path){
        return map.containsKey(path);
    }
    
    public E getImage(String path){
        return map.get(path);
    }
    
    public void clear(){
        map.clear();
        paths=new String[max];
        index=0;
        count=0;
    }

}
