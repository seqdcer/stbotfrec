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
    public static final String ELSE_ACTION_KEY = "onElse";

    private JSONObject triggerObject;
    
    private final ChangeListener trigger = new ChangeListener()
    {
        @Override
        public synchronized void stateChanged(ChangeEvent e) {
            Object action = triggerObject.get(ACTION_KEY);
            Object elseAction = triggerObject.get(ELSE_ACTION_KEY);
            
            if (action != null)
            {
                if (evaluate())
                {
                    Main.CI.runSyncUICommand(localContext, thisContext, TriggerRef.this);
                }
                else if (elseAction != null)
                {
                    Main.CI.runSyncUICommand(localContext, thisContext, TriggerRef.this);
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
            parent.put(key, val);
        }

        ((ValueRef)val).setChangeListener(l);
    }
    
    private boolean isTriggered(String value1, String value2, Object comp)
    {
        double dbl1 = 0, dbl2 = 0;
        String[] list = null;
        
        // int cval = str1.compareTo(str2);

        // prep
        switch (comp.toString())
        {
            case "==":
            case "!=":
            case "<":
            case "<=":
            case ">":
            case ">=":
            {
                try
                {
                    dbl1 = Double.parseDouble(value1);
                }
                catch (Exception ex) {}
                try
                {
                    dbl2 = Double.parseDouble(value2);
                }
                catch (Exception ex) {}
                break;
            }
            case "IN":
            case "!IN":
            {
                if (value2.startsWith("{") && value2.endsWith("}"))
                {
                    value2 = value2.substring(1, value2.length() - 1);
                }

                list = value2.split(",");
                
                for (int i = 0; i < list.length; i++)
                {
                    list[i] = list[i].trim();
                }
                
                break;
            }
        }
        
        // eval
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
            case "!==":
            {
                return !value1.equals(value2);
            }
            case "===":
            {
                return value1.equals(value2);
            }
            case "AND":
            {
                return BooleanValueRef.toBoolean(value1) && BooleanValueRef.toBoolean(value2);
            }
            case "NOT":
            {
                return BooleanValueRef.toBoolean(value1) && !BooleanValueRef.toBoolean(value2);
            }
            case "OR":
            {
                return BooleanValueRef.toBoolean(value1) || BooleanValueRef.toBoolean(value2);
            }
            case "IN":
            {
                for (String val : list)
                {
                    if (value1.equals(val)) return true;
                }

                return false;
            }
            case "!IN":
            {
                for (String val : list)
                {
                    if (value1.equals(val)) return false;
                }

                return true;
            }
            default:
                return false;
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
    public boolean reEvaluate() {
        // nothing to do
        return false;
    }

    @Override
    public final boolean set(Object valueRef) {

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
        return true;
    }
    
    public boolean evaluate()
    {
        return evaluate(triggerObject.get(CONDITION_KEY)) != null;
    }
    
    public Object getAction()
    {
        return triggerObject.get(ACTION_KEY);
    }
    
    public Object getElseAction()
    {
        return triggerObject.get(ELSE_ACTION_KEY);
    }
    
    private Object evaluate(Object condition)
    {
        if (condition != null && condition.toString().length() > 0)
        {
            if (condition instanceof JSONObject)
            {
                JSONObject json = (JSONObject) condition;
                String value1 = StringValueRef.create(localContext, thisContext, evaluate(json.get(VALUE1_KEY)), false).toString();
                String value2 = StringValueRef.create(localContext, thisContext, evaluate(json.get(VALUE2_KEY)), false).toString();
                String operator = StringValueRef.create(localContext, thisContext, evaluate(json.get(OPERATOR_KEY)), false).toString();
                
                if (isTriggered(value1, value2, operator))
                {
                    return true;
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
