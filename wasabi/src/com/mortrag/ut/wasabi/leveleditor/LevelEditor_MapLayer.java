package com.mortrag.ut.wasabi.leveleditor;

import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.map.Initializable.LayerInitializable;
import com.mortrag.ut.wasabi.map.Initializable.ObjectInitializable;
import com.mortrag.ut.wasabi.map.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.util.Constants;

/**
 * Should contain only extra variables useful for the LevelEditor to use internally---no state that
 * needs to be kept when saving/loading. 
 * 
 * @author max
 */
public class LevelEditor_MapLayer extends MapLayer implements LayerInitializable {	
	public transient Array<AtlasRegion> regions;
	public transient int curRegionIdx = 0;
	private transient boolean initialized = false;
	
	public LevelEditor_MapLayer(String regionPrefix, Array<AtlasRegion> regions) {
		super();
		this.getProperties().put(Constants.FD.REGION_PREFIX, regionPrefix);
		this.regions = regions;
		initialized = true;
	}
	
	// begin KRYO-specific
	// --------------------------------------------------------------------------------------------
	
	/**
	 * No-arg constructor for kryo.
	 */
	public LevelEditor_MapLayer() {
	}
	
	public void initialize(TextureAtlas atlas, Map<String, Array<AtlasRegion>> regionMap) {
		if (initialized) {
			return;
		}
		
		// Associate this layer with its regions
		this.regions = regionMap.get(this.getProperties().get(Constants.FD.REGION_PREFIX,
				String.class));
		
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
