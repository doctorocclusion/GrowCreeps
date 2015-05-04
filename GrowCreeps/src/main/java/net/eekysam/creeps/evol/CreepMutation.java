package net.eekysam.creeps.evol;

import java.util.Random;
import java.util.function.BiFunction;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.util.DummyLocalizable;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.apache.commons.math3.genetics.MutationPolicy;

import com.google.common.primitives.Doubles;

public abstract class CreepMutation implements MutationPolicy, BiFunction<Double, Random, Double>
{
	public final int minChanges;
	public final int maxChanges;
	
	public CreepMutation(int minChanges, int maxChanges)
	{
		this.minChanges = minChanges;
		this.maxChanges = maxChanges;
	}
	
	@Override
	public Chromosome mutate(Chromosome original) throws MathIllegalArgumentException
	{
		if (!(original instanceof CreepChrom))
		{
			throw new InvalidRepresentationException(new DummyLocalizable("creep mutation only works with CreepChrom"));
		}
		CreepChrom chrom = (CreepChrom) original;
		double[] data = chrom.data.clone();
		Random rand = new Random();
		int num = this.minChanges;
		if (this.maxChanges > this.minChanges)
		{
			num += rand.nextInt(this.maxChanges - this.minChanges + 1);
		}
		for (int i = 0; i < num; i++)
		{
			int j = rand.nextInt(data.length);
			data[j] = this.apply(data[j], rand);
		}
		return chrom.newFixedLengthChromosome(Doubles.asList(data));
	}
}
