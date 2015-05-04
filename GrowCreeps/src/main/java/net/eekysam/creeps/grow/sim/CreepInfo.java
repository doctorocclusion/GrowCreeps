package net.eekysam.creeps.grow.sim;

import java.util.EnumMap;

public class CreepInfo
{
	public long age = 0;
	public double adjustedAge = 0;
	
	public double health;
	public double food;
	
	public double foodEaten;
	public double foodLost;
	
	public EnumMap<EnumDmgType, Double> dmgTaken;
	
	public CreepInfo()
	{
		this.dmgTaken = new EnumMap<>(EnumDmgType.class);
		for (EnumDmgType type : EnumDmgType.values())
		{
			this.dmgTaken.put(type, 0.0);
		}
	}
}
