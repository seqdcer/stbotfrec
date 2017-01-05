/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engines;

import data.NumberValueRef;
import data.Settings;
import java.util.PriorityQueue;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

/**
 *
 * @author AP
 */
public abstract class Sound {

    public enum ConflictRule {ignore_on_conflict, queue_on_conflict, drop_on_conflict, override_on_conflict};
    
    private static final PriorityQueue<Clip> sfx_queue = new PriorityQueue<>();
    private static final PriorityQueue<Clip> voice_queue = new PriorityQueue<>();
    private static final PriorityQueue<Clip> music_queue = new PriorityQueue<>();

    private static final QueueNextListener sfxQueueListener = new QueueNextListener(sfx_queue, Settings.SFX_VOLUME);
    private static final QueueNextListener voiceQueueListener = new QueueNextListener(voice_queue, Settings.VOICE_VOLUME);
    private static final QueueNextListener musicQueueListener = new QueueNextListener(music_queue, Settings.MUSIC_VOLUME);
    
    private static Clip soundPlaying;
    private static Clip voicePlaying;
    private static Clip musicPlaying;
    
    public static void playSound(String soundFile)
    {
        playSound(soundFile, ConflictRule.ignore_on_conflict);
    }
    
    public synchronized static void playSound(String soundFile, ConflictRule rule)
    {
        Clip clip = Base.getSoundClip(soundFile);
        
        if (clip != null)
        {
            if (soundPlaying != null && soundPlaying.isRunning())
            {
                switch (rule)
                {
                    case queue_on_conflict:
                    {
                        sfx_queue.add(clip);
                        break;
                    }
                    case drop_on_conflict:
                    {
                        clip.close();
                        return;
                    }
                    case override_on_conflict:
                    {
                        soundPlaying.stop();
                        sfx_queue.clear();
                    }
                }
                
            }
            
            float sfxVolume = ((NumberValueRef)NumberValueRef.create(null, null, Base.getVariable(null, null, Base.SETTINGS_ROOT, Settings.SFX_VOLUME), false)).floatValue();

            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            sfxVolume = Math.min(Math.max(sfxVolume, volume.getMinimum()), volume.getMaximum());

            volume.setValue(sfxVolume);
            
            clip.addLineListener(sfxQueueListener);
            
            soundPlaying = clip;
            
            clip.start();
        }
    }
    
    public static void playVoice(String soundFile)
    {
        playVoice(soundFile, ConflictRule.override_on_conflict);
    }
    
    public synchronized static void playVoice(String soundFile, ConflictRule rule)
    {
        Clip clip = Base.getSoundClip(soundFile);
        
        if (clip != null)
        {
            if (voicePlaying != null && voicePlaying.isRunning())
            {
                switch (rule)
                {
                    case queue_on_conflict:
                    {
                        voice_queue.add(clip);
                        break;
                    }
                    case drop_on_conflict:
                    {
                        clip.close();
                        return;
                    }
                    case override_on_conflict:
                    {
                        voicePlaying.stop();
                        voice_queue.clear();
                    }
                }
                
            }
            
            float fVolume = ((NumberValueRef)NumberValueRef.create(null, null, Base.getVariable(null, null, Base.SETTINGS_ROOT, Settings.VOICE_VOLUME), false)).floatValue();

            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            fVolume = Math.min(Math.max(fVolume, volume.getMinimum()), volume.getMaximum());

            volume.setValue(fVolume);
            
            clip.addLineListener(voiceQueueListener);
            
            voicePlaying = clip;
            
            clip.start();
        }
    }
    
    private static class QueueNextListener implements LineListener
    {
        private final PriorityQueue<Clip> queue;
        private final String volumeSetting;
        
        public QueueNextListener(PriorityQueue<Clip> queue, String volumeSetting)
        {
            this.queue = queue;
            this.volumeSetting = volumeSetting;
        }
        
        @Override
        public void update(LineEvent event) {
            if (event.getType() == LineEvent.Type.STOP)
            {
                Clip clip = queue.poll();
                
                if (clip != null)
                {
                    float sfxVolume = ((NumberValueRef)NumberValueRef.create(null, null, Base.getVariable(null, null, Base.SETTINGS_ROOT, volumeSetting), false)).floatValue();

                    FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                    sfxVolume = Math.min(Math.max(sfxVolume, volume.getMinimum()), volume.getMaximum());

                    volume.setValue(sfxVolume);
                    
                    soundPlaying = clip;

                    clip.addLineListener(this);
                    
                    clip.start();
                }
            }
        }
    }
}
