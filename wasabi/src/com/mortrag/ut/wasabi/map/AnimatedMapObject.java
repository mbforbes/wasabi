package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.util.Debug;

public class AnimatedMapObject extends MapObject implements ObjectInitializable, WasabiMapObject {
	private WasabiAnimation wasabiAnimation;
	private float x, y, w, h;
	
	// Non-Kryo'd	
	private transient Vector2 cachedWHCalculator = new Vector2();
	private transient boolean initialized = false;
	
	public AnimatedMapObject(WasabiAnimation wasabiAnimation, float x, float y,
			float w, float h) {
		super(); // explicit
		this.wasabiAnimation = wasabiAnimation;
		this.setName(wasabiAnimation.getObjectName());
		this.setX(x);
		this.setY(y);
		// TODO setters for w/h? Will these need bounding boxes??? (likely...)
		this.w = w;
		this.h = h;
	}

	/**
	 * For Kryo.
	 */
	public AnimatedMapObject() {
		
	}
	
	/**
	 * Call after kryo'd.
	 * @param atlas
	 */
	@Override
	public void initialize(TextureAtlas atlas) {
		if (initialized) {
			return;
		}
		
		// Check kind of redundant because of initialized check, but just keeping pattern.
		if (wasabiAnimation.getAnimation() == null) {
			Array<TextureRegion> frames = Common.getFrames(atlas, wasabiAnimation.getFramePrefix());
			wasabiAnimation.setAnimation(new Animation(wasabiAnimation.getFrameDuration(), frames,
					wasabiAnimation.getAnimationType()));
		}
		
		initialized = true;
	}	
	
	public TextureRegion getCurFrame(float time) {
		return wasabiAnimation.getAnimation().getKeyFrame(time);
	}	
	
	/**
	 * Here we have to change ALL the state (e.g. the name) that makes the underlying MapObject
	 * identifiable.
	 * @param newGuts
	 */
	public void changeObject(WasabiAnimation newGuts) {
		this.wasabiAnimation = newGuts;
		setName(newGuts.getObjectName());
		updateWH();
	}
	
	private void updateWH() {		
		Common.getTextureWH(wasabiAnimation.getAnimation().getKeyFrame(0.0f), cachedWHCalculator);
		this.w = cachedWHCalculator.x;
		this.h = cachedWHCalculator.y;
	}
	
	public void setX(float x) {
		this.x = x;
	}

	public void setY(float y) {
		this.y = y;
	}

	@Override
	public float getX() {
		return x;
	}

	@Override
	public float getY() {
		return y;
	}

	@Override
	public float getWidth() {
		return w;
	}

	@Override
	public float getHeight() {
		return h;
	}
}
