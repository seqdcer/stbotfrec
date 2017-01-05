/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.EnumValueRef;
import data.ImageValueRef;
import data.NumberValueRef;
import data.StringValueRef;
import engines.Base;
import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIScrollbar extends UIElement implements ChangeListener, PropertyChangeListener
{
    public static final String HANDLEON_KEY = "handleOn";
    public static final String HANDLEOFF_KEY = "handleOff";
    public static final String MIN_KEY = "min";
    public static final String MAX_KEY = "max";
    public static final String VALUEREF_KEY = "valueRef";
    public static final String VALUE_KEY = "value";
    public static final String HANDLETRESHOLD_KEY = "handleThreshold";
    public static final String ORIENTATION_KEY = "orientation";
    
    private final NumberValueRef minValue;
    private final NumberValueRef maxValue;
    private final StringValueRef valueRef;
    private final NumberValueRef value;
    private final NumberValueRef handleTreshold;
    private final EnumValueRef orientation;
    private final Handle handle;
    private String prevValueRef;
    
    protected UIScrollbar(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        this.config.put(HANDLEON_KEY, ImageValueRef.create(localContext, config, config.get(HANDLEON_KEY), true));
        this.config.put(HANDLEOFF_KEY, ImageValueRef.create(localContext, config, config.get(HANDLEOFF_KEY), true));
        
        minValue = (NumberValueRef) NumberValueRef.create(localContext, config, config.get(MIN_KEY), true);
        maxValue = (NumberValueRef) NumberValueRef.create(localContext, config, config.get(MAX_KEY), true);
        handleTreshold = (NumberValueRef) NumberValueRef.create(localContext, config, config.get(HANDLETRESHOLD_KEY), true);
        orientation = (EnumValueRef) EnumValueRef.create(localContext, config, config.get(ORIENTATION_KEY), UIElement.ORIENTATION, true);
        valueRef = (StringValueRef) StringValueRef.create(localContext, config, config.get(VALUEREF_KEY), true);
        value = (NumberValueRef) NumberValueRef.create(localContext, config, config.get(VALUE_KEY), true);
        
        config.put(MIN_KEY, minValue);
        config.put(MAX_KEY, maxValue);
        config.put(HANDLETRESHOLD_KEY, handleTreshold);
        config.put(VALUEREF_KEY, valueRef);
        config.put(ORIENTATION_KEY, orientation);
        config.put(VALUE_KEY, value);
        
        handle = new Handle(this);
        add(handle);

        valueRef.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                Base.removeChangeListener(localContext, config, UIScrollbar.this, prevValueRef);
                prevValueRef = valueRef.toString();
                Base.addChangeListener(localContext, config, UIScrollbar.this, prevValueRef);
                propertyChange(null);
            }
        });
        
        prevValueRef = valueRef.toString();
        Base.addChangeListener(localContext, config, UIScrollbar.this, prevValueRef);
        propertyChange(null);
        
        value.setChangeListener(this);
        minValue.setChangeListener(this);
        maxValue.setChangeListener(this);
        handleTreshold.setChangeListener(this);
        orientation.setChangeListener(this);
        
        updateHandle();
    }
    
    private void updateHandle()
    {
        int x, y;
        double p = (value.doubleValue() - minValue.doubleValue()) / (maxValue.doubleValue() - minValue.doubleValue());
        
        if (orientation.getIndex() == 0)
        {   // vertical
            x = (getWidth() - handle.getWidth()) / 2;
            
            y = (int)Math.round(p * (getHeight() - handle.getHeight()));
        }
        else
        {   // horizontal
            y = (getHeight() - handle.getHeight()) / 2;
            
            x = (int)Math.round(p * (getWidth() - handle.getWidth()));
        }

        handle.setLocation(x, y);
    }
    
    private void updateValue(Point clickLocation)
    {
        int value;
        double p;
        
        if (orientation.getIndex() == 0)
        {   // vertical
            p = clickLocation.y / ((double) getHeight() - handle.getHeight());
        }
        else
        {   // horizontal
            p = clickLocation.y / ((double) getWidth() - handle.getWidth());
        }
            
        p = Math.max(0, Math.min(1, p));
        
        double newValue = (p * (maxValue.doubleValue() - minValue.doubleValue()) + minValue.doubleValue());
       
        try {
            Base.setVariable(localContext, config, prevValueRef, newValue);
        } catch (Exception ex) {
            Logger.getLogger(UIScrollbar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
            
    
    @Override
    public void mousePressed(MouseEvent e) {       
        super.mousePressed(e);
        updateValue(e.getPoint());
    }
    
    @Override
    public void mouseDragged(MouseEvent e) {
        super.mousePressed(e);
        updateValue(e.getPoint());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updateHandle();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            Object var = Base.getVariable(localContext, config, prevValueRef);
            Base.setVariable(localContext, config, (var == null) ? 0 : var.toString(),
                    Base.LOCAL_ROOT, UIContainer.COMPONENTS_KEY, Integer.toString(elementIndex), VALUE_KEY);
        } catch (Exception ex) {
            Logger.getLogger(UIScrollbar.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class Handle extends JComponent implements MouseInputListener, AWTEventListener {
        UIScrollbar parent;
        
        private boolean isPressed = false;
        
        public Handle(UIScrollbar parent)
        {
            this.parent = parent;
            
            BufferedImage img = ((ImageValueRef)config.get(HANDLEOFF_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
            
            if (img != null)
            {
                this.setBounds(0, 0, img.getWidth(), img.getHeight());
            }
            
            addMouseListener(this);
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (parent.maxValue.doubleValue() > parent.handleTreshold.doubleValue())
            {
                BufferedImage img; 
                
                if (isPressed)
                {
                    img = ((ImageValueRef)config.get(HANDLEON_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
                }
                else
                {
                    img = ((ImageValueRef)config.get(HANDLEOFF_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
                }
                
                if (img != null)
                    g.drawImage(img, 0, 0, null);
            }
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            forwardMouseEvent(e);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            isPressed = true;
            Toolkit.getDefaultToolkit().addAWTEventListener(this, AWTEvent.MOUSE_MOTION_EVENT_MASK);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            isPressed = false;
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
        }

        @Override
        public void eventDispatched(AWTEvent event) {
            if ((event.getID() & AWTEvent.MOUSE_MOTION_EVENT_MASK) != 0)
            {
                updateValue(SwingUtilities.convertPoint(this, ((MouseEvent)event).getPoint(), parent));
            }
        }
    }
}
