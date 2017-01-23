/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.BooleanValueRef;
import data.ImageValueRef;
import data.StringValueRef;
import data.ToggleRef;
import engines.Base;
import engines.Sound;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIToggle extends UIElement
{    
    public static final String IMAGEON_KEY = "imageOn";
    public static final String IMAGEOFF_KEY = "imageOff";
    public static final String TOGGLE_GROUP_KEY = "toggleGroup";
    public static final String TOGGLE_SOUND_KEY = "toggleSound";
    public static final String ONTOGGLE_KEY = "onToggle";
    public static final String ISON_KEY = "isOn";
    public static final String ALLOWDESELECT_KEY = "allowDeselect";

    private final ToggleRef toggleGroup;
    private final StringValueRef toggleSound;
    
    // state
    private final BooleanValueRef isOn;
    private final BooleanValueRef allowDeselect;
    
    protected UIToggle(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        this.config.put(IMAGEOFF_KEY, ImageValueRef.create(localContext, config, config.get(IMAGEOFF_KEY), true));
        this.config.put(IMAGEON_KEY, ImageValueRef.create(localContext, config, config.get(IMAGEON_KEY), true));
        this.config.put(TOGGLE_SOUND_KEY, StringValueRef.create(localContext, config, config.get(TOGGLE_SOUND_KEY), true));
        
        isOn = (BooleanValueRef) BooleanValueRef.create(localContext, config, config.get(ISON_KEY), true);
        this.config.put(ISON_KEY, isOn);
        
        allowDeselect = (BooleanValueRef) BooleanValueRef.create(localContext, config, config.get(ALLOWDESELECT_KEY), true);
        this.config.put(ALLOWDESELECT_KEY, allowDeselect);
        
        toggleGroup = (ToggleRef) ToggleRef.create(localContext, config, config.get(TOGGLE_GROUP_KEY), true);
        this.config.put(TOGGLE_GROUP_KEY, toggleGroup);
        
        toggleGroup.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                try {
                    Base.setVariable(localContext, config, toggleGroup.isSelected(), Base.LOCAL_ROOT, UIContainer.COMPONENTS_KEY, Integer.toString(index), ISON_KEY);
                } catch (Exception ex) {
                    Logger.getLogger(UIToggle.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        
        toggleSound = (StringValueRef) this.config.get(TOGGLE_SOUND_KEY);
        
        if (toggleGroup.isSelected())
        {
            toggle(true, false);
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (!disabled.getValue())
        {
            Graphics2D g2 = (Graphics2D) g;

            BufferedImage imageOff = ((ImageValueRef)this.config.get(IMAGEOFF_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
            BufferedImage imageOn = ((ImageValueRef)this.config.get(IMAGEON_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
        
            if (mouseHover.getValue() && !isOn.getValue() && imageOn != null && imageOff != null)
            {
                float transparency = Math.abs(100 - Window.UI_TICK % 200) / 100f;

                g2.drawImage(imageOff, 0, 0, this);

                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
                g2.drawImage(imageOn, 0, 0, this);
            }
            else if (imageOn != null && isOn.getValue())
            {
                g2.drawImage(imageOn, 0, 0, this);
            }
            else if (imageOff != null && !isOn.getValue())
            {
                g2.drawImage(imageOff, 0, 0, this);
            }
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);

        if (disabled.getValue()) return;
        
        boolean nval = !isOn.getValue();
        
        if (nval == false && !allowDeselect.getValue())
        {
            return;
        }
        
        toggle(nval, true);
    }
    
    private void toggle(boolean value, boolean playSound)
    {
        try {
            Base.setVariable(localContext, config, value, Base.LOCAL_ROOT, UIContainer.COMPONENTS_KEY, Integer.toString(elementIndex), ISON_KEY);
        } catch (Exception ex) {
            Logger.getLogger(UIToggle.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (playSound)
        {
            String sound = toggleSound.toString();

            if (sound.length() > 0) Sound.playSound(sound);
        }
        
        try {
            toggleGroup.setSelected(value);
        } catch (Exception ex) {
            Logger.getLogger(UIToggle.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (value)
        {
            Object onToggle = this.config.get(ONTOGGLE_KEY);

            if (onToggle != null)
            {
                runCommand(onToggle);
            }
        }
    }
}
