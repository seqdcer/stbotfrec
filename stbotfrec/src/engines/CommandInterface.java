/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engines;

import data.BooleanValueRef;
import data.EnumValueRef;
import data.NumberValueRef;
import data.StringValueRef;
import gui.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author AP
 */
public class CommandInterface {
    private final Window window;
    
    public static final String ADD_TO_LIST = "addToList";
    public static final String CLEAR_VARIABLE = "clearVariable";
    public static final String CLOSE_DIALOG = "closeDialog";
    public static final String CREATE_LIST = "createList";
    public static final String DEBUG_PRINT = "debugPrint";
    public static final String GET_VARIABLE = "getVariable";
    public static final String GET_RANDOM_DOUBLE = "getRandomDouble";
    public static final String GET_RANDOM_LONG = "getRandomLong";
    public static final String GET_RANDOM_FROM_LIST = "getRandomFromList";
    public static final String HARD_EXIT = "hardExit";
    public static final String MOD_VARIABLE = "modVariable";
    public static final String MUL_VARIABLE = "mulVariable";
    public static final String PLAY_SOUND = "playSound";
    public static final String PLAY_VOICE = "playVoice";
    public static final String RUN_COMMAND = "runCommand";
    public static final String RUN_SCRIPT = "runScript";
    public static final String SAVE_USER_DATA = "saveUserData";
    public static final String SELECT_SETTING = "selectSetting";
    public static final String SETUP_ENUM = "setupEnum";
    public static final String SET_REFVARIABLE = "setRefVariable";
    public static final String SET_RNG_SEED = "setRngSeed";
    public static final String SET_VARIABLE = "setVariable";
    public static final String SHOW_DIALOG = "showDialog";
    public static final String SHOW_SCREEN = "showScreen";
    public static final String SUB_VARIABLE = "subVariable";

    private final ScriptEngineManager SEManager = new ScriptEngineManager();
    private final ScriptEngine scriptEngine = SEManager.getEngineByName("JavaScript");
    private final Random rng = new Random();

    private int nextTicket = 0;
    private int nextQueueTicket = 0;
    
    public CommandInterface(Window window)
    {
        this.window = window;
    }
    
    private Object run(JSONObject localContext, JSONObject thisContext, String command, String... passedArgs)
    {
        Object retVal = null;
        
        try
        {
            String[] tokens = parseCmd(command, passedArgs);

            command = tokens[0];

            String[] args = new String[tokens.length - 1];
            System.arraycopy(tokens, 1, args, 0, tokens.length - 1);

            boolean missingArgs = false;

            switch (command)
            {
                case ADD_TO_LIST:
                {
                    if (args.length == 2)
                        Base.addToList(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(), -1,
                                StringValueRef.create(localContext, thisContext, args[1], false).toString());
                    else if (args.length >= 3)
                        Base.addToList(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[1], false)).intValue(),
                                StringValueRef.create(localContext, thisContext, args[2], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case CLEAR_VARIABLE:
                {
                    if (args.length >= 1)
                        Base.setVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(), (Object) "");
                    else
                        missingArgs = true;

                    break;
                }
                case CLOSE_DIALOG:
                {
                    if (args.length >= 1)
                        window.closeDialog(StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case CREATE_LIST:
                {
                    if (args.length >= 1)
                        Base.createList(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case DEBUG_PRINT:
                {
                    if (args.length >= 1)
                    {
                        for (int i = 0; i < args.length; i++)
                        {
                            args[i] = StringValueRef.create(localContext, thisContext, args[i], false).toString();
                        }
                    
                        Logger.getLogger(CommandInterface.class.getName()).log(Level.WARNING, "Debug print: {0}", args);
                    }
                    else
                        missingArgs = true;

                    break;
                }
                case GET_VARIABLE:
                {
                    if (args.length >= 1)
                        retVal = Base.getVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case GET_RANDOM_DOUBLE:
                {
                    if (args.length == 0)
                        retVal = rng.nextDouble();
                    else if (args.length == 1)
                        retVal = (long)Math.floor(rng.nextDouble() * ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[0], false)).doubleValue());
                    else if (args.length >= 2)
                    {
                        double min = ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[0], false)).doubleValue();
                        double max = ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue();
                        retVal = (long)Math.floor(rng.nextDouble() * (max - min) + min);
                    }

                    break;
                }
                case GET_RANDOM_FROM_LIST:
                {
                    if (args.length >= 1)
                    {
                        retVal = Base.getVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    
                        if (retVal instanceof List)
                        {
                            List list = (List) retVal;
                            
                            if (list.isEmpty())
                                retVal = null;
                            else
                            {
                                retVal = list.get(rng.nextInt(list.size()));
                            }
                        }
                        else
                            retVal = null;
                    }
                    else
                        missingArgs = true;

                    break;
                }
                case GET_RANDOM_LONG:
                {
                    if (args.length == 0)
                        retVal = rng.nextLong();
                    else if (args.length == 1)
                        retVal = (long)Math.floor(rng.nextDouble() * ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[0], false)).longValue());
                    else if (args.length >= 2)
                    {
                        long min = ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[0], false)).longValue();
                        long max = ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[1], false)).longValue();
                        retVal = (long)Math.floor(rng.nextDouble() * (max - min) + min);
                    }

                    break;
                }
                case HARD_EXIT:
                {
                    Base.saveSettings();
                    System.exit(0);

                    break;
                }
                case MOD_VARIABLE:
                {
                    if (args.length == 2)
                        Base.modVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(), - Double.MAX_VALUE, Double.MAX_VALUE);
                    else if (args.length == 3)
                        Base.modVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).doubleValue(), Double.MAX_VALUE);
                    else if (args.length == 4)
                        Base.modVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[3], false)).doubleValue());
                    else
                        missingArgs = true;
                    
                    break;
                }
                case MUL_VARIABLE:
                {
                    if (args.length == 2)
                        Base.mulVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(), - Double.MAX_VALUE, Double.MAX_VALUE);
                    else if (args.length == 3)
                        Base.mulVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).doubleValue(), Double.MAX_VALUE);
                    else if (args.length == 4)
                        Base.mulVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[3], false)).doubleValue());
                    else
                        missingArgs = true;
                    
                    break;
                }
                case PLAY_SOUND:
                {
                    if (args.length == 1)
                        Sound.playSound(StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else if (args.length >= 2)
                        Sound.playSound(StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                Sound.ConflictRule.valueOf(StringValueRef.create(localContext, thisContext, args[1], false).toString()));
                    else
                        missingArgs = true;
                    
                    break;
                }
                case PLAY_VOICE:
                {
                    if (args.length == 1)
                        Sound.playVoice(StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else if (args.length >= 2)
                        Sound.playVoice(StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                Sound.ConflictRule.valueOf(StringValueRef.create(localContext, thisContext, args[1], false).toString()));
                    else
                        missingArgs = true;
                    
                    break;
                }
                case RUN_COMMAND:
                {
                    if (args.length >= 1)
                    {
                        Object json = Base.getVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString());
                        
                        if (json != null)
                            this.runUICommand(localContext, thisContext, json);
                    }
                    else
                        missingArgs = true;

                    break;
                }
                case RUN_SCRIPT:
                {
                    if (args.length >= 1)
                    {
                        String scriptFile = StringValueRef.create(localContext, thisContext, args[0], false).toString();
                        String script = Base.getScript(scriptFile);

                        if (script != null)
                        {
                            try {
                                ScriptContext context = new SimpleScriptContext();
                                
                                context.setAttribute("CI", new CIWrapper(localContext, thisContext), ScriptContext.ENGINE_SCOPE);
                                context.setAttribute(ScriptEngine.FILENAME, scriptFile, ScriptContext.ENGINE_SCOPE);
                                
                                String[] sargs = new String[args.length - 1];
                                
                                for (int k = 0; k < sargs.length; k++)
                                {
                                    sargs[k] = StringValueRef.create(localContext, thisContext, args[k + 1], false).toString();
                                }
                                
                                context.setAttribute("scriptArgs", sargs, ScriptContext.ENGINE_SCOPE);
                                
                                scriptEngine.eval(script, context);
                            } catch (ScriptException ex) {
                                Logger.getLogger(CommandInterface.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                    else
                        missingArgs = true;

                    break;
                }
                case SAVE_USER_DATA:
                {
                    if (args.length >= 1)
                        Base.saveUserData(StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case SELECT_SETTING:
                {
                    if (args.length == 2)
                        Base.selectOption(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[1], false)).intValue(), false);
                    else if (args.length > 2)
                        Base.selectOption(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                ((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[1], false)).intValue(),
                                ((BooleanValueRef)BooleanValueRef.create(localContext, thisContext, args[2], false)).getValue());
                    else
                        missingArgs = true;

                    break;
                }
                case SETUP_ENUM:
                {
                    if (args.length >= 2)
                    {
                        String varRef = StringValueRef.create(localContext, thisContext, args[0], false).toString();
                        Object value = Base.getVariable(localContext, thisContext, varRef);
                        
                        String listRef = StringValueRef.create(localContext, thisContext, args[1], false).toString();
                        List list = (List) Base.getVariable(localContext, thisContext, listRef);
                        
                        Base.setVariable(localContext, thisContext, varRef, EnumValueRef.create(localContext, thisContext, value, list, false));
                    }
                    else
                        missingArgs = true;

                    break;
                }
                case SET_REFVARIABLE:
                {
                    if (args.length >= 2)
                        Base.setRefVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                (Object) StringValueRef.create(localContext, thisContext, args[1], false));
                    else
                        missingArgs = true;

                    break;
                }
                case SET_RNG_SEED:
                {
                    if (args.length >= 1)
                        rng.setSeed(((NumberValueRef)NumberValueRef.create(localContext, thisContext, args[0], false)).longValue());
                    else
                        missingArgs = true;

                    break;
                }
                case SET_VARIABLE:
                {
                    if (args.length >= 2)
                        Base.setVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                (Object) StringValueRef.create(localContext, thisContext, args[1], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case SHOW_SCREEN:
                {
                    if (args.length >= 1)
                        window.open(StringValueRef.create(localContext, thisContext, args[0], false).toString());
                    else
                        missingArgs = true;

                    break;
                }
                case SHOW_DIALOG:
                {
                    if (args.length == 1)
                        try {
                            window.openDialog(StringValueRef.create(localContext, thisContext, args[0], false).toString(), false);
                        } catch (Exception ex) {
                            Logger.getLogger(CommandInterface.class.getName()).log(Level.SEVERE, "Error on open dialog: " + args[0], ex);
                        }
                    else if (args.length == 2)
                        try {
                            window.openDialog(StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                    ((BooleanValueRef)BooleanValueRef.create(localContext, thisContext, args[1], false)).getValue());
                        } catch (Exception ex) {
                            Logger.getLogger(CommandInterface.class.getName()).log(Level.SEVERE, "Error on open dialog: " + args[0], ex);
                        }
                    else if (args.length >= 4)
                        try {
                            window.openDialog(StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                    ((BooleanValueRef)BooleanValueRef.create(localContext, thisContext, args[1], false)).getValue(),
                                    ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).intValue(),
                                    ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).intValue());
                        } catch (Exception ex) {
                            Logger.getLogger(CommandInterface.class.getName()).log(Level.SEVERE, "Error on open dialog: " + args[0], ex);
                        }
                    else
                        missingArgs = true;

                    break;
                }
                case SUB_VARIABLE:
                {
                    if (args.length == 2)
                        Base.modVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                - ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(), - Double.MAX_VALUE, Double.MAX_VALUE);
                    else if (args.length == 3)
                        Base.modVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                - ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).doubleValue(), Double.MAX_VALUE);
                    else if (args.length == 4)
                        Base.modVariable(localContext, thisContext, StringValueRef.create(localContext, thisContext, args[0], false).toString(),
                                - ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[1], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[2], false)).doubleValue(),
                                ((NumberValueRef) NumberValueRef.create(localContext, thisContext, args[3], false)).doubleValue());
                    else
                        missingArgs = true;
                    
                    break;
                }
                default:
                {
                    Logger.getLogger(CommandInterface.class.getName()).log(Level.WARNING, "Unknown game command: {0}", command + " (" + Arrays.toString(passedArgs) + ")");
                }
            }

            if (missingArgs)
            {
                Logger.getLogger(CommandInterface.class.getName()).log(Level.WARNING, "Missing arguments for command: {0}", command + " (" + Arrays.toString(passedArgs) + ")");
            }
        }
        catch (Exception e)
        {
            Logger.getLogger(CommandInterface.class.getName()).log(Level.SEVERE, "Error running command: " + command + " (" + Arrays.toString(passedArgs) + ")", e);
        }
        
        return retVal;
    }
    
    private String[] parseCmd(String cmd, String... args)
    {
        ArrayList<String> result = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        
        boolean quoted = false;
        boolean escaped = false;
        
        for (int i = 0; i < cmd.length(); i++)
        {
            char ch = cmd.charAt(i);
            
            switch (ch)
            {
                case '\"':
                {
                    if (escaped)
                    {
                        builder.append(ch);
                        escaped = false;
                    }
                    else
                    {
                        if (builder.length() > 0)
                        {
                            result.add(builder.toString());
                            builder.setLength(0);
                        }
                        
                        quoted = !quoted;
                    }
                    break;
                }
                case ' ':
                {
                    if (quoted)
                    {
                        builder.append(ch);
                    }
                    else
                    {
                        if (builder.length() > 0)
                        {
                            result.add(builder.toString());
                            builder.setLength(0);
                        }
                    }
                    break;
                }
                case '\\':
                {
                    if (escaped)
                    {
                        builder.append(ch);
                    }
                    escaped = !escaped;
                    break;
                }
                default:
                {
                    builder.append(ch);
                    break;
                }
            }
        }
        
        if (builder.length() > 0)
        {
            result.add(builder.toString());
            builder.setLength(0);
        }
        
        result.addAll(Arrays.asList(args));
        
        return result.toArray(new String[result.size()]);
    }
    
    public void runSyncUICommand(JSONObject localContext, JSONObject config, Object json)
    {
        if (!Thread.holdsLock(this))
        {
            int ticket;
            
            synchronized (this)
            {
                ticket = nextQueueTicket;
                nextQueueTicket++;

                while (ticket != nextTicket)
                {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                    }
                }
               
                runUICommand(localContext, config, json);
                nextTicket++;
                notifyAll();
            }
        }
        else
        {
            synchronized (this)
            {
                runUICommand(localContext, config, json);
                notifyAll();
            }
        }
    }
    
    private void runUICommand(JSONObject localContext, JSONObject config, Object json)
    {
        if (json != null)
        {
            if (json instanceof JSONArray)
            {
                JSONArray arr = (JSONArray) json;

                for (Object cmd : arr)
                {
                    runUICommand(localContext, config, cmd);
                }
            }
            else
            {
                String command = StringValueRef.create(localContext, config, json, false).toString();

                if (command.length() > 0)
                {
                    run(localContext, config, command);
                    command = StringValueRef.create(localContext, config, json, false).toString();
                }
            }
        }
    }
    
    public Object runCommand(JSONObject localContext, JSONObject config, String command, String... args)
    {
        return run(localContext, config, command, args);
    }
    
    public class CIWrapper
    {
        private final JSONObject localContext;
        private final JSONObject thisContext;
        
        private CIWrapper(JSONObject localContext, JSONObject thisContext)
        {
            this.localContext = localContext;
            this.thisContext = thisContext;
        }
        
        public Object run(String command)
        {
            return runCommand(localContext, thisContext, command);
        }
        
        public Object run(String command, String... args)
        {
            return runCommand(localContext, thisContext, command, args);
        }
    }
}
