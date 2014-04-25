package obiee.udmlparser.parser;

import java.util.Vector;

import metadata.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Logical Join Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalJoin implements UDMLObject {

	private String			logicalJoinID;
	private Vector <String>	logicalTableIDs = null;

	private void parseLogicalJoinSpec(String line) {
		int indexLogicalTable = line.indexOf(" ON ENTITY ") + 12;
		int indexMULTIPLICITY = line.indexOf(" MULTIPLICITY  ");

		if (indexLogicalTable != -1 || indexMULTIPLICITY != -1) {
			logicalTableIDs.add(line.substring(indexLogicalTable, indexMULTIPLICITY).replace("\"", ""));
		}
		else {
			logicalTableIDs.add("Review definition for this logical join");
		}
	}

	public LogicalJoin (String declare, String subjectArea, Repository udml) {
		String line = "";
		int joinSpecifications = 0;
		String header = declare.trim();
		int indexAS = header.indexOf(" AS ");
		logicalJoinID = header.substring(subjectArea.length(),indexAS).trim().replaceAll("\"", "");

		String logicalJoinSpecification = "DECLARE ROLE \"" + logicalJoinID;
		logicalTableIDs = new Vector<String>();
		while (joinSpecifications < 2) {
			line = udml.nextLine();
			while (!line.contains(logicalJoinSpecification)) {
				line = udml.nextLine();
			}

			//table (logical join spec) found
			parseLogicalJoinSpec(line);
			joinSpecifications++;
		}

		//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
		while (!line.contains("PRIVILEGES") && line.contains(";")) {
			line = udml.nextLine();
		}
	}

	/**
	 * Logical Join XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (logicalJoinID == null) {
			logicalJoinID = "";
		}
		Node nLogicalJoinID = xmldoc.createTextNode(logicalJoinID);

		Element eLogicalJoin = xmldoc.createElement("LogicalJoin");
		Element eLogicalJoinID = xmldoc.createElement("LogicalJoinID");

		eLogicalJoinID.appendChild(nLogicalJoinID);

		eLogicalJoin.appendChild(eLogicalJoinID);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;

		if (logicalTableIDs != null)
			for (int i=0; i< logicalTableIDs.size(); i++) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				if (logicalTableIDs.get(i) == null) {
					nLogicalTable = xmldoc.createTextNode("");
				} else {
					nLogicalTable = xmldoc.createTextNode(logicalTableIDs.get(i));
				}
				eLogicalTable.appendChild(nLogicalTable);
				if (logicalTableIDs.get(i).indexOf("Fact -") != -1 || 
						logicalTableIDs.get(i).toLowerCase().indexOf("fact") != -1)
					eLogicalTable.setAttribute("type", "FACT");
				if (logicalTableIDs.get(i).indexOf("Dim -") != -1 || 
						(logicalTableIDs.get(i).indexOf("Fact -") == -1 && 
						logicalTableIDs.get(i).toLowerCase().indexOf("fact") == -1))
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
