/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.layers;

import java.util.HashMap;
import java.util.Map;

/**
 * This class deals wit attributes of GeometricLayer.
 * For optimisation purpose, the geometric data (ie to be displayed)
 * and the associated attributes have been separated.
 * This class should be used in a synchronised way, ie, only one thread
 * can access one instanciated class
 * @author thoorfr
 */
public class AttributesGeometry implements Cloneable{

    private Map<String, Object> attributes;
    private String[] schema;
    
    
    public AttributesGeometry(String[] attributesSchema) {
    	this.schema=attributesSchema;
    	attributes=new HashMap<String, Object>();
	}

    /**
     * Set the attributes value giving the schema name.
     * @param att
     * @param value
     * @return true if successfully added, false if not or ignored
     */
    public boolean set(String att, Object value) {
        if (!attributes.containsKey(att)) {
            return false;
        } else {
            attributes.put(att, value);
            return true;
        }
    }
    
    /**
     * Deep copy of the attibutes data
     * @return a new instance of Attributes with full copy of data
     */
    @Override
    public AttributesGeometry clone(){
        AttributesGeometry out=new AttributesGeometry(schema);
        out.attributes.putAll(attributes);
        return out;
    }

    /**
     *
     * @return the values of the Attributes in the schema order (getSchema())
     */
    public Object[] getValues() {
        Object[] out = new Object[schema.length];
        int i = 0;
        for (String s : schema) {
            out[i++] = attributes.get(s);
        }
        return out;
    }

    

    /**
     * 
     * @param att
     * @return the value corresponding to the schema key "att"
     */
    public Object get(String att) {
        return attributes.get(att);
    }

    /**
     * 
     * @return the schema of the Attributes instance
     */
    public String[] getSchema() {
        return schema;
    }


    /**
     * add a new column at the end of the schema. THIS IS NOT RECOMMENDED. Prefer
     * the use aof a new Attributes instance with the new schema and copy all the values
     * form the former instance
     * @param name
     * @param type
     * @return
     */
    public boolean addColumn(String name, String type){
        String[] temp=new String[schema.length+1];
        System.arraycopy(schema, 0, temp, 0, schema.length);
        schema=temp;
        schema[schema.length-1]=name;
        
        //TODO:remove
/*        temp=new String[types.length+1];
        System.arraycopy(types, 0, temp, 0, types.length);
        types=temp;
        types[types.length-1]=type;*/
        return true;
    }
    
    /**
     * 
     * @return a String of the form "key1=value1\nkey2=bvalue2" etc....
     */
    @Override
    public String toString(){
        StringBuilder out=new StringBuilder();
        for(String key:schema){
            out.append(key).append("=").append(attributes.get(key)).append("\n");
        }
        
        return out.toString();
    }
}
