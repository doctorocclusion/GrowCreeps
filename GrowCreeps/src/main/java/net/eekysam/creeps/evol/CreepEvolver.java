package net.eekysam.creeps.evol;

import java.util.List;

import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.SelectionPolicy;
import org.apache.commons.math3.genetics.StoppingCondition;

public abstract class CreepEvolver
{
	public abstract List<Chromosome> getInitial();
	
	public abstract CrossoverPolicy crossover();
	
	public abstract MutationPolicy mutation();
	
	public abstract SelectionPolicy selection();
	
	public abstract StoppingCondition condition();
	
	public abstract void simulate(int generation, List<CreepChrom> chroms);
	
	public CreepPopulation evolve(double crossoverRate, double mutationRate, double elitismRate)
	{
		CreepGenetic gen = new CreepGenetic(this.crossover(), crossoverRate, this.mutation(), mutationRate, this.selection());
		return ((CreepPopulation) gen.evolve(new CreepPopulation(this, this.getInitial(), elitismRate), this.condition()));
	}
}
