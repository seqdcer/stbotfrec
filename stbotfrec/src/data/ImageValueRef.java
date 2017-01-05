/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import gui.Animation;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class ImageValueRef extends StringValueRef {
    private static final String HEIGHT_PROPERTY = "height";
    private static final String WIDTH_PROPERTY = "width";
    
    private Animation.FrameSelector selector;
    private Animation value;
    
    protected ImageValueRef(Object valueRef, JSONObject localContext, JSONObject thisContext, boolean sync)
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
            return new ImageValueRef(valueRef, localContext, thisContext, sync);
        }
    }
    
    @Override
    protected void postEvaluate(String oldValue)
    {
        String image = toString();
        
        if (image.length() > 0)
        {
            try
            {
                value = Base.getImage(localContext, thisContext, image);
            
                if (value != null)
                    selector = value.createFrameSelector();
            }
            catch (Exception ex)
            {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        else
        {
            value = null;
        }
        
        super.postEvaluate(oldValue);
    }
    
    public BufferedImage getValue(int time, int maxTime)
    {
        if (value == null) return null;
        
        value.updateFrameSelector(selector, time, maxTime);
        
        return value.getFrame(selector);
    }
    
    public int getHeight()
    {
        if (value == null) return 0;
        
        return value.getHeight();
    }
    
    public int getWidth()
    {
        if (value == null) return 0;
        
        return value.getWidth();
    }
    
    public Animation getValue()
    {
        return value;
    }
    
    @Override
    public Object getObjectProperty(String property)
    {
        switch (property)
        {
            case HEIGHT_PROPERTY:
            {
                return getHeight();
            }
            case WIDTH_PROPERTY:
            {
                return getWidth();
            }
            default:
            {
                return null;
            }
        }
    }
    
    @Override
    public String[] getProperties()
    {
        return new String[]{HEIGHT_PROPERTY, WIDTH_PROPERTY};
    }
}
