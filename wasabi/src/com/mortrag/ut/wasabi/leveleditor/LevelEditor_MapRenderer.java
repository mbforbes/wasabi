package com.mortrag.ut.wasabi.leveleditor;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.Map;
import com.mortrag.ut.wasabi.map.WasabiMapRenderer;

public class LevelEditor_MapRenderer extends WasabiMapRenderer {

	public LevelEditor_MapRenderer(Map map, SpriteBatch spriteBatch, ShapeRenderer sr) {
		super(map, spriteBatch, sr);
	}

}
