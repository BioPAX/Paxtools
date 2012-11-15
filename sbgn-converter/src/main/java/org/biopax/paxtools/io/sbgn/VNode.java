package org.biopax.paxtools.io.sbgn;

import java.util.ArrayList;
import java.util.Iterator;

import org.ivis.layout.LGraphObject;
import org.ivis.layout.LNode;
import org.ivis.layout.Updatable;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Bbox;
import org.ivis.layout.cose.CoSEGraph;


/**
 * VNode class
 * @author: istemi Bahceci
 * */

public class VNode implements Updatable
{
	//Glyph attribute of this VNode
	public Glyph glyph;
	public int clusterID;
	
	ArrayList<Glyph> stateGlyphs;
	ArrayList<Glyph> infoGlyphs;
	
	
	/*Glyph class types*/
	private static final String MACROMOLECULE = "macromolecule";
	private static final  String UNIT_OF_INFORMATION = "unit of information";
	private static final  String STATE_VARIABLE = "state variable";
	private static final  String SOURCE_AND_SINK = "source and sink";
	private static final  String ASSOCIATION = "association";
	private static final  String DISSOCIATION = "dissociation";
	private static final  String OMITTED_PROCESS = "omitted process";
	private static final  String UNCERTAIN_PROCESS = "uncertain process";
	private static final  String SIMPLE_CHEMICAL = "simple chemical";
	private static final  String PROCESS = "process";
	private static final  String COMPLEX = "complex";
	private static final  String AND = "and";
	private static final  String OR = "or";
	private static final  String NOT = "not";
	private static final  String PHENOTYPE = "phenotype";
	private static final  String PERTURBING_AGENT = "perturbing agent";
	private static final  String TAG = "tag";
	private static final  String NUCLEIC_ACID_FEATURE = "nucleic acid feature";
	private static final  String UNSPECIFIED_ENTITY = "unspecified entity";
	
	// Constats used for determining "state of information" and "unit of information" glyph widths according to
	// their labels
	private static final  int LOWERCASE_LETTER_PIXEL_WIDTH = 6;
	private static final  int UPPERCASE_LETTER_PIXEL_WIDTH = 9;

	private static final  int MAX_STATE_AND_INFO_WIDTH = 60;
	
	private static final  int MAX_STATE_AND_INFO_HEIGHT = 20;
	
	private static final  int OFFSET_BTW_INFO_GLYPHS = 10;

	/*Glyph Size Constants for layout*/
	private Bound  SOURCE_AND_SINK_BOUND;
	private Bound  LOGICAL_OPERATOR_BOUND;
	private Bound  PROCESS_NODES_BOUND;
	private Bound  MACROMOLECULE_BOUND;
	private Bound  NUCLEIC_ACID_FEATURE_BOUND;
	private Bound  SIMPLE_CHEMICAL_BOUND;
	private Bound  UNSPECIFIED_ENTITY_BOUND;
	private Bound  PHENOTYPE_BOUND;
	private Bound  PERTURBING_AGENT_BOUND;
	private Bound  TAG_BOUND;
	private Bound  INFO_BOUND;
	private Bound  STATE_BOUND;
	
		
	/**
	 * Default Constructor, sets the geometry of the bounds which are attributes of this class
	 * */
	public VNode()
	{
		SOURCE_AND_SINK_BOUND = new Bound(60,60);
		LOGICAL_OPERATOR_BOUND = new Bound(30,30);
		PROCESS_NODES_BOUND = new Bound(20,20);
		
		MACROMOLECULE_BOUND = new Bound(60,40);
		NUCLEIC_ACID_FEATURE_BOUND = new Bound(140,60);
		
		SIMPLE_CHEMICAL_BOUND = new Bound(40,40);
		UNSPECIFIED_ENTITY_BOUND = new Bound(100,60);
		PHENOTYPE_BOUND = new Bound(140,60);
		TAG_BOUND = new Bound(140,60);
		PERTURBING_AGENT_BOUND = new Bound(140,60);
		
		INFO_BOUND = new Bound(MAX_STATE_AND_INFO_WIDTH,MAX_STATE_AND_INFO_HEIGHT);
		STATE_BOUND = new Bound(MAX_STATE_AND_INFO_WIDTH,MAX_STATE_AND_INFO_HEIGHT);
		
		stateGlyphs = new ArrayList<Glyph>();
		infoGlyphs = new ArrayList<Glyph>();
		
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
		
		this.glyph.getBbox().setX((float) lNode.getRect().getX());
		this.glyph.getBbox().setY((float) lNode.getRect().getY()); 
		
		placeStateAndInfoGlyphs();
	}
	
	/**
	 * Sets the bound of this VNode by given width and height
	 * @param w new width
	 * @param h new height
	 * */
	public void setBounds(float w, float h)
	{
		this.glyph.getBbox().setW(w);
		this.glyph.getBbox().setH(h);
	}
	
	/**
	 * Chooses a proper bound for this VNode according to its class. 
	 * */
	public void setSizeAccordingToClass()
	{
		String glyphClass = this.glyph.getClazz();
		
		/*
		 * need to add bbox objects
		 * */
		 
		Bbox b = new Bbox();
		this.glyph.setBbox(b);
		
		this.glyph.getBbox().setX(300);
		this.glyph.getBbox().setY(300);
		
		if (glyphClass == SOURCE_AND_SINK) 
		{
			setBounds(SOURCE_AND_SINK_BOUND.getWidth(), SOURCE_AND_SINK_BOUND.getHeight());
		} 
		
		else if (glyphClass == AND || glyphClass == OR || glyphClass == NOT )
		{
			setBounds(LOGICAL_OPERATOR_BOUND.getWidth(), LOGICAL_OPERATOR_BOUND.getHeight());
		}
		
		else if (glyphClass == ASSOCIATION || glyphClass == DISSOCIATION || glyphClass == OMITTED_PROCESS || 
				 glyphClass == UNCERTAIN_PROCESS || glyphClass  == PROCESS)
		{
			setBounds(PROCESS_NODES_BOUND.getWidth(), PROCESS_NODES_BOUND.getHeight());
		}
		
		else if (glyphClass == SIMPLE_CHEMICAL)
		{
			setBounds(SIMPLE_CHEMICAL_BOUND.getWidth(), SIMPLE_CHEMICAL_BOUND.getHeight());
		}
		
		else if (glyphClass == UNSPECIFIED_ENTITY) 
		{
			setBounds(UNSPECIFIED_ENTITY_BOUND.getWidth(), UNSPECIFIED_ENTITY_BOUND.getHeight());
		}
		
		else if (glyphClass == MACROMOLECULE) 
		{
			setBounds(MACROMOLECULE_BOUND.getWidth(), MACROMOLECULE_BOUND.getHeight());
		}
		
		else if (glyphClass == NUCLEIC_ACID_FEATURE) 
		{
			setBounds(NUCLEIC_ACID_FEATURE_BOUND.getWidth(), NUCLEIC_ACID_FEATURE_BOUND.getHeight());
		}
		
		else if (glyphClass == STATE_VARIABLE) 
		{
			setBounds(STATE_BOUND.getWidth(), STATE_BOUND.getHeight());
		}
		
		else if (glyphClass == UNIT_OF_INFORMATION)
		{
			setBounds(INFO_BOUND.getWidth(), INFO_BOUND.getHeight());
		}
		
		else if (glyphClass == PHENOTYPE)
		{
			setBounds(PHENOTYPE_BOUND.getWidth(), PHENOTYPE_BOUND.getHeight());
		}
		
		else if (glyphClass == PERTURBING_AGENT)
		{
			setBounds(PERTURBING_AGENT_BOUND.getWidth(), PERTURBING_AGENT_BOUND.getHeight());
		}
		
		else if (glyphClass == TAG)
		{
			setBounds(TAG_BOUND.getWidth(), TAG_BOUND.getHeight());
		}
		else
			setBounds(0, 0);
		
		if (glyphClass == MACROMOLECULE || glyphClass == NUCLEIC_ACID_FEATURE || glyphClass == SIMPLE_CHEMICAL || glyphClass == COMPLEX) 
		{
			updateSizeForStateAndInfo();
		}
		
		
	}
	
	public int setInfoGlyphSizeAccordingToLabel(ArrayList stateList)
	{
		int wholeSize = 0;
		for (Iterator iterator = stateList.iterator(); iterator.hasNext();) 
		{
			Glyph tmpGlyph = (Glyph) iterator.next();
			
			String tmpStr = tmpGlyph.getState().getValue();
			
			if(tmpGlyph.getState().getVariable() != "")
				tmpStr += "@" +tmpGlyph.getState().getVariable(); 
			
			int numOfUpper = 0;
			int numOfLower = 0;
			     
		    for(int i=0; i < tmpStr.length(); i++) 
		    {
		        if(Character.isLowerCase(tmpStr.charAt(i))) 
		        {
		        	numOfLower++;

		        }
		        else
		            numOfUpper++;
		    }
		    
			Bbox b = new Bbox();
			tmpGlyph.setBbox(b);
		    
		    float requiredSize = numOfLower * LOWERCASE_LETTER_PIXEL_WIDTH + numOfUpper * UPPERCASE_LETTER_PIXEL_WIDTH;
		    
		    if(requiredSize < MAX_STATE_AND_INFO_HEIGHT )
				tmpGlyph.getBbox().setW(requiredSize);
		    else
		    	tmpGlyph.getBbox().setW(STATE_BOUND.width);
		    
		    tmpGlyph.getBbox().setH(MAX_STATE_AND_INFO_HEIGHT);
		    
		    wholeSize += tmpGlyph.getBbox().getW();
				
		}
		
	    return wholeSize;
	}
	
	/**
	 * 	If glyph attribute of this VNode object includes any "state of information" or "unit of information" glyphs, this method updates the
	 * size of VNode accordingly.
	 * */
	public void updateSizeForStateAndInfo()
	{
		
		// Find all state and info glyphs
		for (Iterator iterator = this.glyph.getGlyph().iterator(); iterator.hasNext();)
		{
			Glyph glyph = (Glyph) iterator.next();
			
			if (glyph.getClazz() == STATE_VARIABLE) 
			{
				stateGlyphs.add(glyph);
			}
			
			else if (glyph.getClazz() == UNIT_OF_INFORMATION)
			{
				infoGlyphs.add(glyph);
			}		
		}
		
		//Calculate "state of information" glyphs' sizes
		int wholeWidthOfStates = setInfoGlyphSizeAccordingToLabel(stateGlyphs);
		int wholeWidthOfInfos  = setInfoGlyphSizeAccordingToLabel(infoGlyphs);
		
		// Calculate  positions
		int numOfStates = stateGlyphs.size();
		int numOfInfos = infoGlyphs.size();
		
		float requiredWidthForStates = (numOfStates+1) * OFFSET_BTW_INFO_GLYPHS + wholeWidthOfStates;
		float requiredWidthForInfos = (numOfStates+1) * OFFSET_BTW_INFO_GLYPHS + wholeWidthOfInfos;
		
		
		if (this.glyph.getBbox().getW() < requiredWidthForStates || this.glyph.getBbox().getW() < requiredWidthForInfos ) 
		{
			this.glyph.getBbox().setW(Math.max(requiredWidthForStates, requiredWidthForInfos));
		}		
	}
	
	public void placeStateAndInfoGlyphs()
	{
		int numOfStates = stateGlyphs.size();
		int numOfInfos = infoGlyphs.size();
		
		float parent_y_up = this.glyph.getBbox().getY()-INFO_BOUND.height/2;
		float parent_y_bot = this.glyph.getBbox().getY()+this.glyph.getBbox().getH()-STATE_BOUND.height/2;
		float parent_x_up = this.glyph.getBbox().getX();
		String parentID = this.glyph.getId();
		
		int usedWidth = 0;
		for (int i = 0; i < numOfStates; i++) 
		{
			Glyph tmpglyph = stateGlyphs.get(i);
			//set dummy id
			tmpglyph.setId(parentID + ".state." + (i+1) );
			
			tmpglyph.getBbox().setX(parent_x_up+(i+1)*OFFSET_BTW_INFO_GLYPHS + usedWidth);
			tmpglyph.getBbox().setY(parent_y_bot);
			
			usedWidth += tmpglyph.getBbox().getW();
			
		}
		
		usedWidth = 0;
		for (int i = 0; i < numOfInfos; i++) 
		{
			Glyph tmpglyph = infoGlyphs.get(i);
			Bbox b = new Bbox();
			tmpglyph.setBbox(b);
			
			//set dummy id
			tmpglyph.setId(parentID + ".info." + (i+1) );
			
			tmpglyph.getBbox().setX(parent_x_up+(i+1)*OFFSET_BTW_INFO_GLYPHS + usedWidth);
			tmpglyph.getBbox().setY(parent_y_up);
			
			usedWidth += tmpglyph.getBbox().getW();
		}
	}
	
	/**Inner Class for glyph bounds
	 * */
	public class Bound
	{
		public float width;
		public float height;
		
		public Bound(float width, float height)
		{
			this.width = width;
			this.height = height;
		}

		public float getWidth() 
		{
			return width;
		}

		public void setWidth(float width) 
		{
			this.width = width;
		}

		public float getHeight() 
		{
			return height;
		}

		public void setHeight(float height)
		{
			this.height = height;
		}
	}
}
