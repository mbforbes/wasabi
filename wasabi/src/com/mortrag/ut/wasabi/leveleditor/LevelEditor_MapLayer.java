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
import com.mortrag.ut.wasabi.map.AnimatedWasabiMapObject;
import com.mortrag.ut.wasabi.map.Initializable.LayerInitializable;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.map.MapObjectPrimitive;
import com.mortrag.ut.wasabi.map.WasabiAnimation;
import com.mortrag.ut.wasabi.map.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Constants.MP.ObjectType;
import com.mortrag.ut.wasabi.util.Constants.MapObjectPrimitiveType;
import com.mortrag.ut.wasabi.util.Debug;

/**
 * Should contain only extra variables useful for the LevelEditor to use internally---no state that
 * needs to be kept when saving/loading. 
 * 
 * @author max
 */
public class LevelEditor_MapLayer extends MapLayer implements LayerInitializable {
	
	private transient Array<MapObjectPrimitive> primitives; // one of these arrays is null	
	private transient float time = 0.0f; // updated as clock ticks to render animations 
	private transient boolean initialized = false;
	private transient Vector2 cachedWHCalculator = new Vector2();
	private transient int curItemIdx = 0; // this indexes into animations or regions
	
	/**
	 * For object (BG, FG, ...) layers.
	 * @param regionPrefix
	 * @param regions
	 */
	public LevelEditor_MapLayer(MapObjectPrimitiveType type) {
		super();
		this.getProperties().put(Constants.MAP_OBJECT_PRIMITIVE_TYPE, type);
	}
	
	
	/**
	 * Making an interface because of bad data hiding before. (Read: generalizing for animations) 
	 * @return
	 */
	public float getCurItemWidth() {
		return this.primitives.get(curItemIdx).getWidth();
	}

	/**
	 * Making an interface because of bad data hiding before. (Read: generalizing for animations) 
	 * @return
	 */
	public float getCurItemHeight() {
		return this.primitives.get(curItemIdx).getHeight();
	}	

	/**
	 * Changes the texture/animation on the last object in the layer, which is there but is
	 * 'unplaced'.
	 */
	public void nextSprite() {
		curItemIdx = curItemIdx == primitives.size - 1 ? 0 : curItemIdx + 1;
	}
	
	/**
	 * Changes the texture/animation on the last object in the layer, which is there but is
	 * 'unplaced'.
	 */
	public void prevSprite() {
		curItemIdx = curItemIdx == 0 ? primitives.size - 1 : curItemIdx - 1;
	}	
	
	public void tick(float delta) {
		time += delta;
	}
	
	public float getTime() {
		return time;
	}
	
	/**
	 * "Places" the current object and adds the next object to be moved around.
	 * @param x
	 * @param y
	 */
	public void addNextObj(float x, float y) {
		// TODO how to get correct type?
		this.getObjects().add(primitives.get(curItemIdx).getWasabiMapObject(ObjectType.NORMAL, x, y));
	}
	
	
	// begin KRYO-specific
	// --------------------------------------------------------------------------------------------
	
	/**
	 * No-arg constructor for kryo.
	 */
	public LevelEditor_MapLayer() {
	}
	
	public void initialize(Map<MapObjectPrimitiveType, Array<MapObjectPrimitive>> primitiveMap) {
		if (initialized) {
			return;
		}
		
		// associate with primitives
		this.primitives = primitiveMap.get(this.getProperties().get(
				Constants.MAP_OBJECT_PRIMITIVE_TYPE, MapObjectPrimitiveType.class));
		
		// Initialize all of the objects (load from their primitives)
		Iterator<MapObject> oit = this.getObjects().iterator();
		while (oit.hasNext()) {
			MapObject object = oit.next();
			if (object instanceof ObjectInitializable) {
				((ObjectInitializable) object).initialize(primitives);
			}
		}
		
		initialized = true;
	}
	
	
	// end KRYO-specific
	// --------------------------------------------------------------------------------------------	
}
