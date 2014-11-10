/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers.image;

import com.jogamp.opengl.util.texture.Texture;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author thoorfr
 */
public class TextureCacheManager {

    private int max = 128;
    private int count = 0;
    private String[] paths;
    private int index = 0;
    private HashMap<String, Texture> map;

    public TextureCacheManager(int max) {
        this.max = max;
        map = new HashMap<String, Texture>(max,1);
        paths = new String[max];
    }

    public void add(String path, Texture texture) {
        if (count >= max) {
            map.remove(paths[index]);
        }
        map.put(path, texture);
        paths[index++] = path;
        if (index >= max) {
            index = 0;
        }
        count++;
    }

    public boolean isCached(String path) {
        return map.containsKey(path);
    }

    public Texture getTexture(String path) {
        return map.get(path);
    }

    public void clear() {
        try {
            for (String key : Collections.synchronizedSet(map.keySet())) {
                map.remove(key);
            }
        } catch (Exception e) {
            System.out.println("stop");
        }

        map.clear();
        paths = new String[max];
        index = 0;
        count = 0;
    }
}
