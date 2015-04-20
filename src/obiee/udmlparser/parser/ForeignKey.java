package obiee.udmlparser.parser;

import java.util.ArrayList;
import java.util.ListIterator;

import metadata.Repository;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Foreign Key parser class
 * @author danielgalassi@gmail.com
 *
 */
public class ForeignKey implements UDMLObject {

	private String				foreignKeyID;
	private String				foreignKeyName;
	private ArrayList <String>	physicalColumns;
	private String				referencedKey;

	public ForeignKey (String declare, String foreignKey, Repository udml) {
		String line;
		String header = declare.trim();
		int indexREFERENCES = 0;
		int indexAS = header.indexOf(" AS ");
		foreignKeyID = header.substring( foreignKey.length(), indexAS).trim().replaceAll("\"", "").replaceAll("\\p{C}", "?");
		int nextToken = header.indexOf(" HAVING");
		
		if (nextToken > header.indexOf(" UPGRADE ID ") && header.contains(" UPGRADE ID ")) {
			nextToken = header.indexOf(" UPGRADE ID ");
		}
		foreignKeyName = header.substring( indexAS + 4, nextToken).trim().replaceAll("\"", "").replaceAll("\\p{C}", "?");
		physicalColumns = new ArrayList <String> ();

		line = udml.nextLine();
		do {
			line = udml.nextLine().trim();
			header = line;
			indexREFERENCES = line.indexOf(") REFERENCES ");
			if (indexREFERENCES != -1) {
				physicalColumns.add(header.substring(0, indexREFERENCES).
						trim().replaceAll("\"", "").replaceAll("\\p{C}", "?"));
				referencedKey = header.substring(indexREFERENCES + 13).
						trim().replaceAll("\"", "").replaceAll("\\p{C}", "?");
			}
			else {
				physicalColumns.add(header.substring(0, header.indexOf("\",")).trim().replaceAll("\"", ""));
			}
		} while (!line.contains(") REFERENCES "));

		//DISCARD DESCRIPTION AND PRIVILEGES
		while (!(line.contains("PRIVILEGES ") && line.endsWith(";")) && udml.hasNextLine()) {
			line = udml.nextLine();
		}

		header	= null;
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

	public String getID() {
		return foreignKeyID;
	}
}
/*
 * DECLARE FOREIGN KEY <FQ Foreign Key Name> AS <Foreign Key Name> HAVING
 * (
 * <Column 1> ) REFERENCES <Column 2>
 * PRIVILEGES (<...>);
 */
