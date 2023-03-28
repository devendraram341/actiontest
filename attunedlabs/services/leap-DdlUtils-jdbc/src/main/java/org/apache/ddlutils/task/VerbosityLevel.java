package org.apache.ddlutils.task;

import org.apache.tools.ant.types.EnumeratedAttribute;
import org.slf4j.event.Level;

/**
 * Helper class that defines the possible values for the verbosity attribute.
 * 
 * @ant.task ignore="true"
 * @version $Revision: $
 */
public class VerbosityLevel extends EnumeratedAttribute {
	/** The possible levels. */
	private static final String[] LEVELS = { Level.ERROR.toString().toUpperCase(), Level.WARN.toString().toUpperCase(),
			Level.INFO.toString().toUpperCase(), Level.DEBUG.toString().toUpperCase(),
			Level.ERROR.toString().toLowerCase(), Level.WARN.toString().toLowerCase(),
			Level.INFO.toString().toLowerCase(), Level.DEBUG.toString().toLowerCase() };

	/**
	 * Creates an uninitialized verbosity level object.
	 */
	public VerbosityLevel() {
		super();
	}

	/**
	 * Creates an initialized verbosity level object.
	 * 
	 * @param level The level
	 */
	public VerbosityLevel(String level) {
		super();
		setValue(level);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getValues() {
		String[] result = new String[LEVELS.length];

		System.arraycopy(LEVELS, 0, result, 0, LEVELS.length);
		return result;
	}

	/**
	 * Determines whether this is DEBUG verbosity.
	 * 
	 * @return <code>true</code> if this is the DEBUG level
	 */
	public boolean isDebug() {
		return Level.DEBUG.toString().equalsIgnoreCase(getValue());
	}
}