package obiee.udmlparser.parser;

import java.util.Scanner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Hierarchy Dimension Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class HierarchyDimension implements UDMLObject {

	private String hierarchyDimensionID;
	private String hierarchyDimensionName;

	public HierarchyDimension (String declare, String hierarchyDimension, Scanner udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");

		hierarchyDimensionID = trimmedDeclareStatement.substring(	hierarchyDimension.length(), 
				iIndexAS).trim().replaceAll("\"", "");

		hierarchyDimensionName = trimmedDeclareStatement.substring(iIndexAS+4,
				trimmedDeclareStatement.indexOf(" ON")).trim().replaceAll("\"", "");

		line = udml.nextLine();
		//NO FURTHER ACTIONS
		while (line.indexOf("PRIVILEGES") == -1 && line.indexOf(";") == -1) {
			line = udml.nextLine();
		}

		trimmedDeclareStatement	= null;
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
