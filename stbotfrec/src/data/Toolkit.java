/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class Toolkit {

        /**
         * DOES NOT COPY OBJECTS STORED IN THE JSON MAP.
         * 
         * If given argument is not of type JSONObject or JSONArray then the original object is returned.
         * @param json
         * @return 
         */
    public static Object deepCopyJSON(Object json)
    {
        if (json instanceof JSONObject)
        {
            JSONObject original = (JSONObject)json;
            JSONObject copy = new JSONObject();
            
            for (Object key : original.keySet())
            {
                copy.put(key, deepCopyJSON(original.get(key)));
            }
            
            return copy;
        }
        else if (json instanceof JSONArray)
        {
            JSONArray original = (JSONArray)json;
            JSONArray copy = new JSONArray();
            
            for (Object value : original)
            {
                copy.add(deepCopyJSON(value));
            }

            return copy;
        }
        else
        {
            return json;
        }
    }
    
    public static void destroy(Object obj)
    {
        if (obj instanceof JSONObject)
        {
            JSONObject json = (JSONObject)obj;
            
            for (Object key : json.keySet())
            {
                destroy(json.get(key));
            }
        }
        else if (obj instanceof JSONArray)
        {
            JSONArray json = (JSONArray)obj;
            
            for (Object value : json)
            {
                destroy(value);
            }
        }
        else if (obj instanceof ValueRef)
        {
            ((ValueRef)obj).destroy();
        }
    }
}
