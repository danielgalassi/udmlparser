package obiee.udmlparser.parser;

import java.util.Vector;

import metadata.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Logical Joins (in OBI 10g) Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalForeignKey implements UDMLObject {

	private String			logicalForeignKeyJoinID;
	private Vector <String>	logicalTableIDs = null;

	public LogicalForeignKey (String declare, String subjectArea, Repository udml) {
		String line = "";
		String line2 = "";
		int indexLogicalTable = 0;
		String header = declare.trim();
		int indexAS = header.indexOf(" AS ");
		logicalForeignKeyJoinID = header.substring(subjectArea.length(),indexAS).trim().replaceAll("\"", "");

		logicalTableIDs = new Vector<String>();
		while (!line.contains(") COUNTERPART KEY ") && !line.contains(";")) {
			line = udml.nextLine().trim();
		}

		indexLogicalTable = line.substring(0, line.indexOf(") COUNTERPART KEY ")).trim().lastIndexOf("\".\"");
		logicalTableIDs.add(line.substring(0,  indexLogicalTable+1).replace("\"", ""));
		line2 = line.substring(line.indexOf(") COUNTERPART KEY ")+18).trim();
		indexLogicalTable = line2.lastIndexOf("\".\"");
		logicalTableIDs.add(line2.substring(0, indexLogicalTable).replace("\"", ""));

		//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
		while (!line.contains("PRIVILEGES") && !line.contains(";")) {
			line = udml.nextLine();
		}
	}

	/**
	 * Logical Foreign Key XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */

	public Element serialize(Document xmldoc) {
		if (logicalForeignKeyJoinID == null) {
			logicalForeignKeyJoinID = "";
		}
		Node nLogicalJoinID = xmldoc.createTextNode(logicalForeignKeyJoinID);

		Element eLogicalJoin = xmldoc.createElement("LogicalJoin");
		Element eLogicalJoinID = xmldoc.createElement("LogicalJoinID");

		eLogicalJoinID.appendChild(nLogicalJoinID);

		eLogicalJoin.appendChild(eLogicalJoinID);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;
		if (logicalTableIDs != null)
			for (String sLogicalTableID : logicalTableIDs) {
				eLogicalTable = xmldoc.createElement("LogicalTableID");
				if (sLogicalTableID == null)
					nLogicalTable = xmldoc.createTextNode("");
				else
					nLogicalTable = xmldoc.createTextNode(sLogicalTableID);

				eLogicalTable.appendChild(nLogicalTable);
				if (sLogicalTableID.indexOf("Fact") != -1 || 
						sLogicalTableID.indexOf("Measure") != -1)
					eLogicalTable.setAttribute("type", "FACT");
				if (sLogicalTableID.indexOf("Fact") == -1 && 
						sLogicalTableID.indexOf("Measure") == -1)
					eLogicalTable.setAttribute("type", "DIM");
				eLogicalTableList.appendChild(eLogicalTable);
			}

		eLogicalJoin.appendChild(eLogicalTableList);
		eLogicalJoin.setAttribute("type", "LogicalForeignKey-based");
		return eLogicalJoin;
	}
}
/*
 *	DECLARE LOGICAL FOREIGN KEY "Paint"."Sales Facts"."Sales Facts_FKey" AS "Sales Facts_FKey" HAVING
 *	(
 *		  "Paint"."Sales Facts"."MktKey" ) COUNTERPART KEY  "Paint"."Markets"."Market_Key"
 *	PRIVILEGES ( READ);
 *
 *	DECLARE LOGICAL FOREIGN KEY <FQ Logical Foreign Key Name> AS <Short Name> HAVING
 *	(
 *		  <FQ fact table key> ) COUNTERPART KEY  <FQ dim table key>
 *	PRIVILEGES ...;
 */
