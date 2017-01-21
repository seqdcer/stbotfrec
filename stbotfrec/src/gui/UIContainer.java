/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.BooleanValueRef;
import data.ImageValueRef;
import engines.Base;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIContainer extends UIElement
{
    public static final String COMPONENTS_KEY = "components";
    public static final String CURSOR_KEY = "cursor";
    
    // config
    private final ImageValueRef cursorRef;
    private AnimatedCursor cursor;
    
    protected UIContainer(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        setLayout(null);
        addMouseListener( new MouseAdapter() { } );

        // setup        
        cursorRef = (ImageValueRef) ImageValueRef.create(localContext, config, this.config.get(CURSOR_KEY), true);
        this.config.put(CURSOR_KEY, cursorRef);
        
        cursorRef.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                resetCursor();
            }
        });

        resetCursor();
        
        // bottom containers are always visible
        if (index < 0)
        {
            setVisible(true);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        if (cursor == null)
            setCursor(null);
        else
            setCursor(cursor.getCursor(Window.UI_TICK, Window.MAX_UITICK_VALUE));
        
        super.paintComponent(g);
    }
    
    private void resetCursor()
    {
        Animation ani = cursorRef.getValue();
        
        if (ani != null && ani.getFrameCount() > 0)
        {
            cursor = new AnimatedCursor(ani, cursorRef.toString());
        }
        else
        {
            cursor = null;
        }
    }
    
    @Override
    public void finalSetup(UIElement parent)
    {
        super.finalSetup(parent);

        JSONArray components = (JSONArray) config.get(COMPONENTS_KEY);
        
        if (components != null && this.getComponents().length == 0)
        {
            for (int i = 0; i < components.size(); i++)
            {
                JSONObject json = (JSONObject) components.get(i);
                
                // set id
                json.put(Base.ID_KEY, config.get(Base.ID_KEY).toString() + Base.NODE_SEPARATOR + COMPONENTS_KEY + Base.NODE_SEPARATOR + i);
                
                final UIElement element = UIElement.create(json, config, i);
                
                if (element != null)
                {
                    components.set(i, element.config);
                    
                    ((BooleanValueRef)element.config.get(UIElement.IGNOREDRAWBORDER_KEY)).setChangeListener(new ChangeListener()
                    {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            if (element.ignoresBorders())
                            {
                                remove(element);
                                getParent().add(element);
                            }
                            else
                            {
                                getParent().remove(element);
                                add(element, 0);
                            }
                        }
                    });
                    
                    if (element.ignoresBorders())
                    {
                        getParent().add(element, i + 1, 0);
                    }
                    else
                    {
                        add(element, i + 1, 0);
                    }
                    
                    element.finalSetup(this);
                }
            }
        }
    }
}
