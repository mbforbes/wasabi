package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.utils.Array;

/**
 * Inputable things should respond to input. Contrast this with Behaviorable objects that act on
 * their own.
 * 
 * @author max
 *
 */
public interface Inputable {
	public enum Input {
		LEFT,
		RIGHT,
		UP;
	}
	
	public void inputs(Array<Input> inputs);
}
