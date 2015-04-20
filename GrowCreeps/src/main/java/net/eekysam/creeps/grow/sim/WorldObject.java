package net.eekysam.creeps.grow.sim;

import java.util.UUID;

public abstract class WorldObject
{
	private World theWorld;
	
	private boolean remove = false;
	
	public double x;
	public double y;
	
	public double radius;
	
	public double velx;
	public double vely;
	
	public final UUID id;
	
	public WorldObject()
	{
		this.id = UUID.randomUUID();
	}
	
	public void spawn(World world)
	{
		if (this.theWorld == null)
		{
			this.theWorld = world;
			this.theWorld.addSpawn(this);
		}
	}
	
	public void tick(double rate, EnumTickPass pass)
	{
		if (pass == EnumTickPass.MOVE)
		{
			double nx = this.x += this.velx * rate;
			double ny = this.y += this.vely * rate;
			
			if (ny < 0)
			{
				ny = 0;
				this.wallCollision(0, -1);
				if (this.vely < 0)
				{
					this.vely = 0;
				}
			}
			else if (ny > this.theWorld.height)
			{
				ny = this.theWorld.height;
				this.wallCollision(0, 1);
				if (this.vely > 0)
				{
					this.vely = 0;
				}
			}
			
			if (nx < 0)
			{
				nx = 0;
				this.wallCollision(-1, 0);
				if (this.velx < 0)
				{
					this.velx = 0;
				}
			}
			else if (nx > this.theWorld.width)
			{
				nx = this.theWorld.width;
				this.wallCollision(1, 0);
				if (this.velx > 0)
				{
					this.velx = 0;
				}
			}
			
			this.x = nx;
			this.y = ny;
		}
	}
	
	public void wallCollision(double dx, double dy)
	{
		
	}
	
	public void collision(WorldObject other, double distsqr, double dot)
	{
		
	}
	
	public World world()
	{
		return this.theWorld;
	}
	
	public boolean isRemoved()
	{
		return this.remove;
	}
	
	public abstract int getColor();
	
	public abstract double getHardness();
	
	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o instanceof WorldObject)
		{
			return this.id.equals(((WorldObject) o).id);
		}
		return false;
	}
	
	public void render()
	{
		
	}
}
