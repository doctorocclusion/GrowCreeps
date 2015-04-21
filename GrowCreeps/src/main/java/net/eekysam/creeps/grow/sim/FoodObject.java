package net.eekysam.creeps.grow.sim;

public class FoodObject extends BrownianObject
{
	public FoodObject(double radius)
	{
		super(radius);
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
}
