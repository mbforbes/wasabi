package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.math.collision.BoundingBox;

public interface Collidable {
	public boolean getOnGround();
	public void setOnGround(boolean onGround);
	public BoundingBox getBoundingBox();
}
