package net.eekysam.creeps.grow;

import java.util.Random;

import net.eekysam.creeps.grow.sim.AICreep;
import net.eekysam.creeps.grow.sim.FoodObject;
import net.eekysam.creeps.grow.sim.SpikeObject;
import net.eekysam.creeps.grow.sim.World;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.pattern.ElmanPattern;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

public class Main
{
	public static void main(String[] args) throws LWJGLException
	{
		Display.setDisplayMode(new DisplayMode(600, 600));
		Display.create();
		
		int rad = 150;
		int rate = 15;
		double warp = 1.0;
		double speed = 2.0;
		
		World world = new World(rad, warp * 30.0 / rate);
		//new PlayerCreep(10).spawn(world);
		
		CreepSpec spec = new CreepSpec();
		ElmanPattern elman = new ElmanPattern();
		elman.setInputNeurons(11);
		elman.setOutputNeurons(5);
		elman.addHiddenLayer(20);
		elman.setActivationFunction(new ActivationSigmoid());
		spec.network = (BasicNetwork) elman.generate();
		
		Random rand = new Random();
		
		for (int i = 0; i < 8; i++)
		{
			spec.network.reset();
			AICreep creep = new AICreep(spec, null, 10);
			creep.spawn(world);
			creep.randomLoc(rand);
		}
		
		for (int i = 0; i < rand.nextInt(4) + 7; i++)
		{
			FoodObject food = new FoodObject(Math.max(5 + rand.nextGaussian() * 1.2, 1));
			food.spawn(world);
			food.randomLoc(rand);
		}
		
		for (int i = 0; i < rand.nextInt(3) + 5; i++)
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
			Display.sync((int) (rate * speed));
			Display.update();
		}
		
		Display.destroy();
	}
}
