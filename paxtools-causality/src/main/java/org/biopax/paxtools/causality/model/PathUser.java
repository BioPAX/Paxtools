package org.biopax.paxtools.causality.model;

/**
 * While a path is being built, if a PathUSer was provided, its processPath method is called at each
 * addition to the path.
 *
 * @author Ozgun Babur
 */
public interface PathUser
{
	public void processPath(Path path);
}
