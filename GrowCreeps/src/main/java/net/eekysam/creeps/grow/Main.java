package net.eekysam.creeps.grow;

import java.util.Random;

import net.eekysam.creeps.grow.sim.FoodObject;
import net.eekysam.creeps.grow.sim.PlayerCreep;
import net.eekysam.creeps.grow.sim.SpikeObject;
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
		int rate = 30;
		double speed = 1.0;
		
		World world = new World(rad, speed * 30.0 / rate);
		new PlayerCreep(10).spawn(world);
		
		Random rand = new Random();
		
		for (int i = 0; i < rand.nextInt(3) + 6; i++)
		{
			FoodObject food = new FoodObject(Math.max(5 + rand.nextGaussian() * 1.2, 1));
			food.spawn(world);
			food.randomLoc(rand);
		}
		
		for (int i = 0; i < rand.nextInt(3) + 3; i++)
		{
			SpikeObject spike = new SpikeObject(Math.max(5 + rand.nextGaussian() * 1.2, 1));
			spike.spawn(world);
			spike.randomLoc(rand);
		}
		
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
