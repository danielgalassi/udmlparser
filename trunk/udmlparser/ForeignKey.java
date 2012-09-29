package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Foreign Key parser class
 * @author danielgalassi@gmail.com
 *
 */
public class ForeignKey {

	private String			sForKeyID;
	private String			sForKeyName;
	private ArrayList <String>	alsPhysicalColumns;
	//private String			sPhysicalColumn1;
	private String			sReferencedKey;

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
		alsPhysicalColumns = new ArrayList <String> ();
		try {
			line = brUDML.readLine();
			do {
				line = brUDML.readLine().trim();
				sTrimmedDS = line;
				iIndexREFERENCES = line.indexOf(") REFERENCES ");
				if (iIndexREFERENCES != -1) {
					alsPhysicalColumns.add(sTrimmedDS.substring(0, iIndexREFERENCES).
											trim().replaceAll("\"", ""));
					sReferencedKey = sTrimmedDS.substring(iIndexREFERENCES + 13).
												trim().replaceAll("\"", "");
				}
				else {
					alsPhysicalColumns.add(sTrimmedDS.
											substring(0, sTrimmedDS.indexOf("\",")).
											trim().replaceAll("\"", ""));
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
	public Element serialize(Document xmldoc) {
		if (sForKeyID == null) {
			sForKeyID = "";
		}
		Node nForeignKeyID = xmldoc.createTextNode(sForKeyID);
		if (sForKeyName == null) {
			sForKeyName = "";
		}
		Node nForeignKeyName = xmldoc.createTextNode(sForKeyName);

		Element eForeignKey = xmldoc.createElement("ForeignKey");
		Element eForeignKeyID = xmldoc.createElement("ForeignKeyID");
		Element eForeignKeyName = xmldoc.createElement("ForeignKeyName");

		eForeignKeyID.appendChild(nForeignKeyID);
		eForeignKeyName.appendChild(nForeignKeyName);

		eForeignKey.appendChild(eForeignKeyID);
		eForeignKey.appendChild(eForeignKeyName);

		Element eFKColumnList = xmldoc.createElement("FKColumnList");
		Element eFKColumn = null;
		Element eFKColumn2 = null;
		Node nFKColumn = null;
		Node nFKColumn2 = null;

		ListIterator <String> liPhysCols = alsPhysicalColumns.listIterator();
		while (liPhysCols.hasNext()) {
			eFKColumn = xmldoc.createElement("FKColumnID");
			String s = liPhysCols.next();
			if (s == null){
				nFKColumn = xmldoc.createTextNode("");
			} else {
				nFKColumn = xmldoc.createTextNode(s);
			}
			eFKColumn.appendChild(nFKColumn);
			eFKColumnList.appendChild(eFKColumn);
		}

		eFKColumn2 = xmldoc.createElement("ReferencedFK");
		if (sReferencedKey == null) {
			sReferencedKey = "";
		}
		nFKColumn2 = xmldoc.createTextNode(sReferencedKey);

		eFKColumn2.appendChild(nFKColumn2);
		eForeignKey.appendChild(eFKColumn2);

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
