package com.mortrag.ut.wasabi.characters;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Hero {
	
	public static final float MOVE_SPEED = 50.0f;
	
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
	private float timeSinceActionStart;
	public Vector2 position;

	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------
	public Hero() {
		this(0.0f, 0.0f);
	}
	
	public Hero(float x, float y) {
		animations = new HashMap<Action, Animation>();
		position = new Vector2(x, y);
		curAction = Action.IDLE;
		timeSinceActionStart = 0.0f;
	}
	
	// ---------------------------------------------------------------------------------------------
	// API
	// ---------------------------------------------------------------------------------------------
	
	public void idle() {
		setAction(Action.IDLE);
	}
	
	public void moveRight() {
		setAction(Action.RUN);
		position.add(MOVE_SPEED, 0.0f);
	}
	
	public void moveLeft() {
		setAction(Action.RUN);
		position.add(-MOVE_SPEED, 0.0f);
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
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
		batch.draw(animations.get(curAction).getKeyFrame(timeSinceActionStart), getX(), getY());
		batch.end();
	}
	
}
