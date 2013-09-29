package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public interface Advectable {	
	
	public static final float CLAMP_EPSILON = 0.01f;
	
	// Because interfaces can't have fields (in the typical sense), we use such mundane wrappers...
	// Note that Vector2's are uber-mutable, so we don't need setters.
	public Vector2 getP();	
	public Vector2 getV();
	public Vector2 getA();	
	public float getWidth();
	public float getHeight();
	
	/**
	 * @return Whether this object should respond to collidable objects. If this is false and an
	 * object moves, it would be like a ghost.
	 */
	public boolean collides();
	
	/**
	 * E.g. if falling changes animation.
	 */
	public void maybeUpdateAnimations(Array<Inputable.Input> inputs);
}
