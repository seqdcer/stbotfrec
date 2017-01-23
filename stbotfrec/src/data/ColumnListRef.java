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
public class ColumnListRef extends ValueRef {

    private JSONArray columns;
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new ColumnListRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private ColumnListRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }

    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
        
        for (Object obj : columns)
        {
            ((ValueRef)obj).setChangeListener(l);
        }
    }

    @Override
    public String toJSONString() {
        return columns.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        if (property.equals(Base.PROPERTY_LIST_SIZE))
        {
            return columns.size();
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
        
        return columns.get(index);
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
        
        Object obj = columns.get(index);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            columns.set(index, value);
        }
        else
        {
            ((ValueRef)columns.get(index)).set(value);
        }
    }
    
    @Override
    public String[] getProperties() {
        return new String[]{Base.PROPERTY_LIST_SIZE};
    }

    @Override
    public boolean reEvaluate() {
        // nothing to do
        return false;
    }

    @Override
    public final boolean set(Object valueRef) {
        if (valueRef instanceof JSONArray)
        {
            destroy();
            
            columns = (JSONArray) valueRef;
            
            for (int i = 0; i < columns.size(); i++)
            {
                columns.set(i, ColumnRef.create(localContext, thisContext, columns.get(i), isSynced()));
            }
            
            fireChangeEvent();
            return true;
        }
        
        return false;
    }
    
    public ColumnRef getColumn(int i)
    {
        return (ColumnRef)columns.get(i);
    }
    
    public int getColumnCount()
    {
        return columns.size();
    }
    
    @Override
    public void destroy()
    {
        if (columns != null)
        {
            for (Object obj : columns)
            {
                ((ValueRef)obj).destroy();
            }
        }
    }
}
