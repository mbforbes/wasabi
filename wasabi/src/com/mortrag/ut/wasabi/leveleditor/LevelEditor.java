package com.mortrag.ut.wasabi.leveleditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.mortrag.ut.wasabi.WasabiGame;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.input.Command;
import com.mortrag.ut.wasabi.input.Controls;
import com.mortrag.ut.wasabi.input.WasabiInput;
import com.mortrag.ut.wasabi.input.WasabiInput.MouseState;
import com.mortrag.ut.wasabi.map.WasabiAnimation;
import com.mortrag.ut.wasabi.map.WasabiMap;
import com.mortrag.ut.wasabi.map.WasabiMapRenderer;
import com.mortrag.ut.wasabi.map.WasabiTextureMapObject;
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
	private static final int GRID_SPACING = 50;
	
	private static final float ZOOM_DELTA = 0.05f;
	private static final float ZOOM_LIMIT = 0.1f;
	private static final float CAM_MOVE_SPEED = 10.0f;
	private static final float OBJECT_MOVE_SPEED = 1.0f; // for pixel-perfect nudging
	private static final float MAIN_VIEWPORT_WIDTH_FRAC = 0.75f;
	

	// --------------------------------------------------------------------------------------------
	// MEMBERS
	// --------------------------------------------------------------------------------------------
	// Textures, sprites, shapes, fonts
	private TextureAtlas atlas;
	private Array<AtlasRegion> objectRegions, spriteRegions; // each img prefix (e.g. o_*, s_*, ...)
	private Array<WasabiAnimation> characters;
	java.util.Map<String, Array<AtlasRegion>> regionMap; // maps Constants.FD.*_PREFIX -> *Regions
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	
	// Map
	private WasabiMap map;
	private int curLayerIdx;
	private Vector2 curSpritePos;
	private WasabiMapRenderer mapRenderer;
	
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
	private boolean paused = false, drawGridlines = true, snapToGrid = true, dirty = false;
	private File savedFilename = null; // this is set when the user saves the file, unset w/ load
	private MouseState mouseState;
	private Vector3 mouseStateUnprojected;
	
	// saving & whatnot
	private JFileChooser jFileChooser;
	private Kryo kryo;

	// --------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// --------------------------------------------------------------------------------------------

	public LevelEditor(WasabiGame game, WasabiInput input) {
		this.game = game;
		this.input = input;

		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();

		level_width = 5000;
		level_height = 5000;

		// Bit shapes
		shapeRenderer = new ShapeRenderer();		
		
		// Viewports are areas of the java window that have stuff rendered in them.
		// Cameras project things to viewports. They can be zoomed and moved.
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
		batch = new SpriteBatch();
		atlas = new TextureAtlas(Gdx.files.internal("../wasabi-android/assets/wasabi-atlas.atlas"));
		
		// regions
		Array<AtlasRegion> regions = atlas.getRegions();
		regionMap = new HashMap<String, Array<AtlasRegion>>();
		objectRegions = getRegionsPrefix(regions, Constants.FD.OBJ_PREFIX);
		regionMap.put(Constants.FD.OBJ_PREFIX, objectRegions);
		spriteRegions = getRegionsPrefix(regions, Constants.FD.SPRITE_PREFIX);
		regionMap.put(Constants.FD.SPRITE_PREFIX, spriteRegions);
		characters = new Array<WasabiAnimation>(true, Constants.CHARACTERS.length, WasabiAnimation.class);
		for (int i = 0; i < Constants.CHARACTERS.length; i++) {
			characters.add(new WasabiAnimation(Common.getFrames(atlas, Constants.CHARACTERS[i]),
					Constants.CHARACTERS[i], 0.1f, Animation.LOOP_PINGPONG,
					Constants.CHAR_ONAMES[i]));
		}
		
		curLayerIdx = 0;
		curSpritePos = new Vector2();
		
		// Map
		setupEmptyMap();
		// must be called after map made
		mapRenderer = new LevelEditor_MapRenderer(map, batch, shapeRenderer); 
		
		// Input
		commandList = new Array<Command>();
		controls = new LevelEditor_Controls();
		mouseStateUnprojected = new Vector3();
		
		// Saving / Loading
		kryo = new Kryo();
		
		// Remove textureRegion from WasabiTextureMapObject
		FieldSerializer<WasabiTextureMapObject> objSer = new
				FieldSerializer<WasabiTextureMapObject>(kryo, WasabiTextureMapObject.class);
		objSer.removeField("textureRegion");
		kryo.register(WasabiTextureMapObject.class, objSer);
		
		// Make Array serialization work by removing stupid transient fields.
		FieldSerializer<Array<MapLayer>> arraySer = new FieldSerializer<Array<MapLayer>>(kryo, Array.class);
		arraySer.removeField("iterable");
		arraySer.removeField("predicateIterable");
		kryo.register(Array.class, arraySer);
		
		jFileChooser = new JFileChooser();
				
	}
	
	/**
	 * Used in constructor.
	 */
	private Array<AtlasRegion> getRegionsPrefix(Array<AtlasRegion> regions, String prefix) {
		Array<AtlasRegion> result = new Array<AtlasRegion>();
		for(int i = 0; i < regions.size; i++) {
			AtlasRegion cur = regions.get(i);
			if (cur.name.startsWith(prefix)) {
				result.add(cur);
			}
		}
		return result;
	}
	
	
	/**
	 * This is private, but really part of the constructor (so far). Just handles all map setting up
	 * code.
	 */
	private void setupEmptyMap() {
		map = new WasabiMap();
		LevelEditor_MapLayer layer;

		// layer 0: not-collidable: background
		layer = new LevelEditor_MapLayer(Constants.FD.OBJ_PREFIX, objectRegions);
		layer.setName(Constants.MP.LayerType.BG.toString());
		layer.getProperties().put(Constants.MP.LAYER_TYPE, Constants.MP.LayerType.BG);
		map.getLayers().add(layer);
				
		// layer 1: collidable: foreground
		layer = new LevelEditor_MapLayer(Constants.FD.OBJ_PREFIX, objectRegions);
		layer.setName(Constants.MP.LayerType.COLLISION_FG.toString());
		layer.getProperties().put(Constants.MP.LAYER_TYPE, Constants.MP.LayerType.COLLISION_FG);
		map.getLayers().add(layer);
		
		// layer 2: not-collidable: foreground
		layer = new LevelEditor_MapLayer(Constants.FD.OBJ_PREFIX, objectRegions);
		layer.setName(Constants.MP.LayerType.FG.toString());
		layer.getProperties().put(Constants.MP.LAYER_TYPE, Constants.MP.LayerType.FG);
		map.getLayers().add(layer);	
		
		// layer 3: place hero and enemy points
		layer = new LevelEditor_MapLayer(characters);
		layer.setName(Constants.MP.LayerType.CHARACTERS.toString());
		layer.getProperties().put(Constants.MP.LAYER_TYPE, Constants.MP.LayerType.CHARACTERS);
		// default spawn position
		layer.addNextObj(300.0f, 300.0f);
		map.getLayers().add(layer);
		
		// Properties
		MapProperties mp = map.getProperties();
		mp.put(Constants.MP.LEVEL_WIDTH, level_width);
		mp.put(Constants.MP.LEVEL_HEIGHT, level_height);
	}	
	
	// --------------------------------------------------------------------------------------------
	// PRIVATE METHODS
	// --------------------------------------------------------------------------------------------
	
	private LevelEditor_MapLayer getCurLayer() {
		return (LevelEditor_MapLayer) this.map.getLayers().get(curLayerIdx);
	}
	
	private void addTempObjectsToLayers() {
		for (int i = 0; i < map.getLayers().getCount(); i++) {
			LevelEditor_MapLayer layer = (LevelEditor_MapLayer) map.getLayers().get(i);
			if (i != curLayerIdx) {				
				layer.addNextObj(0.0f, 0.0f);
				layer.inactive(); // set this obj to be not visible 
			} else {
				layer.addNextObj(curSpritePos.x, curSpritePos.y);
				layer.active(); // except the current layer--this is visibile!
			}
		}
		
	}
	
	private void removeTempObjectsFromLayers() {
		for (int i = 0; i < map.getLayers().getCount(); i++) {
			LevelEditor_MapLayer layer = (LevelEditor_MapLayer) map.getLayers().get(i);
			if (layer.getObjects().getCount() == 0) {
				Debug.print("Problem: Layer has no objects to remove!");
			} else {
				layer.removeLastObj();
			}
		}
	}
	
	/**
	 * TODO(max): this
	 */
	private void saveMap() {
		// If we're clean, there's nothing to save!
		if (!dirty) {
			return;
		}
		
		// Otherwise, see if we've got a filename stored & get one if not
		File fileToSave = null;
		if (savedFilename == null) {
			int rVal = jFileChooser.showSaveDialog(null);
			if (rVal == JFileChooser.APPROVE_OPTION) {
				fileToSave = jFileChooser.getSelectedFile();
			} else {
				// User canceled save--return!
				return;
			}
		} else {
			fileToSave = savedFilename;
		}
		
		// At this point we're for sure doing the save
		try {
			removeTempObjectsFromLayers();
			Output output = new Output(new FileOutputStream(fileToSave));
			kryo.writeClassAndObject(output, map);
			output.close();
			
			// clean (on freshly saved map) and track last file saved
			dirty = false;
			savedFilename = fileToSave;
			Debug.print("Successfully wrote map to: " + fileToSave);
		} catch (FileNotFoundException e) {
			// TODO(max): Switch to Toast when this is implemented.
			Debug.print(e);
		} finally {
			addTempObjectsToLayers();
		}
	}
	
	/**
	 * TODO(max): test
	 */
	private void loadMap() {
		// Confirm if saving first:
		if (dirty) {
			int confirmVal = JOptionPane.showOptionDialog(null,
					"The level has been modified since last save. Save it before loading another?",
					"Save before load?",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null, null, null);
			switch (confirmVal) {
			case JOptionPane.OK_OPTION:
				saveMap();
				break;
			case JOptionPane.NO_OPTION:
				// Do nothing--they don't want to save! We just continue with the load.
				break;
			case JOptionPane.CANCEL_OPTION:
			default:
				// We return--they didn't mean to load.
				return;
			}
		}
		
		// If we've gotten here, we're going to try to load
		int rVal = jFileChooser.showOpenDialog(null);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			// They picked a file. OK!
			File fileToSave = jFileChooser.getSelectedFile();
			try {
				Input input = new Input(new FileInputStream(fileToSave));
				WasabiMap newMap = (WasabiMap) kryo.readClassAndObject(input);
				input.close();
				
				// Setup the new map
				newMap.initialize(atlas, regionMap, characters);
				addTempObjectsToLayers();
				
				// Once we're here, we've presumably gotten a good new map!
				map.dispose(); // TODO(max): Sure we want to do this? Probably expensive!
				map = newMap;
				mapRenderer.setMap(newMap);
				
				// clean (on freshly loaded map)!				
				dirty = true;
				savedFilename = null;
				Debug.print("Successfully loaded map from: " + jFileChooser.getSelectedFile());				
			} catch (FileNotFoundException e) {
				// TODO(max): Switch to Toast when this is implemented.
				Debug.print(e);
			}
		}
	}	
	
	/**
	 * Switches screen to test chamber.
	 */
	private void testMap() {	
		// take off temp objects
		removeTempObjectsFromLayers();
		
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
	 * The sprite being manipulated is technically already placed; this just readies the next one
	 * up for manipulation on the current layer!
	 */
	private void placeSprite() {
		dirty = true;
		// NOTE(max): In the future, will have swtich based on layer what this does (e.g. if
		// placing spawn point for hero, can only be one.)
		getCurLayer().addNextObj(curSpritePos.x, curSpritePos.y);
	}
	
	/**
	 * Handle cursor press. (Place sprite.)
	 * NOTE(max): This behavior will change when the mouse is moved in other viewports! :-)
	 */
	private void handleCursorPressed() {
		// main window functionality
		placeSprite();
	}
	
	/**
	 * Unprojected the mouse state from window space into world space of the main cam.
	 * NOTE(max): This behavior will change when the mouse is moved in other viewports! :-)
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
		float moveSpeed = snapToGrid ? GRID_SPACING : OBJECT_MOVE_SPEED;
		curSpriteMove(((float) xMove) * moveSpeed, ((float) yMove) * moveSpeed);
	}
	
	private void curSpriteMove(float xAmt, float yAmt) {
		curSpriteSetPosition(curSpritePos.x + xAmt, curSpritePos.y + yAmt);
	}
	

	private void curSpriteSetPosition(float newXReq, float newYReq) {
		LevelEditor_MapLayer layer = getCurLayer();
		float curW = layer.getCurItemWidth();
		float curH = layer.getCurItemHeight();
		float newX = newXReq;
		float newY = newYReq;
		
		// Fix up out-of-bounds movements before moving.
		if (newX < 0) {
			newX = 0.0f;
		} else if (newX + curW > level_width) {
			newX = level_width - curW;
		}
		if (newY < 0) {
			newY = 0.0f;
		} else if (newY + curH > level_height) {
			newY = level_height - curH;
		}
		
		// Adjust if snapping to grid.
		if (snapToGrid) {
			newX = newX - newX % GRID_SPACING;
			newY = newY - newY % GRID_SPACING;
		}
		
		// Finally do the actual setting
		curSpritePos.set(newX, newY);
		getCurLayer().changeCurPos(curSpritePos);
	}
	
	private void renderSprites(Camera c) {
		// everything we placed
		mapRenderer.setView((OrthographicCamera) c);
		mapRenderer.renderBackgroundAndCount();
		mapRenderer.renderCharactersAndCount();
		mapRenderer.renderForegroundAndCount();
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
				LevelEditor_MapLayer layer = getCurLayer();
				int numLayers = map.getLayers().getCount();
				// Normal Level Editor command interpretation
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
				case CAMERA_ZOOM_IN_PRESS:
				case CAMERA_ZOOM_IN_HOLD:
					if (main_cam.zoom >= ZOOM_LIMIT) {
						main_cam.zoom -= ZOOM_DELTA;
					}
					break;
				case CAMERA_ZOOM_OUT_PRESS:
				case CAMERA_ZOOM_OUT_HOLD:
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
					layer.nextSprite();
					break;
				case PREVIOUS_SPRITE:
					layer.prevSprite();
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
				case BOUNDING_BOXES:
					mapRenderer.renderBoundingBoxes = !mapRenderer.renderBoundingBoxes;
					break;
				case CURSOR_MOVED:
					handleCursorMoved();
					break;
				case PRESS_DOWN:
					handleCursorPressed();
					break;
				case NEXT_LAYER:
					layer.inactive();
					curLayerIdx = curLayerIdx == numLayers - 1 ? 0 : curLayerIdx + 1;
					getCurLayer().active();
					break;
				case PREV_LAYER:
					layer.inactive();
					curLayerIdx = curLayerIdx == 0 ? numLayers - 1 : curLayerIdx - 1;
					getCurLayer().active();
					break;
				case TEST_MAP:
					testMap();
					// We don't want any more commands to be registered after we're testing the map,
					// so just finish here!
					return;
//					break;
				case SAVE_MAP:
					saveMap();
					break;
				case LOAD_MAP:
					loadMap();
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
		// update animations (e.g. character placement layers)
		map.tick(delta);
		
		// input
		handleCommands();
		
		// TODO(max): If we're switching to the test chamber (testMap()), we'll still go through
		// one last render cycle here after handleCommands() (which tells the game to switch to the
		// test chamber after the current render). So... do we really want to do everything below?
		// Or have some kind of shortcut hack to stop the rest from happening if we're switching
		// (and any other situations...?).

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
			LevelEditor_MapLayer layer = getCurLayer();
			Debug.debugLine("Layer: " + curLayerIdx + ": " + layer.getName() + " [" +
					(layer.getObjects().getCount() - 1) + " object(s)]");
//			Debug.debugLine("Current img: " + layer.regions.get(layer.curRegionIdx).name);
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
		// Note that this gets called even the first time the level is loaded!
		addTempObjectsToLayers(); 
	}

	@Override
	public void hide() {
		// NOTE(max): Do we need to revoke the controls here?
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
	}

}
