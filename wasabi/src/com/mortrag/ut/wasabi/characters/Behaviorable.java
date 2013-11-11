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
	// -----------------------------
	
	// position, velocity, acceleration
	public Vector2 getP();
	public Vector2 getV();
	public Vector2 getA();
	
	/** Get direction the sprite should be drawn. False is for right. */ 
	public boolean getFacingLeft();
	/** Set direction the sprite should be drawn. False is for right. */ 	
	public void setFacingLeft(boolean facingLeft);
	
	/** Whether the sprite should be animated as moving in a particular direction. This isn't
	  * as simple as velocity or acceleration because the sprite could be being moved somewhere
	  * agains't its "will"---e.g. some external force like an explosion or a push. This, this
	  * is supposed to return intent; this functionality happens in the input, too. */
	public void setGoingLeft(boolean goingLeft);
	public void setGoingRight(boolean goingRight);
	
	/** How fast the sprite should move. */
	public float getMoveAccel();
	
	/** Pass this tick onto the behavior */
	public void tick(float delta);
	
	/** Set the behavior the sprite should use for movement, etc. */
	public void setBehavior(Behavior b);
}
