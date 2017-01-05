/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.ColumnListRef;
import data.ColumnRef;
import data.FontRef;
import data.NumberValueRef;
import data.StringValueRef;
import engines.Base;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class UIList extends UIElement {
    
    public static final String FONTON_KEY = "fontOn";
    public static final String FONTOFF_KEY = "fontOff";
    public static final String COLUMNS_KEY = "columns";
    public static final String SOURCELIST_KEY = "sourceList";
    public static final String SELECTEDINDEX_KEY = "selectedIndex";
    public static final String LISTOFFSET_KEY = "listOffset";
    public static final String MAXLISTOFFSET_KEY = "maxListOffset";
    public static final String MAXVISIBLEITEMS_KEY = "maxVisibleItems";

    FontRef fontOn;
    FontRef fontOff;
    ColumnListRef columns;
    StringValueRef sourceList;
    NumberValueRef listOffset;
    NumberValueRef selectedIndex;
    
    protected UIList(JSONObject config, JSONObject localContext, int index)
    {
        super(config, localContext, index);
        
        // setup
        fontOn = (FontRef) FontRef.create(localContext, config, this.config.get(FONTON_KEY), true);
        fontOff = (FontRef) FontRef.create(localContext, config, this.config.get(FONTOFF_KEY), true);
        columns = (ColumnListRef) ColumnListRef.create(localContext, config, this.config.get(COLUMNS_KEY), true);
        sourceList = (StringValueRef) StringValueRef.create(localContext, config, this.config.get(SOURCELIST_KEY), true);
        listOffset = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(LISTOFFSET_KEY), true);
        selectedIndex = (NumberValueRef) NumberValueRef.create(localContext, config, this.config.get(SELECTEDINDEX_KEY), true);
        
        this.config.put(FONTON_KEY, fontOn);
        this.config.put(FONTOFF_KEY, fontOff);
        this.config.put(COLUMNS_KEY, columns);
        this.config.put(SOURCELIST_KEY, sourceList);
        this.config.put(LISTOFFSET_KEY, listOffset);
        this.config.put(SELECTEDINDEX_KEY, selectedIndex);
        
        sourceList.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                setup();
            }
        });
        
        listOffset.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                Component[] comps = UIList.this.getComponents();
                
                for (Component c : comps)
                {
                    ((ListItem) c).reset(sourceList.toString());
                }
            }
        });
        
        setup();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
    private void setup()
    {
        this.removeAll();
        
        int height = 0;
        int index = 0;
        int textHeight = fontOff.getFont().getHeight();
        
        while (height + textHeight < getHeight())
        {
            add(new ListItem(this, sourceList.toString(), index));
            index++;
            height += textHeight;
        }
        
        int maxListOffset = (int) Base.getVariable(localContext, config, sourceList.toString() + Base.NODE_SEPARATOR + "count");
        
        maxListOffset = Math.max(0, maxListOffset - index);
        
        try {
            Base.setVariable(localContext, config, maxListOffset, Base.LOCAL_ROOT, UIContainer.COMPONENTS_KEY, Integer.toString(elementIndex), MAXLISTOFFSET_KEY);
            Base.setVariable(localContext, config, index, Base.LOCAL_ROOT, UIContainer.COMPONENTS_KEY, Integer.toString(elementIndex), MAXVISIBLEITEMS_KEY);
        } catch (Exception ex) {
            Logger.getLogger(UIList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private class ListItem extends JComponent implements MouseInputListener, ChangeListener {
        private static final String COLUMN_SEPARATOR = "\t";
        private static final String TEXT_KEY = "text";
        
        private final UIList parent;

        private BufferedImage prerenderOn;
        private BufferedImage prerenderOff;
        
        private StringValueRef value;
        private int workIndex;
        
        // state
        private final int index;
        
        public ListItem(UIList parent, String itemRef, int index)
        {
            this.parent = parent;
            this.index = index;
            this.addMouseListener(this);
            reset(itemRef);
        }
        
        public final void reset(String itemRef)
        {
            if (value != null)
            {
                value.setChangeListener(null);
                value.destroy();
            }
            
            if (index == 0)
                workIndex = 0;
            else
                workIndex = index + parent.listOffset.intValue();
            
            value = (StringValueRef) StringValueRef.create(localContext, config, Base.REF_START + itemRef + Base.NODE_SEPARATOR + workIndex + Base.NODE_SEPARATOR + TEXT_KEY + Base.REF_END, true);
            value.setChangeListener(this);
            
            setup();
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (workIndex == parent.selectedIndex.intValue())
            {
                g.drawImage(prerenderOn, 0, 0, null);
            }
            else
            {
                g.drawImage(prerenderOff, 0, 0, null);
            }
        }
        
        @Override
        public void mouseClicked(MouseEvent e) {
            
        }

        @Override
        public void mousePressed(MouseEvent e) {
            parent.selectedIndex.set(workIndex);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
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

        private void setup() {
            String[] text = value.toString().split(COLUMN_SEPARATOR);
            
            prerenderOn = createPrerender(text, parent.fontOn);
            prerenderOff = createPrerender(text, parent.fontOff);
            
            int height = parent.fontOff.getFont().getHeight();
            
            setBounds(0, index * height, parent.getWidth(), height);
        }
        
        private BufferedImage createPrerender(String[] text, FontRef font)
        {
            int height = font.getFont().getHeight();
            int x = 0;

            BufferedImage img = new BufferedImage(parent.getWidth(), height, BufferedImage.TYPE_INT_ARGB); 
            Graphics g = img.createGraphics();
            
            for (int i = 0; i < text.length && i < parent.columns.getColumnCount(); i++)
            {
                ColumnRef column = parent.columns.getColumn(i);
                
                BufferedImage col = font.getFont().createText(text[i], font.getColor(), column.getWidth(), height, column.getHorizontalTextAlignment(), font.getStyle().getIndex(), font.getUpperCase());
                
                g.drawImage(col, x, 0, null);
                
                x += column.getWidth();
            }
            
            g.dispose();
            
            return img;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            setup();
        }
    }
}
