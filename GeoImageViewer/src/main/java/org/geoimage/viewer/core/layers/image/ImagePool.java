/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geoimage.viewer.core.layers.image;

import org.geoimage.def.GeoImageReader;

/**
 *
 * @author thoorfr
 */
public class ImagePool {
    private GeoImageReader[] images;
    private int index=0;
    private boolean[] locked;

    /**
     * 
     * @param gir
     * @param poolsize
     */
    public ImagePool(GeoImageReader gir,int poolsize) {
        images=new GeoImageReader[poolsize];
        for(int i=0;i<poolsize;i++){
            images[i]=gir.clone();
        }
        locked=new boolean[poolsize];
    }
    
    /**
     * 
     * @return
     */
    public GeoImageReader get(){
        if(index>images.length-1) index=0;
        if(locked[index]) return get(0);
        else{
            locked[index]=true;
            return images[index++];
        }
    }

    public void release(GeoImageReader gir){
        for(int i=0;i<images.length;i++){
            if(gir==images[i]){
                locked[i]=false;
            }
        }
    }

    public void dispose(){
        for(int i=0;i<images.length;i++){
            images[i].dispose();
            images[i]=null;
        }
    }

    private GeoImageReader get(int i) {
        if(i==images.length) return null;
        if(index>images.length-1) index=0;
        if(locked[index]) return get(i+1);
        else{
            locked[index]=true;
            return images[index++];
        }
    }


}
