package org.biopax.paxtools.io.sbgn;

import java.util.ArrayList;
import java.util.List;

import org.ivis.layout.LGraphObject;
import org.ivis.layout.LNode;
import org.ivis.layout.Updatable;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Bbox;
import org.ivis.layout.cose.CoSEGraph;



/**
 * VNode Class
 * @author: Istemi Bahceci
 * */

class VNode implements Updatable
{
    //Glyph attribute of this VNode
    public Glyph glyph;

    ArrayList <Glyph> stateGlyphs;
    ArrayList <Glyph> infoGlyphs;

    /*Glyph class types*/
    private static final String  MACROMOLECULE = "macromolecule";
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
    private static final  String NONE = "NA";


    // Constats used for determining "state of information" and "unit of information" glyph widths according to
    // their labels
    private static final  int LOWERCASE_LETTER_PIXEL_WIDTH = 6;
    private static final  int UPPERCASE_LETTER_PIXEL_WIDTH = 9;
    private static final  int MAX_STATE_AND_INFO_WIDTH = 35;
    private static final  int MAX_STATE_AND_INFO_HEIGHT = 10;
    private static final  int OFFSET_BTW_INFO_GLYPHS = 5;
    private static final  int MAX_INFO_BOX_NUMBER = 4;
    private static final  int MAX_MACROMOLECULE_HEIGHT_WITH_INFO_BOXES = 25;
    private static final  int NON_SUPPORTED_GLYPH_OFFSET = 2;

    /*Glyph Size Constants for layout*/
    private static Bound  SOURCE_AND_SINK_BOUND;
    private static Bound  LOGICAL_OPERATOR_BOUND;
    private static Bound  PROCESS_NODES_BOUND;
    private static Bound  MACROMOLECULE_BOUND;
    private static Bound  NUCLEIC_ACID_FEATURE_BOUND;
    private static Bound  SIMPLE_CHEMICAL_BOUND;
    private static Bound  UNSPECIFIED_ENTITY_BOUND;
    private static Bound  PHENOTYPE_BOUND;
    private static Bound  PERTURBING_AGENT_BOUND;
    private static Bound  TAG_BOUND;
    private static Bound  INFO_BOUND;
    private static Bound  STATE_BOUND;
    private static Bound  COMPLEX_BOUND;


    /**
     * Default Constructor, sets the geometry of the bounds which are attributes of this class
     * @param g glyph
     * */
    public VNode(Glyph g)
    {
        SOURCE_AND_SINK_BOUND = new Bound(15,15);
        LOGICAL_OPERATOR_BOUND = new Bound(15,15);
        PROCESS_NODES_BOUND = new Bound(15,15);

        MACROMOLECULE_BOUND = new Bound(48,25);
        NUCLEIC_ACID_FEATURE_BOUND = new Bound(50,25);

        SIMPLE_CHEMICAL_BOUND = new Bound(48,20);
        UNSPECIFIED_ENTITY_BOUND = new Bound(40,40);
        PHENOTYPE_BOUND = new Bound(50,20);
        TAG_BOUND = new Bound(50,20);
        PERTURBING_AGENT_BOUND = new Bound(50,20);
        COMPLEX_BOUND = new Bound(48,20);

        INFO_BOUND = new Bound(MAX_STATE_AND_INFO_WIDTH,MAX_STATE_AND_INFO_HEIGHT);
        STATE_BOUND = new Bound(MAX_STATE_AND_INFO_WIDTH,MAX_STATE_AND_INFO_HEIGHT);

        stateGlyphs = new ArrayList<>();
        infoGlyphs = new  ArrayList<Glyph>();

        this.glyph = g;

        /*
		 * need to add bbox objects
		 */
        Bbox b = new Bbox();
        this.glyph.setBbox(b);

        if (this.glyph.getClazz() == null)
            this.glyph.setClazz(NONE);

        this.setSizeAccordingToClass();
    }

    /**
     * Function that will take place when VNode objects will update in layout process of ChiLay
     *
     * @param lGraphObj LGraphObject for whom the update will take place.
     */
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

    /**
     * Sets the bound of this VNode by given width and height
     * @param w new width
     * @param h new height
     */
    public void setBounds(float w, float h)
    {
        this.glyph.getBbox().setW(w);
        this.glyph.getBbox().setH(h);
    }

    /**
     * Chooses a proper bound for this VNode according to its class.
     */
    public void setSizeAccordingToClass()
    {
        String glyphClass = this.glyph.getClazz();

        //If glyph class is not specified return here
        if (glyphClass.equalsIgnoreCase(NONE))
            return;

        if (glyphClass.equalsIgnoreCase(SOURCE_AND_SINK))
        {
            setBounds(SOURCE_AND_SINK_BOUND.getWidth(), SOURCE_AND_SINK_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(AND) || glyphClass.equalsIgnoreCase(OR) || glyphClass.equalsIgnoreCase(NOT))
        {
            setBounds(LOGICAL_OPERATOR_BOUND.getWidth(), LOGICAL_OPERATOR_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(ASSOCIATION) || glyphClass.equalsIgnoreCase(DISSOCIATION) || glyphClass.equalsIgnoreCase(OMITTED_PROCESS) ||
                glyphClass.equalsIgnoreCase(UNCERTAIN_PROCESS) || glyphClass.equalsIgnoreCase(PROCESS))
        {
            setBounds(PROCESS_NODES_BOUND.getWidth(), PROCESS_NODES_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(SIMPLE_CHEMICAL))
        {
            setBounds(SIMPLE_CHEMICAL_BOUND.getWidth(), SIMPLE_CHEMICAL_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(UNSPECIFIED_ENTITY))
        {
            setBounds(UNSPECIFIED_ENTITY_BOUND.getWidth(), UNSPECIFIED_ENTITY_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(MACROMOLECULE))
        {
            setBounds(MACROMOLECULE_BOUND.getWidth(), MACROMOLECULE_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(NUCLEIC_ACID_FEATURE))
        {
            setBounds(NUCLEIC_ACID_FEATURE_BOUND.getWidth(), NUCLEIC_ACID_FEATURE_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(STATE_VARIABLE))
        {
            setBounds(STATE_BOUND.getWidth(), STATE_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(UNIT_OF_INFORMATION))
        {
            setBounds(INFO_BOUND.getWidth(), INFO_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(PHENOTYPE))
        {
            setBounds(PHENOTYPE_BOUND.getWidth(), PHENOTYPE_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(PERTURBING_AGENT))
        {
            setBounds(PERTURBING_AGENT_BOUND.getWidth(), PERTURBING_AGENT_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(TAG))
        {
            setBounds(TAG_BOUND.getWidth(), TAG_BOUND.getHeight());
        }

        else if (glyphClass.equalsIgnoreCase(COMPLEX))
        {
            setBounds(COMPLEX_BOUND.getWidth(), COMPLEX_BOUND.getHeight());
        }

        if( this.glyph.getClone() != null )
        {
            Bbox glyphBbox = this.glyph.getBbox();
            setBounds(3*glyphBbox.getW()/4, 3*glyphBbox.getH()/4);
        }

        if (glyphClass.equalsIgnoreCase(MACROMOLECULE) || glyphClass.equalsIgnoreCase(NUCLEIC_ACID_FEATURE) || glyphClass.equalsIgnoreCase(SIMPLE_CHEMICAL) || glyphClass.equalsIgnoreCase(COMPLEX))
        {
            updateSizeForStateAndInfo();
        }

    }

    /**
     * Calculates required width according to the given list state and info glyphs of this VNode.
     * This method also previously computes the width and height of state and info glyphs
     * according to their label.
     *
     * @param  stateORinfoList list that keeps state or info glyphs of this VNode
     * @return new width that is adjusted so that all glyphs in stateORinfoList are included.
     * */
    public int calcReqWidthByStateAndInfos(List<Glyph> stateORinfoList)
    {
        int wholeSize = 0;
        int count = 0;

        for (Glyph tmpGlyph: stateORinfoList)
        {
            String text;

            if (tmpGlyph.getState() != null)
            {
                text = tmpGlyph.getState().getValue();

                if (tmpGlyph.getState().getVariable() != null &&
                        tmpGlyph.getState().getVariable().length() > 0)
                {
                    if(tmpGlyph.getState().getVariable() != null)
                        text += "@" + tmpGlyph.getState().getVariable();
                }
            }
            else if (tmpGlyph.getLabel() != null)
            {
                text = tmpGlyph.getLabel().getText();
            }
            else
            {
                throw new RuntimeException("Encountered an information glyph with no state " +
                        "variable (as modification boxes should have) and no label (as molecule type " +
                        "boxed should have). glyph = " + tmpGlyph);
            }

            int numOfUpper = 0;
            int numOfLower = 0;

            for (int i = 0; i < text.length(); i++)
            {
                if (Character.isLowerCase(text.charAt(i)))
                {
                    numOfLower++;

                } else
                    numOfUpper++;
            }

            Bbox b = new Bbox();
            tmpGlyph.setBbox(b);

            //Set width
            float requiredSize = numOfLower * LOWERCASE_LETTER_PIXEL_WIDTH + numOfUpper * UPPERCASE_LETTER_PIXEL_WIDTH;
            if (requiredSize < MAX_STATE_AND_INFO_WIDTH)
                tmpGlyph.getBbox().setW(requiredSize);
            else
                tmpGlyph.getBbox().setW(STATE_BOUND.width);

            //Set height
            tmpGlyph.getBbox().setH(MAX_STATE_AND_INFO_HEIGHT);

            if (count < MAX_INFO_BOX_NUMBER / 2)
                wholeSize += tmpGlyph.getBbox().getW();

            count++;

        }

        return wholeSize;
    }

    /**
     * 	If glyph attribute of this VNode object includes any "state of information" or "unit of information" glyphs, this method updates the
     *  size of VNode accordingly.
     * */
    public void updateSizeForStateAndInfo()
    {
        // Find all state and info glyphs
        for (Glyph glyph : this.glyph.getGlyph())
        {
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
        int wholeWidthOfStates = calcReqWidthByStateAndInfos(stateGlyphs);
        int wholeWidthOfInfos  = calcReqWidthByStateAndInfos(infoGlyphs);

        // Calculate  positions
        int numOfStates = stateGlyphs.size();
        int numOfInfos = infoGlyphs.size();



        //set a maximum width to glyph according to state and info boxes
        int totNumStateInfo = numOfInfos + numOfStates;
        int multiplier = totNumStateInfo <= 2 ? 2 : 3;
        float requiredWidth = multiplier * OFFSET_BTW_INFO_GLYPHS + (multiplier-1) * MAX_STATE_AND_INFO_WIDTH;

        //Adjust heights so that info box offsets are taken into account in layout.
        if(totNumStateInfo > 0 )
            this.glyph.getBbox().setH(this.glyph.getBbox().getH()+MAX_STATE_AND_INFO_HEIGHT/2);

        if (this.glyph.getBbox().getW() < requiredWidth  )
        {
            this.glyph.getBbox().setW(requiredWidth);
        }
    }

    /**
     * Places state and info glyphs of this node
     * */
    public void placeStateAndInfoGlyphs() {
        int numOfStates = stateGlyphs.size();
        int numOfInfos = infoGlyphs.size();


        //Adjust heights so that info box offsets are taken into account in layout.
        if(numOfStates > 0 || numOfInfos > 0)
            this.glyph.getBbox().setH(this.glyph.getBbox().getH()-MAX_STATE_AND_INFO_HEIGHT/2);

        float parent_y_up = this.glyph.getBbox().getY()-INFO_BOUND.height/2;
        float parent_y_bot = this.glyph.getBbox().getY()+this.glyph.getBbox().getH()-INFO_BOUND.height/2;;
        float parent_x_up = this.glyph.getBbox().getX();
        float parentWidth = this.glyph.getBbox().getW();
        String parentID = this.glyph.getId();

        int maxNumberOfStates =   stateGlyphs.size();
        int maxNumberOfInfos =  infoGlyphs.size();
        if (maxNumberOfInfos + maxNumberOfStates > 4)
        {
            if(maxNumberOfInfos < 2 || maxNumberOfStates < 2)
            {
                int minimum = Math.min(maxNumberOfInfos,maxNumberOfStates);

                if (maxNumberOfInfos - minimum == 0)
                {
                    maxNumberOfInfos = minimum;
                    maxNumberOfStates = maxNumberOfStates - maxNumberOfInfos;
                }
                else
                {
                    maxNumberOfStates = minimum;
                    maxNumberOfInfos = maxNumberOfInfos - maxNumberOfStates;
                }
            }
        }
        //else normal placement 
        
        //Some variables for placement of state and info boxes
        int totalNumberOfStateAndInfos = maxNumberOfInfos+maxNumberOfStates;
        Glyph tmpglyph = null;
        int infoIndex = 0;
        int stateIndex = 0;
        int topUsedWidth = 0;
        int bottomUsedWidth = 0;
        int additionalStateInfoOffset = 0;
        boolean placeTopFlag = true;

        //Place state and info glyphs
        for (int i = 0; i < totalNumberOfStateAndInfos; i++)
        {
            //Adjust offsets between state and info boxes
            int offsetMultiplier = i % 4 <= 1 ? 1 : 2;

            if (maxNumberOfInfos - i > 0 && infoGlyphs.size() > 0)
                tmpglyph =  infoGlyphs.get(infoIndex++);
            else if (stateGlyphs.size() > 0)
                tmpglyph = stateGlyphs.get(stateIndex++);

            //Top placement
            if (placeTopFlag)
            {
                if( totalNumberOfStateAndInfos <= 2 )
                {
                    tmpglyph.getBbox().setX(parent_x_up+parentWidth/2-tmpglyph.getBbox().getW()/2);
                    tmpglyph.getBbox().setY(parent_y_up - additionalStateInfoOffset);
                    //set dummy id
                    tmpglyph.setId(parentID + ".info." + (i+1) );
                }
                else
                {
                    //set dummy id
                    tmpglyph.setId(parentID + ".info." + (i+1) );

                    tmpglyph.getBbox().setX(parent_x_up + offsetMultiplier * OFFSET_BTW_INFO_GLYPHS + topUsedWidth);
                    tmpglyph.getBbox().setY(parent_y_up - additionalStateInfoOffset);
                }

                topUsedWidth += tmpglyph.getBbox().getW();
            }
            //bottom placement
            else
            {
                if(( i % 2 == 1 ) && ( totalNumberOfStateAndInfos == 2 || totalNumberOfStateAndInfos == 3 ))
                {
                    tmpglyph.getBbox().setX(parent_x_up+parentWidth/2-tmpglyph.getBbox().getW()/2);
                    tmpglyph.getBbox().setY(parent_y_bot + additionalStateInfoOffset);
                    //set dummy id
                    tmpglyph.setId(parentID + ".state." + (i+1) );
                }
                else
                {
                    //set dummy id
                    tmpglyph.setId(parentID + ".state." + (i+1) );
                    tmpglyph.getBbox().setX(parent_x_up+ offsetMultiplier *OFFSET_BTW_INFO_GLYPHS + bottomUsedWidth);
                    tmpglyph.getBbox().setY(parent_y_bot + additionalStateInfoOffset);
                }

                bottomUsedWidth += tmpglyph.getBbox().getW();
            }

            // Alternate between placing top and bottom
            placeTopFlag = !placeTopFlag;

            //More than 4 state and info glyps are placed with an offset on top of previously placed state and info boxes
            //This can be easily filtered out on the application side where the resultant graphs of this layout is used.
            if ( ( i + 1 ) % 4 == 0 )
                additionalStateInfoOffset += NON_SUPPORTED_GLYPH_OFFSET;
        }

    }

    /**
     * Inner Class for glyph bounds
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