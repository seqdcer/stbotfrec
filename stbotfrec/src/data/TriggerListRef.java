/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class TriggerListRef extends ValueRef {

    private JSONArray triggers;
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new TriggerListRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private TriggerListRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }
    
    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
        
        if (triggers == null) return;
        
        Object trigger;
        
        for (int i = 0; i < triggers.size(); i++)
        {
            trigger = triggers.get(i);
            
            if (!(trigger instanceof ValueRef))
            {
                trigger = TriggerRef.create(localContext, thisContext, trigger, this.isSynced());
                triggers.set(i, trigger);
            }
            
            if ((trigger instanceof ValueRef))
            {
                ((ValueRef)trigger).setChangeListener(l);
            }
        }
    }

    @Override
    public String toJSONString() {
        return triggers.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        if (property.equals(Base.PROPERTY_LIST_SIZE))
        {
            return triggers.size();
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
        
        return triggers.get(index);
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
        
        Object obj = triggers.get(index);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            triggers.set(index, value);
        }
        else
        {
            ((ValueRef)triggers.get(index)).set(value);
        }
    }
    
    @Override
    public String[] getProperties() {
        return new String[]{Base.PROPERTY_LIST_SIZE};
    }

    @Override
    public void reEvaluate() {
        for (int i = 0; triggers != null && i < triggers.size(); i++)
        {
            ((TriggerRef)triggers.get(i)).runTrigger();
        }        
        // nothing to do
    }

    @Override
    public final void set(Object valueRef) {
        destroy();
        
        if (valueRef instanceof JSONArray)
        {
            triggers = (JSONArray) valueRef;
        }
        else
        {
            triggers = new JSONArray();
        }
        
        setChangeListener(this.getChangeListener());
        
        fireChangeEvent();
    }
    
    @Override
    public void destroy()
    {
        if (triggers != null)
        {
            for (Object obj : triggers)
            {
                ((ValueRef)obj).destroy();
            }
        }
    }
}
