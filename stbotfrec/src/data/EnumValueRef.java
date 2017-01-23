/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class EnumValueRef extends StringValueRef {
    public static final String INDEX_PROPERTY = "index";
    
    public static final String[] properties = new String[]{INDEX_PROPERTY};
    
    private final List options;
    private int index;
    
    public EnumValueRef(Object valueRef, JSONObject localContext, JSONObject thisContext, List options, boolean sync)
    {
        super(valueRef, null, localContext, thisContext, sync);
        
        this.options = options;
        postEvaluate(null);
    }
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, List options, boolean sync)
    {
        if (valueRef instanceof ValueRef)
        {
            return valueRef;
        }
        else
        {
            return new EnumValueRef(valueRef, localContext, thisContext, options, sync);
        }
    }
    
    @Override
    protected void postEvaluate(String oldValue)
    {
        index = 0;
        
        if (options != null)
        {
            for (int i = 0; i < options.size(); i++)
            {
                Object obj = options.get(i);

                if (toString().equalsIgnoreCase(obj.toString()))
                {
                    index = i;
                    break;
                }
            }
        }
        
        super.postEvaluate(oldValue);
    }
    
    @Override
    public Object getObjectProperty(String property)
    {
        if (INDEX_PROPERTY.equals(property))
        {
            return index;
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

        return "\"" + getValue() + "\"";
    }
    
    public String getValue()
    {
        if (index >= 0 && index < options.size())
        {
            return options.get(index).toString();
        }
        else
        {
            return "";
        }
    }
    
    public int getIndex()
    {
        return index;
    }

    public boolean selectOption(int mod, boolean rotate) {
        int newValue = index + mod;
        
        if (rotate)
        {
            while (newValue < 0) newValue += options.size();
            newValue = newValue % options.size();
        }
        else
        {
            newValue = Math.min(Math.max(0, newValue), options.size() - 1);
        }
        
        if (newValue != index)
        {
            index = newValue;

            resolvedRef = options.get(index).toString();
            resolvedValue = resolvedRef;
            endValue = resolvedRef;
            
            subRefs.clear();
            params.clear();
            subRefs.add(resolvedRef);

            this.fireChangeEvent();
            return true;
        }
        
        return false;
    }
}
