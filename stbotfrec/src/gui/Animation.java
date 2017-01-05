/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import engines.Base;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 *
 * @author AP
 */
public class Animation {
    private int delayTime;
    private BufferedImage[] frames;
    
    public Animation(int delayTime, BufferedImage[] frames)
    {
        this.delayTime = delayTime;
        this.frames = frames;
    }

    public Animation(Animation animation) {
        this.delayTime = animation.delayTime;
        this.frames = animation.frames;
    }
    
    public FrameSelector createFrameSelector()
    {
        return new FrameSelector();
    }
    
    public void updateFrameSelector(FrameSelector selector, int time, int maxTime)
    {
        if (frames.length <= 1) return;
        
        int timePassed = (time < selector.prevTime) ? (maxTime - selector.prevTime + time) : (time - selector.prevTime);
        
        int frameIndexMod = timePassed / delayTime;
        
        if (frameIndexMod > 0)
        {
            selector.frameIndex = (selector.frameIndex + frameIndexMod) % frames.length;
            selector.prevTime = (selector.prevTime + frameIndexMod * delayTime) % maxTime;
        }
    }
    
    public BufferedImage getFrame(FrameSelector selector)
    {
        return frames[selector.frameIndex];
    }

    public int getHeight() {
        if (frames.length == 0) return 0; else return frames[0].getHeight();
    }

    public int getWidth() {
        if (frames.length == 0) return 0; else return frames[0].getWidth();
    }

    public int getDelayTime() {
        return delayTime;
    }
    
    public class FrameSelector
    {
        private int frameIndex;
        private int prevTime;
    }
    
    public void resize(int width, int height)
    {
        for (int i = 0; i < frames.length; i++)
        {
            resize(i, width, height);
        }
    }
    
    private void resize(int index, int width, int height)
    {
        BufferedImage img = frames[index];
        BufferedImage scaled;
            
        if (width < img.getWidth() || height < img.getHeight())
        {
            int w, h;

            if (img.getWidth() / (float) width >  img.getHeight() / (float) height)
            {
                w = width;
                h = (int)(img.getHeight() / (float) img.getWidth() * w);
            }
            else
            {
                h = height;
                w = (int)(img.getWidth() / (float) img.getHeight() * h);
            }

            img = Base.resize(img, w, h);
        }
            
        scaled = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice().getDefaultConfiguration()
                    .createCompatibleImage(width, height, Transparency.TRANSLUCENT);

        Graphics g = scaled.getGraphics();
        g.drawImage(img, (width  - img.getWidth()) / 2, (height - img.getHeight()) / 2, null);
        g.dispose();

        frames[index] = scaled;
    }
    
    public int getFrameCount()
    {
        return frames.length;
    }
    
    public BufferedImage getFrame(int index)
    {
        return frames[index];
    }
}
