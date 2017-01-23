/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.awt.Color;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class BorderRef extends ValueRef {
    public static final String COLOR_KEY = "color";
    public static final String WIDTH_KEY = "width";
    public static final String DASHED_KEY = "dashed";
    public static final String DASH_LENGTH_KEY = "dashLength";
    public static final String DASH_SPACING_KEY = "dashSpacing";

    private JSONObject borderObject;
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new BorderRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private BorderRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }
    
    public Color getColor()
    {
        ColorValueRef c = (ColorValueRef) ColorValueRef.create(localContext, thisContext, borderObject.get(COLOR_KEY), this.isSynced());
        borderObject.put(COLOR_KEY, c);
        
        return c.getValue();
    }
    
    public int getWidth()
    {
        NumberValueRef num = (NumberValueRef) NumberValueRef.create(localContext, thisContext, borderObject.get(WIDTH_KEY), this.isSynced());
        borderObject.put(WIDTH_KEY, num);
        
        return num.intValue();
    }
    
    public boolean isDashed()
    {
        BooleanValueRef num = (BooleanValueRef) BooleanValueRef.create(localContext, thisContext, borderObject.get(DASHED_KEY), this.isSynced());
        borderObject.put(DASHED_KEY, num);
        
        return num.getValue();
    }
    
    public int getDashLength()
    {
        NumberValueRef num = (NumberValueRef) NumberValueRef.create(localContext, thisContext, borderObject.get(DASH_LENGTH_KEY), this.isSynced());
        borderObject.put(DASH_LENGTH_KEY, num);
        
        return num.intValue();
    }
    
    public int getDashSpacing()
    {
        NumberValueRef num = (NumberValueRef) NumberValueRef.create(localContext, thisContext, borderObject.get(DASH_SPACING_KEY), this.isSynced());
        borderObject.put(DASH_SPACING_KEY, num);
        
        return num.intValue();
    }
    
    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
    }
    
    @Override
    public String toJSONString() {
        return borderObject.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        return borderObject.get(property);
    }
    
    @Override
    public void setObjectProperty(String property, Object value) {
        Object obj = borderObject.get(property);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            borderObject.put(property, value);
        }
        else
        {
            ((ValueRef)borderObject.get(property)).set(value);
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
    public final boolean set(Object valueRef)
    {
        destroy();
        
        if (valueRef instanceof BorderRef)
        {
            borderObject = (JSONObject) Toolkit.deepCopyJSON(((BorderRef)valueRef).borderObject);
        }
        else if (valueRef instanceof JSONObject)
        {
            borderObject = (JSONObject) valueRef;
        }
        else
        {
            borderObject = new JSONObject();
        }
        
        setChangeListener(this.getChangeListener());
        
        fireChangeEvent();
        return true;
    }

    @Override
    public void destroy() {
        if (borderObject != null)
        {
            for (Object key : borderObject.keySet())
            {
                Object obj = borderObject.get(key);

                if (obj instanceof ValueRef)
                {
                    ((ValueRef)obj).destroy();
                }
            }
        }
    }
}
