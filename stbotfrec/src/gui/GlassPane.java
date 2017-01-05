/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import javax.swing.JPanel;

/**
 *
 * @author AP
 */
public class GlassPane extends JPanel {

    public GlassPane()
    {
        setOpaque(false);
        addMouseListener( new MouseAdapter() { } );
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Component c = this.getComponent(0);
        
        if (c != null)
        {
            setCursor(c.getCursor());
        }
    }
    
    public void setCursor()
    {
        
    }
}
