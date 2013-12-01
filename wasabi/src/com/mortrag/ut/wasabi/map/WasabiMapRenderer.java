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
import com.mortrag.ut.wasabi.util.Constants.MP.LayerType;
import com.mortrag.ut.wasabi.util.Pair;

public class WasabiMapRenderer implements MapRenderer {

	// public settings with defaults
	public boolean renderBoundingBoxes = true;
	public ShapeRenderer shapeRenderer = null;
	
	// private
	private int[] layerIdxes, bgLayerIdxes, fgLayerIdxes, charLayerIdxes;
	private Array<Integer> bgLayerIdxesArr, fgLayerIdxesArr, charLayerIdxesArr;
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
		bgLayerIdxesArr = new Array<Integer>();
		fgLayerIdxesArr = new Array<Integer>();
		charLayerIdxesArr = new Array<Integer>();
		boundingBoxes = new Array<BoundingBox>();
	}
	
	public void setMap(Map map) {
		this.map = map;
	}
	
	/**
	 * Always do this before using *layerIdxes. Note: we are assuming a layer's foregroundness or
	 * backgroundness is immutable!
	 */
	private void maybeRecomputeLayerIdxes() {
		int trueLayerCount = map.getLayers().getCount();
		if (savedLayerCount != trueLayerCount) {
			layerIdxes = new int[trueLayerCount];
			bgLayerIdxesArr.clear();
			fgLayerIdxesArr.clear();
			charLayerIdxesArr.clear();
			
			for (int i = 0; i < trueLayerCount; i++) {
				// check bg vs fg; note if not either, then not added to either.
				MapProperties layerProperties = map.getLayers().get(i).getProperties();
				LayerType layerType = (LayerType) layerProperties.get(Constants.MP.LAYER_TYPE);
				switch (layerType) {
				case BG:
					bgLayerIdxesArr.add(i);
					break;
				case COLLISION_FG: // fall through
				case FG:
					fgLayerIdxesArr.add(i);
					break;
				case CHARACTERS:
					charLayerIdxesArr.add(i);
					break;
				}
				// save index
				layerIdxes[i] = i;
			}
			
			// make bg and fg arrays
			bgLayerIdxes = fillIntArr(bgLayerIdxesArr);
			fgLayerIdxes = fillIntArr(fgLayerIdxesArr);
			charLayerIdxes = fillIntArr(charLayerIdxesArr);
			
			// save that we did this so we don't have to recompute!
			savedLayerCount = trueLayerCount;
		}
	}
	
	// Returns new int[] with all objects in arr
	private int[] fillIntArr(Array<Integer> arr) {
		int[] ret = new int[arr.size];
		for (int i = 0; i < arr.size; i++) {
			ret[i] = arr.get(i);
		}
		return ret;
	}
	
	/**
	 * before calling: spriteBatch.begin() must have been called
	 * after calling: you must call spriteBatch.end() 
	 * @param mapObject
	 * @return how many objects were rendered
	 */
	private int renderObject(MapObject mapObject, boolean collectBoundingBoxes, float time) {
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
	
			// Find the true type and draw.
			if (mapObject instanceof WasabiTextureMapObject) {
				// WasabiTextureMapObject
				WasabiTextureMapObject texObj = (WasabiTextureMapObject) mapObject;
				spriteBatch.draw(texObj.getTextureRegion(), objx, objy);
				if (collectBoundingBoxes) {
					boundingBoxes.add(texObj.getBoundingBox());
				}
				return 1;
			} else if (mapObject instanceof AnimatedMapObject) {
				// AnimatedMapObject
				AnimatedMapObject animObj = (AnimatedMapObject) mapObject;
				spriteBatch.draw(animObj.getCurFrame(time), objx, objy);
				return 1;
			} else {
				// Not rendering any other WasabiMapObject's
				return 0;
			}
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
		maybeRecomputeLayerIdxes(); // Always do this before using layerIdxes
		render(layerIdxes);	
	}

	/**
	 * Convenience to return how many objects were rendered and how many total could have been.
	 * @param layers
	 */
	public Pair<Integer, Integer> renderAndCount(int[] layers) {
		spriteBatch.begin();
		int rendered = 0, total = 0;
		// For provided layers...
		for (int i = 0; i < layers.length; i++) {
			MapLayer layer = map.getLayers().get(layers[i]);
			boolean collides = layer.getProperties().get(Constants.MP.LAYER_TYPE) ==
					LayerType.COLLISION_FG;
			// If the layer is visible
			if (layer.isVisible()) {
				float time = 0.0f;
				if (layer instanceof LevelEditor_MapLayer) {
					time = ((LevelEditor_MapLayer) layer).getTime();
				}
				Iterator<MapObject> oit = layer.getObjects().iterator();
				total += layer.getObjects().getCount();
				// For all objects
				while (oit.hasNext()) {
					MapObject mapObject = oit.next();
					// If the object is visible
					if (mapObject.isVisible()) {						
						rendered += renderObject(mapObject, collides, time);
					}
				}
			}			
		}

		spriteBatch.end();
		
		// For the client's bookkeeping.
		renderedAndTotal.first = rendered;
		renderedAndTotal.second = total;
		return renderedAndTotal;
	}
	
	@Override
	public void render(int[] layers) {
		// Throw away return value
		renderAndCount(layers);
	}
	
	public Pair<Integer, Integer> renderBackgroundAndCount() {
		maybeRecomputeLayerIdxes(); // Always do this before using layerIdxes
		return renderAndCount(bgLayerIdxes);
	}
	
	public Pair<Integer, Integer> renderCharactersAndCount() {
		maybeRecomputeLayerIdxes(); // Always do this before using layerIdxes
		return renderAndCount(charLayerIdxes);
	}	
	
	/**
	 * Note: we assume that any collision layers are in the foreground.
	 */
	public Pair<Integer, Integer> renderForegroundAndCount() {
		maybeRecomputeLayerIdxes(); // Always do this before using layerIdxes
		boundingBoxes.clear();
		Pair<Integer, Integer> counts = renderAndCount(fgLayerIdxes);
		if (shapeRenderer != null && renderBoundingBoxes) {			
			Common.renderBoundingBoxes(spriteBatch.getProjectionMatrix(), shapeRenderer,
					boundingBoxes);
		}
		return counts;
	}
	
	
}
