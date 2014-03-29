package com.mortrag.ut.wasabi.graphics;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.mortrag.ut.wasabi.characters.WasabiCharacter;
import com.mortrag.ut.wasabi.util.Constants.LE;
import com.mortrag.ut.wasabi.util.Debug;

public class Common {	
	
	private static final String PAUSED_TEXT = "THIS SHIT'S PAUSED";
	private static final float TEXT_MARGIN = 10.0f; // spacing we give b/w "paused" and commands.
	private static final int ANIMATION_START_NUM = 0; // how Cooper numbers animations
	
	private static final BitmapFont debugFont = makeFont(Color.BLACK, 1.2f), pausedFont =
			makeFont(Color.GRAY, 10.0f), controlsFont = makeFont(Color.WHITE, 1.2f);
	private static float w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();	
	private static Sprite pauseOverlaySprite = makePauseOverlaySprite();
	
	private static BitmapFont makeFont(Color color, float scale) {
		BitmapFont font = new BitmapFont();
		font.setColor(color);
		font.setScale(scale);
		return font;
	}	
	
	private static Sprite makePauseOverlaySprite() {
		// precompute pause overlay into sprite
		Pixmap pixmap = new Pixmap( (int)w, (int)h, Format.RGBA8888 );
		pixmap.setColor( 0f, 0.5f, 1f, 0.8f );
		pixmap.fillRectangle(0, 0, (int)w, (int)h);
		Texture pixmaptex = new Texture( pixmap );
		pixmap.dispose();
		Sprite s = new Sprite(pixmaptex, (int)w, (int)h);
		s.setPosition(0.0f, 0.0f);
		return s;
	}
	
	private static void recomputePauseOverlaySprite() {
		// update w and h, and set a new sprite
		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();
		pauseOverlaySprite = makePauseOverlaySprite();				
	}
	
	private static void maybeRecomputePauseOverlaySprite() {
		// only recompute if w or h has changed 
		if (w != Gdx.graphics.getWidth() || h != Gdx.graphics.getHeight()) {
			recomputePauseOverlaySprite();
		}
	}

	public static void drawPauseOverlay(Camera camera, SpriteBatch batch, String controlsList) {
		// ensure pause overlay is up to date
		maybeRecomputePauseOverlaySprite();
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		// pixmap!?		
		pauseOverlaySprite.draw(batch);

		// draw paused txt
		pausedFont.drawWrapped(batch, PAUSED_TEXT, 0.0f, h, w, HAlignment.CENTER);
		float pausedTxtHeight = pausedFont.getBounds(PAUSED_TEXT).height;
		
		// draw controls
		controlsFont.drawWrapped(batch, controlsList, 0.0f, h - pausedTxtHeight -
				TEXT_MARGIN, w);
		
		batch.end();		
	}
	
	/**
	 * Displays FPS in top-let corner. No idea if works when camera's not overall.
	 * @param batch
	 * @param c
	 */
	public static void displayDebugText(Camera camera, SpriteBatch batch) {
		// Append FPS; get & clear the debug buffer.
		Debug.debugText.append("FPS: " + Gdx.graphics.getFramesPerSecond());
		String debugStr = Debug.debugText.toString();
		Debug.debugText.delete(0, Debug.debugText.length());
		
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		debugFont.drawWrapped(batch, debugStr, 0.0f, Gdx.graphics.getHeight() - LE.LAYER_BOX_HEIGHT
				- (LE.LAYER_BOX_MARGIN * 2), Gdx.graphics.getWidth(), HAlignment.LEFT);
		batch.end();
	}	
	
	/**
	 * Creates new array each time--careful!
	 * @param atlas All 'dem sprites.
	 * @param name String to match filename of. Appends 0, 1, ... until it can't find any more.
	 * @return
	 */
	public static Array<TextureRegion> getFrames(TextureAtlas atlas, String name) {
		int i = ANIMATION_START_NUM;
		Array<TextureRegion> frames = new Array<TextureRegion>();
		while(true) {
			//TextureRegion frame = atlas.findRegion(name + i++);	  
			TextureRegion frame = atlas.createSprite(name + i++);
			if (frame == null) {
				break;
			}
			frames.add(frame);
		}
		return frames;
	}
	
	public static void renderBoundingBoxes(Camera c, ShapeRenderer sr, Array<BoundingBox> boxes) {
		renderBoundingBoxes(c.combined, sr, boxes);
	}
	
	public static void renderBoundingBoxes(Matrix4 projectionMatrix, ShapeRenderer sr, Array<BoundingBox> boxes) {
		sr.setProjectionMatrix(projectionMatrix);
		sr.begin(ShapeType.Line);
		sr.setColor(1.0f, 0.0f, 0.0f, 0.7f);
		
		Iterator<BoundingBox> bit = boxes.iterator();
		while (bit.hasNext()) {
			BoundingBox b = bit.next();
			sr.rect(b.min.x, b.min.y, b.max.x - b.min.x, b.max.y - b.min.y);
		}
		
		sr.end();		
	}
	
	public static void getTextureWH(TextureRegion tex, Vector2 out) {
		// If we use getRegion* on a packed (whitespace stripped) sprite, then we get
		// the compressed width, when we want the full one (as all frames of an
		// animation are set to the same W and H for consistent bounding boxes and
		// animation).

		if (tex instanceof AtlasSprite) {
			out.x = ((AtlasSprite) tex).getWidth();
			out.y = ((AtlasSprite) tex).getHeight();
		} else if (tex instanceof AtlasRegion) {
			out.x = ((AtlasRegion) tex).getRotatedPackedWidth();
			out.y = ((AtlasRegion) tex).getRotatedPackedHeight();
		} else {
			out.x = tex.getRegionWidth();
			out.y = tex.getRegionHeight();
		}		
	}
}
