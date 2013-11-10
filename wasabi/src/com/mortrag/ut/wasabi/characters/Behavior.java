package com.mortrag.ut.wasabi.characters;

/**
 * A behavior is a Strategy pattern implementation for objects that are Behaviorable.
 * 
 * @author max
 *
 */
public interface Behavior {
	public void tick(Behaviorable b, float delta);
}
