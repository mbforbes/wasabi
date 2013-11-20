package com.mortrag.ut.wasabi.map;

import java.util.Iterator;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.map.Initializable.LayerInitializable;

public class WasabiMap extends Map {
	
	public void initialize(TextureAtlas atlas, java.util.Map<String, Array<AtlasRegion>> regionMap) {
		Iterator<MapLayer> lit = this.getLayers().iterator();
		while (lit.hasNext()) {
			MapLayer layer = lit.next();
			if (layer instanceof LayerInitializable) {
				((LayerInitializable) layer).initialize(atlas, regionMap);
			}
		}
	}
}
