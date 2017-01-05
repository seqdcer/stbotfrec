/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import engines.Base;
import data.Settings;
import data.Toolkit;
import engines.CommandInterface;
import init.Main;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class Window extends javax.swing.JFrame {

    public static int MAX_UITICK_VALUE = 100000;
    public static int UI_TICK = 0;
    
    private final Timer painter;
    private UIContainer current;
    private Console console;
    
    private final HashMap<String, Container> dialogs = new HashMap<>();
    
    /**
     * Creates new form Window
     */
    public Window() {
        initComponents();
        
        console = new Console();
        
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                Main.CI.runCommand(null, null, CommandInterface.HARD_EXIT);
            }
        });
        
        painter = new Timer();
        painter.scheduleAtFixedRate(new TimerTask()
        {
            private long lastBlinkUpdate;
            private int lastBlinkValue = 0;
            
            @Override
            public void run() {
                if (current != null) contentPane.repaint();
                
                long time = System.currentTimeMillis();
                
                if (lastBlinkUpdate == 0)
                {
                    lastBlinkUpdate = time;
                }
                
                int value = (int)(time - lastBlinkUpdate) / 10;
                 
                if (value > 0)
                {
                    try{
                        lastBlinkValue = (lastBlinkValue + value) % MAX_UITICK_VALUE;
                        lastBlinkUpdate += value * 10;
                        UI_TICK = lastBlinkValue;
                        Base.setVariable(null, null, lastBlinkValue, Base.RUNTIME_ROOT, Base.RUNTIME_VAR_UI_TICK);
                    } catch (Exception ex) {
                        Logger.getLogger(Window.class.getName()).log(Level.SEVERE, "Fatal error in main loop.", ex);
                        painter.cancel();
                    }
                }
            }
        }, 0, 16);
        
        this.contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DEAD_CEDILLA, 0), "~~~console~~~");

        this.contentPane.getActionMap().put("~~~console~~~", new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (console.isVisible())
                {
                    console.setVisible(false);
                    contentPane.remove(console);
                }
                else
                {
                    Rectangle b = contentPane.getBounds();
                    
                    b.y = b.height - b.height / 3;
                    b.x = 0;
                    b.height = b.height / 3;
                    console.setBounds(b);
                    
                    contentPane.add(console, 9999, 0);
                    console.setVisible(true);
                    console.requestFocusInWindow();
                }
            }
        });
    }

    public void open(String filename)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            try {
                java.awt.EventQueue.invokeAndWait(() -> {
                    open(filename);
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return;
        }
        
        JSONObject config = Base.getWindowConfig(filename);
        
        if (config != null)
        {
            JSONObject screenRes = (JSONObject)Base.getVariable(null, null, Base.SETTINGS_ROOT, Settings.SCREEN_RESOLUTION);
            Object wval = screenRes.get(Settings.WIDTH);
            Object hval = screenRes.get(Settings.HEIGHT);
            
            int width, height;
            
            try
            {
                width = (wval instanceof Number) ? ((Number)wval).intValue() : Integer.parseInt(wval.toString());
                height = (hval instanceof Number) ? ((Number)hval).intValue() : Integer.parseInt(hval.toString());
            }
            catch (Exception e)
            {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, "Could not read resolution settings from the settings file.", e);
                return;
            }
            
            Dimension targetRes = new Dimension(width, height);
            
            UIContainer screen = (UIContainer) UIElement.create(config, config, -1);

            contentPane.add(screen, 1, 0);
            screen.finalSetup(null);
            
            if (current != null)
            {
                for (int i = 0; i < contentPane.getComponentCount(); i++)
                {
                    Component c = contentPane.getComponent(i);
                    
                    if (c != screen)
                    {
                        contentPane.remove(i);
                        i--;
                    }
                }
            }
            
            for (String key : dialogs.keySet())
            {
                Toolkit.destroy(((UIContainer)dialogs.get(key).getComponent(0)).localContext);
            }
            
            dialogs.clear(); 
            
            UIContainer oldscreen = current;
            current = screen;
        
            if (oldscreen != null)
            {
                Toolkit.destroy(oldscreen.localContext);
            }
            
            // resize?
            Dimension size = getContentPane().getSize();
            Dimension fsize = getSize();

            this.setSize(fsize.width + targetRes.width - size.width, fsize.height + targetRes.height - size.height);
        }
        
        this.repaint();
    }
    
    public void openDialog(String filename, boolean modal, int... position) throws Exception
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            try {
                java.awt.EventQueue.invokeAndWait(() -> {
                    try {
                        openDialog(filename, modal, position);
                    } catch (Exception ex) {
                        Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return;
        }
        
        UIContainer screen = null;
       
        if (dialogs.containsKey(filename))
        {
            Component c = dialogs.get(filename);
            
            current.remove(c);

            Container dialog = dialogs.remove(filename);
            Toolkit.destroy(((UIContainer)dialog.getComponent(0)).localContext);
        }
        
        JSONObject config = Base.getWindowConfig(filename);

        if (config != null)
        {
            screen = (UIContainer) UIElement.create(config, config, -1);

            dialogs.put(filename, screen);
            contentPane.add(screen, contentPane.highestLayer() + 1, 0);
            screen.finalSetup(null);
        }
        
        if (screen != null)
        {
            if (position.length < 2)
            {
                Base.setVariable(screen.config, screen.config, (current.getWidth() - screen.getWidth()) / 2, Base.LOCAL_ROOT, UIContainer.X_KEY);
                Base.setVariable(screen.config, screen.config, (current.getHeight() - screen.getHeight()) / 2, Base.LOCAL_ROOT, UIContainer.Y_KEY);
            }
            else
            {
                Base.setVariable(screen.config, screen.config, position[0], Base.LOCAL_ROOT, UIContainer.X_KEY);
                Base.setVariable(screen.config, screen.config, position[1], Base.LOCAL_ROOT, UIContainer.Y_KEY);
            }
        }
        
        if (modal)
        {
            // close all other dialogs
            if (current != null)
            {
                for (int i = 0; i < contentPane.getComponentCount(); i++)
                {
                    Component c = contentPane.getComponent(i);
                    
                    if (c != current)
                    {
                        contentPane.remove(i);
                        i--;
                    }
                }
            }
            
            dialogs.clear();
            
            // embed dialog into a glass pane
            GlassPane glassPane = new GlassPane();
            glassPane.setLayout(null);
            glassPane.setBounds(contentPane.getBounds());
            glassPane.add(screen);
            
            // add
            dialogs.put(filename, glassPane);
            contentPane.add(glassPane, contentPane.highestLayer() + 1, 0);
        }
        
        this.repaint();
    }
    
    public void closeDialog(String filename)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            try {
                java.awt.EventQueue.invokeAndWait(() -> {
                    closeDialog(filename);
                });
            } catch (InterruptedException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return;
        }
        
        Container c = dialogs.get(filename);
        
        if (c != null)
        {
            contentPane.remove(c);
            Container dialog = dialogs.remove(filename);
            Toolkit.destroy(((UIContainer)dialog.getComponent(0)).localContext);
        }
        
        repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        contentPane = new javax.swing.JLayeredPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("4X");
        setBackground(new java.awt.Color(0, 0, 0));
        setName("4X"); // NOI18N
        setResizable(false);
        setSize(new java.awt.Dimension(800, 600));

        contentPane.setBackground(new java.awt.Color(0, 0, 0));
        contentPane.setOpaque(true);
        getContentPane().add(contentPane, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLayeredPane contentPane;
    // End of variables declaration//GEN-END:variables
}
