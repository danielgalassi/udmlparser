package obiee.udmlparser.parser;

import metadata.Repository;

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

	public HierarchyDimension (String declare, String hierarchyDimension, Repository udml) {
		String line;
		String header = declare.trim();
		int asMarker = header.indexOf(" AS ");
		int onMarker = header.indexOf(" ON");

		hierarchyDimensionID = header.substring(hierarchyDimension.length(), asMarker).trim().replaceAll("\"", "");

		hierarchyDimensionName = header.substring(asMarker+4, onMarker).trim().replaceAll("\"", "");

		//NO FURTHER ACTIONS
		line = udml.nextLine();
		while (!(line.contains("PRIVILEGES") && line.endsWith(";")) && udml.hasNextLine()) {
			line = udml.nextLine();
		}
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

	public String getID() {
		return hierarchyDimensionID;
	}
}
/*
DECLARE DIMENSION "Paint"."MarketDim" AS "MarketDim" ON
(
	  "Paint"."MarketDim"."All Markets" ) DEFAULT ROOT  "Paint"."MarketDim"."All Markets"
PRIVILEGES ( READ);
 */
