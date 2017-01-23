/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import data.BooleanValueRef;
import data.BorderRef;
import data.ImageValueRef;
import data.KeyBindsRef;
import data.NumberValueRef;
import data.StringValueRef;
import data.Toolkit;
import data.TriggerListRef;
import engines.Base;
import init.Main;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputListener;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public abstract class UIElement extends JLayeredPane implements MouseInputListener, ContainerListener {
    public static final List<String> HORIZONTAL_ALIGNMENT = Collections.unmodifiableList(Arrays.asList("left", "center", "right"));
    public static final List<String> VERTICAL_ALIGNMENT = Collections.unmodifiableList(Arrays.asList("top", "center", "bottom"));
    public static final List<String> ORIENTATION = Collections.unmodifiableList(Arrays.asList("vertical", "horizontal"));
    
    public static final String TYPE_KEY = "type";
    public static final String COMPONENT_KEY = "component";
    public static final String X_KEY = "x";
    public static final String Y_KEY = "y";
    public static final String WIDTH_KEY = "width";
    public static final String HEIGHT_KEY = "height";
    public static final String BACKGROUND_KEY = "background";
    public static final String VISIBLE_KEY = "visible";
    public static final String IGNOREDRAWBORDER_KEY = "ignoreDrawBorder";

    public static final String TYPE_BUTTON = "button";
    public static final String TYPE_TEXTBOX = "textbox";
    public static final String TYPE_IMAGEBOX = "imagebox";
    public static final String TYPE_TOGGLE = "toggle";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_GRID = "grid";
    public static final String TYPE_CONTAINER = "container";
    public static final String TYPE_SCROLLBAR = "scrollbar";
    public static final String TYPE_EXTRERNAL_COMPONENT = "externalComponent";
    
    public static final String MOUSE_HOVER_KEY = "mouseHover";

    // action triggers    
    public static final String ONLOAD_KEY = "onLoad";
    public static final String ON_MOUSE_ENTERED_KEY = "onMouseEntered";
    public static final String ON_MOUSE_EXITED_KEY = "onMouseExited";
    public static final String ONVISIBLE_KEY = "onVisible";
    public static final String KEYBINDS_KEY = "keyBinds";
    public static final String TRIGGERS_KEY = "triggers";
    public static final String BORDER_KEY = "border";
    public static final String DISABLED_KEY = "disabled";
    
    // state
    protected final BooleanValueRef mouseHover;
    protected final BooleanValueRef visible;
    protected final BooleanValueRef disabled;
    private final Object onMouseEntered;
    private final Object onMouseExited;
    private final KeyBindsRef keyBinds;
    private final BorderRef border;
    protected UIElement parentElement;
    
    // config
    protected final int elementIndex;
    protected final JSONObject config;
    protected final JSONObject localContext;
    
    protected UIElement(JSONObject config, JSONObject localContext, int index)
    {
        this.localContext = localContext;
        this.config =  config;
        this.elementIndex = index;
        this.setOpaque(false);
        
        // setup
        this.config.put(BACKGROUND_KEY, ImageValueRef.create(localContext, config, this.config.get(BACKGROUND_KEY), true));
        this.config.put(X_KEY, NumberValueRef.create(localContext, config, this.config.get(X_KEY), true));
        this.config.put(Y_KEY, NumberValueRef.create(localContext, config, this.config.get(Y_KEY), true));
        this.config.put(WIDTH_KEY, NumberValueRef.create(localContext, config, this.config.get(WIDTH_KEY), true));
        this.config.put(HEIGHT_KEY, NumberValueRef.create(localContext, config, this.config.get(HEIGHT_KEY), true));
        this.config.put(KEYBINDS_KEY, KeyBindsRef.create(localContext, config, config.get(KEYBINDS_KEY), true));
        this.config.put(TRIGGERS_KEY, TriggerListRef.create(localContext, config, config.get(TRIGGERS_KEY), true));
        this.config.put(IGNOREDRAWBORDER_KEY, BooleanValueRef.create(localContext, config, config.get(IGNOREDRAWBORDER_KEY), true));
        
        border = (BorderRef) BorderRef.create(localContext, config, config.get(BORDER_KEY), true);
        this.config.put(BORDER_KEY, border);
        
        mouseHover = (BooleanValueRef) BooleanValueRef.create(localContext, config, false, true);
        this.config.put(MOUSE_HOVER_KEY, mouseHover);
        
        Object val = this.config.get(VISIBLE_KEY);
        
        if (val == null) val = true;
        visible = (BooleanValueRef) BooleanValueRef.create(localContext, config, val, true);
        this.config.put(VISIBLE_KEY, visible);
        
        visible.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                setVisible(visible.getValue());
            }
        });
        
        disabled = (BooleanValueRef) BooleanValueRef.create(localContext, config, config.get(DISABLED_KEY), true);
        this.config.put(DISABLED_KEY, disabled);
        
        onMouseEntered = this.config.get(ON_MOUSE_ENTERED_KEY);
        onMouseExited = this.config.get(ON_MOUSE_EXITED_KEY);
        keyBinds = (KeyBindsRef) this.config.get(KEYBINDS_KEY);
        
        // alicia keys!
        keyBinds.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                setupKeys();
            }
        });
        setupKeys();
        
        border.setChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e) {
                setupBorder();
            }
        });
        
        ChangeListener bounds = new ChangeListener(){

            @Override
            public void stateChanged(ChangeEvent e) {
                setBounds(
                    ((NumberValueRef)config.get(X_KEY)).intValue(),
                    ((NumberValueRef)config.get(Y_KEY)).intValue(),
                    ((NumberValueRef)config.get(WIDTH_KEY)).intValue(),
                    ((NumberValueRef)config.get(HEIGHT_KEY)).intValue());
            }
        };
        
        ((NumberValueRef)config.get(X_KEY)).setChangeListener(bounds);
        ((NumberValueRef)config.get(Y_KEY)).setChangeListener(bounds);
        ((NumberValueRef)config.get(WIDTH_KEY)).setChangeListener(bounds);
        ((NumberValueRef)config.get(HEIGHT_KEY)).setChangeListener(bounds);
        
        bounds.stateChanged(null);
        this.addContainerListener(this);
        setVisible(false);
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        
        super.paintComponent(g);
        
        
        BufferedImage background = ((ImageValueRef)config.get(BACKGROUND_KEY)).getValue(Window.UI_TICK, Window.MAX_UITICK_VALUE);

        if (background != null)
        {
            if (disabled.getValue())
            {
                RescaleOp op = new RescaleOp(new float[]{0.5f, 0.5f, 0.5f, 1}, new float[]{0f, 0f,0f,0f}, null);
                background = op.filter(background, null);
            }
            
            g.drawImage(background, 0, 0, this);
        }
    }

    public boolean ignoresBorders()
    {
        return ((BooleanValueRef)config.get(IGNOREDRAWBORDER_KEY)).getValue();
    }
    
    public static UIElement create(JSONObject config, JSONObject localContext, int index)
    {
        String type = (String) config.get(TYPE_KEY);
        UIElement element;
        
        switch (type)
        {
            case TYPE_BUTTON:
            {
                element = new UIButton(config, localContext, index);
                break;
            }
            case TYPE_TEXTBOX:
            {
                element = new UITextBox(config, localContext, index);
                break;
            }
            case TYPE_IMAGEBOX:
            {
                element = new UIImageBox(config, localContext, index);
                break;
            }
            case TYPE_TOGGLE:
            {
                element = new UIToggle(config, localContext, index);
                break;
            }
            case TYPE_LIST:
            {
                element = new UIList(config, localContext, index);
                break;
            }
            case TYPE_SCROLLBAR:
            {
                element = new UIScrollbar(config, localContext, index);
                break;
            }
            case TYPE_GRID:
            {
                element = new UIGrid(config, localContext, index);
                break;
            }
            case TYPE_CONTAINER:
            {
                element = new UIContainer(config, localContext, index);
                break;
            }
            case TYPE_EXTRERNAL_COMPONENT:
            {
                String comp = StringValueRef.create(localContext, config, config.get(COMPONENT_KEY), true).toString();
                element = null;

                try
                {
                    JSONObject json = Base.getWindowConfig(comp);

                    if (json != null)
                    {
                        config.remove(COMPONENT_KEY);
                        config.remove(TYPE_KEY);
                        json.remove(Base.ID_KEY);
                        json.putAll(config);
                        config.putAll(json);
 
                        return create(config, localContext, index);
                    }
                }
                catch (Exception ex)
                {
                    Logger.getLogger(UIElement.class.getName()).log(Level.SEVERE, "Error: " + ex.getMessage(), ex);
                }
                
                break;
            }
            default:
                return null;
        }
        
        if (element != null)
        {
            element.onLoad();
        }
        
        return element;
    }
    
    public void onLoad()
    {
        runCommand(config.get(ONLOAD_KEY));
        
        ((TriggerListRef)TriggerListRef.create(localContext, config, config.get(TRIGGERS_KEY), true)).reEvaluate();
    }
    
    @Override
    public void mouseEntered(MouseEvent e) {
        forwardMouseEvent(e);
       
        if (!disabled.getValue())
        {
            if (mouseHover.getValue()) return;

            updateHover(true);

            runCommand(onMouseEntered);
        }
        
        forwardMouseEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        forwardMouseEvent(e);
        
        if (!mouseHover.getValue()) return;
        
        updateHover(false);
        
        runCommand(onMouseExited);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        forwardMouseEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        forwardMouseEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        forwardMouseEvent(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        forwardMouseEvent(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        forwardMouseEvent(e);
    }
    
    protected void forwardMouseEvent(MouseEvent e)
    {
        if (e.getSource() != this || !(parentElement instanceof UIContainer)) return;

        JLayeredPane container = (JLayeredPane) parentElement;
        
        int layer = (ignoresBorders()) ? container.highestLayer() : JLayeredPane.getLayer(this);
        
        Point componentPoint = e.getPoint();

        Point componentBelowPoint;
        Dimension size;
        
        for (int i = layer - 1; i > 0; i--)
        {
            Component[] components = container.getComponentsInLayer(i);
            
            for (Component componentBelow : components)
            {
                componentBelowPoint = SwingUtilities.convertPoint(this, componentPoint, componentBelow);
                size = componentBelow.getSize();
                
                if (componentBelowPoint.x >= 0 && componentBelowPoint.y >= 0 &&
                        componentBelowPoint.x <= size.width && componentBelowPoint.y <= size.height)
                {
                    componentBelow.dispatchEvent(new MouseEvent(this, MouseEvent.MOUSE_ENTERED,
                        e.getWhen(), e.getModifiers(), componentBelowPoint.x, componentBelowPoint.y,
                        e.getClickCount(), e.isPopupTrigger()));
                    
                    componentBelow.dispatchEvent(new MouseEvent(this, e.getID(),
                        e.getWhen(), e.getModifiers(), componentBelowPoint.x, componentBelowPoint.y,
                        e.getClickCount(), e.isPopupTrigger()));
                }
                else
                {
                    /*if (e.getID() != MouseEvent.MOUSE_EXITED)
                    {
                        Logger.getLogger(UIElement.class.getName()).log(Level.WARNING, "Mouse event missmatch. Possible UI missconfiguration in " + config.get(UIElement.TYPE_KEY));
                    }*/
                    componentBelow.dispatchEvent(new MouseEvent(this, MouseEvent.MOUSE_EXITED,
                        e.getWhen(), e.getModifiers(), componentBelowPoint.x, componentBelowPoint.y,
                        e.getClickCount(), e.isPopupTrigger()));
                }
            }
        }
    }
    
    private void updateHover(boolean value)
    {
        if (elementIndex >= 0)
        {
            try {
                Base.setVariable(localContext, config, value, Base.LOCAL_ROOT, UIContainer.COMPONENTS_KEY, Integer.toString(elementIndex), MOUSE_HOVER_KEY);
            } catch (Exception ex) {
                Logger.getLogger(UIElement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            try {
                Base.setVariable(localContext, config, value, Base.LOCAL_ROOT, MOUSE_HOVER_KEY);
            } catch (Exception ex) {
                Logger.getLogger(UIElement.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void setupKeys()
    {
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
        this.getActionMap().clear();
        
        HashMap<String, Object> binds = keyBinds.getKeyBinds();
        
        binds.keySet().stream().forEach((String key) -> {
            final Object cmd = binds.get(key);
            
            if (cmd != null)
            {
                this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), cmd);

                this.getActionMap().put(cmd, new AbstractAction()
                {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        runCommand(cmd);
                    }
                });
            }
        });
    }
    
    private void setupBorder()
    {
        if (border != null)
        {
            if (border.isDashed())
            {
                this.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(border.getWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, new float[]{border.getDashLength(), border.getDashSpacing()}, 0), border.getColor()));
//                BasicStroke(float width, int cap, int join, float miterlimit, float[] dash, float dash_phase)
//                this.setBorder(BorderFactory.createDashedBorder(border.getColor(), border.getWidth(), border.getDashLength(), border.getDashSpacing(), false));
            }
            else
            {
                this.setBorder(BorderFactory.createStrokeBorder(new BasicStroke(border.getWidth()), border.getColor()));
            }
        }
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        if (parentElement != null && ignoresBorders())
        {
            x = x + parentElement.getX();
            y = y + parentElement.getY();
        }
        
        super.setBounds(x, y, width, height);
    }
    
    public void finalSetup(UIElement parent)
    {
        parentElement = parent;
        
        setBounds(
                ((NumberValueRef)this.config.get(X_KEY)).intValue(),
                ((NumberValueRef)this.config.get(Y_KEY)).intValue(),
                ((NumberValueRef)this.config.get(WIDTH_KEY)).intValue(),
                ((NumberValueRef)this.config.get(HEIGHT_KEY)).intValue());
        
        if (parent != null)
        {
            parent.addComponentListener(new ComponentListener()
            {
                @Override
                public void componentResized(ComponentEvent e) {
                    // do nothing
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    setBounds(
                        ((NumberValueRef)config.get(X_KEY)).intValue(),
                        ((NumberValueRef)config.get(Y_KEY)).intValue(),
                        ((NumberValueRef)config.get(WIDTH_KEY)).intValue(),
                        ((NumberValueRef)config.get(HEIGHT_KEY)).intValue());
                }

                @Override
                public void componentShown(ComponentEvent e) {
                    setVisible(visible.getValue());
                }

                @Override
                public void componentHidden(ComponentEvent e) {
                    setVisible(visible.getValue());
                }
            });
        }
        
        if (!ignoresBorders())
        {
            addMouseListener(this);
            addMouseMotionListener(this);
        }
        else
        {
            removeMouseListener(this);
            removeMouseMotionListener(this);
        }
        
        setupBorder();
        setVisible(visible.getValue());
    }
    
    @Override
    public void setVisible(boolean value)
    {
        if (isVisible())
        {
            runCommand(config.get(ONVISIBLE_KEY));
        }
        
        if (parentElement != null)
        {
            super.setVisible(value && parentElement.isVisible());
        }
        else
        {
            super.setVisible(value);
        }
        
    }
    
    protected synchronized void runCommand(Object json)
    {
        Main.CI.runSyncUICommand(localContext, config, json);
    }
    
    @Override
    public void componentRemoved(ContainerEvent e)
    {
        if (e.getChild() instanceof UIElement)
        {
            ((UIElement)e.getChild()).destroy();
        }
    }
    
    @Override
    public void componentAdded(ContainerEvent e)
    {
        // do nothing
    }
    
    private void destroy()
    {
        Toolkit.destroy(config);
    }
}
