package com.mortrag.ut.wasabi.characters;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public interface Renderable {
	public Vector2 getP();
	public void render(SpriteBatch batch, float delta);
}
