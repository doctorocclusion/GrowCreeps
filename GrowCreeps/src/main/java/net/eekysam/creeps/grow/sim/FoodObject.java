package net.eekysam.creeps.grow.sim;

public class FoodObject extends BrownianObject
{
	public FoodObject(double radius)
	{
		super(radius * 1.2);
	}
	
	@Override
	public void tick(EnumTickPass pass)
	{
		super.tick(pass);
		if (pass == EnumTickPass.COMPUTE)
		{
			if (this.radius <= 0.2)
			{
				this.kill();
			}
		}
	}
	
	@Override
	public int getColor()
	{
		return 0x00D000;
	}
	
	@Override
	public double getHardness()
	{
		return 1;
	}
	
	@Override
	public void collision(WorldObject other, double distsqr, double velx, double vely)
	{
		if (other instanceof Creep)
		{
			Creep creep = (Creep) other;
			if (creep.info.food < creep.spec.maxFood)
			{
				double speed = this.world().speed;
				double food = 0.6;
				creep.info.food += 2 * food * speed;
				creep.info.foodEaten += 2 * food * speed;
				if (this.radius > 0)
				{
					this.radius -= (0.2 * food * speed) * 1.5;
				}
			}
		}
	}
}
