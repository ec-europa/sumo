/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geoimage.viewer.util;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author thoorfr
 */
public class Utils {

    public static String toString(Map map) {
        String out = "";
        Set keys = map.keySet();
        for (Object o : keys) {
            out += o.toString() + "=";
            Object val = map.get(o);
            if (val == null) {
                out += "null\n";
            } else if (val instanceof String[]) {
                out += "[";
                for (String a : ((String[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof int[]) {
                out += "[";
                for (int a : ((int[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof float[]) {
                out += "[";
                for (float a : ((float[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof boolean[]) {
                out += "[";
                for (boolean a : ((boolean[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof double[]) {
                out += "[";
                for (double a : ((double[]) val)) {
                    out += a + ";";
                }
                out += "]\n";
            } else if (val instanceof long[]) {
                out += "[";
                if (((long[]) val).length > 500) {
                    out += ((long[]) val).length + " elements";
                } else {
                    for (int i = 0; i < ((long[]) val).length; i++) {
                        out += ((long[]) val)[i] + ";";
                    }
                }
                out += "]\n";
            } else {
                out += val.toString() + "\n";
            }
        }
        return out;
    }
}
