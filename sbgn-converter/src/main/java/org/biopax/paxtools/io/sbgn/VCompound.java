package org.biopax.paxtools.io.sbgn;

import java.util.List;
import java.util.ArrayList;
import org.ivis.layout.LGraphObject;
import org.ivis.layout.LGraphObject;
import org.ivis.layout.LNode;
import org.ivis.layout.Updatable;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Bbox;
import org.ivis.layout.cose.CoSEGraph;


/**
 * VCompound Class
 * @author: Istemi Bahceci
 */
class VCompound extends VNode implements Updatable
{
	public List<VNode> children;
	
	/**
	 * Default Constructor, creates a VCompound node by given glyph
	 * @param g Glyph object that VCompound object will be created from.
	 * */
	public VCompound(Glyph g)
	{
		super(g);
		this.children = new ArrayList();
	}
	
	/**
	 * Returns the child list of this VCompound
	 * @return child list of this VCompound.
	 * */
	public List<VNode> getChildren()
	{
		return children;
	}	
}