package com.mortrag.ut.wasabi.testchamber;

import java.util.Iterator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.mortrag.ut.wasabi.graphics.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Debug;

public class TestChamber_MapRenderer implements MapRenderer {

	private int[] layerIdxes;
	
	private Map map;
	private Rectangle viewBounds;
	private float leftedge, rightedge, bottomedge, topedge;
	private SpriteBatch spriteBatch;
	
	public TestChamber_MapRenderer(Map map, SpriteBatch spriteBatch) {
		setMap(map);
		this.spriteBatch = spriteBatch;
		viewBounds = new Rectangle(); 
	}
	
	public void setMap(Map map) {
		this.map = map;
		
		// This is just convenience so we can chain render(...) calls.
		int numLayers = map.getLayers().getCount();
		layerIdxes = new int[numLayers];
		for (int i = 0; i < numLayers; i++) {
			layerIdxes[i] = i;
		}
	}
	
	/**
	 * 
	 * @param mapObject
	 * @return how many objects were rendered
	 */
	private int renderObject(MapObject mapObject) {
		int rendered = 0;
		if (mapObject instanceof WasabiTextureMapObject) {
			WasabiTextureMapObject obj = (WasabiTextureMapObject) mapObject;
			float objx = obj.getX();
			float objy = obj.getY();
			float objw = obj.getWidth();
			float objh = obj.getHeight();
			// Only draw if some part of it will be displayed on the screen.
			if (!(objx + objw < leftedge || objx > rightedge || objy > topedge ||
					objy + objh < bottomedge)) {
				spriteBatch.draw(obj.getTextureRegion(), obj.getX(), obj.getY());
				rendered++;
			}
		} else {
			// Not rendering anything else for now...
		}
		return rendered;
	}
	
	@Override
	public void setView(OrthographicCamera camera) {
		spriteBatch.setProjectionMatrix(camera.combined);
		float width = camera.viewportWidth * camera.zoom;
		float height = camera.viewportHeight * camera.zoom;
		viewBounds.set(camera.position.x - width / 2, camera.position.y - height / 2, width,
				height);
		recomputeEdges();
	}

	@Override
	public void setView(Matrix4 projectionMatrix, float viewboundsX,
			float viewboundsY, float viewboundsWidth, float viewboundsHeight) {
		spriteBatch.setProjectionMatrix(projectionMatrix);
		viewBounds.set(viewboundsX, viewboundsY, viewboundsWidth, viewboundsHeight);
		recomputeEdges();
	}
	
	private void recomputeEdges() {
		leftedge = viewBounds.getX();
		rightedge = leftedge + viewBounds.getWidth();
		bottomedge = viewBounds.getY();
		topedge = bottomedge + viewBounds.getHeight();
	}

	@Override
	public void render() {
		render(layerIdxes);	
	}

	@Override
	public void render(int[] layers) {
		spriteBatch.begin();
		Iterator<MapLayer> lit = map.getLayers().iterator();
		int rendered = 0, total = 0;
		while (lit.hasNext()) {
			MapLayer layer = lit.next();
			if (layer.isVisible()) {
				Iterator<MapObject> oit = layer.getObjects().iterator();
				total += layer.getObjects().getCount();
				while (oit.hasNext()) {
					MapObject mapObject = oit.next();
					rendered += renderObject(mapObject);
				}
			}
		}
		spriteBatch.end();
		Debug.debugText.append("Rendered " + rendered + " / " + total + " objects" + Constants.NL);
	}
}
