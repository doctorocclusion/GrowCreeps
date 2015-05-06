package net.eekysam.creeps.grow.sim;

import java.util.EnumMap;

import net.eekysam.creeps.grow.CreepSpec;

public class CreepInfo
{
	public CreepSpec spec;
	
	public long age = 0;
	public double adjustedAge = 0;
	
	public double health;
	public double food;
	
	public double foodEaten;
	public double foodLost;
	
	public EnumMap<EnumDmgType, Double> dmgTaken;
	
	public CreepInfo(CreepSpec spec)
	{
		this.spec = spec;
		this.dmgTaken = new EnumMap<>(EnumDmgType.class);
		for (EnumDmgType type : EnumDmgType.values())
		{
			this.dmgTaken.put(type, 0.0);
		}
	}
	
	public double fitness()
	{
		double spike = 0;
		if (this.dmgTaken.containsKey(EnumDmgType.SPIKE))
		{
			spike = this.dmgTaken.get(EnumDmgType.SPIKE);
		}
		spike /= this.spec.maxHealth;
		return Math.sqrt(this.adjustedAge / 1000) * 2.0 - (spike * spike) * 1.7 + Math.sqrt(this.foodEaten / this.spec.maxFood) * 3.0;
	}
}
