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
 * @author: istemi Bahceci
 * */
 

public class VCompound extends VNode implements Updatable
{
	public List<VNode> children;
	
	public VCompound(Glyph g)
	{
		super(g);
		this.children = new ArrayList();
	}
	
	public List<VNode> getChildren()
	{
		return children;
	}	
}