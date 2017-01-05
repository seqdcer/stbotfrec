/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.NumberValueRef;
import data.Toolkit;
import engines.Base;
import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIGrid extends UIElement {
    
    public static final String GRIDWIDTH_KEY = "gridWidth";
    public static final String GRIDHEIGHT_KEY = "gridHeight";
    public static final String GRIDX_KEY = "gridX";
    public static final String GRIDY_KEY = "gridY";
    public static final String GRIDOFFSETX_KEY = "gridOffsetX";
    public static final String GRIDOFFSETY_KEY = "gridOffsetY";
    public static final String GRIDELEMENT_KEY = "gridElement";
    public static final String GRIDELEMENTGAP_KEY = "gapBetweenElements";
    public static final String GRID_KEY = "grid";

    NumberValueRef gridWidth;
    NumberValueRef gridHeight;
    NumberValueRef gridOffsetX;
    NumberValueRef gridOffsetY;
    NumberValueRef gridGap;
    JPanel grid;
    
    protected UIGrid(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        gridWidth = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(GRIDWIDTH_KEY), true);
        gridHeight = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(GRIDHEIGHT_KEY), true);
        gridOffsetX = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(GRIDOFFSETX_KEY), true);
        gridOffsetY = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(GRIDOFFSETY_KEY), true);
        gridGap = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(GRIDELEMENTGAP_KEY), true);
        
        this.config.put(GRIDWIDTH_KEY, gridWidth);
        this.config.put(GRIDHEIGHT_KEY, gridHeight);
        this.config.put(GRIDOFFSETX_KEY, gridOffsetX);
        this.config.put(GRIDOFFSETY_KEY, gridOffsetY);
        this.config.put(GRIDELEMENTGAP_KEY, gridGap);
        
        ChangeListener sizeChange = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                setup();
            }
        };
        
        gridWidth.setChangeListener(sizeChange);
        gridHeight.setChangeListener(sizeChange);
        
        ChangeListener offsetChange = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                // max offset in cell spaces
                double maxOffsetX = (grid.getWidth() - getWidth()) / (grid.getWidth() / gridWidth.doubleValue());
                double maxOffsetY = (grid.getHeight() - getHeight()) / (grid.getHeight() / gridHeight.doubleValue());

                // limit current offset
                double offsetX = Math.max(Math.min(gridOffsetX.doubleValue(), gridWidth.doubleValue()), 0);
                double offsetY = Math.max(Math.min(gridOffsetY.doubleValue(), gridHeight.doubleValue()), 0);

                
                offsetX = offsetX / gridWidth.doubleValue() * maxOffsetX;
                offsetY = offsetY / gridHeight.doubleValue() * maxOffsetY;

                int posX = - (int)Math.round(offsetX * (grid.getWidth() / gridWidth.doubleValue()));
                int posY = - (int)Math.round(offsetY * (grid.getHeight() / gridHeight.doubleValue()));
                
                grid.setBounds(posX, posY, grid.getWidth(), grid.getHeight());
            }
        };
        
        gridOffsetX.setChangeListener(offsetChange);
        gridOffsetY.setChangeListener(offsetChange);
        
        setup();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    private void setup()
    {
        this.removeAll();
        
        grid = new JPanel(null);
        grid.setOpaque(false);
        
        JSONObject element = (JSONObject)config.get(GRIDELEMENT_KEY);
        
        NumberValueRef elementWidth = (NumberValueRef) NumberValueRef.create(localContext, config, element.get(UIElement.WIDTH_KEY), true);
        NumberValueRef elementHeight = (NumberValueRef) NumberValueRef.create(localContext, config, element.get(UIElement.HEIGHT_KEY), true);

        grid.setBounds(0, 0, elementWidth.intValue() * gridWidth.intValue() + gridGap.intValue() * (gridWidth.intValue() - 1),
                elementHeight.intValue() * gridHeight.intValue() + gridGap.intValue() * (gridHeight.intValue() - 1));
        
        int posX = 0;
        int posY = 0;
        UIElement comp;
        
        for (int y = 0; y < gridHeight.intValue(); y++)
        {
            for (int x = 0; x < gridWidth.intValue(); x++)
            {
                JSONObject clone = (JSONObject)Toolkit.deepCopyJSON(element);
                clone.put(UIElement.X_KEY, posX);
                clone.put(UIElement.Y_KEY, posY);
                clone.put(GRIDX_KEY, x);
                clone.put(GRIDY_KEY, y);
                clone.put(Base.ID_KEY, localContext.get(Base.ID_KEY).toString() + Base.NODE_SEPARATOR + GRID_KEY + Base.NODE_SEPARATOR + x + Base.NODE_SEPARATOR + y);
                
                try {
                    Base.setVariable(localContext, config, Base.THIS_ROOT + Base.NODE_SEPARATOR + GRID_KEY + Base.NODE_SEPARATOR + x + Base.NODE_SEPARATOR + y, clone);
                
                    comp = UIElement.create(clone, localContext, y * gridWidth.intValue() + x);
                    grid.add(comp);
                    comp.finalSetup(this);
                    
                } catch (Exception ex) {
                    Logger.getLogger(UIGrid.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                posX += elementWidth.intValue() + gridGap.intValue();
            }
            
            posY += elementHeight.intValue() + gridGap.intValue();
            posX = 0;
        }
        
        add(grid);
    }
}
