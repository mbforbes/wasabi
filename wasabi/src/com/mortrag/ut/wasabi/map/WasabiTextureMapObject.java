package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.map.MapObjectPrimitive.TextureMapObjectPrimitive;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Constants.MP.ObjectType;

public class WasabiTextureMapObject extends WasabiMapObject {
	
	// Non-Kryo'd
	private transient TextureRegion	textureRegion;
	private transient boolean initialized = false; 	// for kryo
	
	/**
	 * Hint: pre:
	 * 	Common.getTextureWH(textureRegion, cachedWHComp);
	 *	this.width = cachedWHComp.x;
	 *	this.height = cachedWHComp.y;
	 *
	 * @param textureRegion
	 * @param textureName for loading later
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public WasabiTextureMapObject(TextureRegion textureRegion, String textureName, float x, float y,
			float w, float h) {
		super(x,y,w,h);
		this.textureRegion = textureRegion;
		this.getProperties().put(Constants.MP.OBJ_TEXTURE_NAME, textureName);
		this.getProperties().put(Constants.MP.OBJECT_TYPE, Constants.MP.ObjectType.NORMAL);
		initialized = true;
	}
	
	// begin KRYO-specific
	// --------------------------------------------------------------------------------------------
	/**
	 * No-arg constructor for kryo
	 */
	public WasabiTextureMapObject() {
		super(); // this already happens--just doing it explicitly as a reminder.
		this.getProperties().put(Constants.MP.OBJECT_TYPE, Constants.MP.ObjectType.NORMAL);
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
		if (this.textureRegion == null) {
			String texName = this.getProperties().get(Constants.MP.OBJ_TEXTURE_NAME, String.class);			
			for (int i = 0; i < primitives.size; i++) {
				MapObjectPrimitive prim = primitives.get(i);
				// TODO how to do this generically?
				if (prim instanceof TextureMapObjectPrimitive) {
					TextureMapObjectPrimitive primTex = (TextureMapObjectPrimitive) prim;
					if (primTex.getTextureName().equals(texName)) {
						this.textureRegion = primTex.getTextureRegion();
						initialized = true;
						return;
					}
				}
			}
		}
	}
	
	// end KRYO-specific
	// --------------------------------------------------------------------------------------------	

	
//	public void setTextureRegion(TextureRegion tex) {
//		this.textureRegion = tex;
//		Common.getTextureWH(tex, cachedWHComp);
//		this.setWidth(cachedWHComp.x);
//		this.setHeight(cachedWHComp.y);
//		bbdirty = true;
//	}
	
	public void renderEditor(SpriteBatch batch) {
		batch.draw(textureRegion, getX(), getY(), getWidth(), getHeight());		
	}

	/**
	 * Note: assumes batch.begin() has been called, and batch.end() will be called.
	 * @param batch
	 */
	public void renderGame(SpriteBatch batch) {
		batch.draw(textureRegion, getX(), getY(), getWidth(), getHeight());
	}
}
