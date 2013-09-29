package com.mortrag.ut.wasabi.characters;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Hero implements Inputable, Collidable, Physicsable, Advectable {
	
	public static final float MOVE_ACCEL = 4000.0f;
	public static final float JUMP_ACCEL = 80000.0f;
	
	public static enum Action {
		IDLE,
		RUN,
		JUMP,
		FALL,
		ATTACK;
	}
	
	// ---------------------------------------------------------------------------------------------
	// FIELDS
	// ---------------------------------------------------------------------------------------------
	public Map<Action, Animation> animations;
	
	// state
	private Action curAction;
	private float timeSinceActionStart, w, h;
	private Vector2 p, v, a; // position, velocity, acceleration
	public boolean onGround, collides, facingLeft;

	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------
	public Hero(float w, float h) {
		this(0.0f, 0.0f, w, h);
	}
	
	public Hero(float x, float y, float w, float h) {
		animations = new HashMap<Action, Animation>();
		curAction = Action.IDLE;
		timeSinceActionStart = 0.0f;
		
		p = new Vector2(x, y);
		v = new Vector2(0.0f, 0.0f);
		a = new Vector2(0.0f, 0.0f);
		collides = true; // ghost mode off, lol.
		this.w = w;
		this.h = h;
	}
	
	// ---------------------------------------------------------------------------------------------
	// API
	// ---------------------------------------------------------------------------------------------
	
	public void idle() {
		setAction(Action.IDLE);
	}
	
	public void setAction(Action action) {
		if (curAction != action) {
			curAction = action;
			timeSinceActionStart = 0.0f;
		}		
	}
	
	public void render(SpriteBatch batch, float delta) {
		timeSinceActionStart += delta;
		batch.begin();
		TextureRegion keyFrame = animations.get(curAction).getKeyFrame(timeSinceActionStart);		
		keyFrame.flip(keyFrame.isFlipX() != facingLeft, false);
		batch.draw(keyFrame,p.x, p.y);
		batch.end();
	}

	@Override
	public void input(Input i) {
		switch(i) {
		case LEFT:
			// Midair steering!
			a.x -= MOVE_ACCEL; // TODO(max): Need left animation...
			facingLeft = true;
			if (getOnGround()) {
				setAction(Action.RUN);
			}
			break;
		case RIGHT:
			// Midair steering!
			a.x += MOVE_ACCEL;
			facingLeft = false;
			if (getOnGround()) {
				setAction(Action.RUN);
			}			
			break;
		case UP:
			if (getOnGround()) {	
				a.y += JUMP_ACCEL;
				setAction(Action.JUMP);
			}
			break;
		}
	}

	@Override
	public boolean getOnGround() {
		return onGround;
	}

	@Override
	public void setOnGround(boolean onGround) {
		this.onGround = onGround; 
	}

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
	public boolean collides() {
		return collides;
	}

	@Override
	public Vector2 getP() {
		return p;
	}

	@Override
	public Vector2 getV() {
		return v;
	}

	@Override
	public Vector2 getA() {
		return a;
	}

	@Override
	public float getWidth() {
		return w;
	}

	@Override
	public float getHeight() {
		return h;
	}

	@Override
	public void maybeUpdateAnimations(Array<Inputable.Input> inputs) {
		
		// Start falling action when falling!
		if (!getOnGround() && v.y < 0.0f) {
			setAction(Action.FALL);
		}
		
		// Stop falling action when hits ground
		if (getOnGround() && curAction == Action.FALL) {
				setAction(Action.IDLE);
		}
		
		// If on ground and not pressing exactly one of {LEFT, RIGHT}, make idle.
		boolean pressLeft = inputs.contains(Input.LEFT, true);
		boolean pressRight = inputs.contains(Input.RIGHT, true);
		if (getOnGround() && !(pressLeft ^ pressRight)) {
			setAction(Action.IDLE);
		}
	}
}
