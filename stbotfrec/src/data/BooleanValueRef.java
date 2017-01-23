/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import java.util.Objects;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class BooleanValueRef extends StringValueRef {
    public static final String NUMERIC_PROPERTY = "numeric";
    
    private static final String[] properties = new String[]{NUMERIC_PROPERTY};
    
    private Boolean value;
    
    protected BooleanValueRef(Object valueRef, JSONObject localContext, JSONObject thisContext, boolean sync)
    {
        super(valueRef, null, localContext, thisContext, sync);
    }
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new BooleanValueRef(valueRef, localContext, thisContext, sync);
        }
    }
    
    @Override
    protected void postEvaluate(String oldValue)
    {
        String val = toString();
        
        value = toBoolean(val);
        
        super.postEvaluate(oldValue);
    }
    
    @Override
    public Object getObjectProperty(String property)
    {
        if (NUMERIC_PROPERTY.equals(property))
        {
            return (value) ? 1 : 0;
        }
        else
        {
            return null;
        }
    }
    
    @Override
    public String[] getProperties()
    {
        return properties;
    }
    
    @Override
    public String toJSONString() {
        return Boolean.toString(value);
    }
    
    public boolean getValue()
    {
        return value;
    }
    
    @Override
    public void set(Object valueRef)
    {
        if (valueRef instanceof Boolean)
        {
            Boolean oldValue = value;
            
            value = (Boolean) valueRef;
            
            if (resolvedRef != null && resolvedRef.length() > 0 && resolvedRef.charAt(0) == Base.REF_START)
            {
                Base.removeChangeListener(localContext, thisContext, this, resolvedRef);
            }
            
            resolvedRef = value.toString();
            resolvedValue = resolvedRef;
            endValue = resolvedRef;
            
            subRefs.clear();
            params.clear();
            subRefs.add(resolvedRef);
            
            if (!Objects.equals(value, oldValue) && oldValue != null)
                this.fireChangeEvent();
        }
        else
        {
            super.set(valueRef);
        }
    }
    
    public static boolean toBoolean(String val)
    {
        try
        {
            double dbl = Double.parseDouble(val);
            
            return dbl > 0;
        }
        catch (Exception ex){}
        
        return !(val == null  || val.length() <= 0 || "false".equalsIgnoreCase(val) || "no".equalsIgnoreCase(val));
    }
}
