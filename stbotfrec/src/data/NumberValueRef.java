/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class NumberValueRef extends StringValueRef {
    
    private Number value;
    
    protected NumberValueRef(Object valueRef, JSONObject localContext, JSONObject thisContext, boolean sync)
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
            return new NumberValueRef(valueRef, localContext, thisContext, sync);
        }
    }
    
    @Override
    protected void postEvaluate(String oldValue)
    {
        try
        {
            value = Double.parseDouble(toString());
        }
        catch (Exception e)
        {
            value = 0;
        }
        
        super.postEvaluate(oldValue);
    }
    
    @Override
    public String toJSONString() {
        return value.toString();
    }
    
    public byte byteValue()
    {
        return value.byteValue();
    }
    
    public double doubleValue()
    {
        return value.doubleValue();
    }
    
    public float floatValue()
    {
        return value.floatValue();
    }
    
    public int intValue()
    {
        return value.intValue();
    }
    
    public long longValue()
    {
        return value.longValue();
    }
    
    public short shortValue()
    {
        return value.shortValue();
    }
    
    @Override
    public void set(Object valueRef)
    {
        if (valueRef instanceof Number)
        {
            Number oldValue = value;
            
            value = (Number) valueRef;
            
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
            
            if (oldValue != null && value != oldValue)
                this.fireChangeEvent();
        }
        else
        {
            super.set(valueRef);
        }
    }
}
