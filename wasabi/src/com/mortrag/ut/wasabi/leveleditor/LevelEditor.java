package com.mortrag.ut.wasabi.leveleditor;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.WasabiGame;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.graphics.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.input.Command;
import com.mortrag.ut.wasabi.input.Controls;
import com.mortrag.ut.wasabi.input.WasabiInput;
import com.mortrag.ut.wasabi.input.WasabiInput.MouseState;
import com.mortrag.ut.wasabi.testchamber.TestChamber;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Debug;

public class LevelEditor implements Screen {

	// --------------------------------------------------------------------------------------------
	// CONSTANTS
	// --------------------------------------------------------------------------------------------
	// public
	public static final String NAME = "Level Editor";
	
	// private
	private static final int GRID_SPACING = 20;
	private static final float ZOOM_DELTA = 0.02f;
	private static final float ZOOM_LIMIT = 0.1f;
	private static final float CAM_MOVE_SPEED = 5.0f;
	private static final float SPRITE_MOVE_SPEED = 1.0f; // for pixel-perfect nudging
	private static final float MAIN_VIEWPORT_WIDTH_FRAC = 0.75f; 

	// --------------------------------------------------------------------------------------------
	// MEMBERS
	// --------------------------------------------------------------------------------------------
	// Textures, sprites, shapes, fonts
	private TextureAtlas atlas;
	private Texture texture;
	private SpriteBatch batch;
	private Array<Sprite> sprites, placedSprites;
	private Array<Integer> placedSpriteIdxes;
	private Sprite curSprite;
	private int curSpriteNum;
	private ShapeRenderer shapeRenderer;
	
	// Viewports, Cameras, Window sizes
	private Rectangle overall_viewport, main_viewport, minimap_viewport, detail_viewport;
	private OrthographicCamera overall_cam, main_cam, minimap_cam, detail_cam;	
	// Window w/h, level w/h, main camera (viewport) w, minimap (viewport) h. All units in pixels.
	float w, h, level_width, level_height, main_width, minimap_height;		
	
	// Game, input, controls, commands
	private WasabiGame game;
	private WasabiInput input;
	private Controls controls;
	private Array<Command> commandList;
	
	// State (should make settings obj / map?)
	private boolean paused = false;
	private boolean drawGridlines = true;
	private boolean snapToGrid = true;
	private MouseState mouseState;
	private Vector3 mouseStateUnprojected;

	// --------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// --------------------------------------------------------------------------------------------

	public LevelEditor(WasabiGame game, WasabiInput input) {
		this.game = game;
		this.input = input;

		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();

		// TODO(max): Set these in config.
		level_width = 5000;
		level_height = 5000;

		// Viewports are areas of the java window that have stuff rendered in them.
		// Cameras project things to viewports. They can be zoomed and moved.
		// -----------------------------------------------------------------------------------------
		// Overall (whole window)
		overall_viewport = new Rectangle(0, 0, w, h);
		overall_cam = new OrthographicCamera(w, h);
		overall_cam.translate(w / 2.0f, h / 2.0f, 0.0f);

		// Main (editor area).
		main_width = w * MAIN_VIEWPORT_WIDTH_FRAC;
		main_viewport = new Rectangle(0, 0, main_width, h);
		main_cam = new OrthographicCamera(main_width, h);
		main_cam.translate(main_width / 2.0f, h / 2.0f, 0.0f);

		// Minimap
		minimap_height = (w - main_width) * (level_height / level_width);
		minimap_viewport = new Rectangle(main_width, h - minimap_height, w - main_width,
				minimap_height);
		minimap_cam = new OrthographicCamera(level_width, level_height);
		minimap_cam.translate(level_width / 2.0f, level_height / 2.0f, 0.0f);

		// Detail
		detail_viewport = new Rectangle(main_width, 0, w - main_width, h - minimap_height);
		detail_cam = new OrthographicCamera(w, h); // not sure what to set this to...

		
		// Drawing (sprite batches, textures, ...)
		// -----------------------------------------------------------------------------------------		
		batch = new SpriteBatch();
		atlas = new TextureAtlas(Gdx.files.internal("../wasabi-android/assets/wasabi-atlas.atlas"));
		sprites = getSpritesFromAtlas(atlas); // option 1: strip whitespace 
		//sprites = atlas.createSprites(); // option 2: don't strip whitespace
		placedSprites = new Array<Sprite>();
		placedSpriteIdxes = new Array<Integer>();
		curSpriteNum = 0;
		curSprite = sprites.get(curSpriteNum);
		//TextureAtlas.AtlasSprite as = (TextureAtlas.AtlasSprite) curSprite;
		// CURSPOT (can remove region offset???)
		curSprite.setPosition(0.0f, 0.0f);
		
		
		// Bit shapes
		// -----------------------------------------------------------------------------------------
		shapeRenderer = new ShapeRenderer();

		
		// Input
		// -----------------------------------------------------------------------------------------
		commandList = new Array<Command>();
		controls = new LevelEditor_Controls();
		mouseStateUnprojected = new Vector3();
	}
	
	
	// --------------------------------------------------------------------------------------------
	// PRIVATE METHODS
	// --------------------------------------------------------------------------------------------

	// TODO(max): UPDATE ALL CALLS AND DOCUMENTATION!
	
	/**
	 * Saves current map and switches screen to test chamber.
	 */
	private void testMap() {		
		// Save stuff in a map! Right now just very simple (one layer).
		Map map = new Map();
		MapLayers mapLayers = map.getLayers();
		MapLayer mapLayer = new MapLayer();
		MapObjects mapObjects = mapLayer.getObjects();
		Array<AtlasRegion> regions = atlas.getRegions();
		for (int i = 0; i < placedSpriteIdxes.size; i++) {
			Sprite sprite = placedSprites.get(i);
			TextureRegion textureRegion = regions.get(placedSpriteIdxes.get(i));
			TextureMapObject wasabiTextureMapObject = new WasabiTextureMapObject(textureRegion,
					sprite.getX(), sprite.getY(), sprite.getWidth(), sprite.getHeight());
			mapObjects.add(wasabiTextureMapObject);
		}
		mapLayer.getProperties().put(Constants.MP.COLLIDABLE, true);
		mapLayers.add(mapLayer);
		// MapProperties are important
		MapProperties mapProperties = map.getProperties();
		mapProperties.put(Constants.MP.LEVEL_WIDTH, level_width);
		mapProperties.put(Constants.MP.LEVEL_HEIGHT, level_height);		
		
		
		// Load the test chamber if it hasn't been loaded, or update it.
		TestChamber testChamber = null;
		if (!game.screenLoaded(TestChamber.NAME)) {
			// Screen hasn't been loaded--make it!
			testChamber = new TestChamber(game, input, map, batch, atlas);
			game.addScreen(testChamber, TestChamber.NAME);
		} else {
			// screen has been loaded--just update the map!
			testChamber = (TestChamber) game.getScreen(TestChamber.NAME);
			testChamber.setMap(map);
		}
		
		// switch
		game.getAndSetScreen(TestChamber.NAME);
	}
	
	/**
	 * Returns sprites that KEEP THE GODDAMNED WHITESPACE STRIPPED.
	 */
	private Array<Sprite> getSpritesFromAtlas(TextureAtlas fullAtlas) {
		Array<AtlasRegion> regions = fullAtlas.getRegions();
		Array<Sprite> sprites = new Array<Sprite>(regions.size);
		for (int i = 0; i < regions.size; i++) {
			AtlasRegion r = regions.get(i);
			sprites.add(new Sprite(r, 0, 0, r.packedWidth, r.packedHeight));
		}
		return sprites;
	}
	
	private void placeSprite() {
		Sprite placedSprite = new Sprite(curSprite);
		placedSprites.add(placedSprite);
		placedSpriteIdxes.add(curSpriteNum);
	}
	
	/**
	 * Handle cursor press. (Place sprite.)
	 * TODO(max): This behavior will change when the mouse is moved in other viewports! :-)
	 */
	private void handleCursorPressed() {
		// main window functionality
		placeSprite();
	}
	
	/**
	 * Unprojected the mouse state from window space into world space of the main cam.
	 * TODO(max): This behavior will change when the mouse is moved in other viewports! :-)
	 */
	private void handleCursorMoved() {
		mouseStateUnprojected.x = mouseState.x;
		mouseStateUnprojected.y = mouseState.y;
		main_cam.unproject(mouseStateUnprojected, main_viewport.x, main_viewport.y,
				main_viewport.width, main_viewport.height);
		curSpriteSetPosition(mouseStateUnprojected.x, mouseStateUnprojected.y);
	}
	
	/**
	 * Moves based on GRID_SPACING (snapToGrid on) or SPRITE_MOVE_SPEED (snapToGrid off).
	 * @param xMove -1 for left, 0 for none, 1 for right
	 * @param yMove -1 for down, 0 for none, 1 for up
	 */	
	private void curSpriteNudge(int xMove, int yMove) {
		// Calculate move speed and do tentative translation.
		float moveSpeed = snapToGrid ? GRID_SPACING : SPRITE_MOVE_SPEED;
		curSpriteMove(((float) xMove) * moveSpeed, ((float) yMove) * moveSpeed);
	}
	
	private void curSpriteMove(float xAmt, float yAmt) {
		curSpriteSetPosition(curSprite.getX() + xAmt, curSprite.getY() + yAmt);
	}
	

	private void curSpriteSetPosition(float newXReq, float newYReq) {
		float newX = newXReq;
		float newY = newYReq;
		
		// Fix up out-of-bounds movements before moving.
		if (newX < 0) {
			newX = 0.0f;
		} else if (newX + curSprite.getWidth() > level_width) {
			newX = level_width - curSprite.getWidth();
		}
		if (newY < 0) {
			newY = 0.0f;
		} else if (newY + curSprite.getHeight() > level_height) {
			newY = level_height - curSprite.getHeight();
		}
		
		// Adjust if snapping to grid.
		if (snapToGrid) {
			newX = newX - newX % GRID_SPACING;
			newY = newY - newY % GRID_SPACING;
		}
		curSprite.setPosition(newX, newY);
	}
	
	private void renderSprites(Camera c) {
		batch.setProjectionMatrix(c.combined);
		batch.begin();
		// batch drawing group
		{
			// the ones we've placed
			Iterator<Sprite> sit = placedSprites.iterator();
			while (sit.hasNext()) {
				sit.next().draw(batch);
			}

			// the one we're moving around
			curSprite.draw(batch);
		}
		batch.end();
	}

	private void drawEditorLines(Camera c) {
		// setup
		shapeRenderer.setProjectionMatrix(c.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(Color.BLACK); // alpha doesn't do anything...

		// Line to separate main window
		shapeRenderer.line(main_width, 0, main_width, h);

		// Line to separate minimap from detail view.
		shapeRenderer.line(main_width, h - minimap_height, w, h - minimap_height);

		// end
		shapeRenderer.end();
	}

	private void drawGrid(Camera c) {
		// set up renderer
		shapeRenderer.setProjectionMatrix(c.combined);
		shapeRenderer.begin(ShapeType.Line);
		shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 0.0f); // alpha doesn't do anything...

		// vertical lines
		for (int i = 0; i <= level_width; i += GRID_SPACING) {
			shapeRenderer.line(i, level_height, i, 0);
		}

		// horizontal lines
		for (int i = 0; i <= level_height; i += GRID_SPACING) {
			shapeRenderer.line(0, i, level_width, i);
		}
		shapeRenderer.end();		
	}

	/**
	 * Convenience method for printing.
	 * @param o  thing to print
	 */	
	@SuppressWarnings("unused")
	private void print(Object o) {
		System.out.println(o);
	}

	private void handleCommands() {

		Iterator<Command> cit = commandList.iterator();
		while (cit.hasNext()) {
			LevelEditor_Commands c = (LevelEditor_Commands) cit.next();
			if (paused) {
				// Game is paused command interpretation
				switch(c) {
				case PAUSE:
					resume();
					break;
				default:
					// do nothing
					break;
				}
			} else {
				// Normal Level Editor command interpretation	
				float newx, newy;
				switch(c) {
				case CAMERA_RIGHT:
					main_cam.translate(CAM_MOVE_SPEED, 0, 0);	
					break;
				case CAMERA_LEFT:
					main_cam.translate(-CAM_MOVE_SPEED, 0, 0);					
					break;
				case CAMERA_UP:
					main_cam.translate(0, CAM_MOVE_SPEED, 0);
					break;
				case CAMERA_DOWN:
					main_cam.translate(0, -CAM_MOVE_SPEED, 0);
					break;
				case CAMERA_ZOOM_IN:				
					if (main_cam.zoom >= ZOOM_LIMIT) {
						main_cam.zoom -= ZOOM_DELTA;
					}
					break;
				case CAMERA_ZOOM_OUT:
					main_cam.zoom += ZOOM_DELTA;
					break;
				case MOVE_RIGHT:
					curSpriteNudge(1, 0);
					break;
				case MOVE_LEFT:
					curSpriteNudge(-1, 0);
					break;
				case MOVE_UP:
					curSpriteNudge(0, 1);
					break;
				case MOVE_DOWN:
					curSpriteNudge(0, -1);
					break;
				case PAUSE:
					pause();
					break;
				case NEXT_SPRITE:
					newx = curSprite.getX();
					newy = curSprite.getY();
//					System.out.println("prev x/y: " + newx + "/" + newy);
					curSpriteNum  = (curSpriteNum + 1) % (sprites.size - 1);
					curSprite = sprites.get(curSpriteNum);
					curSpriteSetPosition(newx, newy);
//					System.out.println("new x/y: " + curSprite.getX() + "/" + curSprite.getY());
					break;
				case PREVIOUS_SPRITE:
					newx = curSprite.getX();
					newy = curSprite.getY();
					curSpriteNum = curSpriteNum == 0 ? sprites.size - 1 : curSpriteNum - 1;
					curSprite = sprites.get(curSpriteNum);
					curSpriteSetPosition(newx, newy);
					break;					
				case PLACE_SPRITE:
					placeSprite();
					break;
				case TOGGLE_GRID:
					drawGridlines = !drawGridlines;
					break;
				case TOGGLE_SNAP_TO_GRID:
					snapToGrid = !snapToGrid;
					break;
				case CURSOR_MOVED:
					handleCursorMoved();
					break;
				case PRESS_DOWN:
					handleCursorPressed();
					break;
				case TEST_MAP:
					testMap();
					break;
				default:
					// Do nothing.
					break;
				}
			}	
		} // else (if paused)

		// removes the inputs that were just PRESS actions
		input.clearPress();
	}

	
	// --------------------------------------------------------------------------------------------
	// PUBLIC METHODS
	// --------------------------------------------------------------------------------------------	
	
	@Override
	public void render(float delta) {

		// input
		handleCommands();

		// handle GL stuff
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// update cameras
		overall_cam.update();
		main_cam.update();
		minimap_cam.update();
		detail_cam.update();

		// Draw main sprites and grid
		gl.glViewport((int) main_viewport.x, (int) main_viewport.y,
				(int) main_viewport.width, (int) main_viewport.height);
		renderSprites(main_cam);
		if (drawGridlines) {
			drawGrid(main_cam);	
		}
		
		// Draw minimap sprites
		gl.glViewport((int) minimap_viewport.x, (int) minimap_viewport.y,
				(int) minimap_viewport.width, (int) minimap_viewport.height);
		renderSprites(minimap_cam);

		// Draw overall lines
		gl.glViewport((int) overall_viewport.x, (int) overall_viewport.y,
				(int) overall_viewport.width, (int) overall_viewport.height);		
		drawEditorLines(overall_cam);
		
		// paused overlay
		if (paused) {
			Common.drawPauseOverlay(overall_cam, batch, controls.getControlsList());
		}
		if (Debug.DEBUG) {
			Common.displayFps(overall_cam, batch);
		}
	}
	
	@Override
	public void resize(int width, int height) {
		// only resize if anything has actually changed
		if (width == (int) w && height == (int) h) {
			return;
		}
		w = width;
		h = height;
		
		// reconfig overall viewport/cam
		overall_viewport.width = w;
		overall_viewport.height = h;
		overall_cam.viewportWidth = w;
		overall_cam.viewportHeight = h;
		overall_cam.position.scl(0.0f);
		overall_cam.translate(w / 2.0f, h / 2.0f, 0.0f);

		// reconfig main viewport/cam
		main_width = w * MAIN_VIEWPORT_WIDTH_FRAC;
		main_viewport.width = main_width;
		main_viewport.height = h;
		main_cam.viewportWidth = main_width;
		main_cam.viewportHeight = h;
		main_cam.position.scl(0.0f);
		main_cam.translate(main_width / 2.0f, h / 2.0f, 0.0f);

		// reconfig minimap viewport/ cam
		minimap_height = (w - main_width) * (level_height / level_width);
		minimap_viewport.x = main_width;
		minimap_viewport.y = h - minimap_height;
		minimap_viewport.width = w - main_width;
		minimap_viewport.height = minimap_height;
		//minimap_cam = new OrthographicCamera(level_width, level_height);
		minimap_cam.position.scl(0.0f);
		minimap_cam.translate(level_width / 2.0f, level_height / 2.0f, 0.0f);

		// reconfig detail viewport / cam
		detail_viewport.x = main_width;
		detail_viewport.width = w - main_width;
		detail_viewport.height = h - minimap_height;
		// NOTE(max): Update this once it's been actually set.
		// detail_cam = new OrthographicCamera(...); // not sure what to set this to...		
	}

	@Override
	public void show() {
		mouseState = input.setControls(controls, commandList);
	}

	@Override
	public void hide() {
		// TODO(max): Do we need to revoke the controls here?
		//            Do we need to do anything else here?
	}

	@Override
	public void pause() {
		paused = true;

	}

	@Override
	public void resume() {
		paused = false;
	}

	@Override
	public void dispose() {
		// so this never happens...
		batch.dispose();
		texture.dispose();
	}

}
