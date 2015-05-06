package net.eekysam.creeps.stat;

import java.util.function.Predicate;

import net.eekysam.creeps.evol.CreepPopulation;
import net.eekysam.creeps.grow.sim.World;
import net.eekysam.creeps.stat.TestEvolver.WorldRun;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class MainTest
{
	public static void main(String[] args) throws LWJGLException
	{
		Display.setDisplayMode(new DisplayMode(600, 600));
		Display.create();
		Keyboard.create();
		Keyboard.enableRepeatEvents(false);
		
		TestEvolver evo = new TestEvolver()
		{
			private int gen;
			
			@Override
			public void endGeneration(int generation, SummaryStatistics stats)
			{
				this.gen = generation;
				
				Display.setTitle(String.format("GrowCreeps | Gen - %d | r = %.3f", this.gen, this.maxRegression.getR()));
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				renderGraph(this);
				Display.update();
			}
			
			@Override
			public void preRenderTick(double time, World world)
			{
				Display.setTitle(String.format("GrowCreeps | Gen - %d | t - %.0f", this.gen, time));
				
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
		
		evo.doRender = new Predicate<TestEvolver.WorldRun>()
		{
			boolean render = true;
			
			@Override
			public boolean test(WorldRun t)
			{
				while (Keyboard.next())
				{
					if (Keyboard.getEventKey() == Keyboard.KEY_TAB && Keyboard.getEventKeyState())
					{
						this.render = !this.render;
					}
				}
				return this.render;
			}
		};
		
		CreepPopulation pop = evo.evolve(0.2, 0.7, 0.5, 300);
		pop.runSimulation(300);
		
		Display.destroy();
	}
	
	public static void renderGraph(TestEvolver evo)
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(-1, 1, -1, 1, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		double max = evo.maxStats.getMax();
		double min = evo.meanStats.getMin();
		int num = evo.generationStats.size();
		
		if (num > 1)
		{
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			GL11.glColor3d(0.8, 0.0, 0.0);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (int i = 0; i < num; i++)
			{
				double y = evo.generationStats.get(i).getMax();
				y -= min;
				y /= max - min;
				drawPoint((double) i / (num - 1), y);
			}
			GL11.glEnd();
			GL11.glColor3d(0.0, 0.0, 0.8);
			GL11.glBegin(GL11.GL_LINE_STRIP);
			for (int i = 0; i < num; i++)
			{
				double y = evo.generationStats.get(i).getMean();
				y -= min;
				y /= max - min;
				drawPoint((double) i / (num - 1), y);
			}
			GL11.glEnd();
			GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
		}
	}
	
	public static void drawPoint(double x, double y)
	{
		GL11.glVertex2d((x - 0.5) * 1.9, y * 0.5);
	}
}
