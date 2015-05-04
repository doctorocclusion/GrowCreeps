package net.eekysam.creeps.evol;

import java.util.List;

import net.eekysam.creeps.grow.CreepSpec;

import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.genetics.AbstractListChromosome;
import org.apache.commons.math3.genetics.InvalidRepresentationException;
import org.encog.neural.networks.BasicNetwork;

import com.google.common.primitives.Doubles;

public class CreepChrom extends AbstractListChromosome<Double>
{
	public CreepSpec spec;
	public BasicNetwork brain;
	
	public double[] data;
	
	public double fitness = Double.NaN;
	
	private CreepChrom(CreepSpec spec, BasicNetwork network, List<Double> chrom, boolean load) throws InvalidRepresentationException
	{
		super(chrom);
		this.spec = spec;
		
		this.brain = (BasicNetwork) network.clone();
		if (load)
		{
			this.checkValidity(chrom);
			this.data = Doubles.toArray(chrom);
			this.brain.decodeFromArray(this.data);
		}
		else
		{
			this.data = new double[this.brain.encodedArrayLength()];
			this.brain.encodeToArray(this.data);
		}
	}
	
	public CreepChrom(CreepSpec spec, BasicNetwork network, List<Double> chrom) throws InvalidRepresentationException
	{
		this(spec, network, chrom, true);
	}
	
	@Override
	public double fitness()
	{
		return this.fitness;
	}
	
	@Override
	public AbstractListChromosome<Double> newFixedLengthChromosome(List<Double> chrom)
	{
		return new CreepChrom(this.spec, this.brain, chrom);
	}
	
	public static CreepChrom make(CreepSpec spec, BasicNetwork network)
	{
		double[] gen = new double[network.encodedArrayLength()];
		network.encodeToArray(gen);
		return new CreepChrom(spec, network, Doubles.asList(gen), false);
	}
	
	@Override
	protected void checkValidity(List<Double> chrom) throws InvalidRepresentationException
	{
		if (this.brain == null)
		{
			return;
		}
		int a = chrom.size();
		int b = this.brain.encodedArrayLength();
		if (a != b)
		{
			throw new InvalidRepresentationException(LocalizedFormats.DIMENSIONS_MISMATCH_SIMPLE, a, b);
		}
	}
}
