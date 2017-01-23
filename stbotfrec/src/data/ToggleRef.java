/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public final class ToggleRef extends ValueRef implements PropertyChangeListener {
    
    public static final String TOGGLE_INDEX_KEY = "toggleIndex";
    public static final String TOGGLE_REF_KEY = "toggleGroupRef";
    
    private NumberValueRef toggleIndex;
    private StringValueRef toggleGroupRef;
    private String oldToogleGroupRef;
    private boolean value;
    
    private JSONObject toggleGroup = null;
    
    public static Object create(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        if (object instanceof ValueRef)
        {
            return object;
        }
        else
        {
            return new ToggleRef(localContext, thisContext, object, sync);
        }
    }
    
    private ToggleRef(JSONObject localContext, JSONObject thisContext, Object object, boolean sync)
    {
        super(null, localContext, thisContext, sync);
        
        toggleGroup = (JSONObject) object;


        if (toggleGroup == null) toggleGroup = new JSONObject();
        
        toggleIndex = (NumberValueRef) NumberValueRef.create(localContext, thisContext, toggleGroup.get(TOGGLE_INDEX_KEY), this.isSynced());
        toggleGroup.put(TOGGLE_INDEX_KEY, toggleIndex);

        toggleGroupRef = (StringValueRef) StringValueRef.create(localContext, thisContext, toggleGroup.get(TOGGLE_REF_KEY), this.isSynced());
        toggleGroup.put(TOGGLE_REF_KEY, toggleGroupRef);

        ChangeListener change = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                String nRef = toggleGroupRef.toString();

                boolean register = false;

                if (oldToogleGroupRef == null || !nRef.equals(oldToogleGroupRef))
                {
                    register = isSynced();

                    if (register && oldToogleGroupRef != null && oldToogleGroupRef.charAt(0) == Base.REF_START)
                        Base.removeChangeListener(localContext, thisContext, ToggleRef.this, oldToogleGroupRef);
                }

                oldToogleGroupRef = nRef;

                if (register)
                {
                    Base.addChangeListener(localContext, thisContext, ToggleRef.this, oldToogleGroupRef);
                }

                boolean oldValue = value;

                value = toggleIndex.intValue() == ((NumberValueRef)NumberValueRef.create(null, null, Base.getVariable(localContext, thisContext, oldToogleGroupRef), false)).intValue();

                if (oldValue != value)
                    fireChangeEvent();
            }
        };

        toggleIndex.setChangeListener(change);
        toggleGroupRef.setChangeListener(change);

        oldToogleGroupRef = toggleGroupRef.toString();
        Base.addChangeListener(localContext, thisContext, ToggleRef.this, oldToogleGroupRef);
        value = toggleIndex.intValue() == ((NumberValueRef)NumberValueRef.create(null, null, Base.getVariable(localContext, thisContext, oldToogleGroupRef), false)).intValue();
    }
    
    
    @Override
    public String toJSONString() {
        return toggleGroup.toJSONString();
    }

    @Override
    public Object getObjectProperty(String property) {
        return toggleGroup.get(property);
    }
    
    @Override
    public void setObjectProperty(String property, Object value) {
        Object obj = toggleGroup.get(property);
        
        if (obj == null || !(obj instanceof ValueRef))
        {
            toggleGroup.put(property, value);
        }
        else
        {
            ((ValueRef)toggleGroup.get(property)).set(value);
        }
    }
    
    public void setSelected(boolean value) throws Exception
    {
        if (this.value != value)
        {
            this.value = value;
            
            if (value)
                Base.setVariable(localContext, thisContext, toggleGroupRef.toString(), toggleIndex.intValue());
            else
                Base.setVariable(localContext, thisContext, toggleGroupRef.toString(), -1);
            
            fireChangeEvent();
        }
    }
    
    public boolean isSelected()
    {
        return value;
    }

    @Override
    public boolean reEvaluate() {
        // nope
        return false;
    }

    @Override
    public boolean set(Object valueRef) {
        // nope
        // destroy();
        return false;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        boolean oldValue = value;

        value = toggleIndex.intValue() == ((NumberValueRef)NumberValueRef.create(null, null, Base.getVariable(localContext, thisContext, oldToogleGroupRef), this.isSynced())).intValue();

        if (oldValue != value)
            fireChangeEvent();
    }
    
    @Override
    public void destroy() {
        
        for (Object key : toggleGroup.keySet())
        {
            Object obj = toggleGroup.get(key);
            
            if (obj instanceof ValueRef)
            {
                ((ValueRef)obj).destroy();
            }
        }
    }
}
