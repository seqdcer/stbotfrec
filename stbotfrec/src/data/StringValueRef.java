/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class StringValueRef extends ValueRef implements PropertyChangeListener {
    public static final Character PARAM_SEPARATOR = '$';
    public static final Character ESCAPE_CHARACTER = '&';
    
    protected final ArrayList<Object> subRefs = new ArrayList<>();
    protected final ArrayList<Object> params = new ArrayList<>();
    protected Object originalValue;
    protected String resolvedRef;
    protected Object resolvedValue;
    protected String endValue;
    
    protected StringValueRef(Object valueRef, ValueRef parent, JSONObject localContext, JSONObject thisContext, boolean sync)
    {
        super(parent, localContext, thisContext, sync);
        
        set(valueRef);
    }
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof StringValueRef)
        {
            return valueRef;
        }
        else
        {
            return new StringValueRef(valueRef, null, localContext, thisContext, sync);
        }
    }
    
    private static Object create(ValueRef parent, JSONObject localContext, JSONObject thisContext, Object valueRef, boolean sync)
    {
        if (valueRef instanceof StringValueRef)
        {
            return valueRef;
        }
        else
        {
            return new StringValueRef(valueRef, parent, localContext, thisContext, sync);
        }
    }
    
    @Override
    public void set(Object valueRef)
    {
        originalValue = valueRef;
        subRefs.clear();
        params.clear();
        
        if (valueRef == null)
        {
            resolvedRef = "";
            resolvedValue = "";
        }
        else
        {
            String ref = valueRef.toString();
            StringBuilder subRef = new StringBuilder();
            int depth = 0;
            char ch;
            boolean param = false;
            boolean escape = false;
            
            if (ref.length() > 0)
                subRef.append(ref.charAt(0));
            
            for (int i = 1; i < ref.length(); i++)
            {
                ch = ref.charAt(i);

                if (escape)
                {
                    subRef.append(ch);
                    escape = false;
                }
                else if (ch == ESCAPE_CHARACTER)
                {
                    escape = true;
                }
                else if (param)
                {
                    if (ch == Base.REF_START)
                    {
                        depth++;
                        subRef.append(ch);
                    }
                    else if (ch == PARAM_SEPARATOR)
                    {
                        if (depth == 0) // next parameter start
                        {
                            params.add(StringValueRef.create(this, localContext, thisContext, subRef.toString(), this.isSynced()));
                            subRef.setLength(0);
                        }
                        else // param for sub ref or literal
                        {
                            subRef.append(ch);
                        }
                    }
                    else if (ch == Base.REF_END)
                    {
                        if (depth == 0) // end of param and current ref
                        {
                            params.add(StringValueRef.create(this, localContext, thisContext, subRef.toString(), this.isSynced()));
                            subRef.setLength(0);
                            param = false;
                        }
                        else // param for sub ref or literal
                        {
                            subRef.append(ch);
                            depth--;
                        }
                    }
                    else
                    {
                        subRef.append(ch);
                    }
                }
                else
                {
                    if (ch == Base.REF_START)
                    {
                        if (depth == 0 && subRef.length() > 0)
                        {
                            subRefs.add(subRef.toString());
                            subRef.setLength(0);
                        }

                        depth++;
                        subRef.append(ch);
                    }
                    else if (ch == PARAM_SEPARATOR)
                    {
                        if (depth == 0 && ref.charAt(0) == Base.REF_START) // parameter start for this ref
                        {
                            subRef.append(Base.REF_END);

                            subRefs.add(StringValueRef.create(this, localContext, thisContext, subRef.toString(), this.isSynced()));
                            subRef.setLength(0);
                            param = true;
                        }
                        else // param for sub ref or literal
                        {
                            subRef.append(ch);
                        }
                    }
                    else if (ch == Base.REF_END)
                    {
                        subRef.append(ch);

                        depth--;

                        if (depth < 0)
                        {
                            depth = 0;

                            if (ref.charAt(0) == Base.REF_START && i + 1 < ref.length())
                            {
                                subRefs.clear();
                                subRefs.add(StringValueRef.create(this, localContext, thisContext, ref.substring(0, i + 1), this.isSynced()));
                                subRef.setLength(0);
                            }
                        }
                        else if (depth == 0)
                        {
                            subRefs.add(StringValueRef.create(this, localContext,thisContext,  subRef.toString(), this.isSynced()));
                            subRef.setLength(0);
                        }
                    }
                    else
                    {
                        subRef.append(ch);
                    }
                }
            }
            
            if (subRef.length() > 0)
            {
                if (param)
                {
                    params.add(subRef.toString());
                }
                else
                {
                    subRefs.add(subRef.toString());
                }
                subRef.setLength(0);
            }
        
            subRefs.trimToSize();
            params.trimToSize();
        }
        
        reEvaluate();
    }
    
    @Override
    public String toString()
    {
        return endValue;
    }
    
    @Override
    public void reEvaluate()
    {
        Object oldValue = endValue;
        
        StringBuilder resRef = new StringBuilder();
        
        for (int i = 0; i < subRefs.size(); i++)
        {
            resRef.append(subRefs.get(i).toString());
        }
        
        String oldResolvedRef = resolvedRef;
        resolvedRef = resRef.toString();
        boolean register = false;
        
        if (oldResolvedRef == null || !resolvedRef.equals(oldResolvedRef))
        {
            register = isSynced();
            
            if (register && oldResolvedRef != null && oldResolvedRef.length() > 0 && oldResolvedRef.charAt(0) == Base.REF_START)
                Base.removeChangeListener(localContext, thisContext, this, oldResolvedRef);
        }
        
        if (resolvedRef.length() != 0 && resolvedRef.charAt(0) == Base.REF_START)
        {
            Object var = Base.getVariable(localContext, thisContext, resolvedRef);
            
            if (var != null && var instanceof Double)
            {
                Double dvar = (Double) var;
                
                double dbl = dvar - dvar.longValue();
                
                if (dbl < 0.0000001)
                {
                    resolvedValue = Long.toString(dvar.longValue());
                }
                else
                {
                    resolvedValue = var.toString();
                }
            }
            else
            {
                resolvedValue = (var == null) ? "" : var.toString();
            }
            
            if (register)
            {
                Base.addChangeListener(localContext, thisContext, this, resolvedRef);
            }
        }
        else
        {
            resolvedValue = resolvedRef;
        }
        
        String refvalue = resolvedValue.toString();
        
        if (refvalue.startsWith("" + Base.RECURSIVE_REF_START + Base.REF_START))
        {
            resolvedValue = new StringValueRef(refvalue.substring(1), this, localContext, thisContext, this.isSynced());
        }
        
        endValue = resolvedValue.toString();
        
        // now fill in parameters if any
        for (int i = 0; i < params.size(); i++)
        {
            endValue = endValue.replaceAll("%s" + (i + 1), params.get(i).toString());
        }
        
        // if any are left assume the same order as params is wanted
        for (int i = 0; i < params.size(); i++)
        {
            endValue = endValue.replaceFirst("%s", params.get(i).toString());
        }
        
        postEvaluate((oldValue == null) ? null : oldValue.toString());
    }
    
    protected void postEvaluate(String oldValue)
    {
        if (oldValue == null)
        {
            return;
        }

        fireChangeEvent();
    }
    
    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        reEvaluate();
    }
    
    @Override
    public String toJSONString() {
        return (originalValue == null) ? null : "\"" + originalValue.toString() + "\"";
    }
    
    @Override
    public void destroy()
    {
        if (!this.isSynced())
        {
            return;
        }
        
        for (Object obj : subRefs)
        {
            if (obj instanceof ValueRef)
                ((ValueRef)obj).destroy();
        }
        
        for (Object obj : params)
        {
            if (obj instanceof ValueRef)
                ((ValueRef)obj).destroy();
        }
        
        subRefs.clear();
        params.clear();
        

        if (resolvedRef != null && resolvedRef.length() > 0 && resolvedRef.charAt(0) == Base.REF_START)
                Base.removeChangeListener(localContext, thisContext, this, resolvedRef);
        
        resolvedRef = "";
        
        if (resolvedValue instanceof ValueRef)
        {
            ((ValueRef)resolvedValue).destroy();
            resolvedValue = "";
        }
    }
}
