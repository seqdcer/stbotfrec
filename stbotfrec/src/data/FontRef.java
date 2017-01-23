/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class FontRef extends ValueRef {
    public final List<String> FONT_STYLE = Collections.unmodifiableList(Arrays.asList("regular", "bold"));
    
    public static final String FONT_FILE_KEY = "fontFile";
    public static final String FONT_COLOR_KEY = "color";
    public static final String FONT_STYLE_KEY = "style";
    public static final String FONT_UPPERCASE_KEY = "upperCase";

    private JSONObject font;
    
    private final ChangeListener listener = new ChangeListener()
    {
        @Override
        public void stateChanged(ChangeEvent e) {
            fireChangeEvent();
        }
    };
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new FontRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private FontRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }

    public Font getFont()
    {
        if (font == null) return null;
        
        return Base.getFont(font.get(FONT_FILE_KEY).toString());
    }
    
    public Color getColor()
    {
        ColorValueRef color;
        
        if (font == null || (color = (ColorValueRef)font.get(FONT_COLOR_KEY)).toString().length() == 0)
        {
            return Base.getFont(font.get(FONT_FILE_KEY).toString()).getDefaultColor();
        }
        else
        {
            return color.getValue();
        }
    }
    
    public EnumValueRef getStyle()
    {
        return (EnumValueRef) font.get(FONT_STYLE_KEY);
    }
    
    public boolean getUpperCase()
    {
        return ((BooleanValueRef) font.get(FONT_UPPERCASE_KEY)).getValue();
    }
    
    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
    }

    @Override
    public String toJSONString() {
        return font.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        return font.get(property);
    }
    
    @Override
    public void setObjectProperty(String property, Object value) {
        Object obj = font.get(property);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            font.put(property, value);
        }
        else
        {
            ((ValueRef)font.get(property)).set(value);
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
        
            font = (JSONObject) valueRef;
            
            font.put(FONT_FILE_KEY, StringValueRef.create(localContext, thisContext, font.get(FONT_FILE_KEY), isSynced()));
            font.put(FONT_COLOR_KEY, ColorValueRef.create(localContext, thisContext, font.get(FONT_COLOR_KEY), isSynced()));
            font.put(FONT_STYLE_KEY, EnumValueRef.create(localContext, thisContext, font.get(FONT_STYLE_KEY), FONT_STYLE, isSynced()));
            font.put(FONT_UPPERCASE_KEY, BooleanValueRef.create(localContext, thisContext, font.get(FONT_UPPERCASE_KEY), isSynced()));

            
            ((ValueRef)font.get(FONT_FILE_KEY)).setChangeListener(listener);
            ((ValueRef)font.get(FONT_COLOR_KEY)).setChangeListener(listener);
            ((ValueRef)font.get(FONT_STYLE_KEY)).setChangeListener(listener);
            ((ValueRef)font.get(FONT_UPPERCASE_KEY)).setChangeListener(listener);
            
            fireChangeEvent();
            return true;
        }
        
        return false;
    }
    
    @Override
    public void destroy() {
        if (font != null)
        {
            for (Object key : font.keySet())
            {
                Object obj = font.get(key);

                if (obj instanceof ValueRef)
                {
                    ((ValueRef)obj).destroy();
                }
            }
        }
    }
}
