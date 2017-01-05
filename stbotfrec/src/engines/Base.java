/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engines;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import data.AniFile;
import data.EnumValueRef;
import data.Font;
import data.NumberValueRef;
import data.Settings;
import data.Toolkit;
import data.ValueRef;
import gui.Animation;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author AP
 */
public abstract class Base {
    public static final String DATA_FOLDER = "data";
    public static final String SCRIPTS_FOLDER = "scripts";
    
    public static final String NULL_ROOT = "null";
    public static final String USER_DATA_ROOT = "user";
    public static final String THIS_ROOT = "this";
    public static final String LOCAL_ROOT = "local";
    public static final String RUNTIME_ROOT = "runtime";
    public static final String SETTINGS_ROOT = "settings";
    public static final String STATIC_ROOT = "static";      // read-only
    
    public static final String PROPERTY_LIST_SIZE = "count";      // read-only
    
    public static final Character NODE_SEPARATOR = '/';
    public static final Character REF_END = ']';
    public static final Character REF_START = '[';
    public static final Character RECURSIVE_REF_START = '!';
    
    public static final String ID_KEY = "id";
    
    public static final String RUNTIME_VAR_UI_TICK = "UITick";
    
    private static final JSONObject userData = new JSONObject();
    private static final JSONObject runtimeData = new JSONObject();
    private static final JSONObject staticData = new JSONObject();
    private static final Settings settings = new Settings();
    
    private static final HashMap<String, Font> fonts = new HashMap<>();
    private static final WeakHashMap<String, JSONObject> windows = new WeakHashMap<>();
    private static final WeakHashMap<String, String> scripts = new WeakHashMap<>();
    private static final WeakHashMap<String, Animation> images = new WeakHashMap<>();
    
    private static final Map<String, Set<PropertyChangeListener>> listeners = Collections.synchronizedMap(new HashMap<>());
    
    public static Font getFont(String fontfile)
    {
        Font font = fonts.get(fontfile);
        
        if (font == null)
        {
            font = new Font(
                new File(DATA_FOLDER + File.separator +
                        settings.settings.get(Settings.FONT_SUBFOLDER), fontfile));

            fonts.put(fontfile, font);
        }
        
        return font;
    }
    
    public static void removeChangeListener(JSONObject localContext, JSONObject thisContext, PropertyChangeListener listener, String prevRef)
    {
        Set<PropertyChangeListener> set;
        
        if (prevRef != null)
        {
            prevRef = prevRef.replaceAll("(\\" + REF_START + "|\\" + REF_END + ")", "");
        
            if (prevRef.startsWith(LOCAL_ROOT))
            {
                prevRef = localContext.get(ID_KEY).toString() + NODE_SEPARATOR + prevRef.substring(LOCAL_ROOT.length());
            }
            else if (prevRef.startsWith(THIS_ROOT))
            {
                prevRef = thisContext.get(ID_KEY).toString() + NODE_SEPARATOR + prevRef.substring(THIS_ROOT.length());
            }
            
            prevRef = cleanRef(prevRef);
            
            set = listeners.get(prevRef);
            
            if (set != null)
            {
                set.remove(listener);
            }
        }
    }
    
    public static void addChangeListener(JSONObject localContext, JSONObject thisContext, PropertyChangeListener listener, String newRef)
    {
        Set<PropertyChangeListener> set;
        
        newRef = newRef.replaceAll("(\\" + REF_START + "|\\" + REF_END + ")", "");
        
        if (newRef.startsWith(LOCAL_ROOT))
        {
            newRef = localContext.get(ID_KEY).toString() + NODE_SEPARATOR + newRef.substring(LOCAL_ROOT.length());
        }
        else if (newRef.startsWith(THIS_ROOT))
        {
            newRef = thisContext.get(ID_KEY).toString() + NODE_SEPARATOR + newRef.substring(THIS_ROOT.length());
        }
        
        newRef = cleanRef(newRef);
        
        set = listeners.get(newRef);
        
        if (set == null)
        {
            set = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<PropertyChangeListener, Boolean>()));
            listeners.put(newRef, set);
        }
        
        set.add(listener);
    }
    
    public static Object getVariable(JSONObject localContext, JSONObject thisContext,String ref)
    {
        return getVariable(localContext, thisContext, refToNodes(ref));
    }
    
    private static String[] refToNodes(String ref)
    {
        ref = cleanRef(ref);
        
        if (ref.length() < 1)
        {
            return null;
        }
        
        return ref.split(NODE_SEPARATOR.toString());
    }
    
    public static Object getVariable(JSONObject localContext, JSONObject thisContext, String... nodes)
    {
        if (nodes == null) return null;
        
        JSONObject root = getRoot(localContext, thisContext, nodes);
        JSONAware parent = null;
        Object object;
        
        if (root != null)
        {
            parent = root;
            
            for (int i = 1; i < nodes.length - 1; i++)
            {
                object = getChild(parent, nodes[i]);
                
                if (object == null || !(object instanceof JSONObject || object instanceof List || object instanceof ValueRef))
                {
                    // if root is static data
                    // load it (if available) on demand
                    if (nodes[0].equals(STATIC_ROOT))
                    {
                        File file = new File(DATA_FOLDER, settings.settings.get(Settings.STATIC_CONF_SUBFOLDER).toString());

                        for (int k = 1; k <= i; k++)
                        {
                            file = new File(file, nodes[k]);
                            
                            if (!file.isDirectory() && k < i)
                            {
                                return null;
                            }
                        }
                        
                        if (file.exists() && file.isDirectory())
                        {
                            object = new JSONObject();
                        }
                        else
                        {
                            object = getConfig(file.getPath());
                        }
                            
                        setChild(parent, nodes[i], object);
                        parent = (JSONAware) object;

                        if (parent == null) break; else continue;
                    }
                    
                    parent = null;
                    break;
                }
                else
                {
                    parent = (JSONAware) object;
                }
            }
        }
        
        if (parent == null)
        {
            return null;
        }
        else
        {
            return getChild(parent, nodes[nodes.length - 1]);
        }
    }
    
    private static Object getChild(JSONAware parent, Object key)
    {
        Object object = null;
        
        if (parent instanceof JSONObject)
        {
            object = ((JSONObject)parent).get(key);
        }
        else if (parent instanceof List)
        {
            if (PROPERTY_LIST_SIZE.equalsIgnoreCase(key.toString()))
            {
                return ((List)parent).size();
            }
            
            int index = 0;

            try
            {
                index = Integer.parseInt(key.toString());
            }
            catch (Exception e) {}

            List list = (List)parent;
            
            if (index < 0 || index >= list.size()) return null;
            
            object = list.get(index);
        }
        else if (parent instanceof ValueRef)
        {
            object = ((ValueRef)parent).getObjectProperty(key.toString());
        }
        
        return object;
    }
    
    public static void setRefVariable(JSONObject localContext, JSONObject thisContext, String ref, Object value) throws Exception
    {
        if (value == null) return;
        
        value = Character.toString(RECURSIVE_REF_START) + Character.toString(REF_START) + value.toString() + Character.toString(REF_END);
        
        setVariable(localContext, thisContext, ref, value);
    }
    
    public static void setVariable(JSONObject localContext, JSONObject thisContext, String ref, Object value) throws Exception
    {
        setVariable(localContext, thisContext, value, refToNodes(ref));
    }
    
    public static void setVariable(JSONObject localContext, JSONObject thisContext, Object value, String... nodes) throws Exception
    {
        if (nodes == null) throw new Exception("Missing or faulty variable path.");
        
        JSONObject root = getRoot(localContext, thisContext, nodes);
        JSONAware parent = null;
        Object object;
        
        if (root != null)
        {
            parent = root;
            
            for (int i = 1; i < nodes.length - 1; i++)
            {
                object = getChild(parent, nodes[i]);
                
                if (object != null && object instanceof JSONAware)
                {
                    parent = (JSONAware) object;
                }
                else
                {
                    JSONObject newObject = new JSONObject();
                    
                    setChild(parent, nodes[i], newObject);
                    
                    parent = newObject;
                }
            }
        }
        
        Object child = getChild(parent, nodes[nodes.length - 1]);
        
        if (child != null && child instanceof ValueRef)
        {
            ((ValueRef)child).set(value);
        }
        else
        {
            setChild(parent, nodes[nodes.length - 1], value);
        }
        
        if (child instanceof ValueRef)
        {
            ValueRef objRef = (ValueRef)child;
            
            String[] properties = objRef.getProperties();
            
            if (properties != null)
            {
                String[] propNodes = new String[nodes.length + 1];
                System.arraycopy(nodes, 0, propNodes, 0, nodes.length);
                
                for (String p : properties)
                {
                    propNodes[nodes.length] = p;
                    fireVariableChangeEvent(localContext, thisContext, child, objRef.getObjectProperty(p), propNodes);
                }
            }
            else
            {
                fireVariableChangeEvent(localContext, thisContext, child, value, nodes);
            }
        }
        else
        {
            fireVariableChangeEvent(localContext, thisContext, child, value, nodes);
        }
    }
    
    public static void mulVariable(JSONObject localContext, JSONObject thisContext, String ref, double modValue, double minValue, double maxValue) throws Exception
    {
        mulVariable(localContext, thisContext, modValue, minValue, maxValue, refToNodes(ref));
    }
    
    public static void mulVariable(JSONObject localContext, JSONObject thisContext, double modValue, double minValue, double maxValue, String... nodes) throws Exception
    {
        if (nodes == null) return;
        
        JSONObject root = getRoot(localContext, thisContext, nodes);
        JSONAware parent = null;
        Object object;
        
        if (root != null)
        {
            parent = root;
            
            for (int i = 1; i < nodes.length - 1; i++)
            {
                object = getChild(parent, nodes[i]);
                
                if (object == null || !(object instanceof JSONObject || object instanceof List))
                {
                    JSONObject newObject = new JSONObject();
                    
                    setChild(parent, nodes[i], newObject);
                    
                    parent = newObject;
                }
                else
                {
                    parent = (JSONAware) object;
                }
            }
        }
        
        Object child = getChild(parent, nodes[nodes.length - 1]);
        double endValue;
        
        if (child != null)
        {
            if (child instanceof NumberValueRef)
            {
                NumberValueRef number = (NumberValueRef)child;
            
                endValue = Math.max(minValue, Math.min(maxValue, number.doubleValue() * modValue));
                number.set(endValue);
            }
            else if (child instanceof Number)
            {
                endValue = Math.max(minValue, Math.min(maxValue, ((Number)child).doubleValue() * modValue));
                setChild(parent, nodes[nodes.length - 1], endValue);
            }
            else
            {
                double value = 0;
                
                try
                {
                    value = Double.parseDouble(child.toString());
                }
                catch (Exception e) {}
                
                endValue = Math.max(minValue, Math.min(maxValue, value * modValue));
                setChild(parent, nodes[nodes.length - 1], endValue);
            }
        }
        else
        {
            endValue = Math.max(minValue, Math.min(maxValue, 0));
            setChild(parent, nodes[nodes.length - 1], endValue);
        }
        
        // fire events
        if (child instanceof ValueRef)
        {
            ValueRef objRef = (ValueRef)child;
            
            String[] properties = objRef.getProperties();
            
            if (properties != null)
            {
                String[] propNodes = new String[nodes.length + 1];
                System.arraycopy(nodes, 0, propNodes, 0, nodes.length);
                
                for (String p : properties)
                {
                    propNodes[nodes.length] = p;
                    fireVariableChangeEvent(localContext, thisContext, child, objRef.getObjectProperty(p), propNodes);
                }
            }
        }

        fireVariableChangeEvent(localContext, thisContext, child, endValue, nodes);
    }
    
    public static void modVariable(JSONObject localContext, JSONObject thisContext, String ref, double modValue, double minValue, double maxValue) throws Exception
    {
        modVariable(localContext, thisContext, modValue, minValue, maxValue, refToNodes(ref));
    }
    
    public static void modVariable(JSONObject localContext, JSONObject thisContext, double modValue, double minValue, double maxValue, String... nodes) throws Exception
    {
        if (nodes == null) return;
        
        JSONObject root = getRoot(localContext, thisContext, nodes);
        JSONAware parent = null;
        Object object;
        
        if (root != null)
        {
            parent = root;
            
            for (int i = 1; i < nodes.length - 1; i++)
            {
                object = getChild(parent, nodes[i]);
                
                if (object == null || !(object instanceof JSONObject || object instanceof List))
                {
                    JSONObject newObject = new JSONObject();
                    
                    setChild(parent, nodes[i], newObject);
                    
                    parent = newObject;
                }
                else
                {
                    parent = (JSONAware) object;
                }
            }
        }
        
        Object child = getChild(parent, nodes[nodes.length - 1]);
        double endValue;
        
        if (child != null)
        {
            if (child instanceof NumberValueRef)
            {
                NumberValueRef number = (NumberValueRef)child;
            
                endValue = Math.max(minValue, Math.min(maxValue, number.doubleValue() + modValue));
                number.set(endValue);
            }
            else if (child instanceof Number)
            {
                endValue = Math.max(minValue, Math.min(maxValue, ((Number)child).doubleValue() + modValue));
                setChild(parent, nodes[nodes.length - 1], endValue);
            }
            else
            {
                double value = 0;
                
                try
                {
                    value = Double.parseDouble(child.toString());
                }
                catch (Exception e) {}
                
                endValue = Math.max(minValue, Math.min(maxValue, value + modValue));
                setChild(parent, nodes[nodes.length - 1], endValue);
            }
        }
        else
        {
            endValue = Math.max(minValue, Math.min(maxValue, modValue));
            setChild(parent, nodes[nodes.length - 1], endValue);
        }
        
        // fire events
        if (child instanceof ValueRef)
        {
            ValueRef objRef = (ValueRef)child;
            
            String[] properties = objRef.getProperties();
            
            if (properties != null)
            {
                String[] propNodes = new String[nodes.length + 1];
                System.arraycopy(nodes, 0, propNodes, 0, nodes.length);
                
                for (String p : properties)
                {
                    propNodes[nodes.length] = p;
                    fireVariableChangeEvent(localContext, thisContext, child, objRef.getObjectProperty(p), propNodes);
                }
            }
        }

        fireVariableChangeEvent(localContext, thisContext, child, endValue, nodes);
    }
    
    public static void createList(JSONObject localContext, JSONObject thisContext, String ref) throws Exception
    {
        ref = cleanRef(ref);
        
        if (ref.length() < 1)
        {
            return;
        }
        
        String[] nodes = ref.split(NODE_SEPARATOR.toString());
        
        setVariable(localContext, thisContext, new JSONArray(), nodes);
    }
    
    public static void addToList(JSONObject localContext, JSONObject thisContext, String ref, int pos, Object value) throws Exception
    {
        ref = cleanRef(ref);
        
        if (ref.length() < 1)
        {
            return;
        }
        
        ref = cleanRef(ref);
        
        String[] nodes = ref.split(NODE_SEPARATOR.toString());
        
        Object variable = getVariable(localContext, thisContext, nodes);
        
        if (variable instanceof List && variable instanceof JSONAware)
        {
            if (pos < 0)
            {
                setChild((JSONAware)variable, Integer.toString(((List)variable).size()), value);
            }
            else
            {
                List list = (List) variable;
                
                while (list.size() < pos)
                {
                    list.add(new JSONObject());
                }
                
                list.add(pos, value);
            }
            
            fireVariableChangeEvent(localContext, thisContext, null, variable, nodes);
        }
    }
    
    private static void setChild(JSONAware parent, String key, Object value)
    {
        if (parent instanceof JSONObject)
        {
            ((JSONObject)parent).put(key, value);
        }
        else if (parent instanceof ValueRef)
        {
            ((ValueRef)parent).setObjectProperty(key, value);
        }
        else
        {
            int index = 0;

            try
            {
                index = Integer.parseInt(key);
            }
            catch (Exception e) {}

            List list = (List)parent;
            
            if (list == null)
            {
                list = new JSONArray();
            }
            
            while (list.size() <= index)
            {
                list.add(new JSONObject());
            }

            list.set(index, value);
        }
    }
    
    protected static void fireVariableChangeEvent(JSONObject localContext, JSONObject thisContext, Object oldValue, Object newValue, String... nodes)
    {
        StringBuilder ref = new StringBuilder();

        for (int i = 0; i < nodes.length; i++)
        {
            String n = nodes[i];
            
            if (i == 0 && n.startsWith(LOCAL_ROOT))
            {
                n = localContext.get(ID_KEY).toString();
            }
            else if (i == 0 && n.startsWith(THIS_ROOT))
            {
                n = thisContext.get(ID_KEY).toString();
            }
            
            if (ref.length() > 0)
                ref.append(Base.NODE_SEPARATOR);
            
            ref.append(n);
          
            
            if (i < nodes.length - 1)
            {
                fireVariableChangeEvent(ref.toString(), null, (Object) null);
            }
            else
            {
                fireVariableChangeEvent(ref.toString(), oldValue, newValue);
            }
        }
        
        
    }
    
    public static void fireVariableChangeEvent(String ref, Object oldValue, Object newValue)
    {
        ref = cleanRef(ref);
        
        Set<PropertyChangeListener> set = listeners.get(ref);
        
        if (set != null)
        {
            PropertyChangeEvent evt = new PropertyChangeEvent(Base.class, ref, oldValue, newValue);
            
            PropertyChangeListener[] listArray = new PropertyChangeListener[set.size()];
            listArray = set.toArray(listArray);
            
            for (PropertyChangeListener l : listArray)
            {
                if (l != null)
                    l.propertyChange(evt);
            }
        }
    }
    
    public static JSONObject getWindowConfig(String configname)
    {
        if (configname == null) return null;
        
        JSONObject object = windows.get(configname);

        if (object == null)
        {
            File jsonfile = new File(DATA_FOLDER + File.separator +
                    settings.settings.get(Settings.GUI_CONF_SUBFOLDER) + File.separator +
                    Base.getVariable(null, null, SETTINGS_ROOT, Settings.SCREEN_RESOLUTION, Settings.WIDTH) + "x" +
                    Base.getVariable(null, null, SETTINGS_ROOT, Settings.SCREEN_RESOLUTION, Settings.HEIGHT), configname);
            
            object = getConfig(jsonfile.getPath());
            
            if (object != null)
            {
                windows.put(configname, object);
            }
        }
        
        return (object == null) ? null : (JSONObject)Toolkit.deepCopyJSON(object);
    }
    
    public static JSONObject getConfig(String configname)
    {
        JSONParser parser = new JSONParser();
        JSONObject object = null;
        
        File configFile = new File(configname + ".json");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            object = (JSONObject)parser.parse(reader);

            if (!object.containsKey(ID_KEY))
            {
                object.put(ID_KEY, cleanRef(configFile.getPath()));
            }
        } catch (ParseException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, "Unable to parse config file: " + configFile.getPath(), ex);
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, "Unable to load config file: " + configFile.getPath(), ex);
        }
        
        return (JSONObject)Toolkit.deepCopyJSON(object);
    }
    
    public static String getScript(String scriptname)
    {
        if (scriptname == null) return null;
        
        String script = scripts.get(scriptname );

        if (script == null)
        {
            try {
                File scriptfile = new File(DATA_FOLDER + File.separator + SCRIPTS_FOLDER, scriptname + ".js");
                
                byte[] encoded = Files.readAllBytes(Paths.get(scriptfile.getPath()));
                
                script = new String(encoded, "UTF8");
                
                if (script.length() > 0)
                {
                    scripts.put(scriptname, script);
                }
                else
                {
                    script = null;
                }
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return script;
    }
    
    public static Animation getImage(JSONObject localContext, JSONObject thisContext, String path)
    {
        if (path == null) return null;
        
        Animation object = images.get(path);

        if (object == null)
        {
            String suffix = path.substring(path.lastIndexOf('.') + 1);
            
            File image = new File(DATA_FOLDER + File.separator
                    + settings.settings.get(Settings.IMAGES_SUBFOLDER), path);

            try {
                int delayTime = 10;
                ArrayList<BufferedImage> imgArray = new ArrayList<>();
                
                if (!image.exists())
                {
                    throw new FileNotFoundException("File not found: " + image.getPath());
                }
                else
                {
                    if ("zip".equalsIgnoreCase(suffix))
                    {
                        AniFile ani = new AniFile(image);
                        
                        delayTime = ani.getDelayTime();
                        imgArray = ani.getFrames();
                    }
                    else
                    {
                        ImageReader reader = ImageIO.getImageReadersByFormatName(suffix).next();

                        reader.setInput(ImageIO.createImageInputStream(image));

                        int numImages = reader.getNumImages(true);

                        IIOMetadata imageMetaData = reader.getImageMetadata(0);
                        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

                        try
                        {
                            IIOMetadataNode root = (IIOMetadataNode)reader.getImageMetadata(0).getAsTree(metaFormatName);

                            int nNodes = root.getLength();

                            for (int i = 0; i < nNodes; i++)
                            {
                                if (root.item(i).getNodeName().compareToIgnoreCase("GraphicControlExtension") == 0)
                                {
                                     delayTime = Integer.parseInt(((IIOMetadataNode)root.item(i)).getAttribute("delayTime"));
                                     break;
                                }
                           }
                        }
                        catch (Exception ex)
                        {
                        }

                        for (int i = 0; i < numImages; i++) {

                            imgArray.add(reader.read(i));
                        }
                    }
                }
                
                object = new Animation(delayTime, imgArray.toArray(new BufferedImage[imgArray.size()]));
                
                // resize?
                /*
                JSONObject screenRes = (JSONObject)Base.getVariable(null, Base.SETTINGS_ROOT, Settings.SCREEN_RESOLUTION);
                JSONObject baseRes = (JSONObject)Base.getVariable(null, Base.SETTINGS_ROOT, Settings.BASE_RESOLUTION);

                Dimension orgRes = new Dimension(((Number)baseRes.get(Settings.WIDTH)).intValue(), ((Number)baseRes.get(Settings.HEIGHT)).intValue());
                Dimension targetRes = new Dimension(((Number)screenRes.get(Settings.WIDTH)).intValue(), ((Number)screenRes.get(Settings.HEIGHT)).intValue());

                if (!orgRes.equals(targetRes))
                {
                    object = resize(object, (int)(object.getWidth() * (float) targetRes.width / orgRes.width), (int)(object.getHeight() * (float) targetRes.height / orgRes.height));
                }*/
                    
                images.put(path, object);
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, "Unable to load image/animation: " + path, ex);
            }
        }
        
        return object;
    }
    
    public static BufferedImage resize(BufferedImage image, int width, int height)
    {
        if (image.getWidth() < width || image.getHeight() < height)
        {
            ResampleOp resampleOp = new ResampleOp(width, height);
            resampleOp.setNumberOfThreads(1);
            resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

            BufferedImage scaled = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height, Transparency.TRANSLUCENT);

            Graphics g = scaled.getGraphics();
            g.drawImage(resampleOp.filter(image, null), 0, 0, null);
            g.dispose();
            return scaled;
        }
        else
        {
            BufferedImage scaled = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration()
                .createCompatibleImage(width, height, Transparency.TRANSLUCENT);

            Graphics2D g = (Graphics2D) scaled.getGraphics();
            g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );
            g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );
            g.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
            );
            
            g.drawImage(image, 0, 0, width, height, null);
            g.dispose();
            return scaled;
        }
    }
    
    public static void saveSettings()
    {
        settings.saveSettings();
    }
    
    public static void selectOption(JSONObject localContext, JSONObject thisContext, String ref, int mod, boolean rotate)
    {
        selectOption(localContext, thisContext, mod, rotate, refToNodes(ref));
    }
    
    public static void selectOption(JSONObject localContext, JSONObject thisContext, int mod, boolean rotate, String... nodes)
    {
        if (nodes == null) return;
        
        Object enumVariable = Base.getVariable(localContext, thisContext, nodes);
        
        if (enumVariable instanceof EnumValueRef)
        {
            EnumValueRef var = (EnumValueRef)enumVariable;
            var.selectOption(mod, rotate);
            
            String[] properties = var.getProperties();
            
            if (properties != null)
            {
                String[] propNodes = new String[nodes.length + 1];
                System.arraycopy(nodes, 0, propNodes, 0, nodes.length);
                
                for (String p : properties)
                {
                    propNodes[nodes.length] = p;
                    fireVariableChangeEvent(localContext, thisContext, var, var.getObjectProperty(p), propNodes);
                }
            }
            
            fireVariableChangeEvent(localContext, thisContext, enumVariable, var.getValue(), nodes);
        }
    }

    public static Clip getSoundClip(String soundFile) {
        if (soundFile == null) return null;
        
        try {
            File file = new File(DATA_FOLDER + File.separator + settings.settings.get(Settings.AUDIO_SUBFOLDER), soundFile + ".wav");

            AudioInputStream stream = AudioSystem.getAudioInputStream(file);

            Clip clip = AudioSystem.getClip();

            // Open audio clip and load samples from the audio input stream.
            clip.open(stream);

            clip.addLineListener(new LineListener()
            {
                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP)
                    {
                        ((Clip)event.getSource()).close();
                    }
                }
            });

            return clip;
        } catch (Exception ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    public static void saveUserData(String name) {
        
        FileWriter writer = null;
        
        try {
            writer = new FileWriter(new File(name));
            userData.writeJSONString(writer);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
            } catch (IOException ex) {
                Logger.getLogger(Base.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private static JSONObject getRoot(JSONObject localContext, JSONObject thisContext, String[] nodes)
    {
        if (nodes == null) return null;
        
        switch (nodes[0])
        {
            case RUNTIME_ROOT:
            {
                return runtimeData;
            }
            case LOCAL_ROOT:
            {
                return localContext;
            }
            case THIS_ROOT:
            {
                return thisContext;
            }
            case SETTINGS_ROOT:
            {
                return settings.settings;
            }
            case STATIC_ROOT:
            {
                return staticData;
            }
            case USER_DATA_ROOT:
            {
                return userData;
            }
            case NULL_ROOT:
            {
                return null;
            }
        }
        
        Logger.getLogger(Base.class.getName()).log(Level.SEVERE, "No such data root: {0}", nodes[0]);
        return null;
    }
    
    private static String cleanRef(String ref)
    {
        ref = ref.replaceAll("(\\" + REF_START + "|\\" + REF_END + ")", "");
        ref = ref.replaceAll("\\\\", NODE_SEPARATOR.toString());
        ref = ref.replaceAll("//", NODE_SEPARATOR.toString());
        
        return ref;
    }
}
