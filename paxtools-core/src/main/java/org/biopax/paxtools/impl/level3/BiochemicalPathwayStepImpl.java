package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;


public class BiochemicalPathwayStepImpl extends PathwayStepImpl implements BiochemicalPathwayStep
{
	private Conversion stepConversion;

	private StepDirection stepDirection;

	private Set<Process> stepProcess = new StepProcessSet();

	private Logger log = LoggerFactory.getLogger(BiochemicalPathwayStepImpl.class);

	public BiochemicalPathwayStepImpl()
	{
	}

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
	public Class<? extends BiochemicalPathwayStep> getModelInterface()
	{
		return BiochemicalPathwayStep.class;
	}

	//
	// biochemicalPathwayStep interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property stepConversion
	public Conversion getStepConversion()
	{
		return stepConversion;
	}

	public void setStepConversion(Conversion highLander)
	{
		if (this.stepConversion != null)
		{
			if (this.stepConversion == highLander) 
				return;
			else { 
				synchronized (this.stepConversion) {
					this.stepConversion.getStepProcessOf().remove(this);
				}
			}
		}
		
		this.stepConversion = highLander; //can be null!
		
		if(this.stepConversion != null) {
			synchronized (this.stepConversion) {
				this.stepConversion.getStepProcessOf().add(this);
			}
		}
	}

	@Override
	public void addStepProcess(Process process)
	{
		if (process instanceof Conversion)
		{ // restriction: BiochemicalPathwayStep.stepProcess can contain Control interactions only
			if (this.stepConversion == null) {
				//auto-fix gently...
				log.warn("Auto-fix: put Conversion " + process.getUri() +
						" as stepConversion instead of stepProcess of the BiochemicalPathwayStep");
				setStepConversion((Conversion) process);
			} else if (this.stepConversion == process) {
				log.info("Ignoring addStepProcess as the Conversion was set as BiochemicalPathwayStep/stepConversion");
			} else {
				throw new IllegalBioPAXArgumentException(
					"BiochemicalPathwayStep can have only one Conversion. Did you mean to use " +
						"the setStepConversion method instead?");
			}
		} else if (process == null || process instanceof Control){
			super.addStepProcess(process);
		} else {
			throw new IllegalBioPAXArgumentException(
					"Only Control processes can be in BiochemicalPathwayStep/stepProcess");
		}
	}

	// Property STEP-DIRECTION
	public StepDirection getStepDirection()
	{
		return stepDirection;
	}

	public void setStepDirection(StepDirection newSTEP_DIRECTION)
	{
		stepDirection = newSTEP_DIRECTION;
	}

	@Override
	public Set<Process> getStepProcess()
	{
		return stepProcess;
	}


	private class StepProcessSet extends AbstractSet<Process> implements Serializable
	{
		@Override public Iterator<Process> iterator()
		{
			final Iterator<Process> proci = BiochemicalPathwayStepImpl.super.getStepProcess().iterator();

			return new Iterator<Process>()
			{
				boolean procEnd = false;

				@Override public boolean hasNext()
				{
					return proci.hasNext() || !(procEnd || stepConversion == null);
				}

				@Override public Process next()
				{
					if (procEnd) throw new NoSuchElementException();
					if (proci.hasNext()) return proci.next();
					else
					{
						procEnd = true;
						if (stepConversion == null) throw new NoSuchElementException();
						else return stepConversion;
					}
				}

				@Override public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override public int size()
		{
			return BiochemicalPathwayStepImpl.super.getStepProcess().size() + (stepConversion == null ? 0 : 1);
		}

		@Override public boolean contains(Object o)
		{
			return o != null &&
				((stepConversion != null && stepConversion.equals(o))
					|| BiochemicalPathwayStepImpl.super.getStepProcess().contains(o));
		}

	}
}
