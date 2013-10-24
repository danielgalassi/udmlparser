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

	private String			presentationTableID;
	private String			presentationTableName;
	private String			presentationTableMappingID;
	private String			presentationDispayName;
	private String			presentationDescription;
	private Vector <String>	folderAttributeIDs = null;
	private String[]		presentationTableAliases = null;

	public EntityFolder (String declare, 
						 String entityFolder, 
						 BufferedReader udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");
		presentationTableID = trimmedDeclareStatement.substring(entityFolder.length(), iIndexAS).
												trim().replaceAll("\"", "");
		if (trimmedDeclareStatement.indexOf(" ENTITY ") != -1 && 
			trimmedDeclareStatement.indexOf(" ENTITY ") != trimmedDeclareStatement.
												lastIndexOf(" ENTITY ")) {
			presentationTableName = trimmedDeclareStatement.substring(iIndexAS + 4, 
									trimmedDeclareStatement.indexOf(" ENTITY ", iIndexAS)).
									trim().replaceAll("\"", "");
			presentationTableMappingID = trimmedDeclareStatement.substring(trimmedDeclareStatement.
									indexOf(" ENTITY ", iIndexAS + 4) + 8).
									trim().replaceAll("\"", "");
		}
		else {
			presentationTableName = trimmedDeclareStatement.substring(iIndexAS + 4).
									trim().replaceAll("\"", "");
			presentationTableMappingID = "";
		}

		try {
			//FOLDER ATTRIBUTES LIST
			line = udml.readLine().trim().replaceAll("\"", "");
			if (line.indexOf("FOLDER ATTRIBUTES ") != -1) {
				folderAttributeIDs = new Vector<String>();
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
					folderAttributeIDs.add(line.substring(0,line.length()-1));
				} while (line.charAt(line.length()-1) != ')');
			}

			//ALIASES
			if (line.indexOf(";") == -1) {
				do {
					line = udml.readLine().trim().replaceAll("\"", "");
				} while (line.indexOf("ALIASES (") != -1);
				if (line.indexOf("ALIASES (") != -1)
					presentationTableAliases = line.substring(
										line.indexOf("ALIASES (")+9, 
										line.lastIndexOf(")")).
										trim().replaceAll("\"", "").split(",");
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES
			while ( line.indexOf("PRIVILEGES") == -1 &&
					line.indexOf(";") == -1){
				line = udml.readLine();
				//DISPLAY NAME
				int displayNameIdx = line.indexOf("DISPLAY NAME ");
				if (displayNameIdx != -1){
					presentationDispayName = line.trim().substring(displayNameIdx+13, line.lastIndexOf(" ON")).trim().replaceAll("\"", "");
				}
				
				//DESCRIPTION
				if (line.indexOf("DESCRIPTION ") != -1){
					int iIdexOpen = line.indexOf("{")+1;
					int length = line.length();
					presentationDescription = line.substring(
							iIdexOpen,
							length).
							replaceAll("}", "").trim();
					//LARGE TEXT
					while (line.indexOf("}") == -1){
						line = udml.readLine().trim();
						presentationDescription += "\n";
						presentationDescription += line.trim().replaceAll("}", "");
					}
				}
			}

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		trimmedDeclareStatement	= null;
		line		= null;
	}

	/**
	 * Entity Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (presentationTableID == null) {
			presentationTableID = "";
		}
		Node nPresentationTableID = xmldoc.createTextNode(presentationTableID);

		if (presentationTableName == null) {
			presentationTableName = "";
		}
		Node nPresentationTableName = xmldoc.createTextNode(presentationTableName);

		if (presentationTableMappingID == null) {
			presentationTableMappingID = "";
		}
		Node nPresentationTableMappingID = xmldoc.createTextNode(presentationTableMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes
		if (presentationDispayName == null) {
			presentationDispayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(presentationDispayName);
		if (presentationDescription == null) {
			presentationDescription = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(presentationDescription);

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

		if(presentationTableAliases != null)
			for (int i=0; i< presentationTableAliases.length; i++) {
				ePresentationTableAlias = xmldoc.createElement("PresentationTableAlias");
				if (presentationTableAliases[i] == null) {
					nCatalogFolderAlias = xmldoc.createTextNode("");
				} else {
					nCatalogFolderAlias = xmldoc.createTextNode(presentationTableAliases[i]);
				}
				ePresentationTableAlias.appendChild(nCatalogFolderAlias);
				ePresentationTableAliasList.appendChild(ePresentationTableAlias);
			}

		ePresentationTable.appendChild(ePresentationTableAliasList);

		Element ePresentationAttributeIDList = xmldoc.createElement("PresentationAttributeIDList");
		Element ePresentationAttributeID = null;
		Node nPresentationAttributeID = null;

		if(folderAttributeIDs != null)
			for (String sFolderAttribID : folderAttributeIDs) {
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
