/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author AP
 */
public class AniFile {
    
    private static final String DELAYTIME_PROPERTY = "delayTime";

    
    private ArrayList<BufferedImage> frames = new ArrayList<>();
    private int delayTime = 10;
    
    public AniFile(File file)
    {
        ZipFile zf = null;
        HashMap<String, BufferedImage> frameMap = new HashMap<>();
        
        try
        {
            zf = new ZipFile(file);
            
            // load config
            ZipEntry ze = zf.getEntry("config.json");
            
            if (ze != null)
            {
                JSONParser parser = new JSONParser();
        
                JSONObject object;
                
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)))) {
                    object = (JSONObject)parser.parse(reader);
                }
                
                if (object != null)
                {
                    try
                    {
                        delayTime = ((Number) object.get(DELAYTIME_PROPERTY)).intValue();
                    } catch (Exception e) {}
                }
            }

            Enumeration<? extends ZipEntry> entries = zf.entries();
            String name, lname;
            
            while (entries.hasMoreElements())
            {
                ze = entries.nextElement();
                name = ze.getName();
                lname = name.toLowerCase();
                
                if (lname.endsWith(".png") || lname.endsWith(".svg"))
                {
                    InputStream in = null;
                    BufferedImage img;
                    
                    try
                    {
                        in = zf.getInputStream(ze);
                        img = ImageIO.read(in);
                        
                        frameMap.put(lname, img);
                        
                        in.close();
                    }
                    catch (Exception e)
                    {
                        Logger.getLogger(AniFile.class.getName()).log(Level.SEVERE, "Unable to load animation frame: " + file.getPath() + File.separator + name, e);
                    }
                    finally
                    {
                        if (in != null) in.close();
                    }
                }
            }

            zf.close();
        }
        catch (Exception e)
        {
            Logger.getLogger(AniFile.class.getName()).log(Level.SEVERE, "Unable to load font file: " + file.getPath(), e);
        }
        finally
        {
            if (zf != null)
            {
                try
                {
                    zf.close();
                }
                catch (Exception ex)
                {
                    Logger.getLogger(AniFile.class.getName()).log(Level.WARNING, "Unable to close font file: " + file.getPath(), ex);
                }
            }
        }
        
        ArrayList<String> keys = new ArrayList<>(frameMap.keySet());
        Collections.sort(keys);
        
        keys.stream().forEach((key) -> {
            frames.add(frameMap.get(key));
        });
    }
    
    public ArrayList<BufferedImage> getFrames()
    {
        return frames;
    }
    
    public int getDelayTime()
    {
        return delayTime;
    }
}
