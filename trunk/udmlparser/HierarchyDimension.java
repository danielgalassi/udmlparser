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

	private String sHierDimID;
	private String sHierDimName;

	public HierarchyDimension (	String sDeclareStmt, 
								String sHierarchyDimension, 
								BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");

		sHierDimID = sTrimmedDS.substring(	sHierarchyDimension.length(), 
											iIndexAS).
											trim().replaceAll("\"", "");

		sHierDimName = sTrimmedDS.substring(iIndexAS+4,
											sTrimmedDS.indexOf(" ON")).
											trim().replaceAll("\"", "");
		
		try {
			line = brUDML.readLine();
			//NO FURTHER ACTIONS
			while ( line.indexOf("PRIVILEGES") == -1 && 
					line.indexOf(";") == -1)
				line = brUDML.readLine();
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
		if (sHierDimID == null) {
			sHierDimID = "";
		}
		Node nHierarchyDimensionID = xmldoc.createTextNode(sHierDimID);
		if (sHierDimName == null) {
			sHierDimName = "";
		}
		Node nHierarchyDimensionName = xmldoc.createTextNode(sHierDimName);

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
