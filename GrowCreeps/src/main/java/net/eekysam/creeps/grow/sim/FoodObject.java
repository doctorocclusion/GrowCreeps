package net.eekysam.creeps.grow.sim;

public class FoodObject extends BrownianObject
{
	public FoodObject(double radius)
	{
		super(radius);
	}
	
	@Override
	public void tick(double rate, EnumTickPass pass)
	{
		super.tick(rate, pass);
		if (pass == EnumTickPass.COMPUTE)
		{
			if (this.radius < 0)
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
		return 0.3;
	}
	
	@Override
	public void collision(WorldObject other, double distsqr, double dot)
	{
		if (other instanceof Creep)
		{
			Creep creep = (Creep) other;
			if (creep.food < Creep.foodMax)
			{
				creep.food += 2;
				if (this.radius > 0)
				{
					this.radius -= 0.2;
				}
			}
		}
	}
}
