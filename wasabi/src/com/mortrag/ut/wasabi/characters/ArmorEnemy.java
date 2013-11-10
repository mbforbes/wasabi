package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.mortrag.ut.wasabi.graphics.Common;

public class ArmorEnemy extends Enemy implements Physicsable, Behaviorable {

	// ---------------------------------------------------------------------------------------------
	// CONSTANTS
	// ---------------------------------------------------------------------------------------------
	public static final float DEFAULT_MOVE_ACCEL = 6000.0f;
	
	// ---------------------------------------------------------------------------------------------
	// FIELDS
	// ---------------------------------------------------------------------------------------------
	
	private Behavior behavior;
	private float moveAccel;
	
	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------
	
	public ArmorEnemy(float x, float y, TextureAtlas atlas) {
		this(x, y, atlas, DEFAULT_MOVE_ACCEL);
	}
	
	public ArmorEnemy(float x, float y, TextureAtlas atlas, float moveAccel) {
		super(x, y);
		addAnimationsToArmorEnemy(atlas);
		setAction(Action.IDLE);
		this.moveAccel = moveAccel;
		behavior = new PatrolBehavior(1.0f, 4000.0f);
	}
	
	// ---------------------------------------------------------------------------------------------
	// PRIVATE
	// ---------------------------------------------------------------------------------------------
		
	
	private void addAnimationsToArmorEnemy(TextureAtlas atlas) {
		animations.put(Action.IDLE, new Animation(0.1f, Common.getFrames(atlas, "s_keisatsu"),
				Animation.NORMAL));
	}
	
	// ---------------------------------------------------------------------------------------------
	// API
	// ---------------------------------------------------------------------------------------------
		

	@Override
	public void applyPhysics(Physics accel) {
		switch(accel) {
		case GRAVITY:
			if (!getOnGround()) {
				a.y += Physicsable.A_GRAVITY;
			}
			break;
		case FRICTION:
			// Friction always. Otherwise jumping feels weird.
			//if (getOnGround()) {
				v.x *= Physicsable.S_FRICTION;
			//}
			break;
		}
	}

	@Override
	public void tick(float delta) {
		behavior.tick(this, delta);
	}

	@Override
	public void setBehavior(Behavior b) {
		behavior = b;		
	}

	@Override
	public float getMoveAccel() {
		return moveAccel;
	}	
		
}
