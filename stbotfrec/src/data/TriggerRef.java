/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import init.Main;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class TriggerRef extends ValueRef {
    public static final String CONDITION_KEY = "condition";
    public static final String VALUE1_KEY = "value1";
    public static final String VALUE2_KEY = "value2";
    public static final String OPERATOR_KEY = "operator";
    public static final String ACTION_KEY = "onTriggered";

    private JSONObject triggerObject;
    
    private final ChangeListener trigger = new ChangeListener()
    {
        @Override
        public synchronized void stateChanged(ChangeEvent e) {
            Object action = triggerObject.get(ACTION_KEY);
            
            if (action != null)
            {
                if (evaluate(triggerObject.get(CONDITION_KEY)) != null)
                {
                    Main.CI.runSyncUICommand(localContext, thisContext, action);
                }
            }
            
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
            return new TriggerRef(localContext, thisContext, valueRef, sync);
        }
    }
    
    private TriggerRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        set(object);
    }

    @Override
    public void setChangeListener(ChangeListener l)
    {
        super.setChangeListener(l);
    }
    
    private void setChangeListener(ChangeListener l, JSONObject parent, String key)
    {
        Object val = parent.get(key);

        if (val == null)
        {
            return;
        }
        else if (val instanceof JSONObject)
        {
            JSONObject json = (JSONObject) val;
            
            setChangeListener(l, json, VALUE1_KEY);
            setChangeListener(l, json, VALUE2_KEY);
            setChangeListener(l, json, OPERATOR_KEY);
            
            return;
        }
        else if (!(val instanceof ValueRef))
        {
            val = StringValueRef.create(localContext, thisContext, val, this.isSynced());
            triggerObject.put(key, val);
        }

        ((ValueRef)val).setChangeListener(l);
    }
    
    private boolean isTriggered(Object value1, Object value2, Object comp)
    {
        if (value1 instanceof Double && value2 instanceof Double)
        {
            double dbl1 = (Double) value1;
            double dbl2 = (Double) value2;
            
            switch (comp.toString())
            {
                case "==":
                {
                    return Math.abs(dbl1 - dbl2) < 0.000001;
                }
                case "!=":
                {
                    return Math.abs(dbl1 - dbl2) >= 0.000001;
                }
                case "<":
                {
                    return dbl1 < dbl2;
                }
                case "<=":
                {
                    return dbl1 <= dbl2;
                }
                case ">":
                {
                    return dbl1 > dbl2;
                }
                case ">=":
                {
                    return dbl1 >= dbl2;
                }
                case "AND":
                {
                    return dbl1 != 0 && dbl2 != 0;
                }
                case "OR":
                {
                    return dbl1 != 0 || dbl2 != 0;
                }
                default:
                    return false;
            }
        }
        else
        {
            String str1 = value1.toString();
            String str2 = value2.toString();
            
            int cval = str1.compareTo(str2);
            
            switch (comp.toString())
            {
                case "==":
                {
                    return cval == 0;
                }
                case "!=":
                {
                    return cval != 0;
                }
                case "<":
                {
                    return cval < 0;
                }
                case "<=":
                {
                    return cval <= 0;
                }
                case ">":
                {
                    return cval > 0;
                }
                case ">=":
                {
                    return cval >= 0;
                }
                case "AND":
                {
                    return BooleanValueRef.toBoolean(str1) && BooleanValueRef.toBoolean(str2);
                }
                case "OR":
                {
                    return BooleanValueRef.toBoolean(str1) || BooleanValueRef.toBoolean(str2);
                }
                case "in":
                {
                    String[] list = str2.split(",");
                    
                    for (String val : list)
                    {
                        cval = str1.compareTo(val);
                            
                        if (cval == 0) return true;
                    }
                    
                    return false;
                }
                case "!in":
                {
                    String[] list = str2.split(",");
                    
                    for (String val : list)
                    {
                        cval = str1.compareTo(val);
                            
                        if (cval == 0) return false;
                    }
                    
                    return true;
                }
                default:
                    return false;
            }
        }
    }

    @Override
    public String toJSONString() {
        return triggerObject.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        return triggerObject.get(property);
    }
    
    @Override
    public void setObjectProperty(String property, Object value) {
        Object obj = triggerObject.get(property);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            triggerObject.put(property, value);
        }
        else
        {
            ((ValueRef)triggerObject.get(property)).set(value);
        }
    }
    
    @Override
    public String[] getProperties() {
        return null;
    }

    @Override
    public void reEvaluate() {
        // nothing to do
    }

    @Override
    public final void set(Object valueRef) {

        if (valueRef instanceof TriggerRef)
        {
            triggerObject = (JSONObject) Toolkit.deepCopyJSON(((TriggerRef)valueRef).triggerObject);
        }
        else if (valueRef instanceof JSONObject)
        {
            triggerObject = (JSONObject) valueRef;
        }
        else
        {
            triggerObject = new JSONObject();
        }
        
        setChangeListener(this.getChangeListener());
        setChangeListener(trigger, triggerObject, CONDITION_KEY);
        
        fireChangeEvent();
    }
    
    public Object evaluate(Object condition)
    {
        if (condition != null && condition.toString().length() > 0)
        {
            if (condition instanceof JSONObject)
            {
                JSONObject json = (JSONObject) condition;
                String value1 = StringValueRef.create(localContext, thisContext, evaluate(json.get(VALUE1_KEY)), false).toString();
                String value2 = StringValueRef.create(localContext, thisContext, evaluate(json.get(VALUE2_KEY)), false).toString();
                String operator = StringValueRef.create(localContext, thisContext, evaluate(json.get(OPERATOR_KEY)), false).toString();
                
                try
                {
                    if (isTriggered(Double.parseDouble(value1), Double.parseDouble(value2), operator))
                    {
                        return true;
                    }
                }
                catch (Exception ex)
                {
                    if (isTriggered(value1, value2, operator))
                    {
                        return true;
                    }
                }
            }
            else
            {
                return condition;
            }
        }
        
        return null;
    }
    
    public final void runTrigger()
    {
        trigger.stateChanged(null);
    }
    
    @Override
    public void destroy() {
        
        destroy(triggerObject);
    }
    
    private void destroy(JSONObject data) {
        for (Object key : data.keySet())
        {
            Object obj = data.get(key);
            
            if (obj instanceof ValueRef)
            {
                ((ValueRef)obj).destroy();
            }
            else if (obj instanceof JSONObject)
            {
                destroy((JSONObject)obj);
            }
        }
    }
}
