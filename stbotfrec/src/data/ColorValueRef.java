/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import java.awt.Color;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class ColorValueRef extends StringValueRef {

    private Color value;
    
    protected ColorValueRef(Object valueRef, JSONObject localContext, JSONObject thisContext, boolean sync)
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
            return new ColorValueRef(valueRef, localContext, thisContext, sync);
        }
    }
    
    @Override
    protected void postEvaluate(String oldValue)
    {
        try
        {
            value = new Color((int)Long.parseLong(toString(), 16), true);
        }
        catch (Exception e)
        {
            value = Color.WHITE;
        }
        
        super.postEvaluate(oldValue);
    }
    
    public Color getValue()
    {
        return value;
    }
}
