package obiee.udmlparser.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ListIterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Foreign Key parser class
 * @author danielgalassi@gmail.com
 *
 */
public class ForeignKey {

	private String				foreignKeyID;
	private String				foreignKeyName;
	private ArrayList <String>	physicalColumns;
	private String				referencedKey;

	public ForeignKey (String declare,
			 String foreignKey,
			 BufferedReader udml) {
		String line;
		String sTrimmedDS = declare.trim();
		int iIndexREFERENCES = 0;
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		foreignKeyID = sTrimmedDS.substring( foreignKey.length(), iIndexAS).
												trim().replaceAll("\"", "").replaceAll("\\p{C}", "?");
		int iNextToken = sTrimmedDS.indexOf(" HAVING");
		if (iNextToken > sTrimmedDS.indexOf(" UPGRADE ID ") &&
				sTrimmedDS.indexOf(" UPGRADE ID ") > -1)
			iNextToken = sTrimmedDS.indexOf(" UPGRADE ID ");
		foreignKeyName = sTrimmedDS.substring( iIndexAS + 4, iNextToken).
											trim().replaceAll("\"", "").replaceAll("\\p{C}", "?");
		physicalColumns = new ArrayList <String> ();
		try {
			line = udml.readLine();
			do {
				line = udml.readLine().trim();
				sTrimmedDS = line;
				iIndexREFERENCES = line.indexOf(") REFERENCES ");
				if (iIndexREFERENCES != -1) {
					physicalColumns.add(sTrimmedDS.substring(0, iIndexREFERENCES).
											trim().replaceAll("\"", "").replaceAll("\\p{C}", "?"));
					referencedKey = sTrimmedDS.substring(iIndexREFERENCES + 13).
												trim().replaceAll("\"", "").replaceAll("\\p{C}", "?");
				}
				else {
					physicalColumns.add(sTrimmedDS.
											substring(0, sTrimmedDS.indexOf("\",")).
											trim().replaceAll("\"", ""));
				}
			} while (line.indexOf(") REFERENCES ") == -1);

			//DISCARD DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1)
				line = udml.readLine();

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
		if (foreignKeyID == null)
			foreignKeyID = "";
		Node nForeignKeyID = xmldoc.createTextNode(foreignKeyID);
		
		if (foreignKeyName == null)
			foreignKeyName = "";
		Node nForeignKeyName = xmldoc.createTextNode(foreignKeyName);

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

		ListIterator <String> liPhysCols = physicalColumns.listIterator();
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
		if (referencedKey == null) {
			referencedKey = "";
		}
		nFKColumn2 = xmldoc.createTextNode(referencedKey);

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
