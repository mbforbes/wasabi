package com.mortrag.ut.wasabi.leveleditor;

import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.map.AnimatedMapObject;
import com.mortrag.ut.wasabi.map.Initializable.LayerInitializable;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.map.WasabiAnimation;
import com.mortrag.ut.wasabi.map.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Debug;

/**
 * Should contain only extra variables useful for the LevelEditor to use internally---no state that
 * needs to be kept when saving/loading. 
 * 
 * @author max
 */
public class LevelEditor_MapLayer extends MapLayer implements LayerInitializable {
	
	// We do use this for some state...
	private int curItemIdx = 0; // this indexes into animations or regions
	
	private transient Array<WasabiAnimation> animations; // one of these arrays is null
	private transient Array<AtlasRegion> regions; // one of these arrays is null	
	private transient float time = 0.0f; // updated as clock ticks to render animations 
	private transient boolean initialized = false;
	private transient Vector2 cachedWHCalculator = new Vector2();
	
	
	/**
	 * For object (BG, FG, ...) layers.
	 * @param regionPrefix
	 * @param regions
	 */
	public LevelEditor_MapLayer(String regionPrefix, Array<AtlasRegion> regions) {
		super();
		this.getProperties().put(Constants.FD.REGION_PREFIX, regionPrefix);
		this.regions = regions;
		initialized = true;
	}
	
	/**
	 * For character (hero, enemy, ...) layers.
	 * @param animations
	 */
	public LevelEditor_MapLayer(Array<WasabiAnimation> wasabiAnimations) {
		super();
		this.getProperties().put(Constants.FD.REGION_PREFIX, Constants.FD.CHAR_PREFIX);
		this.animations = wasabiAnimations;
		initialized = true;
	}
	
	/**
	 * Making an interface because of bad data hiding before. (Read: generalizing for animations) 
	 * @return
	 */
	public float getCurItemWidth() {
		updateCurItemCachedWH();
		return cachedWHCalculator.x;
	}
	
	private void updateCurItemCachedWH() {
		TextureRegion curItem = null;
		if (this.animations != null) {
			// animations
			curItem = animations.get(curItemIdx).getAnimation().getKeyFrame(time);
		} else {
			curItem = regions.get(curItemIdx);
		}
		Common.getTextureWH(curItem, cachedWHCalculator);		
	}
	
	/**
	 * Making an interface because of bad data hiding before. (Read: generalizing for animations) 
	 * @return
	 */
	public float getCurItemHeight() {
		updateCurItemCachedWH();
		return cachedWHCalculator.y;
	}	

	/**
	 * Changes the texture/animation on the last object in the layer, which is there but is
	 * 'unplaced'.
	 */
	public void nextSprite() {
		if (animations != null) {
			// animations
			curItemIdx = curItemIdx == animations.size - 1 ? 0 : curItemIdx + 1;
		} else {
			// regions
			curItemIdx = curItemIdx == regions.size - 1 ? 0 : curItemIdx + 1;
		}
		updateLastObject();
	}
	
	/**
	 * Changes the texture/animation on the last object in the layer, which is there but is
	 * 'unplaced'.
	 */
	public void prevSprite() {
		if (animations != null) {
			// animations
			curItemIdx = curItemIdx == 0 ? animations.size - 1 : curItemIdx - 1;
		} else {
			// regions
			curItemIdx = curItemIdx == 0 ? regions.size - 1 : curItemIdx - 1;
		}
		updateLastObject();
	}	
	
	public void tick(float delta) {
		time += delta;
	}
	
	public float getTime() {
		return time;
	}
	
	/**
	 * Updates the animation or textureRegion of the final (currently possibly placed) object in
	 * this layer.z
	 */
	private void updateLastObject() {
		if (animations != null) {
			// animations
			AnimatedMapObject curObj = (AnimatedMapObject) this.getObjects().get(this.getObjects().
					getCount() - 1); // get last
			curObj.changeObject(animations.get(curItemIdx));
		} else {
			// regions
			TextureMapObject curObj = (TextureMapObject) this.getObjects().get(this.getObjects().
					getCount() - 1); // get last
			curObj.setTextureRegion(regions.get(curItemIdx));
		}		
	}
	
	/**
	 * "Places" the current object and adds the next object to be moved around.
	 * @param x
	 * @param y
	 */
	public void addNextObj(float x, float y) {
		// See if we're changing the spawn point. We allow 'two' spawn points, technically, with the
		// rule that the last one must be the temp current object that's being moved around.
		if (this.getProperties().get(Constants.MP.LAYER_TYPE, Constants.MP.LayerType.class) ==
			Constants.MP.LayerType.CHARACTERS &&
			animations.get(curItemIdx).getObjectName().equals(Constants.ON.SPAWN_POINT)) {
			
			// count how many spawn points so far
			AnimatedMapObject spawnPointCached = null;
			int numSpawnPoints = 0;
			for (int i = 0; i < getObjects().getCount(); i++) {
				MapObject curObj = getObjects().get(i);
				if (curObj.getName().equals(Constants.ON.SPAWN_POINT)) {
					numSpawnPoints++;
					if (spawnPointCached == null) {
						spawnPointCached = (AnimatedMapObject) curObj;
					}
				}
			}
			
			// See if we already have the two spawn points
			if (numSpawnPoints >= 2) {
				spawnPointCached.setX(x);
				spawnPointCached.setY(y);
				return;				
			}
		}
		
		// calculate w, h
		float w, h;
		TextureRegion frame;
		if (animations != null) {
			// animations
			frame = animations.get(curItemIdx).getAnimation().getKeyFrame(0.0f);
		} else {
			// regions
			frame = regions.get(curItemIdx);
		}
		Common.getTextureWH(frame, cachedWHCalculator);
		w = cachedWHCalculator.x;
		h = cachedWHCalculator.y;
		
		if (animations != null) {
			// animations
			this.getObjects().add(new AnimatedMapObject(animations.get(curItemIdx), x, y, w, h));
		} else {
			// regions
			AtlasRegion tex = regions.get(curItemIdx);
			this.getObjects().add(new WasabiTextureMapObject(tex, tex.name, x, y, w, h));
		}		
		
	}
	
	/**
	 * Does involve object deletion, so call only when going to level editor / saving.
	 */
	public void removeLastObj() {
		this.getObjects().remove(this.getObjects().getCount() - 1);
	}
	
	/**
	 * Call when switching away from layer; sets 'next to place' object visible off.
	 */
	public void inactive() {
		this.getObjects().get(this.getObjects().getCount() - 1).setVisible(false);
	}
	
	/**
	 * Call when switching away from layer; sets 'next to place' object visible off.
	 */
	public void active() {
		this.getObjects().get(this.getObjects().getCount() - 1).setVisible(true);
	}
	
	public void changeCurPos(Vector2 curSpritePos) {
		// This might be called in-between switching from the level editor to the test chamber
		// (somehow...) and if there aren't objects in the active layer, this will make it crash.
		// Doing this check first should (hopefully) fix that.
		if (this.getObjects().getCount() == 0) {
			return;
		}
		
		if (animations != null) {
			// animations
			AnimatedMapObject curObj = (AnimatedMapObject) this.getObjects().get(
					this.getObjects().getCount() - 1);
			curObj.setX(curSpritePos.x);
			curObj.setY(curSpritePos.y);
		} else {
			// regions
			WasabiTextureMapObject curObj = (WasabiTextureMapObject) this.getObjects().get(
					this.getObjects().getCount() - 1);
			curObj.setX(curSpritePos.x);
			curObj.setY(curSpritePos.y);			
			
		}
	}
	
	
	// begin KRYO-specific
	// --------------------------------------------------------------------------------------------
	
	/**
	 * No-arg constructor for kryo.
	 */
	public LevelEditor_MapLayer() {
	}
	
	public void initialize(TextureAtlas atlas, Map<String, Array<AtlasRegion>> regionMap,
			Array<WasabiAnimation> wasabiAnimations) {
		if (initialized) {
			return;
		}
		
		if (this.getProperties().get(Constants.MP.LAYER_TYPE, Constants.MP.LayerType.class) ==
				Constants.MP.LayerType.CHARACTERS) {
			// Associate this layer with the characters
			this.animations = wasabiAnimations;
			
		} else {
			// Associate this layer with its regions
			this.regions = regionMap.get(this.getProperties().get(Constants.FD.REGION_PREFIX,
					String.class));
		}
		
		
		// Initialize all of the objects (load their textures)
		Iterator<MapObject> oit = this.getObjects().iterator();
		while (oit.hasNext()) {
			MapObject object = oit.next();
			if (object instanceof ObjectInitializable) {
				((ObjectInitializable) object).initialize(atlas);
			}
		}
		
		initialized = true;
	}
	
	
	// end KRYO-specific
	// --------------------------------------------------------------------------------------------	
}
