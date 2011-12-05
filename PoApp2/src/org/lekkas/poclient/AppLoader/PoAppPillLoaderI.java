package org.lekkas.poclient.AppLoader;

public interface PoAppPillLoaderI {
	public void killRunningApp();
	public void loadAndStartApp(String path);
}
