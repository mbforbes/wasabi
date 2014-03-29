package com.mortrag.ut.wasabi.map;

import java.util.Iterator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.leveleditor.LevelEditor_MapLayer;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Debug;
import com.mortrag.ut.wasabi.util.Pair;

public class WasabiMapRenderer implements MapRenderer {

	public enum Mode {
		EDITOR,
		GAME;
	}
	
	// public settings with defaults
	public boolean renderBoundingBoxes = true;
	public ShapeRenderer shapeRenderer = null;
	public Mode mode = Mode.EDITOR;
	
	// private
	private Array<BoundingBox> boundingBoxes;
	private int savedLayerCount = 0;
	private Pair<Integer, Integer> renderedAndTotal;
	private Map map;
	private Rectangle viewBounds;
	private float leftedge, rightedge, bottomedge, topedge;
	private SpriteBatch spriteBatch;
	
	public WasabiMapRenderer(Map map, SpriteBatch spriteBatch, ShapeRenderer shapeRenderer) {
		this(map, spriteBatch);
		this.shapeRenderer = shapeRenderer;
	}	
	
	public WasabiMapRenderer(Map map, SpriteBatch spriteBatch) {
		setMap(map);
		this.spriteBatch = spriteBatch;
		viewBounds = new Rectangle();
		renderedAndTotal = new Pair<Integer,Integer>(0,0);
		boundingBoxes = new Array<BoundingBox>();
	}
	
	public void setMap(Map map) {
		this.map = map;
	}
	
	/**
	 * before calling: spriteBatch.begin() must have been called
	 * after calling: you must call spriteBatch.end() 
	 * @param mapObject
	 * @return how many objects were rendered
	 */
	private int renderObject(MapObject mapObject) {
		if (mapObject instanceof WasabiMapObject) {
			// Get and check dimensions; only draw if some part of it will be displayed on the
			// screen. 
			WasabiMapObject obj = (WasabiMapObject) mapObject; 
			float objx = obj.getX();
			float objy = obj.getY();
			float objw = obj.getWidth();
			float objh = obj.getHeight();
			if (objx + objw < leftedge || objx > rightedge || objy > topedge ||
					objy + objh < bottomedge) {
				return 0;
			}
	
			// Draw; type will implement this.
			switch (mode) {
			case EDITOR:
				obj.renderEditor(spriteBatch);
				break;
			case GAME:
				obj.renderGame(spriteBatch);
				break;
			}
			
			// Save bounding boxes? (TODO why doesn't the object just do this?)
			if (obj.getCollides()) {
				boundingBoxes.add(obj.getBoundingBox());
			}
			return 1;
		} else {
			// Not rendering any other MapObject's
			return 0;
		}
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
		renderRangeAndCount(0, map.getLayers().getCount() - 1);
	}
	
	public Pair<Integer, Integer> renderAndCount() {
		return renderRangeAndCount(0, map.getLayers().getCount() - 1);
	}

	/**
	 * Convenience to return how many objects were rendered and how many total could have been.
	 * 
	 * @param low inclusive
	 * @param high inclusive, 0-based (must go to at most size(layeres) - 1)
	 */
	public Pair<Integer, Integer> renderRangeAndCount(int low, int high) {
		// For bookkeeping
		renderedAndTotal.first = 0;
		renderedAndTotal.second = 0;		

		spriteBatch.begin();
		for (int i = low; i <= high; i++) {
			MapLayer layer = map.getLayers().get(i);
			renderLayerAndCount(layer, renderedAndTotal);
		}
		spriteBatch.end();
		
		// For the client's bookkeeping.
		return renderedAndTotal;
	}
	
	/**
	 * NOTE: throws away saved value of rendered/total objects.
	 * before calling: must call spriteBatch.begin()
	 * after calling: must call spriteBatch.end()
	 * @param layer
	 */
	public void renderLayer(MapLayer layer) {
		renderLayerAndCount(layer, renderedAndTotal);
		// avoid overflow
		renderedAndTotal.first = 0;
		renderedAndTotal.second = 0;
	}
	
	private void renderLayerAndCount(MapLayer layer, Pair<Integer, Integer> out) {
		// If the layer is visible
		if (layer.isVisible()) {
			Iterator<MapObject> oit = layer.getObjects().iterator();
			out.second += layer.getObjects().getCount();
			// For all objects
			while (oit.hasNext()) {
				MapObject mapObject = oit.next();
				// If the object is visible
				if (mapObject.isVisible()) {						
					out.first += renderObject(mapObject);
				}
			}
		}
	}
	
	@Override
	public void render(int[] layers) {
		Debug.print("THIS IS A STUPID METHOD");
		throw new RuntimeException("THIS IS A STUPID METHOD");
	}	
	
	/**
	 * Must call before rendering begins where you care about bounding boxes.
	 */
	public void start() {
		boundingBoxes.clear();		
	}
}
