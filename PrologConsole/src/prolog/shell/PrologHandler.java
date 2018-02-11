package prolog.shell;

import org.gjt.sp.jedit.*;

import java.io.*;

/**
 * A macro handler for PrologConsole.<br/>
 * What macros are in Prolog? They are just Prolog theories which an user want to load
 * at startup to initialize the Prolog engine. These so-called macros are to be put
 * in the %USER-HOME%\.jedit\startup directory.
 * 
 * @author	<a href="mailto:gpian@softhome.net">Giulio Piancastelli</a>
 * @version	1.0 - 31-gen-03
 */	

public class PrologHandler extends Macros.Handler {

    private static final String GLOB = ".pro";

    public PrologHandler() {
        super("prolog"); // the name in the properties file
    }

    public Macros.Macro createMacro(String macroName, String path) {
        String label = Macros.Macro.macroNameToLabel(macroName);
        // remove the GLOB extension from the label
        label = label.substring(0, label.length() - GLOB.length());
        return new Macros.Macro(this, macroName, label, path);
    }

    public void runMacro(View view, Macros.Macro macro) {
        try {
            alice.tuprolog.Theory t = new alice.tuprolog.Theory(new FileInputStream(macro.getPath()));
            PrologConsole.getInstance().addTheory(t);
        } catch (Exception e) {
            org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.ERROR, this, e);
        }
    }

} // end PrologHandler class