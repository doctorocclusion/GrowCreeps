package net.eekysam.creeps.grow;

import net.eekysam.creeps.grow.sim.PlayerCreep;
import net.eekysam.creeps.grow.sim.World;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class Main
{
	public static void main(String[] args) throws LWJGLException
	{
		Display.setDisplayMode(new DisplayMode(500, 500));
		Display.create();
		
		int rad = 100;
		int rate = 5;
		
		World world = new World(rad);
		world.accel = 30.0 / rate;
		new PlayerCreep().spawn(world);
		
		while (!Display.isCloseRequested())
		{
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(-rad, rad, -rad, rad, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			
			world.tick();
			Display.sync(rate);
			Display.update();
		}
		
		Display.destroy();
	}
}
