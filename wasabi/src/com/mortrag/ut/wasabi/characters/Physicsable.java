package com.mortrag.ut.wasabi.characters;

public interface Physicsable {
	// acceleration constants A_*
	public static final float A_GRAVITY = -3000.0f;
	
	// scalars S_*
	public static final float S_FRICTION = 0.9f;
	
	public enum Physics {
		GRAVITY,
		FRICTION;
	}
	
	public void applyPhysics(Physics accel);
}
