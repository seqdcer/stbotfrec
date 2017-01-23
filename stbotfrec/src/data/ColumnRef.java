/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import gui.UIElement;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class ColumnRef extends ValueRef {
    public static final String WIDTH_KEY = "width";
    public static final String TEXT_ALIGN_KEY = "horizontalTextAlignment";

    private JSONObject columnData;
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new ColumnRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private ColumnRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }

    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
        
        ((ValueRef)columnData.get(WIDTH_KEY)).setChangeListener(l);
        ((ValueRef)columnData.get(TEXT_ALIGN_KEY)).setChangeListener(l);
    }

    @Override
    public String toJSONString() {
        return columnData.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        return columnData.get(property);
    }
    
    @Override
    public void setObjectProperty(String property, Object value) {
        Object obj = columnData.get(property);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            columnData.put(property, value);
        }
        else
        {
            ((ValueRef)columnData.get(property)).set(value);
        }
    }
            
    @Override
    public String[] getProperties() {
        return null;
    }

    @Override
    public boolean reEvaluate() {
        // nothing to do
        return false;
    }

    @Override
    public final boolean set(Object valueRef) {
        if (valueRef instanceof JSONObject)
        {
            destroy();
        
            columnData = (JSONObject) valueRef;
            
            columnData.put(WIDTH_KEY, NumberValueRef.create(localContext, thisContext, columnData.get(WIDTH_KEY), isSynced()));
            columnData.put(TEXT_ALIGN_KEY, EnumValueRef.create(localContext, thisContext, columnData.get(TEXT_ALIGN_KEY), UIElement.HORIZONTAL_ALIGNMENT, isSynced()));
            
            fireChangeEvent();
            return true;
        }
        
        return false;
    }
    
    public int getWidth()
    {
        return ((NumberValueRef)columnData.get(WIDTH_KEY)).intValue();
    }
    
    public int getHorizontalTextAlignment()
    {
        return ((EnumValueRef)columnData.get(TEXT_ALIGN_KEY)).getIndex();
    }
    
    @Override
    public void destroy() {
        if (columnData != null)
        {
            for (Object key : columnData.keySet())
            {
                Object obj = columnData.get(key);

                if (obj instanceof ValueRef)
                {
                    ((ValueRef)obj).destroy();
                }
            }
        }
    }
}
