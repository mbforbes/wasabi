package com.mortrag.ut.wasabi.client;

import com.mortrag.ut.wasabi.WasabiGame;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class GwtLauncher extends GwtApplication {
	@Override
	public GwtApplicationConfiguration getConfig () {
		GwtApplicationConfiguration cfg = new GwtApplicationConfiguration(1680, 1050);
		return cfg;
	}

	@Override
	public ApplicationListener getApplicationListener () {
		return new WasabiGame();
	}
}