package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Hierarchy Dimension Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class HierarchyDimension {

	private String hierarchyDimensionID;
	private String hierarchyDimensionName;

	public HierarchyDimension (	String declare, 
								String hierarchyDimension, 
								BufferedReader udml) {
		String line;
		String sTrimmedDS = declare.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");

		hierarchyDimensionID = sTrimmedDS.substring(	hierarchyDimension.length(), 
											iIndexAS).
											trim().replaceAll("\"", "");

		hierarchyDimensionName = sTrimmedDS.substring(iIndexAS+4,
											sTrimmedDS.indexOf(" ON")).
											trim().replaceAll("\"", "");
		
		try {
			line = udml.readLine();
			//NO FURTHER ACTIONS
			while ( line.indexOf("PRIVILEGES") == -1 && 
					line.indexOf(";") == -1)
				line = udml.readLine();
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
	}

	/**
	 * Hierarchy Dimension XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (hierarchyDimensionID == null) {
			hierarchyDimensionID = "";
		}
		Node nHierarchyDimensionID = xmldoc.createTextNode(hierarchyDimensionID);
		if (hierarchyDimensionName == null) {
			hierarchyDimensionName = "";
		}
		Node nHierarchyDimensionName = xmldoc.createTextNode(hierarchyDimensionName);

		Element eHierDim = xmldoc.createElement("HierarchyDimension");
		Element eHierDimID = xmldoc.createElement("HierarchyDimensionID");
		Element eHierDimName = xmldoc.createElement("HierarchyDimensionName");

		eHierDimID.appendChild(nHierarchyDimensionID);
		eHierDimName.appendChild(nHierarchyDimensionName);

		eHierDim.appendChild(eHierDimID);
		eHierDim.appendChild(eHierDimName);

		return eHierDim;
	}
}
/*
DECLARE DIMENSION "Paint"."MarketDim" AS "MarketDim" ON
(
	  "Paint"."MarketDim"."All Markets" ) DEFAULT ROOT  "Paint"."MarketDim"."All Markets"
PRIVILEGES ( READ);
 */
