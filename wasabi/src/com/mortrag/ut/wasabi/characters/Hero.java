package com.mortrag.ut.wasabi.characters;

import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

import com.mortrag.ut.wasabi.graphics.Common;

public class Hero extends WasabiCharacter implements Renderable, Inputable, Collidable, Physicsable, Advectable {

	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------
	
	public static final float MOVE_ACCEL = 5000.0f;
	public static final float JUMP_ACCEL = 80000.0f;
	
	// ---------------------------------------------------------------------------------------------
	// FIELDS
	// ---------------------------------------------------------------------------------------------

	private boolean inputLeft, inputRight, inputUp;
	
	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------

	public Hero(float x, float y, TextureAtlas atlas) {
		super(x,y);
		addAnimationsToHero(atlas);	
		setAction(Action.IDLE);
	}
	
	// ---------------------------------------------------------------------------------------------
	// PRIVATE
	// ---------------------------------------------------------------------------------------------
	
	
	private void addAnimationsToHero(TextureAtlas atlas) {
		animations.put(Action.RUN, new Animation(0.07f, Common.getFrames(atlas, "s_wasRun"),
				Animation.LOOP));
		animations.put(Action.JUMP, new Animation(0.1f, Common.getFrames(atlas, "s_wasJump"),
				Animation.NORMAL));
		animations.put(Action.FALL, new Animation(0.1f, Common.getFrames(atlas, "s_wasFall"),
				Animation.NORMAL));
		animations.put(Action.IDLE, new Animation(0.1f, Common.getFrames(atlas, "s_wasIdle"),
				Animation.LOOP_PINGPONG));
		animations.put(Action.ATTACK, new Animation(0.1f, Common.getFrames(atlas, "s_wasAtk"),
				Animation.NORMAL));
	}
		
	
	// ---------------------------------------------------------------------------------------------
	// API
	// ---------------------------------------------------------------------------------------------
	
	// INPUTABLE
	// ---------------------------------------------------------------------------------------------

	
	@Override
	public void inputs(Array<Input> inputs) {
		// TODO(max): Hackey... clear previous inputs
		inputLeft = false;
		inputRight = false;
		inputUp = false;
		
		// iterate through new inputs
		Iterator<Input> inpit = inputs.iterator();
		while (inpit.hasNext()) {
			Input i = inpit.next();
			switch(i) {
			case LEFT:
				inputLeft = true;
				break;
			case RIGHT:
				inputRight = true;
				break;
			case UP:
				inputUp = true;
				break;
			}			
		}
		
		// This separation (currently) has two purposes:
		// 1) the inputs are not filtered for duplicates. this should probaby
		//    be done somewhere before here. however, it was possible to press
		//    'A' and leftarrow at the same time, and run twice as fast left.
		//    Oops.
		// 
		// 2) This way we save the state of the input so that we can use them in
		//    other methods, e.g. for updating animations. Otherwise we have to
		//    pass the inputs to the animation update, which doesn't make sense
		//    non-inputable characters.
		if (inputLeft) {
			// Midair steering!
			a.x -= MOVE_ACCEL;
			facingLeft = true;
		}
		if (inputRight) {
			// Midair steering
			a.x += MOVE_ACCEL;
			facingLeft = false;
		}
		if (inputUp) {
			// Maybe jump.
			if (getOnGround()) {	
				a.y += JUMP_ACCEL;
				// action SHOULD be update later (fingers crossed)
				//setAction(Action.JUMP);
			}
		}
	}
	
	// PHYSICSABLE
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
	public boolean getGoingLeft() {
		return inputLeft;
	}

	@Override
	public boolean getGoingRight() {
		return inputRight;
	}
}
