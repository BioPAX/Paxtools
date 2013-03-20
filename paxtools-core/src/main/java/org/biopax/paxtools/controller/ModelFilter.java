package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.Model;

/**
 * A generic filter interface for Models.
 */
public interface ModelFilter {

	Model filter(final Model model);
	
}
