package com.mortrag.ut.wasabi.map;

import java.util.Iterator;

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.leveleditor.LevelEditor_MapLayer;
import com.mortrag.ut.wasabi.map.Initializable.LayerInitializable;
import com.mortrag.ut.wasabi.util.Constants.MapObjectPrimitiveType;

public class WasabiMap extends Map {
	
	public void initialize(java.util.Map<MapObjectPrimitiveType, Array<MapObjectPrimitive>> primitiveMap) {
		Iterator<MapLayer> lit = this.getLayers().iterator();
		while (lit.hasNext()) {
			MapLayer layer = lit.next();
			if (layer instanceof LayerInitializable) {
				((LayerInitializable) layer).initialize(primitiveMap);
			}
		}
	}
	
	/**
	 * Tick all of the layers (if they are LevelEditor_MapLayer's (currently)).
	 * @param delta
	 */
	public void tick(float delta) {
		for (int i = 0; i < this.getLayers().getCount(); i++) {
			MapLayer layer = this.getLayers().get(i);
			if (layer instanceof LevelEditor_MapLayer) {
				((LevelEditor_MapLayer) layer).tick(delta);
			}
		}
	}
}
