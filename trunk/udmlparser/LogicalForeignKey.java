package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Logical Joins (in OBI 10g) Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class LogicalForeignKey {

	private String			sLogicalFKJoinID;
	private Vector <String>	vLogicalTablesID = null;

	public LogicalForeignKey (String sDeclareStmt,
			String sSubjectArea,
			BufferedReader brUDML) {
		String line = "";
		String line2 = "";
		int iLogicalTable = 0;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sLogicalFKJoinID = sTrimmedDS.substring(sSubjectArea.length(),iIndexAS).
							trim().replaceAll("\"", "");
		try {
			vLogicalTablesID = new Vector<String>();
			while ( line.indexOf(") COUNTERPART KEY ") == -1 && line.indexOf(";") == -1)
				line = brUDML.readLine().trim();
			iLogicalTable = line.substring(0, line.indexOf(") COUNTERPART KEY ")).trim().lastIndexOf("\".\"");
			vLogicalTablesID.add(line.substring(0,  iLogicalTable+1).replace("\"", ""));
			line2 = line.substring(line.indexOf(") COUNTERPART KEY ")+18).trim();
			iLogicalTable = line2.lastIndexOf("\".\"");
			vLogicalTablesID.add(line2.substring(0, iLogicalTable).replace("\"", ""));

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = brUDML.readLine();
			
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
		line2		= null;
	}

	/**
	 * Logical Foreign Key XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */

	public Element serialize(Document xmldoc) {
		if (sLogicalFKJoinID == null) {
			sLogicalFKJoinID = "";
		}
		Node nLogicalJoinID = xmldoc.createTextNode(sLogicalFKJoinID);

		Element eLogicalJoin = xmldoc.createElement("LogicalJoin");
		Element eLogicalJoinID = xmldoc.createElement("LogicalJoinID");

		eLogicalJoinID.appendChild(nLogicalJoinID);

		eLogicalJoin.appendChild(eLogicalJoinID);

		Element eLogicalTableList = xmldoc.createElement("LogicalTableIDList");
		Element eLogicalTable = null;
		Node nLogicalTable = null;
		if (vLogicalTablesID != null)
			for (String sLogicalTableID : vLogicalTablesID) {
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
