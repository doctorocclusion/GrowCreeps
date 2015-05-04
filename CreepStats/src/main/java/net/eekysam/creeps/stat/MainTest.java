package net.eekysam.creeps.stat;

import net.eekysam.creeps.evol.CreepPopulation;
import net.eekysam.creeps.grow.sim.World;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class MainTest
{
	public static void main(String[] args) throws LWJGLException
	{
		Display.setDisplayMode(new DisplayMode(600, 600));
		Display.create();
		
		TestEvolver evo = new TestEvolver()
		{
			@Override
			public void startWorld(boolean render, int generation)
			{
				Display.setTitle(String.format("GrowCreeps | Gen - %d", generation));
				
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				
				Display.update();
			}
			
			@Override
			public void preRenderTick(double time, World world)
			{
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glOrtho(-150, 150, -150, 150, 1, -1);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
			}
			
			@Override
			public void postRenderTick()
			{
				Display.sync(30);
				Display.update();
			}
		};
		
		CreepPopulation pop = evo.evolve(0.2, 0.2, 0.5);
		evo.lockRender = true;
		pop.runSimulation(1000);
		
		Display.destroy();
	}
}
