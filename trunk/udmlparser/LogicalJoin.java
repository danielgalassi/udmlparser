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

	private void parseLogicalJoinSpec(String line) {
		int iLogicalTable = line.indexOf(" ON ENTITY ") + 12;
		int iMULTIPLICITY = line.indexOf(" MULTIPLICITY  ");
		if (iLogicalTable != -1 || iMULTIPLICITY != -1)
			vLogicalTablesID.add(line.substring(iLogicalTable, iMULTIPLICITY).replace("\"", ""));
		else
			vLogicalTablesID.add("Review definition for this logical join");
	}

	public LogicalJoin (String sDeclareStmt,
			String sSubjectArea,
			BufferedReader brUDML) {
		String line = "";
		int iSpecsFound = 0;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sLogicalJoinID = sTrimmedDS.substring(sSubjectArea.length(),iIndexAS).
							trim().replaceAll("\"", "");
		try {
			String sLogicalJoinSpec = "DECLARE ROLE \"" + sLogicalJoinID;
			vLogicalTablesID = new Vector<String>();
			while (iSpecsFound < 2) {
				line = brUDML.readLine();
				while (line.indexOf(sLogicalJoinSpec) == -1)
					line = brUDML.readLine();

				//table (logical join spec) found
				parseLogicalJoinSpec(line);
				iSpecsFound++;
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
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
	 * Logical Join XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (sLogicalJoinID == null) {
			sLogicalJoinID = "";
		}
		Node nLogicalJoinID = xmldoc.createTextNode(sLogicalJoinID);

		Element eLogicalJoin = xmldoc.createElement("LogicalJoin");
		Element eLogicalJoinID = xmldoc.createElement("LogicalJoinID");

		eLogicalJoinID.appendChild(nLogicalJoinID);

		eLogicalJoin.appendChild(eLogicalJoinID);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (vLogicalTablesID != null)
			for (int i=0; i< vLogicalTablesID.size(); i++) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				if (vLogicalTablesID.get(i) == null) {
					nLogicalTable = xmldoc.createTextNode("");
				} else {
					nLogicalTable = xmldoc.createTextNode(vLogicalTablesID.get(i));
				}
				eLogicalTable.appendChild(nLogicalTable);
				if (vLogicalTablesID.get(i).indexOf("Fact -") != -1)
					eLogicalTable.setAttribute("type", "FACT");
				if (vLogicalTablesID.get(i).indexOf("Dim -") != -1)
					eLogicalTable.setAttribute("type", "DIM");
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
