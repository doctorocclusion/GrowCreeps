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
			private int gen;
			
			@Override
			public void startWorld(boolean render, int generation)
			{
				this.gen = generation;
				
				Display.setTitle(String.format("GrowCreeps | Gen - %d", this.gen));
				
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				
				renderGraph(this);
				
				Display.update();
			}
			
			@Override
			public void preRenderTick(double time, World world)
			{
				Display.setTitle(String.format("GrowCreeps | Gen - %d | t - %.0f", this.gen, time * world.speed));
				
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				
				GL11.glMatrixMode(GL11.GL_PROJECTION);
				GL11.glLoadIdentity();
				GL11.glOrtho(-150, 150, -150, 150, 1, -1);
				GL11.glMatrixMode(GL11.GL_MODELVIEW);
			}
			
			@Override
			public void postRenderTick()
			{
				renderGraph(this);
				
				Display.sync(30);
				Display.update();
			}
		};
		
		CreepPopulation pop = evo.evolve(0.1, 0.4, 0.4);
		evo.lockRender = true;
		pop.runSimulation(evo.generations);
		
		Display.destroy();
	}
	
	public static void renderGraph(TestEvolver evo)
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-1, 1, -1, 1, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		double max = evo.allStats.getMax();
		double min = evo.allStats.getMin();
		int num = evo.runStats.size();
		
		if (num > 1)
		{
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glColor3d(0.0, 0.0, 0.8);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (int i = 0; i < num; i++)
			{
				double y = evo.runStats.get(i).getMean();
				y -= min;
				y /= max - min;
				double x = (double) i / (num - 1);
				GL11.glVertex2d((x - 0.5) * 1.9, y * 0.2 + 0.75);
			}
			GL11.glEnd();
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}
}
