package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Catalog Folder Parser Class
 * @author danielgalassi@gmail.com
 *
 */
public class CatalogFolder {

	private String			sCatFolderID;
	private String			sCatFolderName;
	private String[]		saCatFolderAliases = null;
	private String			sCatFolderMappingID;
	private Vector <String>	vEntityFolderID = null;

	private String			sPressDispayName;
	private String			sPressDescription;

	public CatalogFolder(String sDeclareStmt,
			String sCatFolder,
			BufferedReader brUDML) {
		String line;
		String sTrimmedDS = sDeclareStmt.trim();
		int iIndexAS = sTrimmedDS.indexOf(" AS ");
		//finds custom icons in Subject Areas
		int iICONIDX = sTrimmedDS.indexOf(" ICON INDEX ");
		sCatFolderID = sTrimmedDS.substring(sCatFolder.length(), iIndexAS).
				trim().replaceAll("\"", "");
		if (iICONIDX != -1)
			sCatFolderName = sTrimmedDS.substring(iIndexAS+4, iICONIDX).
			trim().replaceAll("\"", "");
		else
			sCatFolderName = sTrimmedDS.substring(iIndexAS+4).
			trim().replaceAll("\"", "");
		try {
			//SUBJECT AREA
			line = brUDML.readLine().trim().replaceAll("\"", "");

			if (line.indexOf("SUBJECT AREA ") != -1)
				sCatFolderMappingID = line.substring(line.
						indexOf("SUBJECT AREA ")+13).
						trim().replaceAll("\"", "");

			//ENTITY FOLDERS LIST
			line = brUDML.readLine().trim().replaceAll("\"", "");
			if (line.indexOf("ENTITY FOLDERS (") != -1) {
				vEntityFolderID = new Vector<String>();
				do {
					line = brUDML.readLine().trim().replaceAll("\"", "");
					vEntityFolderID.add(line.substring(0,line.length()-1));
				} while (line.charAt(line.length()-1) != ')');
			}

			//NO FURTHER ACTIONS FOR DESCRIPTION AND PRIVILEGES,
			//RECOVERING ALIASES
			do {
				line = brUDML.readLine().trim().replaceAll("\"", "");
				if (line.indexOf("ALIASES (") != -1)
					saCatFolderAliases = line.substring(line.
							indexOf("ALIASES (")+9, line.lastIndexOf(")")).
							trim().replaceAll("\"", "").split(",");

				//DISPLAY NAME
				if (line.indexOf("DISPLAY NAME ") != -1){
					int i1 = line.indexOf("DISPLAY NAME ")+13;
					int i2 = line.lastIndexOf(" ON");
					sPressDispayName = line.trim().substring(
							i1,
							i2).
							trim().replaceAll("\"", "");
				}

				//DESCRIPTION
				if (line.indexOf("DESCRIPTION ") != -1){
					sPressDescription = line.trim().substring(
							line.indexOf("{")+1,
							line.length()).
							trim().replaceAll("}", "");
					//LARGE TEXT
					while (line.indexOf("}") == -1){
						line = brUDML.readLine().trim();
						sPressDescription += "\n";
						sPressDescription += line.trim().replaceAll("}", "");
					}
				}
			} while (line.indexOf(";") == -1);

		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}

		sTrimmedDS	= null;
		line		= null;
	}

	/**
	 * Catalog Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		if (sCatFolderID == null)
			sCatFolderID = "";
		Node nPresentationCatalogID = xmldoc.createTextNode(sCatFolderID);

		if (sCatFolderName == null)
			sCatFolderName = "";
		Node nPresentationCatalogName = xmldoc.createTextNode(sCatFolderName);

		if (sCatFolderMappingID == null)
			sCatFolderMappingID = "";
		Node nCatalogFolderMappingID = xmldoc.createTextNode(sCatFolderMappingID);
		//added DISPLAY NAME and DESCRIPTION nodes

		if (sPressDispayName == null)
			sPressDispayName = "";
		Node nPresentationColumnDisplayName = xmldoc.createTextNode(sPressDispayName);

		if (sPressDescription == null)
			sPressDescription = "";
		Node nPresentationColumnDescription = xmldoc.createTextNode(sPressDescription);

		Element ePresentationCatalog = xmldoc.createElement("PresentationCatalog");
		Element ePresentationCatalogID = xmldoc.createElement("PresentationCatalogID");
		Element ePresentationCatalogName = xmldoc.createElement("PresentationCatalogName");
		Element eCatalogFolderMappingID = xmldoc.createElement("PresentationCatalogMappingID");
		//added DISPLAY NAME and DESCRIPTION elements
		Element ePresentationDisplayName = xmldoc.createElement("displayName");
		Element ePresentationDescription = xmldoc.createElement("description");

		ePresentationCatalogID.appendChild(nPresentationCatalogID);
		ePresentationCatalogName.appendChild(nPresentationCatalogName);
		eCatalogFolderMappingID.appendChild(nCatalogFolderMappingID);
		ePresentationDisplayName.appendChild(nPresentationColumnDisplayName);
		ePresentationDescription.appendChild(nPresentationColumnDescription);

		ePresentationCatalog.appendChild(ePresentationCatalogID);
		ePresentationCatalog.appendChild(ePresentationCatalogName);
		ePresentationCatalog.appendChild(eCatalogFolderMappingID);
		ePresentationCatalog.appendChild(ePresentationDisplayName);
		ePresentationCatalog.appendChild(ePresentationDescription);

		Element eCatalogFolderAliasList = xmldoc.createElement("PresentationCatalogAliasList");
		Element eCatalogFolderAlias = null;
		Node nCatalogFolderAlias = null;

		if(saCatFolderAliases != null)
			for (String sCatFolderAlias : saCatFolderAliases) {
				eCatalogFolderAlias = xmldoc.createElement("PresentationCatalogAlias");
				if (sCatFolderAlias == null)
					nCatalogFolderAlias = xmldoc.createTextNode("");
				else
					nCatalogFolderAlias = xmldoc.createTextNode(sCatFolderAlias);

				eCatalogFolderAlias.appendChild(nCatalogFolderAlias);
				eCatalogFolderAliasList.appendChild(eCatalogFolderAlias);
			}

		ePresentationCatalog.appendChild(eCatalogFolderAliasList);

		Element ePresentationFolderList = xmldoc.createElement("PresentationTableIDList");
		Element ePresentationFolder = null;
		Node nPresentationFolder = null;

		if(vEntityFolderID != null)
			for (String sEntityFolderID : vEntityFolderID) {
				ePresentationFolder = xmldoc.createElement("PresentationTableID");
				if (sEntityFolderID == null)
					nPresentationFolder = xmldoc.createTextNode("");
				else
					nPresentationFolder = xmldoc.createTextNode(sEntityFolderID);

				ePresentationFolder.appendChild(nPresentationFolder);
				ePresentationFolderList.appendChild(ePresentationFolder);
			}

		ePresentationCatalog.appendChild(ePresentationFolderList);
		return ePresentationCatalog;
	}

}
/*
 * DECLARE CATALOG FOLDER <FQ Catalog Name> AS <Catalog Name>
 * SUBJECT AREA <FQ Subject Area Name>
 * ENTITY FOLDERS (
 * <FQ Entity Folder Name>,
 * <FQ Entity Folder Name>)
 * ALIASES (<Catalog Folder Alias>)
 * DESCRIPTION {<...>}
 * PRIVILEGES (<...>);
 */
