package data;

//import list
import engines.Base;
import init.Main;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

/**
Settings are loaded from settings.ini
*/
public class Settings
{
    public JSONObject settings;
    
    // settings file
    private final File file;
    
    /**
     * PROPERTY FIELDS
     */
    public static final String AUDIO_SUBFOLDER = "audioSubFolder";
    public static final String IMAGES_SUBFOLDER = "imagesSubFolder";
    public static final String GUI_CONF_SUBFOLDER = "guiSubFolder";
    public static final String FONT_SUBFOLDER = "fontSubFolder";
    public static final String SCRIPTS_SUBFOLDER = "scriptsSubfolder";
    public static final String STATIC_CONF_SUBFOLDER = "staticSubFolder";
    public static final String SCREEN_RESOLUTION = "screenResolution";
    public static final String BASE_RESOLUTION = "baseResolutionWidth";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String SFX_VOLUME = "sfxVolume";
    public static final String VOICE_VOLUME = "voiceVolume";
    public static final String MUSIC_VOLUME = "musicVolume";
    public static final String KEYBINDS = "keybinds";
    
    /**
	Set's the default vaules, loads saved settings.
     */
    public Settings()
    {
        file = new File(new File(System.getProperty("user.home"), "." + Main.APPNAME), "settings");
        
        settings = Base.getConfig(file.getPath());
        
        if (settings == null)
        {
            settings = new JSONObject();
        }
    }
    
    /**
     * Saves settings.
     * If the file doesn't exist it is created.
     */
    public void saveSettings()
    {
        try
        {
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(new File(file.getParent(), file.getName() + ".json")); 
            settings.writeJSONString(writer);
            writer.close();
        }
        catch (Exception e)
        {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, "Unable to save settings to configuration file.", e);
        }
    }
}
