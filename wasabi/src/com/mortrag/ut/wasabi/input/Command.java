package com.mortrag.ut.wasabi.input;

/**
 * Enums should implement this interface so that we have a generic interface for commands for all
 * levels (Screen's).
 * 
 * @author max 
 */
public interface Command {
	
	/**
	 * 
	 * @return a string that describes the given command.
	 */
	public String toString();
	
	/**
	 * 
	 * @return the pressing type of the command.
	 */
	public Type getType();
	
	/**
	 * Describes how the command will be fired.
	 * 
	 * @author max
	 */
	public enum Type {
		/**
		 * This command will only fire on "press down", and won't continue to fire when held. Think
		 * of it as "once." Also used for normal mouse move events.
		 */
		PRESS,
		
		/**
		 * This command will continue to fire from "press down" to "press up".
		 */
		HOLD
	}
}
