/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
public class Font {
    private static final String FORCE_ALL_CAPS_KEY = "forceAllCaps";
    private static final String CHAR_PADDING_KEY = "charPadding";
    private static final String LINE_PADDING_KEY = "linePadding";
    private static final String HEIGHT_KEY = "height";
    private static final String SPACE_CHAR_WIDTH_KEY = "spaceCharWidth";
    private static final String DEFAULT_COLOR_KEY = "defaultColor";
    
    private HashMap<Character, BufferedImage> chars = new HashMap<>();
    
    private boolean forceAllCaps = false;
    private byte charPadding = 1;
    private byte linePadding = 1;
    private byte height = 9;
    private Color defaultColor = Color.WHITE;
    private BufferedImage spaceChar = null;
    
    public Font(File file)
    {
        ZipFile zf = null;
        
        try
        {
            zf = new ZipFile(file);
            
            // load config
            ZipEntry ze = zf.getEntry("config.json");
            
            if (ze != null)
            {
                JSONParser parser = new JSONParser();
        
                BufferedReader reader = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
                
                JSONObject object = (JSONObject)parser.parse(reader);
                
                reader.close();
                
                if (object != null)
                {
                    try
                    {
                        forceAllCaps = (boolean) object.get(FORCE_ALL_CAPS_KEY);
                    } catch (Exception e) {}
                    try
                    {
                        charPadding = ((Number) object.get(CHAR_PADDING_KEY)).byteValue();
                    } catch (Exception e) {}
                    try
                    {
                        linePadding = ((Number) object.get(LINE_PADDING_KEY)).byteValue();
                    } catch (Exception e) {}
                    try
                    {
                        height = ((Number) object.get(HEIGHT_KEY)).byteValue();
                    } catch (Exception e) {}
                    try
                    {
                        defaultColor = ((ColorValueRef)ColorValueRef.create(null, null, object.get(DEFAULT_COLOR_KEY), false)).getValue();
                    } catch (Exception e) {}
                    try
                    {
                        int spaceWidth = ((Number) object.get(SPACE_CHAR_WIDTH_KEY)).byteValue();
                        
                        if (spaceWidth > 0)
                        {
                            spaceChar = new BufferedImage(spaceWidth, Math.max(height, 1), BufferedImage.TYPE_INT_ARGB);
                            
                            Graphics2D g2 = spaceChar.createGraphics();
        
                            //clear
                            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
                            g2.fillRect(0, 0, spaceChar.getWidth(), spaceChar.getHeight());
                            g2.dispose();
                            
                            chars.put(' ', spaceChar);
                        }
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
                    char ch;
                    int chInt;
                    
                    try
                    {
                        in = zf.getInputStream(ze);
                        img = ImageIO.read(in);
                        chInt = Integer.parseInt(lname.substring(0, lname.indexOf(".")).trim());
                        ch = (char)chInt;
                        
                        chars.put(ch, img);
                        
                        in.close();
                    }
                    catch (Exception e)
                    {
                        Logger.getLogger(Font.class.getName()).log(Level.SEVERE, "Unable to load font character: " + file.getPath() + File.separator + name, e);
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
            Logger.getLogger(Font.class.getName()).log(Level.SEVERE, "Unable to load font file: " + file.getPath(), e);
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
                    Logger.getLogger(Font.class.getName()).log(Level.WARNING, "Unable to close font file: " + file.getPath(), ex);
                }
            }
        }
    }
    
    public BufferedImage createText(String text, Color color, int maxWidth, int maxHeight, int horizontalAlignment, int style, boolean uppercase)
    {
        HashMap<Character, BufferedImage> cache = new HashMap<>();
        
        ArrayList<ArrayList<BufferedImage>> lines = new ArrayList<>();
        ArrayList<Integer> linewidths = new ArrayList<>();
        
        ArrayList<BufferedImage> line = new ArrayList<>();
        ArrayList<BufferedImage> word = new ArrayList<>();
        int lineWidth = 0;
        int wordWidth = 0;
        int textHeight = height;
        int charWidth;
        char character;
        
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        
        BufferedImage img;
        
        lines.add(line);
        
        for (int charIndex = 0; charIndex < text.length(); charIndex++)
        {
            character = text.charAt(charIndex);
            
            if (forceAllCaps || uppercase) character = Character.toUpperCase(character);
            
            if (character == ' ')
            {
                if (word.size() > 0)
                {
                    if (lineWidth + wordWidth > maxWidth)
                    {
                        if (textHeight > maxHeight) break;
                            
                        line = new ArrayList<>();
                        lines.add(line);
                        textHeight += height + linePadding;
                        linewidths.add(lineWidth);
                        lineWidth = 0;
                    }
                    
                    line.addAll(word);
                    lineWidth += wordWidth;
                    wordWidth = 0;
                    word.clear();
                }
                
                img = getCharImage(character, hsb[0], hsb[1] + ((style == 1) ? 1 : 0), color.getAlpha() / 255f, cache);
                
                if (img != null)
                {
                    charWidth = img.getWidth() + (line.isEmpty() ? 0 : charPadding);
                    
                    if (lineWidth + wordWidth > maxWidth)
                    {
                        if (textHeight > maxHeight) break;
                            
                        line = new ArrayList<>();
                        lines.add(line);
                        textHeight += height + linePadding;
                        linewidths.add(lineWidth);
                        lineWidth = 0;
                    }
                    else
                    {
                        line.add(img);
                        lineWidth += charWidth;
                    }
                }
            }
            else if (character == '\n')
            {
                if (textHeight > maxHeight) break;

                if (word.size() > 0)
                {
                    line.addAll(word);
                    lineWidth += wordWidth;
                    wordWidth = 0;
                    word.clear();
                }
                
                line = new ArrayList<>();
                lines.add(line);
                textHeight += height + linePadding;
                linewidths.add(lineWidth);
                lineWidth = 0;
            }
            else
            {
                img = getCharImage(character, hsb[0], hsb[1] + ((style == 1) ? 1 : 0), color.getAlpha() / 255f, cache);
                
                if (img != null)
                {
                    charWidth = img.getWidth() + ((line.isEmpty() && word.isEmpty()) ? 0 : charPadding);
                    
                    word.add(img);
                    wordWidth += charWidth;
                }
                else
                {
                    System.out.println("hm");
                }
            }
        }
        
        if (!word.isEmpty())
        {
            if (lineWidth + wordWidth > maxWidth)
            {
                line = new ArrayList<>();
                lines.add(line);
                textHeight += height + linePadding;
                linewidths.add(lineWidth);
                lineWidth = 0;
            }

            line.addAll(word);
            lineWidth += wordWidth;
            word.clear();
        }
        
        linewidths.add(lineWidth);
        
        img = new BufferedImage(Math.max(1, maxWidth), Math.max(textHeight, 1), BufferedImage.TYPE_INT_ARGB); 
        Graphics2D g2 = img.createGraphics();
        
        //clear
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        g2.fillRect(0, 0, maxWidth, textHeight);

        //reset composite
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

        int x, y = 0;
        
        for (int i = 0; i < lines.size(); i++)
        {
            switch (horizontalAlignment)
            {
                case 1: // center
                {
                    x = (maxWidth - linewidths.get(i)) / 2;
                    break;
                }
                case 2: // right
                {
                    x = maxWidth - linewidths.get(i);
                    break;
                }
                default:
                {
                    x = 0;
                }
            }

            for (BufferedImage bi: lines.get(i))
            {
                g2.drawImage(bi, x, y, null);

                x += bi.getWidth() + charPadding;
            }

            y += height + linePadding;
        }
        
        g2.dispose();
        
        return img;
    }
    
    private BufferedImage tintChar(BufferedImage charImg, float hue, float saturation, float brightness)
    {
        BufferedImage img = new BufferedImage(charImg.getWidth(), charImg.getHeight(),
            BufferedImage.TRANSLUCENT);
        Graphics2D graphics = img.createGraphics();
        
        graphics.drawImage(charImg, null, 0, 0);
        graphics.dispose();
        
        int pixel;
        float[] hsb = new float[3];
        
        // tint
        for(int y = 0; y < img.getHeight(); y++)
        {
            for(int x = 0; x < img.getWidth(); x++)
            {
                pixel = img.getRGB(x, y);

                if ((pixel & 0xFFFFFF) != 0)
                {
                    Color.RGBtoHSB((pixel >> 16) & 0xFF,
                            (pixel >> 8) & 0xFF, pixel & 0xFF, hsb);

                    pixel = Color.HSBtoRGB(hue, Math.min(1, (saturation + hsb[1]) / 2), Math.min(1, hsb[2] * (1 + brightness)));
                    
                    img.setRGB(x, y, pixel);
                }
            }
        }
        
        return img;
    }
    
    private BufferedImage getCharImage(Character ch, float hue, float saturation, float brightness, HashMap<Character, BufferedImage> cache)
    {
        BufferedImage img = cache.get(ch);
        
        if (img == null)
        {
            img = chars.get(ch);
            
            if (img != null)
            {
                img = tintChar(img, hue, saturation, brightness);
                cache.put(ch, img);
            }
        }
        
        return img;
    }
    
    public Color getDefaultColor()
    {
        return defaultColor;
    }
    
    public int getHeight()
    {
        return height;
    }
}
