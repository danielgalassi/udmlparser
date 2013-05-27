package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Entity Folder Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class EntityFolder {

	private String			sPresTableID;
	private String			sPresTableName;
	private String			sPresTableMappingID;
	private String			sPressDispayName;
	private String			sPressDescription;
	private Vector <String>	vFolderAttributesID = null;
	private String[]		saPresentationTableAliases = null;

	public EntityFolder (String sDeclareStmt, 
						 String sEntityFolder, 
						 BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		sPresTableID = sTrimmedDS.substring(sEntityFolder.length(), iIndexAS).
												trim().replaceAll("\"", "");
		if (sTrimmedDS.indexOf(" ENTITY ") != -1 && 
			sTrimmedDS.indexOf(" ENTITY ") != sTrimmedDS.
												lastIndexOf(" ENTITY ")) {
			sPresTableName = sTrimmedDS.substring(iIndexAS + 4, 
									sTrimmedDS.indexOf(" ENTITY ", iIndexAS)).
									trim().replaceAll("\"", "");
			sPresTableMappingID = sTrimmedDS.substring(sTrimmedDS.
									indexOf(" ENTITY ", iIndexAS + 4) + 8).
									trim().replaceAll("\"", "");
		}
		else {
			sPresTableName = sTrimmedDS.substring(iIndexAS + 4).
									trim().replaceAll("\"", "");
			sPresTableMappingID = "";
		}

		try {
			//FOLDER ATTRIBUTES LIST
			line = brUDML.readLine().trim().replaceAll("\"", "");
			if (line.indexOf("FOLDER ATTRIBUTES ") != -1) {
				vFolderAttributesID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					vFolderAttributesID.add(line.substring(0,line.length()-1));
				} while (line.charAt(line.length()-1) != ')');
			}

			//ALIASES
			if (line.indexOf(";") == -1) {
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
				} while (line.indexOf("ALIASES (") != -1);
				if (line.indexOf("ALIASES (") != -1)
					saPresentationTableAliases = line.substring(
										line.indexOf("ALIASES (")+9, 
										line.lastIndexOf(")")).
										trim().replaceAll("\"", "").split(",");
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1){
				line = brUDML.readLine();
				//DISPLAY NAME
				if (line.indexOf("DISPLAY NAME ") != -1){
					sPressDispayName = line.trim().substring(
										line.indexOf("DISPLAY NAME ")+13,
										line.lastIndexOf(" ON")).
										trim().replaceAll("\"", "");
				}
				
				//DESCRIPTION
				if (line.indexOf("DESCRIPTION ") != -1){
					int iIdexOpen = line.indexOf("{")+1;
					int length = line.length();
					sPressDescription = line.substring(
							iIdexOpen,
							length).
							replaceAll("}", "").trim();
					//LARGE TEXT
					while (line.indexOf("}") == -1){
						line = brUDML.readLine().trim();
						sPressDescription += "\n";
						sPressDescription += line.trim().replaceAll("}", "");
					}
				}
			}

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
	}

	/**
	 * Entity Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (sPresTableID == null) {
			sPresTableID = "";
		}
		Node nPresentationTableID = xmldoc.createTextNode(sPresTableID);

		if (sPresTableName == null) {
			sPresTableName = "";
		}
		Node nPresentationTableName = xmldoc.createTextNode(sPresTableName);

		if (sPresTableMappingID == null) {
			sPresTableMappingID = "";
		}
		Node nPresentationTableMappingID = xmldoc.createTextNode(sPresTableMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes
		if (sPressDispayName == null) {
			sPressDispayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(sPressDispayName);
		if (sPressDescription == null) {
			sPressDescription = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(sPressDescription);

		Element ePresentationTable = xmldoc.createElement("PresentationTable");
		Element ePresentationTableID = xmldoc.createElement("PresentationTableID");
		Element ePresentationTableName = xmldoc.createElement("PresentationTableName");
		Element ePresentationTableMappingID = xmldoc.createElement("PresentationTableMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");

		ePresentationTableID.appendChild(nPresentationTableID);
		ePresentationTableName.appendChild(nPresentationTableName);
		ePresentationTableMappingID.appendChild(nPresentationTableMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationTable.appendChild(ePresentationTableID);
		ePresentationTable.appendChild(ePresentationTableName);
		ePresentationTable.appendChild(ePresentationTableMappingID);
		ePresentationTable.appendChild(ePresentationDisplayName);
		ePresentationTable.appendChild(ePresentationDescription);

		Element ePresentationTableAliasList = xmldoc.createElement("PresentationTableAliasList");
		Element ePresentationTableAlias = null;
		Node nCatalogFolderAlias = null;

		if(saPresentationTableAliases != null)
			for (int i=0; i< saPresentationTableAliases.length; i++) {
				ePresentationTableAlias = xmldoc.createElement("PresentationTableAlias");
				if (saPresentationTableAliases[i] == null) {
					nCatalogFolderAlias = xmldoc.createTextNode("");
				} else {
					nCatalogFolderAlias = xmldoc.createTextNode(saPresentationTableAliases[i]);
				}
				ePresentationTableAlias.appendChild(nCatalogFolderAlias);
				ePresentationTableAliasList.appendChild(ePresentationTableAlias);
			}

		ePresentationTable.appendChild(ePresentationTableAliasList);

		Element ePresentationAttributeIDList = xmldoc.createElement("PresentationAttributeIDList");
		Element ePresentationAttributeID = null;
		Node nPresentationAttributeID = null;

		if(vFolderAttributesID != null)
			for (String sFolderAttribID : vFolderAttributesID) {
				ePresentationAttributeID = xmldoc.createElement("PresentationAttributeID");
				if (sFolderAttribID == null)
					nPresentationAttributeID = xmldoc.createTextNode("");
				else
					nPresentationAttributeID = xmldoc.createTextNode(sFolderAttribID);

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
