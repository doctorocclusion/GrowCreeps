package net.eekysam.creeps.evol;

import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.SelectionPolicy;

class CreepGenetic extends GeneticAlgorithm
{
	public CreepGenetic(CrossoverPolicy crossoverPolicy, double crossoverRate, MutationPolicy mutationPolicy, double mutationRate, SelectionPolicy selectionPolicy) throws OutOfRangeException
	{
		super(crossoverPolicy, crossoverRate, mutationPolicy, mutationRate, selectionPolicy);
	}
	
	@Override
	public Population nextGeneration(final Population current)
	{
		if (current instanceof CreepPopulation)
		{
			((CreepPopulation) current).runSimulation(this.getGenerationsEvolved());
		}
		return super.nextGeneration(current);
	}
}
