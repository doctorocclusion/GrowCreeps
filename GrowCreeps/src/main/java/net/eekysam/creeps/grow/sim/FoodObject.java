package net.eekysam.creeps.grow.sim;

public class FoodObject extends BrownianObject
{
	public boolean start = true;
	public boolean move = false;
	
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
		else if (pass == EnumTickPass.LAST)
		{
			if (this.start)
			{
				if (this.move)
				{
					this.randomLoc(this.rand);
				}
				else
				{
					this.start = false;
				}
			}
			this.move = false;
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
	public void spawn(World world)
	{
		super.spawn(world);
		this.start = true;
	}
	
	@Override
	public void collision(WorldObject other, double distsqr, double velx, double vely)
	{
		if (other instanceof Creep)
		{
			this.move = true;
			Creep creep = (Creep) other;
			if (creep.info.food < creep.spec.maxFood)
			{
				double speed = this.world().speed;
				double food = 0.4;
				if (this.radius > 0)
				{
					this.radius -= (0.2 * food * speed);
				}
				food *= (creep.action1 + 0.3);
				creep.info.food += 2 * food * speed;
				creep.info.foodEaten += 2 * food * speed;
			}
			this.velx += 0.02 * (this.x - creep.x) / this.radius;
			this.vely += 0.02 * (this.y - creep.y) / this.radius;
		}
	}
}
