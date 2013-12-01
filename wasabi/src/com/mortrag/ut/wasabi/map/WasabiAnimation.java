package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class WasabiAnimation {
	
	private String framePrefix, objectName;
	private float frameDuration;
	private int animationType;
	
	// Non-Kryo'd for loading
	private transient Animation animation;
	
	public WasabiAnimation(Array<? extends TextureRegion> frames, String framePrefix,
			float frameDuration, int animationType, String objectName) {
		this.animation = new Animation(frameDuration, frames, animationType);
		this.framePrefix = framePrefix;
		this.frameDuration = frameDuration;
		this.animationType = animationType;
		this.objectName = objectName;
	}
	
	/**
	 * For Kryo.
	 */
	public WasabiAnimation() {		
	}
	
	public Animation getAnimation() {
		return animation;
	}
	
	public void setAnimation(Animation animation) {
		this.animation = animation;
	}
	
	public String getFramePrefix() {
		return framePrefix;
	}
	
	public float getFrameDuration() {
		return frameDuration;
	}
	
	public int getAnimationType() {
		return animationType;
	}
	
	public String getObjectName() {
		return objectName;
	}
}
