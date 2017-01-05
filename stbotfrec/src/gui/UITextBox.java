/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.EnumValueRef;
import data.FontRef;
import data.NumberValueRef;
import data.StringValueRef;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UITextBox  extends UIElement {
    
    public static final String TEXT_X_KEY = "textXPos";
    public static final String TEXT_Y_KEY = "textYPos";
    public static final String TEXT_WIDTH_KEY = "textWidth";
    public static final String TEXT_HEIGHT_KEY = "textHeight";
    public static final String FONT_KEY = "font";
    public static final String TEXT_KEY = "text";
    public static final String HORIZONTAL_TEXT_ALLIGNMENT_KEY = "horizontalTextAlignment";
    public static final String VERTICAL_TEXT_ALLIGNMENT_KEY = "verticalTextAlignment";
    
    // state
    StringValueRef text;
    FontRef font;
    EnumValueRef horizontalTextAlign;
    EnumValueRef verticalTextAlign;
    BufferedImage prerender;
    
    protected UITextBox(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        this.config.put(TEXT_X_KEY, NumberValueRef.create(localContext, config, this.config.get(TEXT_X_KEY), true));
        this.config.put(TEXT_Y_KEY, NumberValueRef.create(localContext, config, this.config.get(TEXT_Y_KEY), true));
        this.config.put(TEXT_WIDTH_KEY, NumberValueRef.create(localContext, config, this.config.get(TEXT_WIDTH_KEY), true));
        this.config.put(TEXT_HEIGHT_KEY, NumberValueRef.create(localContext, config, this.config.get(TEXT_HEIGHT_KEY), true));
        
        this.config.put(TEXT_KEY, StringValueRef.create(localContext, config, this.config.get(TEXT_KEY), true));
        this.config.put(FONT_KEY, FontRef.create(localContext, config, this.config.get(FONT_KEY), true));
        this.config.put(HORIZONTAL_TEXT_ALLIGNMENT_KEY, EnumValueRef.create(localContext, config, this.config.get(HORIZONTAL_TEXT_ALLIGNMENT_KEY), HORIZONTAL_ALIGNMENT, true));
        this.config.put(VERTICAL_TEXT_ALLIGNMENT_KEY, EnumValueRef.create(localContext, config, this.config.get(VERTICAL_TEXT_ALLIGNMENT_KEY), VERTICAL_ALIGNMENT, true));
        
        font = (FontRef)this.config.get(FONT_KEY);
        text = (StringValueRef) this.config.get(TEXT_KEY);
        horizontalTextAlign = (EnumValueRef)this.config.get(HORIZONTAL_TEXT_ALLIGNMENT_KEY);
        verticalTextAlign = (EnumValueRef)this.config.get(VERTICAL_TEXT_ALLIGNMENT_KEY);
        
        if (font != null && font.getFont() != null)
            prerender = font.getFont().createText(text.toString(), font.getColor(),
                    ((NumberValueRef)this.config.get(TEXT_WIDTH_KEY)).intValue(), ((NumberValueRef)this.config.get(TEXT_HEIGHT_KEY)).intValue(), horizontalTextAlign.getIndex(),
                    font.getStyle().getIndex(), font.getUpperCase());
        
        ChangeListener textChangeListener = (ChangeEvent e) -> {
           if (font != null && font.getFont() != null)
                prerender = font.getFont().createText(text.toString(), font.getColor(),
                    ((NumberValueRef)this.config.get(TEXT_WIDTH_KEY)).intValue(), ((NumberValueRef)this.config.get(TEXT_HEIGHT_KEY)).intValue(), horizontalTextAlign.getIndex(),
                    font.getStyle().getIndex(), font.getUpperCase());
        };
        
        font.setChangeListener(textChangeListener);
        text.setChangeListener(textChangeListener);
        horizontalTextAlign.setChangeListener(textChangeListener);
        ((NumberValueRef)this.config.get(TEXT_WIDTH_KEY)).setChangeListener(textChangeListener);
        ((NumberValueRef)this.config.get(TEXT_HEIGHT_KEY)).setChangeListener(textChangeListener);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (prerender != null)
        {
            int y;
            
            switch (verticalTextAlign.getIndex())
            {
                case 1:     // center
                {
                    y = (this.getHeight() - prerender.getHeight()) / 2;
                    break;
                }
                case 2:     // bottom
                {
                    y = this.getHeight() - prerender.getHeight();
                    break;
                }
                default:    // top
                    y = 0;
            }
            
            g.drawImage(prerender, ((NumberValueRef)this.config.get(TEXT_X_KEY)).intValue(), ((NumberValueRef)this.config.get(TEXT_Y_KEY)).intValue() + y, this);
        }
    }
}
