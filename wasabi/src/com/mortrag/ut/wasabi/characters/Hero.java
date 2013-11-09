package com.mortrag.ut.wasabi.characters;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Pair;

public class Hero implements Inputable, Collidable, Physicsable, Advectable {
	
	public static final float MOVE_ACCEL = 5000.0f;
	public static final float JUMP_ACCEL = 80000.0f;
	
	public static enum Action {
		NONE, //DEBUG--should never be seen; used during construction to force loading of idle w/h
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
	private Map<Action, Pair<Integer, Integer>> actionSizes;
	private Vector2 p, v, a; // position, velocity, acceleration
	public boolean onGround, collides, facingLeft;
	private BoundingBox boundingBox; 

	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------

	public Hero(float x, float y, TextureAtlas atlas) {
		animations = new HashMap<Action, Animation>();
		curAction = Action.NONE; // temp--this removed before constructor end
		timeSinceActionStart = 0.0f;
		addAnimationsToHero(atlas);
		
		p = new Vector2(x, y);
		v = new Vector2(0.0f, 0.0f);
		a = new Vector2(0.0f, 0.0f);
		collides = true; // ghost mode off, lol.
		
		actionSizes = new HashMap<Action, Pair<Integer, Integer>>();
		setAction(Action.IDLE);
		boundingBox = new BoundingBox(new Vector3(), new Vector3());
	}
	
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
	
	public void idle() {
		setAction(Action.IDLE);
	}
	
	public void setAction(Action action) {
		if (curAction != action) {
			curAction = action;
			timeSinceActionStart = 0.0f;
			
			// auto set width!
			if (actionSizes.containsKey(action)) {
				Pair<Integer, Integer> size = actionSizes.get(action);
				w = (float) size.first;
				h = (float) size.second;
			} else {
				TextureRegion frame = animations.get(curAction).getKeyFrame(timeSinceActionStart);
				w = frame.getRegionWidth();
				h = frame.getRegionHeight();
				actionSizes.put(action, new Pair<Integer, Integer>((int) w, (int) h));
			}
		}		
	}
	
	public void render(SpriteBatch batch, float delta) {
		timeSinceActionStart += delta;
		batch.begin();
		TextureRegion keyFrame = animations.get(curAction).getKeyFrame(timeSinceActionStart);		
		keyFrame.flip(keyFrame.isFlipX() != facingLeft, false);
		
		if (keyFrame instanceof AtlasSprite) {
			AtlasSprite spriteKeyFrame = (AtlasSprite) keyFrame;
			spriteKeyFrame.setPosition(p.x, p.y);
			((AtlasSprite) keyFrame).draw(batch);
		} else {
			batch.draw(keyFrame,p.x, p.y);
		}
		batch.end();
	}

	@Override
	public void input(Input i) {
		switch(i) {
		case LEFT:
			// Midair steering!
			a.x -= MOVE_ACCEL;
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

	@Override
	public BoundingBox getBoundingBox() {
		// Avoiding creating new Vector3's each time by mucking about the internal state and then
		// letting it clean up.
		boundingBox.min.set(p.x, p.y, 0.0f);
		boundingBox.max.set(p.x + w, p.y + h, Constants.BB_ZMAX);
		boundingBox.set(boundingBox.min, boundingBox.max);
		return boundingBox;
	}
}
