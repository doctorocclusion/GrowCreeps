package net.eekysam.creeps.grow.sim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.GL11;

public class World
{
	public final double radius;
	
	public final int wallColor = 0xC0C0C0;
	public final double ambiHardness = 0.1;
	
	public final double speed;
	
	public boolean doRender = true;
	
	private ArrayDeque<WorldObject> toSpawn = new ArrayDeque<WorldObject>();
	private ArrayList<WorldObject> objects = new ArrayList<>();
	
	public ArrayList<Collision> collisions = new ArrayList<>();
	
	public World(double radius, double speed)
	{
		this.radius = radius;
		this.speed = speed;
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
			GL11.glColor3d(0.1, 0.1, 0.1);
			renderCircle(0, 0, this.radius, 30, 0, 2 * Math.PI);
			for (WorldObject obj : this.objects)
			{
				obj.render();
			}
		}
		else
		{
			for (WorldObject obj : this.objects)
			{
				obj.tick(this.speed, pass);
			}
		}
	}
	
	public List<WorldObject> getObjects()
	{
		return Collections.unmodifiableList(this.objects);
	}
	
	public static void renderCircle(double x, double y, double r, double n, double start, double end)
	{
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		GL11.glVertex2d(x, y);
		double w = (end - start) / n;
		for (int i = 0; i <= n; i++)
		{
			double th = w * i + start;
			GL11.glVertex2d(x + Math.cos(th) * r, y + Math.sin(th) * r);
		}
		GL11.glEnd();
	}
	
	public static double rad(double degrees)
	{
		return (degrees / 180.0) * Math.PI;
	}
}
