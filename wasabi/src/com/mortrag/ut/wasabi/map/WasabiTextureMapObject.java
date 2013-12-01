package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.util.Constants;

public class WasabiTextureMapObject extends TextureMapObject implements ObjectInitializable, WasabiMapObject {
	private float width;
	private float height;
	private BoundingBox boundingBox;
	private String regionName;
	private boolean bbdirty;
	
	private transient Vector2 cachedWHComp = new Vector2();
	
	// for kryo
	private transient boolean initialized = false;
	
	
	public WasabiTextureMapObject(TextureRegion textureRegion, String regionName, float x, float y,
			float width, float height) {
		super(textureRegion);		
		setX(x);
		setY(y);
		setWidth(width);
		setHeight(height);
		setRegionName(regionName);
		boundingBox = new BoundingBox(new Vector3(x, y, 0.0f),
				new Vector3(x + width, y + height, Constants.BB_ZMAX));
		bbdirty = false;
		initialized = true;
	}
	
	// begin KRYO-specific
	// --------------------------------------------------------------------------------------------
	/**
	 * No-arg constructor for kryo
	 */
	public WasabiTextureMapObject() {
		super(); // this already happens--just doing it explicitly as a reminder.
		bbdirty = true;
	}
	
	/**
	 * Call after kryo'd.
	 * @param atlas
	 */
	public void initialize(TextureAtlas atlas) {
		if (initialized) {
			return;
		}
		
		// Check kind of redundant because of initialized check, but just keeping pattern.
		if (this.getTextureRegion() == null) {
			this.setTextureRegion(atlas.findRegion(regionName));
		}
		
		initialized = true;
	}
	
	// end KRYO-specific
	// --------------------------------------------------------------------------------------------	
	
	public float getWidth() {
		return this.width;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public String getRegionName() {
		return this.regionName;
	}
	
	/**
	 * Note: have to be careful, as must recompute every time one of the x,y or dimensions of this
	 * sprite changes... we're storing it now (and you're retrieving it) under the assumption that
	 * it is not changing because it's part of the map.
	 * @return the original, non-updated BoundingBox
	 */
	public BoundingBox getBoundingBox() {
		if (bbdirty) {
			float x = this.getX(), y = this.getY();
			boundingBox.min.set(x, y, 0.0f);
			boundingBox.max.set(x + width, y + height, Constants.BB_ZMAX);
			boundingBox.set(boundingBox.min, boundingBox.max);
		}
		return boundingBox;
	}
	
	public void setWidth(float width) {
		if (this.width != width) {
			this.width = width;
			bbdirty = true;
		}
	}
	
	public void setHeight(float height) {
		if (this.height != height) {
			this.height = height;
			bbdirty = true;			
		}
	}
	
	@Override
	public void setX(float x) {
		super.setX(x);
		bbdirty = true;
	}
	
	@Override
	public void setY(float y) {
		super.setY(y);
		bbdirty = true;
	}
	
	@Override
	public void setTextureRegion(TextureRegion tex) {
		super.setTextureRegion(tex);
		Common.getTextureWH(tex, cachedWHComp);
		this.setWidth(cachedWHComp.x);
		this.setHeight(cachedWHComp.y);
		bbdirty = true;
	}
	
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
}
