package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Folder Attribute Parser class
 * @author danielgalassi@gmail.com
 *
 */
public class FolderAttribute {

	private String		presentationColumnID;
	private String		presentationColumnName;
	private String		presentationColumnMappingID;
	private String		presentationDispayName;
	private String		presentationDescription;
	private String[] 	presentationColumnAliases = null;
	

	public FolderAttribute (String declare, 
							String presentationColumn,
							BufferedReader udml) {
		String line;
		String trimmedDeclareStatement = declare.trim();
		int iIndexAS = trimmedDeclareStatement.indexOf(" AS ");
		int iIndexLA = trimmedDeclareStatement.indexOf(" LOGICAL ATTRIBUTE ");
		presentationColumnID = trimmedDeclareStatement.substring(presentationColumn.length(), iIndexAS).
												trim().replaceAll("\"", "");
		presentationColumnName = trimmedDeclareStatement.substring(iIndexAS+4, 
						trimmedDeclareStatement.indexOf( " LOGICAL ATTRIBUTE ",iIndexAS)).
						trim().replaceAll("\"", "");

		if (trimmedDeclareStatement.indexOf(" OVERRIDE LOGICAL NAME") == -1)
			presentationColumnMappingID = trimmedDeclareStatement.substring(iIndexLA+19).
												trim().replaceAll("\"", "");
		else
			presentationColumnMappingID = trimmedDeclareStatement.substring(iIndexLA+19, 
								trimmedDeclareStatement.indexOf(" OVERRIDE LOGICAL NAME")).
								trim().replace("\"", "");

		try {
			//ALIASES
			do {
				line = udml.readLine().trim().replaceAll("\"", "");
				if(line.indexOf("ALIASES (") != -1)
					presentationColumnAliases = line.substring(
										line.indexOf("ALIASES (")+9, 
										line.lastIndexOf(")")).
										trim().replaceAll("\"", "").split(",");
				
				//DISPLAY NAME
				if (line.indexOf("DISPLAY NAME ") != -1){
					presentationDispayName = line.trim().substring(
										line.indexOf("DISPLAY NAME ")+13,
										line.lastIndexOf(" ON")).
										trim().replaceAll("\"", "");
				}
				
				//DESCRIPTION
				if (line.indexOf("DESCRIPTION") != -1){
					presentationDescription = line.trim().substring(
							line.indexOf("{")+1,
							line.length()).
							trim().replaceAll("}", "");
					//LARGE TEXT
					while (line.indexOf("}") == -1){
						line = udml.readLine().trim();
						presentationDescription += "\n";
						presentationDescription += line.trim().replaceAll("}", "");
					}
				}
					
			} while (line.indexOf("PRIVILEGES") == -1 &&
					 line.indexOf(";") == -1);

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		trimmedDeclareStatement	= null;
		line		= null;
	}

	/**
	 * Folder Attribute XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (presentationColumnID == null) {
			presentationColumnID = "";
		}
		Node nPresentationColumnID = xmldoc.createTextNode(presentationColumnID);
		if (presentationColumnName == null) {
			presentationColumnName = "";
		}
		Node nPresentationColumnName = xmldoc.createTextNode(presentationColumnName);
		if (presentationColumnMappingID == null) {
			presentationColumnMappingID = "";
		}
		Node nPresentationColumnMappingID = xmldoc.createTextNode(presentationColumnMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes
		if (presentationDispayName == null) {
			presentationDispayName = "";
		}
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(presentationDispayName);
		if (presentationDescription == null) {
			presentationDescription = "";
		}
		Node nPresentationColumnDescription = xmldoc.createTextNode(presentationDescription);

		Element ePresentationColumn = xmldoc.createElement("PresentationColumn");
		Element ePresentationColumnID = xmldoc.createElement("PresentationColumnID");
		Element ePresentationColumnName = xmldoc.createElement("PresentationColumnName");
		Element ePresentationColumnMappingID = xmldoc.createElement("PresentationColumnMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");
		
		ePresentationColumnID.appendChild(nPresentationColumnID);
		ePresentationColumnName.appendChild(nPresentationColumnName);
		ePresentationColumnMappingID.appendChild(nPresentationColumnMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationColumn.appendChild(ePresentationColumnID);
		ePresentationColumn.appendChild(ePresentationColumnName);
		ePresentationColumn.appendChild(ePresentationColumnMappingID);
		ePresentationColumn.appendChild(ePresentationDisplayName);
		ePresentationColumn.appendChild(ePresentationDescription);

		Element ePresentationColumnAliasList = xmldoc.createElement("PresentationColumnAliasList");
		Element ePresentationColumnAlias = null;
		Node nCatalogFolderAlias = null;

		if(presentationColumnAliases != null)
			for (String sPresColAlias : presentationColumnAliases) {
				ePresentationColumnAlias = xmldoc.createElement("PresentationColumnAlias");
				if (sPresColAlias == null)
					nCatalogFolderAlias = xmldoc.createTextNode("");
				else
					nCatalogFolderAlias = xmldoc.createTextNode(sPresColAlias);

				ePresentationColumnAlias.appendChild(nCatalogFolderAlias);
				ePresentationColumnAliasList.appendChild(ePresentationColumnAlias);
			}

		ePresentationColumn.appendChild(ePresentationColumnAliasList);
		return ePresentationColumn;
	}
}
/*
 * DECLARE FOLDER ATTRIBUTE <FQ Folder Attribute Name> AS <Folder Attribute Name> LOGICAL ATTRIBUTE  <FQ Logical Column Name>
 *  ALIASES (<Folder Attribute Alias>)
 *  PRIVILEGES ( <...>);
 */
