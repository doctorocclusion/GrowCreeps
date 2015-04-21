package net.eekysam.creeps.grow.sim;

import java.util.Random;

public abstract class BrownianObject extends WorldObject
{
	public static final double velMax = 0.2;
	public static final double acc = 0.01;
	public static final double fric = 0.98;
	
	public Random rand;
	
	public BrownianObject(double radius)
	{
		super(radius);
		this.rand = new Random();
	}
	
	@Override
	public void tick(double rate, EnumTickPass pass)
	{
		super.tick(rate, pass);
		if (pass == EnumTickPass.MOVE)
		{
			this.velx += this.rand.nextGaussian() * acc;
			this.vely += this.rand.nextGaussian() * acc;
			
			double f = fric;
			if (rate != 1.0)
			{
				f = Math.exp(Math.log(f) * rate);
			}
			this.velx *= f;
			this.vely *= f;
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
