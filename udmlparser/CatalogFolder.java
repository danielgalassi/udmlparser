package udmlparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Catalog Folder Parser Class
 * @author dgalassi
 *
 */
public class CatalogFolder {

	private String			sCatFolderID;
	private String			sCatFolderName;
	private String[]		saCatFolderAliases = null;
	private String			sCatFolderMappingID;
	private Vector <String>	vEntityFolderID = null;

	public CatalogFolder(String sDeclareStmt,
							String sCatFolder,
							BufferedReader brUDML) {
		String line;
		sCatFolderID = sDeclareStmt.trim().substring(sCatFolder.length(),
				sDeclareStmt.trim().indexOf(" AS ")).
				trim().replaceAll("\"", "");
		sCatFolderName = sDeclareStmt.trim().substring(sDeclareStmt.
				indexOf(" AS ")+4).trim().replaceAll("\"", "");

		try {
			//SUBJECT AREA
			line = brUDML.readLine().trim().replaceAll("\"", "");

			if(line.indexOf("SUBJECT AREA ") != -1)
				sCatFolderMappingID = line.substring(line.
						indexOf("SUBJECT AREA ")+13).
						trim().replaceAll("\"", "");

			//ENTITY FOLDERS LIST
			line = brUDML.readLine().trim().replaceAll("\"", "");
			if(line.indexOf("ENTITY FOLDERS (") != -1) {
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
				if(line.indexOf("ALIASES (") != -1)
					saCatFolderAliases = line.substring(line.
							indexOf("ALIASES (")+9, line.lastIndexOf(")")).
							trim().replaceAll("\"", "").split(",");
			} while (line.indexOf(";") == -1);
		} catch (IOException e) {
			System.out.println ("IO exception =" + e);
		}
	}

	/**
	 * Catalog Folder XML serializer
	 * @param xmldoc XML document
	 * @return XML fragment
	 */
	public Element serialize(Document xmldoc) {
		Node nPresentationCatalogID = xmldoc.createTextNode(sCatFolderID);
		Node nPresentationCatalogName = xmldoc.createTextNode(sCatFolderName);
		Node nCatalogFolderMappingID = xmldoc.createTextNode(sCatFolderMappingID);

		Element ePresentationCatalog = xmldoc.createElement("PresentationCatalog");
		Element ePresentationCatalogID = xmldoc.createElement("PresentationCatalogID");
		Element ePresentationCatalogName = xmldoc.createElement("PresentationCatalogName");
		Element eCatalogFolderMappingID = xmldoc.createElement("PresentationCatalogMappingID");

		ePresentationCatalogID.appendChild(nPresentationCatalogID);
		ePresentationCatalogName.appendChild(nPresentationCatalogName);
		eCatalogFolderMappingID.appendChild(nCatalogFolderMappingID);

		ePresentationCatalog.appendChild(ePresentationCatalogID);
		ePresentationCatalog.appendChild(ePresentationCatalogName);
		ePresentationCatalog.appendChild(eCatalogFolderMappingID);

		Element eCatalogFolderAliasList = xmldoc.createElement("PresentationCatalogAliasList");
		Element eCatalogFolderAlias = null;
		Node nCatalogFolderAlias = null;

		if(saCatFolderAliases != null)
			for (int i=0; i< saCatFolderAliases.length; i++) {
				eCatalogFolderAlias = xmldoc.createElement("PresentationCatalogAlias");
				nCatalogFolderAlias = xmldoc.createTextNode(saCatFolderAliases[i]);
				eCatalogFolderAlias.appendChild(nCatalogFolderAlias);
				eCatalogFolderAliasList.appendChild(eCatalogFolderAlias);
			}

		ePresentationCatalog.appendChild(eCatalogFolderAliasList);

		Element ePresentationFolderList = xmldoc.createElement("PresentationTableIDList");
		Element ePresentationFolder = null;
		Node nPresentationFolder = null;

		if(vEntityFolderID != null)
			for (int i=0; i< vEntityFolderID.size(); i++) {
				ePresentationFolder = xmldoc.createElement("PresentationTableID");
				nPresentationFolder = xmldoc.createTextNode(vEntityFolderID.get(i));
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
