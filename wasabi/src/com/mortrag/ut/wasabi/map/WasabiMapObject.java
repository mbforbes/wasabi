package com.mortrag.ut.wasabi.map;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Constants.MP.ObjectType;

public abstract class WasabiMapObject extends MapObject implements ObjectInitializable {
	// TODO should these all be properties? And just expose it all through a common API? Or would
	// it not be so simple?
	private float x, y, w, h;
	private ObjectType type;
	private boolean collides;
	private BoundingBox boundingBox;
	private boolean bbdirty;
	
	/**
	 * Should call subclassInit(...) before using.
	 */
	public WasabiMapObject() {
	}
	
	public WasabiMapObject(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		boundingBox = new BoundingBox(new Vector3(x, y, 0.0f),
				new Vector3(x + w, y + h, Constants.BB_ZMAX));
		bbdirty = false;	
	}
	
//	/**
//	 * Only subclasses should call this.
//	 * @param x
//	 * @param y
//	 * @param w
//	 * @param h
//	 */
//	public void subclassInit(float x, float y, float w, float h) {
//		this.x = x;
//		this.y = y;
//		this.w = w;
//		this.h = h;
//		boundingBox = new BoundingBox(new Vector3(x, y, 0.0f),
//				new Vector3(x + w, y + h, Constants.BB_ZMAX));
//		bbdirty = false;		
//	}
	
	public float getX() {
		return x;
	}
	public void setX(float newx) {
		if (x != newx) {
			x = newx;
			bbdirty = true;
		}
	}
	public float getY() {
		return y;
	}
	public void setY(float newy) {
		if (y != newy) {
			y = newy;
			bbdirty = true;
		}
	}

	public BoundingBox getBoundingBox() {
		if (bbdirty) {
			float curx = this.getX(), cury = this.getY(), curw = this.getWidth(),
					curh = this.getHeight();
			boundingBox.min.set(curx, cury, 0.0f);
			boundingBox.max.set(curx + curw, cury + curh, Constants.BB_ZMAX);
			boundingBox.set(boundingBox.min, boundingBox.max);
			bbdirty = false;
		}
		return boundingBox;
	}
	
	public float getWidth() {
		return w;
	}
	public float getHeight() {
		return h;
	}
	
	public ObjectType getType() {
		return this.getProperties().get(Constants.MP.OBJECT_TYPE, ObjectType.class);
	}
	
	public boolean getCollides() {
		return this.collides;
	}
	
	public abstract void renderEditor(SpriteBatch batch);
	public abstract void renderGame(SpriteBatch batch);
}
