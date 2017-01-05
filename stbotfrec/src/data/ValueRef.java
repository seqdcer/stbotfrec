/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engines.Base;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public abstract class ValueRef implements JSONAware {
    
    protected final JSONObject localContext;
    protected final JSONObject thisContext;
    private ChangeListener listener = null;
    private final ValueRef parent;
    private boolean isSynced = true;
    
    public ValueRef(ValueRef parent, JSONObject localContext, JSONObject thisContext, boolean sync)
    {
        this.parent = parent;
        this.localContext = localContext;
        this.thisContext = thisContext;
        this.isSynced = sync;
    }
    
    public abstract void reEvaluate();
    
    public void setChangeListener(ChangeListener l)
    {
        listener = l;
    }
    
    protected final ChangeListener getChangeListener()
    {
        return listener;
    }
    
    protected final void fireChangeEvent()
    {
        if (!isSynced)
        {
            return;
        }
        
        if (parent != null)
        {
            parent.reEvaluate();
        }

        if (listener != null) listener.stateChanged(new ChangeEvent(this));
    }
    
    public Object getObjectProperty(String property)
    {
        return null;
    }
    
    public void setObjectProperty(String property, Object value)
    {
    }
    
    public String[] getProperties()
    {
        return null;
    }
    
    public boolean isSynced()
    {
        return isSynced;
    }
    
    public abstract void set(Object valueRef);
    
    public abstract void destroy();
}
