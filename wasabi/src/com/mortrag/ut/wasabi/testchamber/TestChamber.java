package com.mortrag.ut.wasabi.testchamber;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.WasabiGame;
import com.mortrag.ut.wasabi.characters.Advectable;
import com.mortrag.ut.wasabi.characters.ArmorEnemy;
import com.mortrag.ut.wasabi.characters.Behaviorable;
import com.mortrag.ut.wasabi.characters.Collidable;
import com.mortrag.ut.wasabi.characters.Hero;
import com.mortrag.ut.wasabi.characters.Inputable;
import com.mortrag.ut.wasabi.characters.Inputable.Input;
import com.mortrag.ut.wasabi.characters.Physicsable.Physics;
import com.mortrag.ut.wasabi.characters.Physicsable;
import com.mortrag.ut.wasabi.characters.WasabiCharacter;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.input.Command;
import com.mortrag.ut.wasabi.input.Controls;
import com.mortrag.ut.wasabi.input.WasabiInput;
import com.mortrag.ut.wasabi.input.WasabiInput.MouseState;
import com.mortrag.ut.wasabi.leveleditor.LevelEditor;
import com.mortrag.ut.wasabi.map.WasabiMapObject;
import com.mortrag.ut.wasabi.map.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Constants.MP.CharObjectType;
import com.mortrag.ut.wasabi.util.Debug;
import com.mortrag.ut.wasabi.util.Pair;

public class TestChamber implements Screen {
	
	// ---------------------------------------------------------------------------------------------
	// CONSTANTS
	// ---------------------------------------------------------------------------------------------
	// public
	public static final String NAME = "Test Chamber";
	
	// ---------------------------------------------------------------------------------------------	
	// FIELDS
	// ---------------------------------------------------------------------------------------------	
	// Game
	private WasabiGame game;
	
	// Cameras and Viewports	
	private Camera mainCam, overallCam;
	// currently just have one viewport that is the size of the screen	
	private Rectangle mainViewport; 
	// do we need the level w/h? surely...
	private float screenWidth, screenHeight, levelWidth, levelHeight;
	
	// Graphics
	private SpriteBatch batch;
	@SuppressWarnings("unused") // Probably will need this at some point.
	private TextureAtlas atlas;
	private ShapeRenderer shapeRenderer;
	
	// Input
	private InputMultiplexer inputMultiplexer;
	private WasabiInput input;
	private Controls controls;
	private Array<Command> commandList;
	@SuppressWarnings("unused") // Probably will need this at some point.
	private MouseState mouseState;
	
	// Map
	private Map map;
	private TestChamber_MapRenderer mapRenderer;
	
	// State
	private boolean paused = false, frameByFrame = false, nextFrame = false;
	
	// Characters!
	private WasabiCharacter hero; // only for debug and camera! (where hero is)
	private Array<Inputable> inputables;
	private Array<Inputable.Input> inputs; // for use in handleCommands(...)
	private Array<Physicsable> physicsables;
	private Array<Advectable> advectables;
	private Array<BoundingBox> boundaries, characterBBs;
	private Array<WasabiCharacter> characters;
	private Array<Behaviorable> behaviorables;
	
	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------	
	
	public TestChamber(WasabiGame game, InputMultiplexer inputMultiplexer, Map map, SpriteBatch batch,
			TextureAtlas atlas) {		
		this.game = game;
		this.inputMultiplexer = inputMultiplexer;
		// assuming WasabiInput is first input processor
		this.input = (WasabiInput) inputMultiplexer.getProcessors().get(0);
		this.map = map;
		MapProperties mapProperties = map.getProperties();
		levelWidth = (Float) mapProperties.get(Constants.MP.LEVEL_WIDTH);
		levelHeight = (Float) mapProperties.get(Constants.MP.LEVEL_HEIGHT);		
		this.batch = batch;
		this.atlas = atlas;
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		shapeRenderer = new ShapeRenderer();
		mainViewport = new Rectangle(0, 0, screenWidth, screenHeight);
		mainCam = new OrthographicCamera(screenWidth, screenHeight);
		mainCam.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);
		mainCam.update();
		overallCam = new OrthographicCamera(screenWidth, screenHeight);
		overallCam.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);
		overallCam.update();
		controls = new TestChamber_Controls();
		commandList = new Array<Command>();
		mapRenderer = new TestChamber_MapRenderer(map, batch, shapeRenderer);
		mapRenderer.setView((OrthographicCamera) mainCam);	
		boundaries = new Array<BoundingBox>();
		characterBBs = new Array<BoundingBox>();
		updateBoundingBoxes(); // map collision layers
		
		// collections
		characters = new Array<WasabiCharacter>();
		behaviorables = new Array<Behaviorable>();
		inputs = new Array<Inputable.Input>();
		inputables = new Array<Inputable>();
		physicsables = new Array<Physicsable>();
		advectables = new Array<Advectable>();
		
		populateCharacters();
	}
	
	// ---------------------------------------------------------------------------------------------
	// EXTERNAL API (aside from auto-called stuff)
	// ---------------------------------------------------------------------------------------------
	
	public void setMap(Map map) {
		this.map = map;
		MapProperties mapProperties = map.getProperties();
		levelWidth = (Float) mapProperties.get(Constants.MP.LEVEL_WIDTH);
		levelHeight = (Float) mapProperties.get(Constants.MP.LEVEL_HEIGHT);
		mapRenderer.setMap(map);
		updateBoundingBoxes();
		populateCharacters();
	}
	
	// ---------------------------------------------------------------------------------------------
	// PRIVATE
	// ---------------------------------------------------------------------------------------------
	
	/**
	 * Should be called on construction and on map change.
	 */
	private void populateCharacters() {
		// clear any previous arrays
		characters.clear();
		inputables.clear();
		physicsables.clear();
		advectables.clear();
		behaviorables.clear();
		
		// check all layers
		for (int layerIdx = 0; layerIdx < map.getLayers().getCount(); layerIdx++) {
			MapLayer layer = map.getLayers().get(layerIdx);
			
			// check all objects in layer
			for (int objIdx = 0; objIdx < layer.getObjects().getCount(); objIdx++) {
				WasabiMapObject curObj = (WasabiMapObject) layer.getObjects().get(objIdx);
				CharObjectType charObjType = curObj.getProperties().get(Constants.MP.CHAR_OBJECT_TYPE, CharObjectType.class);
				// if we've got a character spawn point, then deal with it
				if (charObjType == Constants.MP.CharObjectType.SPAWN_POINT) {
					hero = new Hero(curObj.getX(), curObj.getY(), atlas);
					characters.add(hero);
				} else if (charObjType == Constants.MP.CharObjectType.ARMOR_ENEMY) {
					characters.add(new ArmorEnemy(curObj.getX(), curObj.getY(), atlas));
				} else {
					// that's all the charaaceters we've implemented...
				}
			}

		}		
		
		// now, populate other lists we care about from character list
		Iterator<WasabiCharacter> cit = characters.iterator();
		while (cit.hasNext()) {
			WasabiCharacter c = cit.next();
			if (c instanceof Inputable) {
				inputables.add((Inputable) c);
			}
			if (c instanceof Physicsable) {
				physicsables.add((Physicsable) c);
			}
			if (c instanceof Advectable) {
				advectables.add((Advectable) c);
			}
			if (c instanceof Behaviorable) {
				behaviorables.add((Behaviorable) c);
			}
		}		
	}
	
	private void updateBoundingBoxes() {
		// get fresh
		boundaries.clear();
		
		// add object bounding boxes
		Iterator<MapLayer> lit = map.getLayers().iterator();		
		while (lit.hasNext()) {
			MapLayer layer = lit.next();
			Iterator<MapObject> oit = layer.getObjects().iterator();
			while (oit.hasNext()) {
				WasabiMapObject obj = (WasabiMapObject) oit.next();
				if (obj.getCollides()) {
					boundaries.add(obj.getBoundingBox());
				}
			}
		}
		
		// add level bounding boxes
		boundaries.add(new BoundingBox(new Vector3(0.0f, -1.0f, 0.0f),
				new Vector3(levelWidth, 0.0f, 0.0f))); // bottom
		boundaries.add(new BoundingBox(new Vector3(-1.0f, 0.0f, 0.0f),
				new Vector3(0.0f, levelHeight, 0.0f))); // left
		boundaries.add(new BoundingBox(new Vector3(0.0f, levelHeight, 0.0f),
				new Vector3(levelWidth, levelHeight + 1.0f, 0.0f))); // top
		boundaries.add(new BoundingBox(new Vector3(levelWidth, 0.0f, 0.0f),
				new Vector3(levelWidth + 1.0f, levelHeight, 0.0f))); // right
	}
	
	private void backToEditor() {
		if (!game.screenLoaded(LevelEditor.NAME)) {
			// Screen hasn't been loaded--make it!
			// NOTE: this is kind of a weird concept; who knows if it would work.  
			game.addScreen(new LevelEditor(game, inputMultiplexer), LevelEditor.NAME);
		}
		game.getAndSetScreen(LevelEditor.NAME);		
	}
	
	private void translateCommandsToInputs() {
		inputs.clear();
		Iterator<Command> cit = commandList.iterator();
		while (cit.hasNext()) {
			TestChamber_Commands c = (TestChamber_Commands) cit.next();
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
				// Normal test chamber command interpretation.
				switch(c) {
				case MOVE_RIGHT:
					inputs.add(Input.RIGHT);
					break;
				case MOVE_LEFT:
					inputs.add(Input.LEFT);
					break;
				case JUMP:
					inputs.add(Input.UP);
					break;
				case BOUNDING_BOXES:
					mapRenderer.renderBoundingBoxes = !mapRenderer.renderBoundingBoxes;
					break;
				case FRAME_BY_FRAME:
					frameByFrame = !frameByFrame;
					break;
				case NEXT_FRAME:
					nextFrame = true;
					break;					
				case PAUSE:
					pause();
					break;
				case BACK_TO_EDITOR:
					backToEditor();
					break;
				default:
					// Do nothing.
					break;
				} // switch (normal game state)
			} // else (if paused)
		} // while (more commands)
		
		// removes the inputs that were just PRESS actions
		input.clearPress();
	}
	
	private void sendInputsToInputables() {
		// process inputables. These variable names are horrible. Sorry.
		Iterator<Inputable> inpit = inputables.iterator();
		while (inpit.hasNext()) {
			Inputable ip = inpit.next();
			ip.inputs(inputs);
		}
	}
	
	private void physics() {
		Iterator<Physicsable> pit = physicsables.iterator();
		while (pit.hasNext()) {
			Physicsable p = pit.next();
			
			// Everyone gets gravity
			p.applyPhysics(Physics.GRAVITY);
			
			// Friction to all for ... yeah
			p.applyPhysics(Physics.FRICTION);
		}
	}
	
	/**
	 * "reset" A: forces accelerations to be zeroed out; recomputed each step
	 */
	private void zeroAccelerations() {
		Iterator<Advectable> ait = advectables.iterator();
		while (ait.hasNext()) {
			ait.next().getA().scl(0.0f);
		}		
	}
	
	/**
	 * pre: zeroAccelerations()
	 * @param delta
	 */
	private void advectWithCollisions(float delta) {
		// debug info
		Debug.debugLine("Num collision boundaries: " + boundaries.size);
		
		Iterator<Advectable> ait = advectables.iterator();
		while (ait.hasNext()) {
			Advectable adv = ait.next();
			Vector2 a = adv.getA();
			Vector2 v = adv.getV();
			Vector2 p = adv.getP();

			// move first. most vector2 methods mutate the objects, so we explicitly do this.
			a.scl(delta); 
			v.add(a);
			v.scl(delta);
			
			// clamp tiny values to 0
			if ((v.x > 0.0f && v.x < Advectable.CLAMP_EPSILON) ||
					(v.x < 0.0f && v.x > -Advectable.CLAMP_EPSILON)) {
				v.x = 0.0f;
			}
			if ((v.y > 0.0f && v.y < Advectable.CLAMP_EPSILON) || 
					(v.y < 0.0f && v.y > -Advectable.CLAMP_EPSILON)) {
				v.y = 0.0f;
			}			
			
			// then handle collisions, if it's going to collide (double check)
			if (adv.collides() && adv instanceof Collidable) {
				Collidable cadv = (Collidable) adv;
				BoundingBox pbb = cadv.getPrevBoundingBox();
				// Note: local primitives probably not necessary anymore.
				float prevMinX = pbb.min.x, prevMinY = pbb.min.y, prevMaxX = pbb.max.x,
						prevMaxY = pbb.max.y;
				boolean onGroundPrev = cadv.getOnGround();
				
				// set now so bounding box will be updated; will correct if collisions				
				p.add(v); 
				
				// may need to update animations (e.g. falling). Doing this before bounding box
				// computed because bounding box depends on animations!
				adv.maybeUpdateAnimations();
				
				//boolean hitGround = false;
				boolean onGround = false;
				
				// TODO(max): Can easily optimize. See ETH ref'd site, or just do level zones.
				Iterator<BoundingBox> bit = boundaries.iterator();
				while (bit.hasNext()) {
					// Need to recompute object bounding box in case it changes. Probably shouldn't
					// actually do this for every other object but...
					BoundingBox objBox = cadv.getBoundingBox();
					BoundingBox boundary = bit.next();
					if (objBox.intersects(boundary)) {
						// Figure out what kind of intersection... maybe this part means the whole
						// boundary box thing is useless...
						if (prevMinX >= boundary.max.x && objBox.min.x < boundary.max.x &&
								// disable sticky floors
								!((objBox.min.y == boundary.max.y && onGroundPrev ) ||
										// disable sticky ceilings
										objBox.max.y == boundary.min.y)) {
							// collide on left
							p.set(boundary.max.x, p.y);
							v.scl(0.0f, 1.0f); // stop vx
						} else if (prevMaxX <= boundary.min.x && objBox.max.x > boundary.min.x &&
								// disable sticky floors
								!((objBox.min.y == boundary.max.y && onGroundPrev ) ||
										// disable sticky ceilings
										objBox.max.y == boundary.min.y)) {
							// collide on right
							p.set(p.x - (objBox.max.x - boundary.min.x), p.y);
							v.scl(0.0f, 1.0f); // stop vx
						}
						if (prevMinY >= boundary.max.y && objBox.min.y < boundary.max.y &&
								// Avoid the corner case (literally)...
								prevMaxX >= boundary.min.x) {
							// collide on bottom
							p.set(p.x, boundary.max.y);
							v.scl(1.0f, 0.0f); // stop vy
							// NOTE: would detect the frame that the ground was first hit here.
							onGround = true;
						} else if (prevMaxY <= boundary.min.y && objBox.max.y > boundary.min.y) {
							// collide on top
							p.set(p.x, p.y - (objBox.max.y - boundary.min.y));
							v.scl(1.0f, 0.0f); // stop vy
						}
						
						// Detect (resting) on ground
						if (objBox.min.y == boundary.max.y) {
							onGround = true;
						}
					}
				}
				cadv.setOnGround(onGround); 
				cadv.setPrevBoundingBox(cadv.getBoundingBox()); 
			} else { // if (adv.collides() && adv instanceof Collidable)
				p.add(v);
			}
			
			// "unscale" V 
			v.scl(1.0f / delta); // un-scale v
		}	
	}
	

	/**
	 * Render level and character bounding boxes.
	 */
	private void renderCharacterBoundingBoxes() {
		// update character BBs
		characterBBs.clear();
		for (int i = 0; i < characters.size; i++) {
			characterBBs.add(characters.get(i).getBoundingBox());	
		}
		
		// render them
		Common.renderBoundingBoxes(mainCam, shapeRenderer, characterBBs);
	}

	/**
	 * Tick all behaviors
	 * @param delta time since last render called
	 */
	private void behaviors(float delta) {
		Iterator<Behaviorable> bit = behaviorables.iterator();
		while (bit.hasNext()) {
			Behaviorable b = bit.next();
			b.tick(delta);
		}
	}
	
	private void update(float delta) {
		// Get commands issued by user and map to real state changes
		translateCommandsToInputs();
		
		// If we're doing frame-by-frame and weren't told to advance the frame, don't
		// do so.
		if (frameByFrame && !nextFrame) {
			return;
		}
		nextFrame = false;
		
		// Zero out accelerations from last update.
		zeroAccelerations();
		
		// Add input force
		sendInputsToInputables();
		
		if (!paused) {
			// Add behavior forces	
			behaviors(delta);
			// Add physics forces.
			physics();
		}
		
		if (!paused) {
			// Advect, taking care of collisions.
			advectWithCollisions(delta);
		}		
		
	}
	
	// ---------------------------------------------------------------------------------------------	
	// PUBLIC (auto-called)
	// ---------------------------------------------------------------------------------------------	
	@Override
	public void render(float delta) {
		// Do non-rendering code
		// ---------------------
		update(delta);
		
		// Actual rendering below.
		// -----------------------

		// Handle GL stuff!
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Report hero stats
		Debug.debugLine("Hero P: " + hero.getP());
		Debug.debugLine("Hero V: " + hero.getV());
		Debug.debugLine("Hero A: " + hero.getA());
		Debug.debugLine("Hero on ground: " + hero.getOnGround());
		
		// Update cameras!
		// Main cam:
		Vector2 heroP = hero.getP();
		Vector3 camPos = mainCam.position;
		mainCam.translate(-camPos.x + heroP.x,
				-camPos.y + heroP.y + Constants.HERO_CAM_OFFSET_Y,
				0.0f);
		mainCam.update();
		batch.setProjectionMatrix(mainCam.combined);
		shapeRenderer.setProjectionMatrix(mainCam.combined);
		Debug.debugLine("Camera pos: " + mainCam.position);
		
		// Overall (e.g. debug, pause) cam
		overallCam.update();
		
		// We're doin' the main viewport.
		gl.glViewport((int) mainViewport.x, (int) mainViewport.y, (int) mainViewport.width,
				(int) mainViewport.height);
		
		// Some counts for rendering...
		int rendered = 0, total = 0;
		
		// Render the map background.
		mapRenderer.setView((OrthographicCamera)mainCam);
		Pair<Integer, Integer> counts = mapRenderer.renderAndCount();
		// TODO render characters in the map renderer
		rendered += counts.first;
		total += counts.second;
		
		Debug.debugLine("Rendered " + rendered + " / " + total + " objects.");
		
		// TODO this shouldn't be necessary
//		// Render character bounding boxes.
//		if (Debug.DEBUG && mapRenderer.renderBoundingBoxes) {
//			renderCharacterBoundingBoxes();
//		}
		
		// Paused overlay
		if (paused) {
			Common.drawPauseOverlay(overallCam, batch, controls.getControlsList());
		}
		if (Debug.DEBUG) {
			Common.displayDebugText(overallCam, batch);
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO(max) call this upon show() as well? (in case resize happened in another screen?)
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		mainViewport.width = screenWidth;
		mainViewport.height = screenHeight;
		mainCam.viewportWidth = screenWidth;
		mainCam.viewportHeight = screenHeight;
		mainCam.position.scl(0.0f);
		// TODO(max): Probably don't want to do this (should set camera position to what it was
		// before if it changed... in fact, maybe this isn't necessary at all?).
		mainCam.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);	
		
		mainCam.update();
		mapRenderer.setView((OrthographicCamera) mainCam);
	}

	@Override
	public void show() {
		mouseState = input.setControls(controls, commandList);
		input.clearAll();
	}

	@Override
	public void hide() {
		// TODO anything needs to be here?
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
		// TODO Auto-generated method stub
	}

}
