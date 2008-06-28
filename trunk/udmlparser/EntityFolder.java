package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Entity Folder Parser class
 * @author dgalassi
 *
 */
public class EntityFolder {

	private String			sPresentationTableID;
	private String			sPresentationTableName;
	private String			sPresentationTableMappingID;
	private Vector <String>	vFolderAttributesID = null;
	private String[]		saPresentationTableAliases = null;

	public EntityFolder (String sDeclareStmt, String sEntityFolder, BufferedReader brUDML) {
		String line;
		sPresentationTableID = sDeclareStmt.trim().substring(sEntityFolder.length(),sDeclareStmt.trim().indexOf(" AS ")).trim().replaceAll("\"", "");
		if (sDeclareStmt.indexOf(" ENTITY ") != -1 && 
				sDeclareStmt.indexOf(" ENTITY ") != sDeclareStmt.lastIndexOf(" ENTITY ")) {
			sPresentationTableName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4,sDeclareStmt.trim().indexOf(" ENTITY ", sDeclareStmt.trim().indexOf(" AS "))).trim().replaceAll("\"", "");
			sPresentationTableMappingID = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" ENTITY ", sDeclareStmt.trim().indexOf(" AS ")+4)+8).trim().replaceAll("\"", "");
		}
		else {
			sPresentationTableName = sDeclareStmt.trim().substring(sDeclareStmt.indexOf(" AS ")+4).trim().replaceAll("\"", "");
			sPresentationTableMappingID = "";
		}

		try {
			//FOLDER ATTRIBUTES LIST
			line = brUDML.readLine().trim().replaceAll("\"", "");
			if(line.indexOf("FOLDER ATTRIBUTES ") != -1) {
				vFolderAttributesID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					vFolderAttributesID.add(line.substring(0,line.length()-1));
				} while (line.charAt(line.length()-1) != ')');
			}

			//ALIASES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
			} while (line.indexOf("ALIASES (") != -1);
			if(line.indexOf("ALIASES (") != -1)
				saPresentationTableAliases = line.substring(line.indexOf("ALIASES (")+9, line.lastIndexOf(")")).trim().replaceAll("\"", "").split(",");

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
			} while (line.indexOf("PRIVILEGES") == -1 && line.indexOf(";") == -1);
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

	}

	/**
	 * Entity Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nPresentationTableID = xmldoc.createTextNode(sPresentationTableID);
		Node nPresentationTableName = xmldoc.createTextNode(sPresentationTableName);
		Node nPresentationTableMappingID = xmldoc.createTextNode(sPresentationTableMappingID);

		Element ePresentationTable = xmldoc.createElement("PresentationTable");
		Element ePresentationTableID = xmldoc.createElement("PresentationTableID");
		Element ePresentationTableName = xmldoc.createElement("PresentationTableName");
		Element ePresentationTableMappingID = xmldoc.createElement("PresentationTableMappingID");

		ePresentationTableID.appendChild(nPresentationTableID);
		ePresentationTableName.appendChild(nPresentationTableName);
		ePresentationTableMappingID.appendChild(nPresentationTableMappingID);

		ePresentationTable.appendChild(ePresentationTableID);
		ePresentationTable.appendChild(ePresentationTableName);
		ePresentationTable.appendChild(ePresentationTableMappingID);

		Element ePresentationTableAliasList = xmldoc.createElement("PresentationTableAliasList");
		Element ePresentationTableAlias = null;
		Node nCatalogFolderAlias = null;

		if(saPresentationTableAliases != null)
			for (int i=0; i< saPresentationTableAliases.length; i++) {
				ePresentationTableAlias = xmldoc.createElement("PresentationTableAlias");
				nCatalogFolderAlias = xmldoc.createTextNode(saPresentationTableAliases[i]);
				ePresentationTableAlias.appendChild(nCatalogFolderAlias);
				ePresentationTableAliasList.appendChild(ePresentationTableAlias);
			}

		ePresentationTable.appendChild(ePresentationTableAliasList);

		Element ePresentationAttributeIDList = xmldoc.createElement("PresentationAttributeIDList");
		Element ePresentationAttributeID = null;
		Node nPresentationAttributeID = null;

		if(vFolderAttributesID != null)
			for (int i=0; i< vFolderAttributesID.size(); i++) {
				ePresentationAttributeID = xmldoc.createElement("PresentationAttributeID");
				nPresentationAttributeID = xmldoc.createTextNode(vFolderAttributesID.get(i));
				ePresentationAttributeID.appendChild(nPresentationAttributeID);
				ePresentationAttributeIDList.appendChild(ePresentationAttributeID);
			}

		ePresentationTable.appendChild(ePresentationAttributeIDList);
		return ePresentationTable;
	}
}
/*
 * DECLARE ENTITY FOLDER <FQ Entity Folder Name> AS <Entity Folder Name> ENTITY  <FQ Logical Table Name>
 *  FOLDER ATTRIBUTES  (
 *  <FQ Entity Attribute Name>,
 *  <FQ Entity Attribute Name> )
 *  PRIVILEGES (<...>);
 */
