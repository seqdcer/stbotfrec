/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class KeyBindsRef extends ValueRef {
    public static final String KEY_KEY = "key";
    public static final String ACTION_KEY = "action";

    private JSONArray keyBinds;
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new KeyBindsRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private KeyBindsRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }

    public HashMap<String, Object> getKeyBinds()
    {
        if (keyBinds == null) return null;
        
        HashMap<String, Object> values = new HashMap<>();
        JSONObject json;
        
        for (Object keyBind: keyBinds)
        {
            if (keyBind instanceof JSONObject)
            {
                json = (JSONObject) keyBind;
                
                try
                {
                    values.put(json.get(KEY_KEY).toString(), json.get(ACTION_KEY));
                }
                catch (Exception e)
                {}
            }
        }
        
        return values;
    }
    
    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
        
        if (keyBinds == null) return;
        
        JSONObject json;
        
        for (Object keyBind: keyBinds)
        {
            if (keyBind instanceof JSONObject)
            {
                json = (JSONObject) keyBind;
                
                Object val = json.get(KEY_KEY);

                if (!(val instanceof ValueRef))
                {
                    val = StringValueRef.create(localContext, thisContext, val, isSynced());
                    json.put(KEY_KEY, val);
                }

                ((ValueRef)val).setChangeListener(l);
            }
        }
    }

    @Override
    public String toJSONString() {
        return keyBinds.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        if (property.equals(Base.PROPERTY_LIST_SIZE))
        {
            return keyBinds.size();
        }

        int index;
        
        try
        {
            index = Integer.parseInt(property);
        }
        catch (Exception ex)
        {
            return null;
        }
        
        return keyBinds.get(index);
    }
    
    @Override
    public void setObjectProperty(String property, Object value) {
        int index;
        
        try
        {
            index = Integer.parseInt(property);
        }
        catch (Exception ex)
        {
            return;
        }
        
        Object obj = keyBinds.get(index);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            keyBinds.set(index, value);
        }
        else
        {
            ((ValueRef)keyBinds.get(index)).set(value);
        }
    }
    
    @Override
    public String[] getProperties() {
        return new String[]{Base.PROPERTY_LIST_SIZE};
    }

    @Override
    public void reEvaluate() {
        // nothing to do
    }

    @Override
    public final void set(Object valueRef) {
        destroy();
        
        if (valueRef instanceof JSONArray)
        {
            keyBinds = (JSONArray) valueRef;
        }
        else
        {
            keyBinds = new JSONArray();
        }
        
        setChangeListener(this.getChangeListener());
        
        fireChangeEvent();
    }
    
    @Override
    public void destroy()
    {
        if (keyBinds != null)
        {
            for (Object obj : keyBinds)
            {
                ((ValueRef)obj).destroy();
            }
        }
    }
}
