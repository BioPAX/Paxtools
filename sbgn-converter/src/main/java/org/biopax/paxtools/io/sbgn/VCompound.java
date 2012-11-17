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
	
	/**
	 *
	 *  Function that will take place when VNode objects will update in layout process of ChiLay
	 * @Override
	 * @param lGraphObj LGraphObject for whom the update will take place.
	 * */
	public void update(LGraphObject lGraphObj) 
	{
		if (lGraphObj instanceof CoSEGraph) 
		{
			return;
		}
		
		LNode lNode = (LNode)lGraphObj;
		
		this.glyph.getBbox().setX((float) lNode.getLeft());
		this.glyph.getBbox().setY((float) lNode.getTop()); 
		
		this.placeStateAndInfoGlyphs();
		
	}
	
	
}