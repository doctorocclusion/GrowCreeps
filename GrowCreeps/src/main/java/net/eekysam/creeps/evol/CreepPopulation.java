package net.eekysam.creeps.evol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.Population;

public class CreepPopulation extends ElitisticListPopulation
{
	public final CreepEvolver evolver;
	
	public CreepPopulation(CreepEvolver evolver, List<Chromosome> chroms, double rate)
	{
		super(chroms, chroms.size(), rate);
		this.evolver = evolver;
	}
	
	public CreepPopulation(CreepEvolver evolver, List<Chromosome> chroms, int limit, double rate)
	{
		super(chroms, limit, rate);
		this.evolver = evolver;
	}
	
	public CreepPopulation(CreepEvolver evolver, int limit, double rate)
	{
		super(limit, rate);
		this.evolver = evolver;
	}
	
	public void runSimulation(int generation)
	{
		ArrayList<CreepChrom> chroms = new ArrayList<CreepChrom>(this.getPopulationSize());
		Iterator<Chromosome> it = this.iterator();
		while (it.hasNext())
		{
			Chromosome chrom = it.next();
			if (chrom instanceof CreepChrom)
			{
				chroms.add((CreepChrom) chrom);
			}
		}
		this.evolver.simulate(generation, chroms);
	}
	
	@Override
	public Population nextGeneration()
	{
		Population next = super.nextGeneration();
		CreepPopulation out = new CreepPopulation(this.evolver, this.getPopulationLimit(), this.getElitismRate());
		Iterator<Chromosome> it = next.iterator();
		while (it.hasNext())
		{
			out.addChromosome(it.next());
		}
		return out;
	}
}
