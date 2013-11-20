package com.mortrag.ut.wasabi.testchamber;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.Map;
import com.mortrag.ut.wasabi.map.WasabiMapRenderer;
import com.mortrag.ut.wasabi.util.Debug;
import com.mortrag.ut.wasabi.util.Pair;

public class TestChamber_MapRenderer extends WasabiMapRenderer {

	public TestChamber_MapRenderer(Map map, SpriteBatch spriteBatch, ShapeRenderer sr) {
		super(map, spriteBatch, sr);
	}

	@Override
	public void render(int[] layers) {
		Pair<Integer, Integer> renderedAndCount = super.renderAndCount(layers);
		Debug.debugLine("Rendered " + renderedAndCount.first + " / " + renderedAndCount.second +
				" objects");
	}
}
