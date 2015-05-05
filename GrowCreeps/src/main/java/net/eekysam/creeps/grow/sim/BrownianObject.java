package net.eekysam.creeps.grow.sim;

import java.util.Random;

public abstract class BrownianObject extends WorldObject
{
	public static final double velMax = 0.2;
	public static final double acc = 0.01;
	public static final double fric = 0.98;
	
	public Random rand;
	
	public double adjfric;
	
	public BrownianObject(double radius)
	{
		super(radius);
		this.rand = new Random();
	}
	
	@Override
	public void spawn(World world)
	{
		super.spawn(world);
		this.adjfric = Math.exp(Math.log(fric) * this.speed);
	}
	
	@Override
	public void tick(EnumTickPass pass)
	{
		super.tick(pass);
		if (pass == EnumTickPass.MOVE)
		{
			this.velx += (this.rand.nextDouble() * 2 - 1) * acc;
			this.vely += (this.rand.nextDouble() * 2 - 1) * acc;
			
			this.velx *= this.adjfric;
			this.vely *= this.adjfric;
			double vel = this.velx * this.velx + this.vely * this.vely;
			if (vel > velMax * velMax)
			{
				double scale = velMax / Math.sqrt(vel);
				this.velx *= scale;
				this.vely *= scale;
			}
		}
	}
}
