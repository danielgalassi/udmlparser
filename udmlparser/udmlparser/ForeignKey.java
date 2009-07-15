package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Foreign Key parser class
 * @author danielgalassi@gmail.com
 *
 */
public class ForeignKey {

	private String			sForKeyID;
	private String			sForKeyName;
	private String			sPhysicalColumn1;
	private String			sPhysicalColumn2;

	public ForeignKey (String sDeclareStmt,
			 String sForKey,
			 BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexREFERENCES = 0;
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sForKeyID = sTrimmedDS.substring( sForKey.length(), iIndexAS).
												trim().replaceAll("\"", "");
		sForKeyName = sTrimmedDS.substring( iIndexAS + 4, 
											sTrimmedDS.indexOf(" HAVING")).
											trim().replaceAll("\"", "");
		try {
			line = brUDML.readLine();
			do {
				line = brUDML.readLine().trim();
				if (line.indexOf(") REFERENCES ") != -1) {
					sTrimmedDS = line.trim();
					iIndexREFERENCES = line.indexOf(") REFERENCES ");
					sPhysicalColumn1 = sTrimmedDS.substring(0, iIndexREFERENCES).
											trim().replaceAll("\"", "");
					sPhysicalColumn2 = sTrimmedDS.substring(iIndexREFERENCES + 13).
											trim().replaceAll("\"", "");
				}
			} while (line.indexOf(") REFERENCES ") == -1);

			//DISCARD DESCRIPTION AND PRIVILEGES
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
	 * Foreign Key XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	@SuppressWarnings("unchecked")
	public Element serialize(Document xmldoc) {
		Node nForeignKeyID = xmldoc.createTextNode(sForKeyID);
		Node nForeignKeyName = xmldoc.createTextNode(sForKeyName);

		Element eForeignKey = xmldoc.createElement("ForeignKey");
		Element eForeignKeyID = xmldoc.createElement("ForeignKeyID");
		Element eForeignKeyName = xmldoc.createElement("ForeignKeyName");

		eForeignKeyID.appendChild(nForeignKeyID);
		eForeignKeyName.appendChild(nForeignKeyName);

		eForeignKey.appendChild(eForeignKeyID);
		eForeignKey.appendChild(eForeignKeyName);

		Element eFKColumnList = xmldoc.createElement("FKColumnList");
		Element eFKColumn1 = null;
		Element eFKColumn2 = null;
		Node nFKColumn1 = null;
		Node nFKColumn2 = null;

		eFKColumn1 = xmldoc.createElement("FKColumnID");
		eFKColumn1.setAttribute("isReferenced", "false");
		nFKColumn1 = xmldoc.createTextNode(sPhysicalColumn1);
		
		eFKColumn2 = xmldoc.createElement("FKColumnID");
		eFKColumn1.setAttribute("isReferenced", "true");
		nFKColumn2 = xmldoc.createTextNode(sPhysicalColumn2);

		eFKColumn1.appendChild(nFKColumn1);
		eFKColumn2.appendChild(nFKColumn2);
		
		eFKColumnList.appendChild(eFKColumn1);
		eFKColumnList.appendChild(eFKColumn2);

		eForeignKey.appendChild(eFKColumnList);
		return eForeignKey;

	}
}
/*
 * DECLARE FOREIGN KEY <FQ Foreign Key Name> AS <Foreign Key Name> HAVING
 * (
 * <Column 1> ) REFERENCES <Column 2>
 * PRIVILEGES (<...>);
 */
