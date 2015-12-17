/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.core.api;

/**
 * the class deals with possible arguments types that can be passed to execute a
 * ConsoleAction. It constructs automatically the GUI according to the settings of this class
 * @author thoorfr
 */
public class Argument {
    private String name;
    private Class<?> type;
    public final static Class<Integer> INTEGER = java.lang.Integer.class;
    public final static Class<Double> DOUBLE = java.lang.Double.class;
    public final static Class <Float>FLOAT = java.lang.Float.class;
    public final static Class <String>STRING = java.lang.String.class;
    public final static Class <Boolean>BOOLEAN = java.lang.Boolean.class;
    public final static Class <java.util.Date>DATE = java.util.Date.class;
    public final static Class <java.io.File>FILE = java.io.File.class;
    public final static Class <Void>DIRECTORY = Void.class;
    
    private boolean optional;
    private Object defaultValue;
    private Object[] values;

    public Argument(String name, Class type, boolean optional, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.optional=optional;
        this.defaultValue=defaultValue;
    }
    
    /**
     * Using this method will force the UI to setup a combobox for choosing the values of the argument
     * @param values an array of chooseable values
     */
    public void setPossibleValues(Object[] values){
        this.values=values;
    }
    
    /**
     * 
     * @return the possible values to be picked up
     */
    public Object[] getPossibleValues(){
        return values;
    }

    /**
     *
     * @return the name of the argument like "Threshold"
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the argument like "Threshold"
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * type of the argument, to be selected from the static field of the class,
     * like Argument.DATE for a date. It will trigger the right UI component in the dialog box
     * @return
     */
    public Class getType() {
        return type;
    }

    /**
     * Sets the default value
     * @param value
     */
    public void setValue(Class value) {
        this.type = value;
    }

    /**
     * to indicate if the arguments has to be filled or not
     * @return
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * set true if the argument can be null
     * @param optional
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     *
     * @return return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value
     *
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
