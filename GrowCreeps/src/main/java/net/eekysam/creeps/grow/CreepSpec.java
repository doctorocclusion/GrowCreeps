package net.eekysam.creeps.grow;

public class CreepSpec
{
	public double radius = 10;
	
	public int baseColor = 0x0000D0;
	public double rotSpeed = 0.05;
	public double maxVel = 0.7;
	public double accel = 0.10;
	public double accelSide = 0.06;
	public double fric = 0.05;
	
	public double maxHealth = 200;
	public double maxFood = 100;
	public double startingFood = 50;
	
	public void copyFrom(CreepSpec spec)
	{
		this.radius = spec.radius;
		
		this.baseColor = spec.baseColor;
		this.rotSpeed = spec.rotSpeed;
		this.maxVel = spec.maxVel;
		this.accel = spec.accel;
		this.accelSide = spec.accelSide;
		this.fric = spec.fric;
		
		this.maxHealth = spec.maxHealth;
		this.maxFood = spec.maxFood;
		this.startingFood = spec.startingFood;
	}
}
