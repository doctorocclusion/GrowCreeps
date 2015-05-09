package net.eekysam.creeps.stat;

import java.util.List;

import net.eekysam.creeps.evol.CreepPopulation;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class CreepSolver
{
	public class EvolveResult
	{
		public List<DescriptiveStatistics> generationStats;
		public CreepPopulation result;
		public double[] finalFitness;
	}
	
	public CreepSolver()
	{
	}
	
	public EvolveResult run(double crossoverRate, double mutationRate, double elitismRate, int generations, int lastSample)
	{
		Evolver evo = this.makeEvolver();
		EvolveResult out = new EvolveResult();
		out.result = evo.evolve(crossoverRate, mutationRate, elitismRate, generations);
		out.generationStats = evo.generationStats;
		out.finalFitness = evo.simulateWorld(out.result.bestCreep(), lastSample);
		return out;
	}
	
	public Evolver makeEvolver()
	{
		return new Evolver();
	}
}
