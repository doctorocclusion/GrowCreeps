package net.eekysam.creeps.grow.sim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class World
{
	public final double width;
	public final double height;
	
	public final int wallColor = 0xC0C0C0;
	public final double ambiHardness = 0.1;
	
	public double accel = 1.0;
	
	public boolean doRender = true;
	
	private ArrayDeque<WorldObject> toSpawn = new ArrayDeque<WorldObject>();
	private ArrayList<WorldObject> objects = new ArrayList<>();
	
	public ArrayList<Collision> collisions = new ArrayList<>();
	
	public World(double width, double height)
	{
		this.width = width;
		this.height = height;
	}
	
	public void addSpawn(WorldObject object)
	{
		this.toSpawn.add(object);
	}
	
	public void tick()
	{
		for (EnumTickPass pass : EnumTickPass.values())
		{
			if (pass != EnumTickPass.RENDER || this.doRender)
			{
				this.tick(pass);
			}
		}
	}
	
	public void tick(EnumTickPass pass)
	{
		if (pass == EnumTickPass.UPDATE_LIST)
		{
			Iterator<WorldObject> objsit = this.objects.iterator();
			while (objsit.hasNext())
			{
				if (objsit.next().isRemoved())
				{
					objsit.remove();
				}
			}
			while (!this.toSpawn.isEmpty())
			{
				this.objects.add(this.toSpawn.pop());
			}
		}
		else if (pass == EnumTickPass.COLLISIONS)
		{
			this.collisions.clear();
			for (int i = 0; i < this.objects.size(); i++)
			{
				WorldObject a = this.objects.get(i);
				
				for (int j = i + 1; j < this.objects.size(); j++)
				{
					WorldObject b = this.objects.get(j);
					
					double dist = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
					
					if (dist <= (a.radius + b.radius) * (a.radius + b.radius))
					{
						this.collisions.add(new Collision(a, b, dist, a.velx * b.velx + a.vely * b.vely));
					}
				}
			}
			for (Collision col : this.collisions)
			{
				col.a.collision(col.b, col.distsqr, col.velDot);
				col.b.collision(col.a, col.distsqr, col.velDot);
			}
		}
		else if (pass == EnumTickPass.RENDER)
		{
			for (WorldObject obj : this.objects)
			{
				obj.render();
			}
		}
		else
		{
			for (WorldObject obj : this.objects)
			{
				obj.tick(this.accel, pass);
			}
		}
	}
	
	public List<WorldObject> getObjects()
	{
		return Collections.unmodifiableList(this.objects);
	}
}
