package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.map.MapObjectPrimitive.AnimatedMapObjectPrimitive;
import com.mortrag.ut.wasabi.util.Constants;

public class AnimatedWasabiMapObject extends WasabiMapObject {
//	private WasabiAnimation wasabiAnimation;
	
	// Non-Kryo'd
	private transient WasabiAnimation animation;
	private transient boolean initialized = false;
	private transient float time = 0.0f;
	
	public AnimatedWasabiMapObject(WasabiAnimation animation, float x, float y, float w, float h) {
		super(x,y,w,h);
		this.animation = animation;
		this.getProperties().put(Constants.MP.ANIM_FILE_PREFIX, animation.getFramePrefix());
		this.getProperties().put(Constants.MP.ANIM_FRAME_DURATION, animation.getFrameDuration());
		this.getProperties().put(Constants.MP.ANIM_TYPE, animation.getAnimationType());
	}
	
	public void tick(float delta) {
		time += delta;
	}

	/**
	 * For Kryo.
	 */
	public AnimatedWasabiMapObject() {
		
	}
	
	/**
	 * Call after kryo'd.
	 * @param atlas
	 */
	@Override
	public void initialize(Array<MapObjectPrimitive> primitives) {
		if (initialized) {
			return;
		}
		
		// Check kind of redundant because of initialized check, but just keeping pattern.
		if (this.animation == null) {
			String texName = this.getProperties().get(Constants.MP.ANIM_FILE_PREFIX, String.class);			
			for (int i = 0; i < primitives.size; i++) {
				MapObjectPrimitive prim = primitives.get(i);
				// TODO how to do this generically?
				if (prim instanceof AnimatedMapObjectPrimitive) {
					AnimatedMapObjectPrimitive primAnim = (AnimatedMapObjectPrimitive) prim;
					if (primAnim.getFilePrefix().equals(texName)) {
						this.animation = primAnim.getWasabiAnimation();
						initialized = true;
						return;
					}
				}
			}
		}
	}	
	
	public TextureRegion getCurFrame(float time) {
		return animation.getAnimation().getKeyFrame(time);
	}	
	

	@Override
	public void renderEditor(SpriteBatch batch) {
		// TODO Auto-generated method stub
		batch.draw(this.animation.getAnimation().getKeyFrame(time), getX(), getY(), getWidth(), getHeight());
	}

	@Override
	public void renderGame(SpriteBatch batch) {
		batch.draw(this.animation.getAnimation().getKeyFrame(time), getX(), getY(), getWidth(), getHeight());
		// TODO Auto-generated method stub
		
	}
}
