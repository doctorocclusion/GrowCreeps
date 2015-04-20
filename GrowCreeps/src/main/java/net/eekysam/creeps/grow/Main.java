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
		
		World world = new World(300, 300);
		new PlayerCreep().spawn(world);
		
		while (!Display.isCloseRequested())
		{
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, 300, 0, 300, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			
			world.tick();
			Display.sync(30);
			Display.update();
		}
		
		Display.destroy();
	}
}
