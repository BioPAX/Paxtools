package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.ModelImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Named;
import org.biopax.paxtools.model.level3.XReferrable;
import org.biopax.paxtools.model.level3.Xref;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a simple BioPAX merger, a utility class to merge one 
 * (normalized) biopax model into the other, 
 * based on the RDFId (URI) identity.
 * <p/>
 * Note that it skips (does not copy "source" elements to "target" nor - update/merge their object properties)
 * if the target (model) has got the elements with the same IDs (thus only new elements are copied and refreshed).
 * <p/>
 * <p/>
 * Note also that this merger does not preserve the integrity of the passed models! 'Target' will be a
 * merged model and 'source' may become unusable.
 * <p/>
 * Use With Care!
 */
public class SimpleMerger
{
	private static final Log log = LogFactory.getLog(SimpleMerger.class);

	private final EditorMap map;

	/**
	 * @param map a class to editor map for the elements to be modified.
	 */
	public SimpleMerger(EditorMap map)
	{
		this.map = map;
	}


	/**
	 * Merges the <em>source</em> model into <em>target</em> model.
	 *
	 * @param target model into which merging process will be done
	 * @param source model that is going to be merged with <em>target</em>
	 */
	public void merge(Model target, Model source)
	{
		// this may work not as expected for some Models...
		if (!(target instanceof ModelImpl))
		{
			log.warn("'target': using user's Model implementation, "
			         + target.getClass().getCanonicalName());
		}
		if (!(source instanceof ModelImpl))
		{
			log.warn("'source': using user's Model implementation,"
			         + source.getClass().getCanonicalName());
		}

		// get all the objects from source, iterate
		Set<BioPAXElement> sourceElements = source.getObjects();
		for (BioPAXElement bpe : sourceElements)
		{
			BioPAXElement paxElement = target.getByID(bpe.getRDFId());
			/* 
			 * if there is present the element with the same id, skip,
			 * do not merge this one (see the warning below...)
			 */
			if (paxElement == null)
			{
				target.add(bpe);
				/* Warning: 
				 * concrete target Model implementations
				 * may add not only 'bpe' but also
				 * all its dependents (using cascades/recursion); 
				 * it might also override target's properties
				 * with the corresponding ones from the source, 
				 * even though SimpleMerger avoids this; 
				 * also, is such cases, the number of times
				 * this loop body is called can be less that
				 * the number of elements in sourceElements set 
				 * that were't originally present in the target 
				 * model, or - even equals to one)
				 */
			}
		}
		
		// Now that target model has all the unique IDs from both models,
		// "re-wire" object relationships:
		
		/*
		 * One may think she could iterate over 
		 * newly added elements only.., but, in fact,
		 * life is tricky, and models can be inconsistent, 
		 * and a 'target' model's element may have already
		 * pointed to a 'source' element, and vice versa...
		 * 
		 * So, we'll refresh the objects properties (re-link to target's) 
		 * for not only those just added elements, but also (and at least) all 'source' elements!
		 * (doing this for all 'target' could catch more fish but would be wasting of resources 
		 * in 99% of situations that I can imagine...)
		 */
		for (BioPAXElement bpe : sourceElements)
		{
			updateObjectFields(bpe, target);
		}
	}

	/**
	 * Merges the <em>source</em> element and its "downstream" dependents into <em>target</em> model.
	 *
	 * @param target
	 * @param source
	 */
	public void merge(Model target, BioPAXElement source)
	{
		Model m = map.getLevel().getDefaultFactory().createModel();
		(new Fetcher(map)).fetch(source, m);
		merge(target, m);
	}


	/**
	 * Updates each value of <em>existing</em> element, using the value(s) of <em>update</em>.
	 *
	 * @param update BioPAX element of which values are used for update
	 */
	private void updateObjectFields(BioPAXElement update, Model target)
	{
		Set<PropertyEditor> editors =
				map.getEditorsOf(update);
		for (PropertyEditor editor : editors)
		{
			if (editor instanceof ObjectPropertyEditor)
			{
				if (editor.isMultipleCardinality())
				{
					Set<BioPAXElement> values = new HashSet<BioPAXElement>(
							(Set<BioPAXElement>) editor.getValueFromBean(update));
					for (BioPAXElement value : values) // threw concurrent modification exception here; fixed above.
					{
						migrateToTarget(update, target, editor, value);
					}
				}
				else
				{
					BioPAXElement value = (BioPAXElement) editor.getValueFromBean(update);
					migrateToTarget(update, target, editor, value);
				}
			}
		}
	}


	private void migrateToTarget(BioPAXElement update, Model target,
	                             PropertyEditor editor, BioPAXElement value)
	{
		if (value != null)
		{
			BioPAXElement newValue = target.getByID(value.getRDFId());
			if (newValue == null)
			{
				throw new IllegalStateException("Target model must " +
				                                "have got the element with id=" + value.getRDFId()
				                                + " at this point, but getById returned null!");
			}
			if (editor.isMultipleCardinality())
			{
				editor.removeValueFromBean(value, update);
			}
			editor.setValueToBean(newValue, update);
		}
	}

	
	/**
	 * This method either puts the new element under the required ID 
	 * (updates if the old ID different) to the model or removes the
	 * (old) element and uses another existing one (with the specified ID) instead
	 * (it then updates the internal object references as well)
	 * 
	 * @param target - Model
	 * @param bpe - element to update (must be present in the model)
	 * @param targetRdfid - the ID to use (another element in the model might have the same ID; then the element will be wisely ignored)
	 * @return
	 */
	public void merge(final Model target, final BioPAXElement bpe, final String targetRdfid) 
	{	
		// anything to do at all?
		if(bpe == null || targetRdfid == null 
			|| "".equals(targetRdfid.trim())) 
		{
			if(log.isWarnEnabled())
				log.warn("Won't update (null or empty element/ID argument)!");
			return;
		}
				
		String currentId = bpe.getRDFId();
		
		// if the target model contains the (target) element/ID
		final BioPAXElement v = target.getByID(targetRdfid);
		if(v == bpe) { 
			// the element is already there known under the targetID
			assert(currentId.equals(targetRdfid)); // hardly possible otherwise... ;)
			// nothing to do except for the quick fix... (and if java assersions are off)
			if(!currentId.equals(targetRdfid)) {
				log.error("model.idMap: key is not in sync with the value bpe's rdfid!");
				bpe.setRDFId(targetRdfid);
			}
		} 
		else if(v != null) { 
			// model does contain a different (from bpe) object with the required targetID 
			
			/* we could simply set the new ID and use merge(model, bpe) method, 
			 * but must take extra care about the not equivalent replacement 
			 * and the situation when elements in the target model refer to this bpe elememnt 
			 * as well as its replacement 'v'! 
			 * It's possible for inconsistent models (linking to external elements...)
			 */
			
			// we'll use that existing element as the replacement for the bpe...
			if(log.isInfoEnabled())
				log.info("Target element found! " +
					"Now re-setting object properties to "
					+ v + " ("+ targetRdfid + ") from " 
					+ bpe + " (" + bpe.getRDFId() + "), where found");
			
			// TODO do our best for not equivalent elements (it may happen...)
			if(!v.isEquivalent(bpe)) {
				String msg = "Resulting (target) element: " +
				v + " (" + v.getRDFId() + ", " + v.getModelInterface().getSimpleName()
				+ ") MIGHT be of a DIFFERENT type or semantics from: " + 
				bpe + " (" + bpe.getRDFId() + ", " + bpe.getModelInterface().getSimpleName() 
				+ ")!";
				// are they at least of the same type?
				if(v.getModelInterface().equals(bpe.getModelInterface())) {
					log.error(msg); // can live with it

					 // TODO what about things like Uniprot isoforms (e.g., Q9BVL2-2)?
					
					 // TODO think to skip it at all, but for now - will copy names, xrefs, and comments
					if(v instanceof XReferrable 
						&& bpe instanceof XReferrable) { // the second is for sure ;)
						// copy at least something...
						for(Xref x : ((XReferrable)bpe).getXref()) {
							((XReferrable) v).addXref(x);
						}
						
						((XReferrable) v).getComment()
							.addAll(((XReferrable) bpe).getComment());
						
						if(v instanceof Named) {
							((Named)v).getName().addAll(((Named)bpe).getName());
						}
					}
				} else {
					throw new RuntimeException(msg); // too bad!
				}
			}
			
			AbstractTraverser traverser = new AbstractTraverser(map) {
				@Override
				protected void visit(Object range, BioPAXElement domain, Model model,
						PropertyEditor editor) {
					if(editor instanceof ObjectPropertyEditor && bpe.equals(range)) {
						// replaces the reference to 'bpe' (range) with the 'v'
						if(editor.isMultipleCardinality()) {
							editor.removeValueFromBean(bpe, domain);
						}
						editor.setValueToBean(v, domain);
						if(log.isDebugEnabled()) {
							log.debug("Replaced " + bpe.getRDFId() + 
								" with " + v.getRDFId() + "; " + editor.toString() + 
								"; (domain) bean: " + domain);
						}
					}
				}
			};
			
			// traverse starting from each "parent" object
			for(BioPAXElement element : target.getObjects()) {
				traverser.traverse(element, target);
			}
			// remove the (now dangling) object
			target.remove(bpe);
			
			// smoke test...
			if(bpe instanceof Xref)
				assert(((Xref)bpe).getXrefOf().isEmpty());
			else if(bpe instanceof EntityReference)
				assert(((EntityReference)bpe).getEntityReferenceOf().isEmpty());
		} 
		else if(target.contains(bpe)) { 
			// replace ID of existing object
			target.updateID(bpe.getRDFId(), targetRdfid);
		} else {
			/* 
			 * it's a new object (which, by the way, may still refer to target's elements), 
			 * which also requires the new ID to be set
			 */
			if(log.isInfoEnabled())
				log.info("The target model does not have neither element: " +
					bpe + " nor ID: " + targetRdfid +
					"; so we'll update the ID for the element " +
					"and merge it to the model.");
			bpe.setRDFId(targetRdfid);
			merge(target, bpe);
		}
	}
}