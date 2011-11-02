package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Logical Join Parser class
 * @author dgalassi
 *
 */
public class LogicalJoin {

	private String			sLogicalJoinID;
	private Vector <String>	vLogicalTablesID = null;
	private Vector <String> vHierDimensionsID = null;

	private void parseLogicalTable(String line) {
		int iIndexSA = line.indexOf(") SUBJECT AREA ");
		if (line.endsWith(","))
			vLogicalTablesID.add(line.substring(0, line.length()-1));
		if (iIndexSA != -1)
			vLogicalTablesID.add(line.substring(0, iIndexSA));
	}

	private void parseHierDim(String line) {
		vHierDimensionsID.add(line.substring(0, line.length()-1));
	}

	public LogicalJoin (String sDeclareStmt,
						String sSubjectArea,
						BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
System.out.println(sTrimmedDS);
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sLogicalJoinID = sTrimmedDS.substring(sSubjectArea.length(),iIndexAS).
												trim().replaceAll("\"", "");
System.out.println(sLogicalJoinID);
/*
		try {
			line = brUDML.readLine();
			
			//HIERARCHY DIMENSIONS
			if (line.endsWith("DIMENSIONS (")) {
				vHierDimensionsID = new Vector<String>();
				line = brUDML.readLine().trim().replaceAll("\"", "");
				while (( line.indexOf("LOGICAL TABLES (") == -1) || 
						(line.indexOf("PRIVILEGES") != -1 && 
						 line.indexOf(";") != -1)) {
					parseHierDim(line);
					line = brUDML.readLine().trim().replaceAll("\"", "");
				};
			}

			//LOGICAL TABLES LIST
			if (line.endsWith("LOGICAL TABLES (")) {
				vLogicalTablesID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					parseLogicalTable(line);
				} while (line.indexOf(") SUBJECT AREA ") == -1);
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = brUDML.readLine();

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
*/
		sTrimmedDS	= null;
		line		= null;
	}

	/**
	 * Logical Join XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nLogicalJoinID = xmldoc.createTextNode(sLogicalJoinID);

		Element eLogicalJoin = xmldoc.createElement("LogicalJoin");
		Element eLogicalJoinID = xmldoc.createElement("LogicalJoinID");

		eLogicalJoinID.appendChild(nLogicalJoinID);

		eLogicalJoin.appendChild(eLogicalJoinID);

		Element eHierDimensionList = xmldoc.createElement("HierarchyDimensionIDList");
		Element eHierDim = null;
		Node nHierDim = null;
		
		if (vHierDimensionsID != null)
			for (int i=0; i< vHierDimensionsID.size(); i++) {
				eHierDim = xmldoc.createElement("HierarchyDimensionID");
				nHierDim = xmldoc.createTextNode(vHierDimensionsID.get(i));
				eHierDim.appendChild(nHierDim);
				eHierDimensionList.appendChild(eHierDim);
			}
		
		eLogicalJoin.appendChild(eHierDimensionList);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (vLogicalTablesID != null)
			for (int i=0; i< vLogicalTablesID.size(); i++) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				nLogicalTable = xmldoc.createTextNode(vLogicalTablesID.get(i));
				eLogicalTable.appendChild(nLogicalTable);
				eLogicalTableList.appendChild(eLogicalTable);
			}

		eLogicalJoin.appendChild(eLogicalTableList);
		return eLogicalJoin;
	}
}
/*
 *	DECLARE ROLE RELATIONSHIP "Relationship_2004:3135491005258178" AS "Relationship_2004:3135491005258178"
 *	ON (
 *	"Relationship_2004:3135491005258178"."Role_2010:223391074282433",
 *	"Relationship_2004:3135491005258178"."Role_2010:223401074282433" )
 *	PRIVILEGES ( READ);
 *	DECLARE ROLE "Relationship_2004:3135491005258178"."Role_2010:223391074282433" AS "Role_2010:223391074282433" ON ENTITY  "Core"."Dim - Opportunity Hierarchy" MULTIPLICITY  ZERO TO ONE
 *	PRIVILEGES ( READ);
 *	DECLARE ROLE "Relationship_2004:3135491005258178"."Role_2010:223401074282433" AS "Role_2010:223401074282433" ON ENTITY  "Core"."Dim - Opportunity" MULTIPLICITY  ZERO TO MANY
 *	PRIVILEGES ( READ);
 *
 *	DECLARE ROLE RELATIONSHIP <relationship id> AS <relationship name>
 *	ON (
 *	<relationship id>.<role id 1>,
 *	<relationship id>.<role id 2> )
 *	PRIVILEGES ( READ);
 *	DECLARE ROLE <relationship id>.<role id 1> AS <role name> ON ENTITY  <FQ logical table name> MULTIPLICITY  ZERO TO ONE
 *	PRIVILEGES ( READ);
 *	DECLARE ROLE <relationship id>.<role id 2> AS <role name> ON ENTITY  <FQ logical table name> MULTIPLICITY  ZERO TO MANY
 *	PRIVILEGES ( READ);
 */
