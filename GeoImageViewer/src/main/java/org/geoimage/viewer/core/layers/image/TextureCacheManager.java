/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.image;

import java.util.concurrent.ConcurrentHashMap;

import com.jogamp.opengl.util.texture.Texture;

/**
 *
 * @author thoorfr
 */
public class TextureCacheManager {

    private int max = 128;
    private int count = 0;
    private String[] paths;
    private int index = 0;
    private ConcurrentHashMap<String, Texture> map;

    public TextureCacheManager(int max) {
        this.max = max;
        map = new ConcurrentHashMap <String, Texture>(max);
        paths = new String[max];
    }
    
    /**
     * 
     * @param idTile
     * @param texture
     */
    public void add(String idTile, Texture texture) {
        if (count > max) {
            map.remove(paths[index]);
            count--;
            index--;
        }
        map.put(idTile, texture);
        paths[index++] = idTile;
        if (index >= max) {
            index = 0;
        }
        count++;
    }
    
    public boolean isCached(String tileId) {
        return map.containsKey(tileId);
    }
    
    
    public Texture getTexture(String idTile) {
        return map.get(idTile);
    }

    public void clear() {
        try {
           /* for (String key : Collections.synchronizedSet(map.keySet())) {
                map.remove(key);
            }*/
            map.clear();
        } catch (Exception e) {
            System.out.println("stop");
        }

        map.clear();
        paths = new String[max];
        index = 0;
        count = 0;
    }

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
    
    
}
