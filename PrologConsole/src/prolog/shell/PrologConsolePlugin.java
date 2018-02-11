package prolog.shell;

import org.gjt.sp.jedit.*;

import console.*;

import java.util.*;

/**
 * Main class for the PrologConsole jEdit plugin.
 *
 * @author  Giulio Piancastelli
 * @version 0.4 - Wednesday 29th January, 2003
 */
public class PrologConsolePlugin extends EditPlugin {

    public static final String MENU = "prologconsole.menu";

	public void start() {
        // register the PrologConsole shell to the Console plugin
        //Shell.registerShell(PrologConsole.getInstance());
        // register the PrologConsole macro handler
        Macros.registerHandler(new PrologHandler());
        //menuItems.addElement(GUIUtilities.loadMenu(MENU));
	}

    public void createMenuItems(Vector menuItems) {
        menuItems.addElement(GUIUtilities.loadMenu(MENU));
    }

} // end PrologConsolePlugin class