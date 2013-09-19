package com.mortrag.ut.wasabi;

/**
 * Enums should implement this interface so that we have a generic interface for commands for all
 * levels (Screen's).
 * 
 * @author max 
 */
public interface Commands {
	
	/**
	 * 
	 * @return a string that describes the given command.
	 */
	public String toString();
	public Type getType();
	
	public enum Type {
		PRESS,
		HOLD
	}
}
