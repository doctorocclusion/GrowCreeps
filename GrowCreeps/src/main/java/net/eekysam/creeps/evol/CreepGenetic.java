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
		Population next = super.nextGeneration(current);
		Population out = next;
		/*
		if (current instanceof CreepPopulation)
		{
			CreepPopulation cp = (CreepPopulation) current;
			out = new CreepPopulation(cp.evolver, cp.getPopulationLimit(), cp.getElitismRate());
			out.addChromosome(current.getFittestChromosome());
			Iterator<Chromosome> it = next.iterator();
			while (out.getPopulationSize() < out.getPopulationLimit() && it.hasNext())
			{
				out.addChromosome(it.next());
			}
		}
		*/
		return out;
	}
}
