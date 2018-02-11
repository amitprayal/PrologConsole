package prolog.shell;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.gui.*;

import console.*;

import alice.tuprolog.*;
import alice.tuprolog.event.*;

import java.awt.*;
import javax.swing.*;

/**
 * PrologConsole shell to be integrated in jEdit through the Console plugin.
 *
 * @author  <a href="mailto:gpian@softhome.net">Giulio Piancastelli</a>
 * @version 0.4 - Friday 31st January, 2003
 */
public final class PrologConsole extends Shell implements OutputListener, SpyListener {

    private static final PrologConsole singleton = new PrologConsole();

    // Shell related fields
	private Console console;
    private Output output;
    private Output error;

    // Prolog related fields
	private Prolog engine;
	private SolveInfo info;
	
	private PrologConsole() {
		super("tuProlog");
    }

    public static PrologConsole getInstance() {
        return singleton;
    }

    /**
     * Perform late initialization of the Prolog engine.
     *
     * @since 0.3
     */
    private void initEngine() {
        if (engine == null) {
            // setting up the Prolog stuff
            engine = new Prolog();
            engine.addOutputListener(this);
            engine.addSpyListener(this);
        }
    }

    /* Implementing abstract Shell methods */

    public void printPrompt(Console console, Output output)
	{
		output.writeAttrs(ConsolePane.colorAttributes(console.getInfoColor()),"?-");
		output.writeAttrs(null, " ");
	}
    
    public void printInfoMessage(Output output) {
        initEngine();
        String info = engine.getVersion();
        output.print(null, "tuProlog System - Release " + info);
    }
                
    public void execute(Console console,String input, Output output,Output error, String command) {
        initEngine();
        this.console = console;
        this.output = output;
        this.error = error;
        /* Parsing command string */
        if (command.equals(";"))
            // asking for another solution to the current goal
            nextSolution();
        else
            if (command.equals("new."))
                // asking to create a new theory
                newTheory();
            else
                if (command.equals("consult."))
                    // asking to consult the theory in the current jEdit buffer
                    addTheory(console.getView().getBuffer());
                else
                    if (command.equals("listing."))
                        // asking to list all the current theory's clauses
                        viewTheory();
                    else
                        // asking to solve a goal
                        solveGoal(command);
        output.commandDone();
    }

    /**
     * No need for a stop method, since execution does not occur in a separate thread.
     */
    public void stop(Console console) {
        // Do nothing
    }

    public boolean waitFor(Console console) {
        return true;
    }

    /**
     * Set up the Prolog system for a new blank theory.
     */
    public void newTheory() {
        initEngine();
        engine.clearTheory();
        Color infoColor = console.getInfoColor();
        output.print(infoColor, "Old Theory cleared up.\nSetting a new theory...\nReady.");
    }

    /**
     * Add the theory from the current jEdit buffer to the Prolog system
     * current theory.
     *
     * @param buffer The current jEdit buffer.
     */
    public void addTheory(Buffer buffer) {
        initEngine();
        String bufferName = buffer.getName();
		int lines = buffer.getLineCount();
		String bufferText = "";
		for (int i = 0; i < lines; i++)
			bufferText += buffer.getLineText(i) + "\n";
        Color infoColor = console.getInfoColor();
		output.print(infoColor, "Consulting Theory " + bufferName + "...");
		try {
			engine.addTheory(new Theory(bufferText));
			output.print(infoColor, "Theory Consulted");
		} catch (InvalidTheoryException ite) {
            Color errorColor = console.getErrorColor();
			error.print(errorColor, "Error consulting theory.");
		}
    }

    /**
     * Add a theory to the Prolog engine and log the <code>InvalidTheoryException</code>
     * thrown if the theory is not a valid one.
     *
     * @param t The theory to be added to the Prolog engine.
     */
    public void addTheory(Theory t) {
        initEngine();
        try {
            engine.addTheory(t);
        } catch (InvalidTheoryException e) {
            org.gjt.sp.util.Log.log(org.gjt.sp.util.Log.ERROR, this, e);
        }
    }

    /**
     * Display all the current theory's clauses in the output text area.
     */
    public void viewTheory() {
        initEngine();
        Theory theory = engine.getTheory();
		java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
		try {
			stream.write(theory.toString().getBytes());
			output.print(null, new String(stream.toByteArray()));
		} catch (java.io.IOException ioe) {
            Color warningColor = console.getWarningColor();
			error.print(warningColor, "Problems getting current theory.");
		}
    }

    /**
	 * Solve a Prolog goal.
     *
     * @param goal A <code>java.lang.String</code> representation of the goal
     * to solve.
	 */
	private void solveGoal(String goal) {
		try {
			info = engine.solve(goal);
			if (info.isHalted()) {
				/* Do... nothing? */
			} else{
				if (!info.isSuccess())
                    output.print(null, "No.");
				else
					if (!engine.hasOpenAlternatives()) {
						String binds = info.getSolution().toString();
						if (binds.equals(""))
							output.print(null, "Yes\nSolution: " + engine.toString(info.getSolution()));
						else
							output.print(null, "Yes.\nBindings: " + binds + "\nSolution: " + engine.toString(info.getSolution()));
					} else {
						String binds = info.getSolution().toString();
						output.print(null, "Yes.\nBindings: " +binds + "\nSolution: " + engine.toString(info.getSolution()) + " ? ( ; for more solutions )");
					}
			}		
		} catch (MalformedGoalException mge) {
            /* some kind of fatal error */
            Color errorColor = console.getErrorColor();
			error.print(errorColor, "Syntax Error in Goal.");
		} catch (Exception e) {
            Color warningColor = console.getWarningColor();
			error.print(warningColor, e.getMessage());
		}
	}

    /**
	 * Check for another solution in solving a goal with mutiple open choice points.
     * Takes no parameters, because the goal is the last being solved.
	 */
	private void nextSolution() {
		try {
			info = engine.solveNext();
			if (info.isHalted()) {
				/* Do... nothing? */
			} else
				if (!info.isSuccess())
                    output.print(null, "No.");
				else {
					String binds = info.getSolution().toString();
					//output.print(null, binds + engine.toString(info.getSolution()) + " ?");
					output.print(null, "Yes.\nBindings: " +binds + "\nSolution: " + engine.toString(info.getSolution()) + " ? ( ; for more solutions )");
				}
		} catch (NoMoreSolutionException nmse) {
			output.print(null, "No.");
		} catch (Exception e) {
            Color warningColor = console.getWarningColor();
			output.print(warningColor, e.getMessage());
		}
	}

	/* Implementing OutputListener */

	public void onOutput(OutputEvent event) {
        final String OUTPUT_PROMPT = "[Output] ";
        output.print(null, OUTPUT_PROMPT + event.getMsg());
	}

    /* Implementing SpyListener */

    public void onSpy(SpyEvent event) {
        final String SPY_PROMPT = "[Spy] ";
        output.print(null, SPY_PROMPT + event.getMsg());
    }

} // end PrologConsole class