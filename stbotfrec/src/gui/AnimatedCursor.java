/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;

/**
 *
 * @author AP
 */
public class AnimatedCursor {

    private int delayTime;
    private Cursor[] frames;
    
    private int currentFrameIndex;
    private int prevTime;
    private boolean change = true;
    
    public AnimatedCursor(Animation animation, String name) {
        
        Animation ani = new Animation(animation);
        
        Dimension cursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(ani.getWidth(), ani.getHeight());
        
        ani.resize(cursorSize.width, cursorSize.height);
        
        frames = new Cursor[ani.getFrameCount()];
        
        for (int i = 0; i < frames.length; i++)
        {
            frames[i] = Toolkit.getDefaultToolkit().createCustomCursor(ani.getFrame(i), new Point(ani.getWidth() / 2, ani.getHeight() / 2), name + i);
        }
        
        delayTime = ani.getDelayTime();
    }
    
    public Cursor getCursor(int time, int maxTime)
    {
        if (frames.length == 1) return frames[0];
        
        int timePassed = ((time < prevTime) ? (maxTime - prevTime + time) : (time - prevTime));
        
        int frameIndexMod = timePassed / delayTime;
        change = frameIndexMod > 0;
        
        if (change)
        {
            currentFrameIndex = (currentFrameIndex + frameIndexMod) % frames.length;
            prevTime = (prevTime + frameIndexMod * delayTime) % maxTime;
        }
        
        return frames[currentFrameIndex];
    }
    
    public boolean isLatestGetDifferent()
    {
        return change;
    }
}
