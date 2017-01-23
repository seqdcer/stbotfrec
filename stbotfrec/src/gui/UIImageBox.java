/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.ImageValueRef;
import data.StringValueRef;
import engines.Sound;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIImageBox extends UIElement {
    
    public static final String ONCLICK_KEY = "onClick";
    public static final String CLICK_SOUND_KEY = "clickSound";
    public static final String IMAGE_KEY = "image";
    
    // state
    private final StringValueRef clickSound;

    protected UIImageBox(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        this.config.put(IMAGE_KEY, ImageValueRef.create(localContext, config, this.config.get(IMAGE_KEY), true));
        this.config.put(CLICK_SOUND_KEY, StringValueRef.create(localContext, config, config.get(CLICK_SOUND_KEY), true));
        
        clickSound = (StringValueRef) this.config.get(CLICK_SOUND_KEY);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        BufferedImage image = ((ImageValueRef)config.get(IMAGE_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);

        if (image != null)
        {
            if (disabled.getValue())
            {
                RescaleOp op = new RescaleOp(new float[]{0.5f, 0.5f, 0.5f, 1}, new float[]{0f, 0f,0f,0f}, null);
                image = op.filter(image, null);
            }
        }

        g.drawImage(image, 0, 0, this);
    }
    
    @Override
    public void mouseReleased(MouseEvent e) {
        super.mouseReleased(e);
        
        String sound = clickSound.toString();
            
        if (sound.length() > 0) Sound.playSound(sound);
        
        Object onClick = this.config.get(ONCLICK_KEY);

        if (onClick != null)
        {
            runCommand(onClick);
        }
        
        forwardMouseEvent(e);
    }
}
