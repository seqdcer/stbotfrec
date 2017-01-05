package init;


import engines.CommandInterface;
import gui.Window;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author AP
 */
public class Main {
    
    public static final String APPNAME = "4X";
    public static CommandInterface CI;
    
    public static void main(String[] args)
    {
        java.awt.EventQueue.invokeLater(() -> {
            Window window = new Window();
            window.setVisible(true);
            CI = new CommandInterface(window);
            CI.runCommand(null, null, "runScript", "init");
        });
    }
}
