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
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
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
import com.mortrag.ut.wasabi.input.Command;
import com.mortrag.ut.wasabi.input.Controls;
import com.mortrag.ut.wasabi.input.WasabiInput;
import com.mortrag.ut.wasabi.input.WasabiInput.MouseState;
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
	private Camera camera;
	// currently just have one viewport that is the size of the screen	
	private Rectangle mainViewport; 
	// do we need the level w/h? surely...
	private float screenWidth, screenHeight, levelWidth, levelHeight;
	
	// Graphics
	private SpriteBatch batch;
	private TextureAtlas atlas;
	private Animation wasabiRunAnimation;
	
	// Input
	private WasabiInput input;
	private Controls controls;
	private Array<Command> commandList;
	private MouseState mouseState;
	
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
	private Array<Collidable> collidables;
	private Array<Physicsable> physicsables;
	private Array<Advectable> advectables;
	
	// Avoid GC!
	private Vector2 nextP = new Vector2();
	
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
		camera = new OrthographicCamera(screenWidth, screenHeight);
		camera.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);
		camera.update();
		controls = new TestChamber_Controls();
		commandList = new Array<Command>();
		mapRenderer = new TestChamber_MapRenderer(map, batch);
		mapRenderer.setView((OrthographicCamera) camera);
		hero = new Hero(100f, 100f, 140f, 140f); // TODO(max): Figure out w/h automatically.
		addAnimationsToHero();
		
		// collections
		inputs = new Array<Inputable.Input>();
		inputables = new Array<Inputable>();
		inputables.add(hero);
		
		collidables = new Array<Collidable>();
		collidables.add(hero);
		
		physicsables = new Array<Physicsable>();
		physicsables.add(hero);
		
		advectables = new Array<Advectable>();
		advectables.add(hero);
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
	}
	
	// ---------------------------------------------------------------------------------------------
	// PRIVATE
	// ---------------------------------------------------------------------------------------------
	
	private void addAnimationsToHero() {
		hero.animations.put(Action.RUN, new Animation(0.1f, Common.getFrames(atlas, "s_wasRun"),
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
		// TODO(max): Turn objects into collision surfaces.

		Iterator<Advectable> ait = advectables.iterator();
		while (ait.hasNext()) {
			Advectable a = ait.next();
			
			// move first. most vector2 methods mutate the objects, so we explicitly do this.
			a.getA().scl(delta); 
			a.getV().add(a.getA());
			a.getV().scl(delta);
			nextP.scl(0.0f).add(a.getP()).add(a.getV());
			
			// then handle collisions, if it's going to collide
			if (a.collides()) {
				// just doing level boundaries for now... Update this (and setOnGround(...) calls)!!
				if (nextP.x < 0.0f) {
					nextP.x = 0.0f;
					a.getV().scl(0.0f, 1.0f); // stop vx
				} else if (nextP.x + a.getWidth() > levelWidth) {
					nextP.x = levelWidth - a.getWidth();
					a.getV().scl(0.0f, 1.0f); // stop vx
				}
				if (nextP.y < 0.0f) {
					nextP.y = 0.0f;
					a.getV().scl(1.0f, 0.0f); // stop vy
				} else if (nextP.y + a.getHeight() > levelHeight) {
					nextP.y = levelHeight - a.getHeight();
					a.getV().scl(1.0f, 0.0f); // stop vy
				}
				
				// This should be true because of a.collides(), but this is safer..
				if (a instanceof Collidable) {
					Collidable c = (Collidable) a;
					// TODO(max): Make this more complicated check....
					c.setOnGround(nextP.y == 0.0f);
				}
			}
			
			a.getP().set(nextP);
			
			// "reset" A and "unscale" V 
			a.getA().set(Vector2.Zero); // forces (accelerations) zeroed out; recomputed each step
			a.getV().scl(1.0f / delta); // un-scale v
			
			// clamp tiny values to 0
			Vector2 v = a.getV();
			if ((v.x > 0.0f && v.x < Advectable.CLAMP_EPSILON) ||
					(v.x < 0.0f && v.x > -Advectable.CLAMP_EPSILON)) {
				v.x = 0.0f;
			}
			if ((v.y > 0.0f && v.y < Advectable.CLAMP_EPSILON) || 
					(v.y < 0.0f && v.y > -Advectable.CLAMP_EPSILON)) {
				v.y = 0.0f;
			}
			
			// may need to update animations (e.g. falling)
			a.maybeUpdateAnimations(inputs);
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
		
		// Physics!
		physics();
		
		// Report hero stats before A zeroed out
		Debug.debugText.append("Hero P: " + hero.getP() + Constants.NL);
		Debug.debugText.append("Hero V: " + hero.getV() + Constants.NL);
		Debug.debugText.append("Hero A: " + hero.getA() + Constants.NL);
		Debug.debugText.append("Hero on ground: " + hero.getOnGround() + Constants.NL);
		
		// Advect, taking care of collisions.
		advectWithCollisions(delta);
		
		// Handle GL stuff!
		GL20 gl = Gdx.graphics.getGL20();
		gl.glClearColor(1, 1, 1, 1);
		gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		// Update cameras!
		camera.update();
		
		// We're doin' the main viewport.
		gl.glViewport((int) mainViewport.x, (int) mainViewport.y, (int) mainViewport.width,
				(int) mainViewport.height);
		
		// Render the map!
		mapRenderer.render();
		
		// Render the hero!
		hero.render(batch, delta);
		
		// Paused overlay
		if (paused) {
			Common.drawPauseOverlay(camera, batch, controls.getControlsList());
		}
		if (Debug.DEBUG) {
			Common.displayFps(camera, batch);
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO(max) call this upon show() as well? (in case resize happened in another screen?)
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();
		mainViewport.width = screenWidth;
		mainViewport.height = screenHeight;
		camera.viewportWidth = screenWidth;
		camera.viewportHeight = screenHeight;
		camera.position.scl(0.0f);
		// TODO(max): Probably don't want to do this (should set camera position to what it was
		// before if it changed... in fact, maybe this isn't necessary at all?).
		camera.translate(screenWidth/ 2.0f, screenHeight / 2.0f, 0.0f);	
		
		camera.update();
		mapRenderer.setView((OrthographicCamera) camera);
	}

	@Override
	public void show() {
		mouseState = input.setControls(controls, commandList);
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
