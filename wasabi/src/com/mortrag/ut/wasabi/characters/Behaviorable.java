package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.math.Vector2;

/**
 * Behaviorable objects act on their own. Constrast this with inputable things that respond to 
 * input.
 *  
 * @author max
 *
 */
public interface Behaviorable {
	// state available to behaviors
	public Vector2 getP();
	public Vector2 getV();
	public Vector2 getA();
	public boolean getFacingLeft();
	public void setFacingLeft(boolean facingLeft);
	public float getMoveAccel();
	
	public void tick(float delta);
	public void setBehavior(Behavior b);
}
