package com.mortrag.ut.wasabi.characters;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Pair;

public abstract class WasabiCharacter implements Renderable, Advectable, Collidable {
	// ---------------------------------------------------------------------------------------------	
	// FIELDS
	// ---------------------------------------------------------------------------------------------
	
	// public
	public Map<Action, Animation> animations;
	public static enum Action {
		NONE, //DEBUG--should never be seen; used during construction to force loading of idle w/h
		IDLE,
		RUN,
		JUMP,
		FALL,
		ATTACK;
	}
	
	// protected state
	protected Vector2 p, v, a; // position, velocity, acceleration
	protected float timeSinceActionStart, w, h;	
	protected Action curAction;
	protected boolean facingLeft, collides, onGround, onGroundPrev;
	protected BoundingBox boundingBox, prevBoundingBox; 	
	protected Map<Action, Pair<Integer, Integer>> actionSizes;	
	
	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------
	
	public WasabiCharacter(float x, float y) {
		
		// creation
		animations = new HashMap<Action, Animation>();
		actionSizes = new HashMap<Action, Pair<Integer, Integer>>();
		p = new Vector2(x, y);
		v = new Vector2(0.0f, 0.0f);
		a = new Vector2(0.0f, 0.0f);	
		boundingBox = new BoundingBox(new Vector3(), new Vector3());
		prevBoundingBox = new BoundingBox(new Vector3(), new Vector3());		
		
		// defaults
		curAction = Action.NONE; // temp--this removed before constructor end
		timeSinceActionStart = 0.0f;
		collides = true; // ghost mode off by default
	}
	
	// ---------------------------------------------------------------------------------------------
	// API
	// ---------------------------------------------------------------------------------------------
		
	
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
	
	public float getW() {
		return w;
	}
	
	public float getH() {
		return h;
	}
	
	public void setW(float newW) {
		w = newW;
	}
	
	public void setH(float newH) {
		h = newH;
	}
	
	public boolean getFacingLeft() {
		return facingLeft;	
	}

	public void setFacingLeft(boolean facingLeft) {
		this.facingLeft = facingLeft;
	}
	
	public abstract boolean getGoingLeft();
	public abstract boolean getGoingRight();
	
	
	@Override
	public void render(SpriteBatch batch, float delta) {
		// TODO Auto-generated method stub
		timeSinceActionStart += delta;
		batch.begin();
		TextureRegion keyFrame = animations.get(curAction).getKeyFrame(timeSinceActionStart);		
		keyFrame.flip(keyFrame.isFlipX() != facingLeft, false);
		
		if (keyFrame instanceof AtlasSprite) {
			AtlasSprite spriteKeyFrame = (AtlasSprite) keyFrame;
			spriteKeyFrame.setPosition(getP().x, getP().y);
			((AtlasSprite) keyFrame).draw(batch);
		} else {
			batch.draw(keyFrame, getP().x, getP().y);
		}
		batch.end();
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
	public BoundingBox getBoundingBox() {
		// Avoiding creating new Vector3's each time by mucking about the internal state and then
		// letting it clean up.
		boundingBox.min.set(getP().x, getP().y, 0.0f);
		boundingBox.max.set(getP().x + w, getP().y + h, Constants.BB_ZMAX);
		boundingBox.set(boundingBox.min, boundingBox.max);
		return boundingBox;
	}

	@Override
	public BoundingBox getPrevBoundingBox() {
		return prevBoundingBox;
	}

	@Override
	public void setPrevBoundingBox(BoundingBox b) {
		prevBoundingBox.set(b);
	}	
	
	@Override
	public void maybeUpdateAnimations() {
		// Jump when on ground and starting to move up
		if (!getOnGround() && getV().y > 0.0f) {
			setAction(Action.JUMP);
		}
		
		// Start falling action when falling!
		if (!getOnGround() && getV().y < 0.0f) {
			setAction(Action.FALL);
		}
		
		// Stop falling action when hits ground
		if (getOnGround() && curAction == Action.FALL) {
			setAction(Action.IDLE);
		}
		
		// If on ground and not pressing exactly one of {LEFT, RIGHT}, make idle.
		if (getOnGround() && !(this.getGoingLeft() ^ this.getGoingRight()) && getV().y == 0.0f) {
			setAction(Action.IDLE);
		}		
		
		// Running is moving
		if (getOnGround() && (this.getGoingLeft() ^ this.getGoingRight())) {
			setAction(Action.RUN);
		}
	}	
	
	public void setAction(Action action) {
		if (curAction != action && animations.containsKey(action)) {
			curAction = action;
			timeSinceActionStart = 0.0f;
			
			// auto set width!
			if (actionSizes.containsKey(action)) {
				Pair<Integer, Integer> size = actionSizes.get(action);
				setW((float) size.first);
				setH((float) size.second);
			} else {
				TextureRegion frame = animations.get(curAction).getKeyFrame(timeSinceActionStart);
				if (frame instanceof AtlasSprite) {
					// If we use getRegion* on a packed (whitespace stripped) sprite, then we get
					// the compressed width, when we want the full one (as all frames of an
					// animation are set to the same W and H for consistent bounding boxes and
					// animation).
					AtlasSprite atlasSpriteFrame = (AtlasSprite) frame;
					setW(atlasSpriteFrame.getWidth());
					setH(atlasSpriteFrame.getHeight());
				} else {
					setW(frame.getRegionWidth());
					setH(frame.getRegionHeight());
				}				
				actionSizes.put(action, new Pair<Integer, Integer>((int) getW(), (int) getH()));
			}
		}
	}	

}
