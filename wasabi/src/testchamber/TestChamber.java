package testchamber;

import java.util.Iterator;

import leveleditor.LevelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.WasabiGame;
import com.mortrag.ut.wasabi.characters.Hero;
import com.mortrag.ut.wasabi.characters.Hero.Action;
import com.mortrag.ut.wasabi.graphics.Common;
import com.mortrag.ut.wasabi.input.Command;
import com.mortrag.ut.wasabi.input.Controls;
import com.mortrag.ut.wasabi.input.WasabiInput;
import com.mortrag.ut.wasabi.input.WasabiInput.MouseState;
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
	private float animTime = 0.0f;
	// TODO(max): Refactor into hero
	
	// Characters!
	private Hero hero;
	
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
		hero = new Hero();
		addAnimationsToHero();
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
					hero.moveRight();
					break;
				case MOVE_LEFT:
					hero.moveLeft();
					break;
				case JUMP:
					//curSpriteNudge(0, 1);
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
				}
			}	
		} // else (if paused)

		// removes the inputs that were just PRESS actions
		input.clearPress();
	}
	
	// TODO(max): yeah...
	private void physics() {
		
	}

	
	// ---------------------------------------------------------------------------------------------	
	// PUBLIC (auto-called)
	// ---------------------------------------------------------------------------------------------	
	@Override
	public void render(float delta) {
		Debug.print(delta);
		// Input!
		handleCommands();
		
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
