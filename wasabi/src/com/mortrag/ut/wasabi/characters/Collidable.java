package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.math.collision.BoundingBox;

/**
 * Collidables are always Advectable. We never explicitly have collections
 * of Collidables because they are always selected from the collections of
 * Advectables.
 * 
 * @author max
 *
 */
public interface Collidable {
	public boolean getOnGround();
	public void setOnGround(boolean onGround);
	public BoundingBox getBoundingBox();
	public BoundingBox getPrevBoundingBox();
	public void setPrevBoundingBox(BoundingBox b);
}
