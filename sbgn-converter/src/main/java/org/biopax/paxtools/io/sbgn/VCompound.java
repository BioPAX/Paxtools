package org.biopax.paxtools.io.sbgn;

import java.util.List;
import java.util.ArrayList;
import org.ivis.layout.LGraphObject;


/**
 * VCompound Class
 * @author: istemi Bahceci
 * */
 

public class VCompound extends VNode
{
	public VCompound()
	{
		super();
		this.children = new ArrayList();
	}
	
	public List<VNode> getChildren()
	{
		return children;
	}
	
	public List<VNode> children;
}