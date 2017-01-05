/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.BooleanValueRef;
import data.ImageValueRef;
import data.NumberValueRef;
import data.StringValueRef;
import engines.Base;
import engines.Sound;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIButton extends UIElement implements PropertyChangeListener
{    
    public static final String IMAGEON_KEY = "imageOn";
    public static final String IMAGEOFF_KEY = "imageOff";
    public static final String ONCLICK_KEY = "onClick";
    public static final String REPEAT_CLICKS_KEY = "repeatClicks";
    public static final String REPEAT_CLICKS_RATE_KEY = "repeatClickRate";
    public static final String CLICK_SOUND_KEY = "clickSound";
    
    // state
    private boolean isPressed = false;
    private final StringValueRef clickSound;
    private int pressTick = 0;
    private int clickRepeats = 0;
    
    protected UIButton(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        this.config.put(IMAGEOFF_KEY, ImageValueRef.create(localContext, config, config.get(IMAGEOFF_KEY), true));
        this.config.put(IMAGEON_KEY, ImageValueRef.create(localContext, config, config.get(IMAGEON_KEY), true));
        this.config.put(CLICK_SOUND_KEY, StringValueRef.create(localContext, config, config.get(CLICK_SOUND_KEY), true));
        this.config.put(REPEAT_CLICKS_KEY, BooleanValueRef.create(localContext, config, config.get(REPEAT_CLICKS_KEY), true));
        this.config.put(REPEAT_CLICKS_RATE_KEY, NumberValueRef.create(localContext, config, config.get(REPEAT_CLICKS_RATE_KEY), true));
        
        clickSound = (StringValueRef) this.config.get(CLICK_SOUND_KEY);
        
        Base.addChangeListener(localContext, config, this, Base.RUNTIME_ROOT + Base.NODE_SEPARATOR + Base.RUNTIME_VAR_UI_TICK);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        
        BufferedImage imageOff = ((ImageValueRef)this.config.get(IMAGEOFF_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
        BufferedImage imageOn = ((ImageValueRef)this.config.get(IMAGEON_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);
        
        if (mouseHover.getValue() && !isPressed && imageOn != null && imageOff != null)
        {
            float transparency = Math.abs(100 - Window.UI_TICK % 200) / 100f;
            
            g2.drawImage(imageOff, 0, 0, this);
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, transparency));
            g2.drawImage(imageOn, 0, 0, this);
        }
        else if (imageOn != null && isPressed)
        {
            g2.drawImage(imageOn, 0, 0, this);
        }
        else if (imageOff != null && !isPressed)
        {
            g2.drawImage(imageOff, 0, 0, this);
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {       
        isPressed = true;
        pressTick = (int) Base.getVariable(localContext, config, Base.RUNTIME_ROOT, Base.RUNTIME_VAR_UI_TICK);
        clickRepeats = 0;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        isPressed = false;
        
        if (clickRepeats == 0)
        {
            String sound = clickSound.toString();
            
            if (sound.length() > 0) Sound.playSound(sound);
            
            Object onClick = this.config.get(ONCLICK_KEY);
            
            if (onClick != null)
            {
               runCommand(onClick);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (isPressed && ((BooleanValueRef)config.get(REPEAT_CLICKS_KEY)).getValue() && evt.getPropertyName().endsWith(Base.RUNTIME_VAR_UI_TICK))
        {
            Object newValue = evt.getNewValue();
            
            int time = (int)newValue - pressTick;
            
            while (time < 0) time += Window.MAX_UITICK_VALUE;
            
            if (time >= ((NumberValueRef)config.get(REPEAT_CLICKS_RATE_KEY)).intValue())
            {
                pressTick = (int)evt.getNewValue();
                clickRepeats++;
                
                String sound = clickSound.toString();
            
                if (sound.length() > 0) Sound.playSound(sound);
                
                Object onClick = this.config.get(ONCLICK_KEY);
                
                if (onClick != null)
                {
                    runCommand(onClick);
                }
            }
        }
    }
}
