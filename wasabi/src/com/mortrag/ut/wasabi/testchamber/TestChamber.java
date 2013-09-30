package com.mortrag.ut.wasabi.testchamber;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
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
import com.mortrag.ut.wasabi.characters.Collidable;
import com.mortrag.ut.wasabi.characters.Hero;
import com.mortrag.ut.wasabi.characters.Hero.Action;
import com.mortrag.ut.wasabi.characters.Inputable;
import com.mortrag.ut.wasabi.characters.Inputable.Input;
import com.mortrag.ut.wasabi.characters.Physicsable.Physics;
import com.mortrag.ut.wasabi.characters.Physicsable;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.graphics.WasabiTextureMapObject;
import com.mortrag.ut.wasabi.input.Command;
import com.mortrag.ut.wasabi.input.Controls;
import com.mortrag.ut.wasabi.input.WasabiInput;
//import com.mortrag.ut.wasabi.input.WasabiInput.MouseState;
import com.mortrag.ut.wasabi.leveleditor.LevelEditor;
import com.mortrag.ut.wasabi.util.Constants;
import com.mortrag.ut.wasabi.util.Debug;

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
	private TextureAtlas atlas;
	
	// Input
	private WasabiInput input;
	private Controls controls;
	private Array<Command> commandList;
	//private MouseState mouseState;
	
	// Map
	private Map map;
	private TestChamber_MapRenderer mapRenderer;
	
	// State
	private boolean paused = false;
	// TODO(max): Refactor into hero
	
	// Physics!
	BoundingBox b = new BoundingBox();
	
	// Characters!
	private Hero hero;
	private Array<Inputable> inputables;
	private Array<Inputable.Input> inputs; // for use in handleCommands(...)
//	private Array<Collidable> collidables;
	private Array<Physicsable> physicsables;
	private Array<Advectable> advectables;
	private Array<BoundingBox> boundaries;
	
	// Avoid GC!
	
	// ---------------------------------------------------------------------------------------------
	// CONSTRUCTORS
	// ---------------------------------------------------------------------------------------------	
	
	public TestChamber(WasabiGame game, WasabiInput input, Map map, SpriteBatch batch,
			TextureAtlas atlas) {		
		this.game = game;
		this.input = input;
		this.map = map;
		MapProperties mapProperties = map.getProperties();
		levelWidth = (Float) mapProperties.get(Constants.MP.LEVEL_WIDTH);
		levelHeight = (Float) mapProperties.get(Constants.MP.LEVEL_HEIGHT);
		this.batch = batch;
		this.atlas = atlas;
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		mainViewport = new Rectangle(0, 0, screenWidth, screenHeight);
		mainCam = new OrthographicCamera(screenWidth, screenHeight);
		mainCam.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);
		mainCam.update();
		overallCam = new OrthographicCamera(screenWidth, screenHeight);
		overallCam.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);
		overallCam.update();
		controls = new TestChamber_Controls();
		commandList = new Array<Command>();
		mapRenderer = new TestChamber_MapRenderer(map, batch);
		mapRenderer.setView((OrthographicCamera) mainCam);
		hero = new Hero(100f, 100f, 140f, 140f); // TODO(max): Figure out w/h automatically.
		addAnimationsToHero();
		
		// collections
		inputs = new Array<Inputable.Input>();
		inputables = new Array<Inputable>();
		inputables.add(hero);
		
//		collidables = new Array<Collidable>();
//		collidables.add(hero);
		
		physicsables = new Array<Physicsable>();
		physicsables.add(hero);
		
		advectables = new Array<Advectable>();
		advectables.add(hero);
		
		boundaries = new Array<BoundingBox>();
		
		// map collision layers
		updateBoundingBoxes();
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
	}
	
	// ---------------------------------------------------------------------------------------------
	// PRIVATE
	// ---------------------------------------------------------------------------------------------
	
	private void updateBoundingBoxes() {
		boundaries.clear();
		Iterator<MapLayer> lit = map.getLayers().iterator();		
		while (lit.hasNext()) {
			MapLayer layer = lit.next();
			if ((Boolean) layer.getProperties().get(Constants.MP.COLLIDABLE)) {
				addBoundingBoxes(layer, boundaries);
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
	
	private void addAnimationsToHero() {
		hero.animations.put(Action.RUN, new Animation(0.07f, Common.getFrames(atlas, "s_wasRun"),
				Animation.LOOP));
		hero.animations.put(Action.JUMP, new Animation(0.1f, Common.getFrames(atlas, "s_wasJump"),
				Animation.NORMAL));
		hero.animations.put(Action.FALL, new Animation(0.1f, Common.getFrames(atlas, "s_wasFall"),
				Animation.NORMAL));
		hero.animations.put(Action.IDLE, new Animation(0.1f, Common.getFrames(atlas, "s_wasIdle"),
				Animation.LOOP_PINGPONG));
		hero.animations.put(Action.ATTACK, new Animation(0.1f, Common.getFrames(atlas, "s_wasAtk"),
				Animation.NORMAL));
	}
	
	private void addBoundingBoxes(MapLayer layer, Array<BoundingBox> boxes) {
		Iterator<MapObject> oit = layer.getObjects().iterator();
		while (oit.hasNext()) {
			MapObject mapObject = oit.next();
			// Right now we only deal with our custom WasabiTextureMapObjects...
			if (mapObject instanceof WasabiTextureMapObject) {
				boxes.add(((WasabiTextureMapObject) mapObject).getBoundingBox());
			}
		}
	}
	
	private void backToEditor() {
		if (!game.screenLoaded(LevelEditor.NAME)) {
			// Screen hasn't been loaded--make it!
			game.addScreen(new LevelEditor(game, input), LevelEditor.NAME);
		}
		game.getAndSetScreen(LevelEditor.NAME);		
	}
	
	private void handleCommands() {
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

		// process inputables. These variable names are horrible. Sorry.
		Iterator<Input> iit = inputs.iterator();
		while (iit.hasNext()) {
			Input i = iit.next();
			Iterator<Inputable> inpit = inputables.iterator();
			while (inpit.hasNext()) {
				Inputable ip = inpit.next();
				ip.input(i);
			}
		}
		
		// removes the inputs that were just PRESS actions
		input.clearPress();
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
	
	private void advectWithCollisions(float delta) {
		// debug info
		Debug.debugText.append("Num collision boundaries: " + boundaries.size + Constants.NL);
		
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
			
			// then handle collisions, if it's going to collide (double check)
			if (adv.collides() && adv instanceof Collidable) {
				// NOTE(max): pbb (prevBoundingBox) is useless as soon as the position changes!!! So
				// we extract and save what we care about in locals.
				BoundingBox pbb = ((Collidable) adv).getBoundingBox();
				float prevMinX = pbb.min.x, prevMinY = pbb.min.y, prevMaxX = pbb.max.x,
						prevMaxY = pbb.max.y;
				
				p.add(v); // set now so bounding box wil be updated; will correct if collisions
				
				//boolean hitGround = false;
				boolean onGround = false;
				
				// TODO(max): Can easily optimize. See ETH ref'd site, or just do level zones.
				Iterator<BoundingBox> bit = boundaries.iterator();
				while (bit.hasNext()) {
					// Need to recompute object bounding box in case it changes. Probably shouldn't
					// actually do this for every other object but...
					BoundingBox objBox = ((Collidable) adv).getBoundingBox();
					BoundingBox boundary = bit.next();
					if (objBox.intersects(boundary)) {
						// Figure out what kind of intersection... maybe this part means the whole
						// boundary box thing is useless...
						if (prevMinX >= boundary.max.x && objBox.min.x < boundary.max.x) {
							// collide on left
							p.set(boundary.max.x, p.y);
							v.scl(0.0f, 1.0f); // stop vx
						} else if (prevMaxX <= boundary.min.x && objBox.max.x > boundary.min.x) {
							// collide on right
							p.set(p.x - (objBox.max.x - boundary.min.x), p.y);
							v.scl(0.0f, 1.0f); // stop vx
						}
						if (prevMinY >= boundary.max.y && objBox.min.y < boundary.max.y) {
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
				((Collidable) adv).setOnGround(onGround); 
			} else {
				p.add(v); // just move
			}
			
			// "reset" A and "unscale" V 
			a.set(Vector2.Zero); // forces (accelerations) zeroed out; recomputed each step
			v.scl(1.0f / delta); // un-scale v
			
			// clamp tiny values to 0
			if ((v.x > 0.0f && v.x < Advectable.CLAMP_EPSILON) ||
					(v.x < 0.0f && v.x > -Advectable.CLAMP_EPSILON)) {
				v.x = 0.0f;
			}
			if ((v.y > 0.0f && v.y < Advectable.CLAMP_EPSILON) || 
					(v.y < 0.0f && v.y > -Advectable.CLAMP_EPSILON)) {
				v.y = 0.0f;
			}
			
			// may need to update animations (e.g. falling)
			adv.maybeUpdateAnimations(inputs);
		}
		
		
	}

	
	// ---------------------------------------------------------------------------------------------	
	// PUBLIC (auto-called)
	// ---------------------------------------------------------------------------------------------	
	@Override
	public void render(float delta) {
		// Input!
		handleCommands();
		
		// Enemies!
		// (TBD)
		
		if (!paused) {
			// Physics!
			physics();
		}
		
		// Report hero stats before A zeroed out
		Debug.debugText.append("Hero P: " + hero.getP() + Constants.NL);
		Debug.debugText.append("Hero V: " + hero.getV() + Constants.NL);
		Debug.debugText.append("Hero A: " + hero.getA() + Constants.NL);
		Debug.debugText.append("Hero on ground: " + hero.getOnGround() + Constants.NL);
		
		if (!paused) {
			// Advect, taking care of collisions.
			advectWithCollisions(delta);
		}
		
		// Handle GL stuff!
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Update cameras!
		// Main cam:
		Vector2 heroP = hero.getP();
		Vector3 camPos = mainCam.position;
		mainCam.translate(-camPos.x + heroP.x,
				-camPos.y + heroP.y + Constants.HERO_CAM_OFFSET_Y,
				0.0f);
		mainCam.update();
		batch.setProjectionMatrix(mainCam.combined);
		Debug.debugText.append("Camera pos: " + mainCam.position + Constants.NL);
		
		// Overall (e.g. debug, pause) cam
		overallCam.update();
		
		// We're doin' the main viewport.
		gl.glViewport((int) mainViewport.x, (int) mainViewport.y, (int) mainViewport.width,
				(int) mainViewport.height);
		
		// Render the map!
		mapRenderer.render();
		
		// Render the hero!
		hero.render(batch, delta);
		
		// Paused overlay
		if (paused) {
			Common.drawPauseOverlay(overallCam, batch, controls.getControlsList());
		}
		if (Debug.DEBUG) {
			Common.displayFps(overallCam, batch);
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
		//mouseState = input.setControls(controls, commandList);
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
