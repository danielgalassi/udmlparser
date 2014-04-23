package obiee.udmlparser.utils;

import java.util.Vector;

/**
 * General Utilities class
 * @author danielgalassi@gmail.com
 *
 */
public class Utils {

	/**
	 * UDML calculation parser -identifies both, Logical and Physical columns
	 * @param sPrefixStr where to get the Subject Area prefix from
	 * @param sCalculation contains the expression to parse
	 * @param isDerived points out the type of expression to evaluate 
	 * 			(derived column or mapping calculation)
	 * @return Vector containing (logical or physical) column IDs
	 */
	public static Vector<String> CalculationParser (String sPrefixStr, String sCalculation, boolean isDerived) {
		String subjectAreaPrefix	= "\"" + sPrefixStr.substring(0, sPrefixStr.indexOf(".")) + "\"";
		String sExpr				= sCalculation.toString();
		String remainingExpression	= sCalculation.toString();
		
		Vector <String>	columnMappingIDs = null;

		int expressionBegins = 0;
		int startPosition = 0;
		int firstOccurrence = 0;
		int endPosition = 0;
		int loop = 8;

		boolean isDuplicated;

		if(sExpr.indexOf(subjectAreaPrefix) != -1)
			columnMappingIDs = new Vector<String>();

		if (isDerived) {
			loop = 4;
		}

		while (sExpr.indexOf(subjectAreaPrefix) != -1) {
			startPosition = sExpr.indexOf(subjectAreaPrefix);
			//gets the first occurrence of ."
			firstOccurrence = sExpr.indexOf(".\"");
			endPosition = sExpr.indexOf(subjectAreaPrefix) + subjectAreaPrefix.length();
			//4 = derived calculation; 8 = LTS calculation
			for (int i=0; i<loop; i++) {
				sExpr = sExpr.substring(firstOccurrence);
				firstOccurrence = sExpr.indexOf("\"") + 1;
				endPosition += firstOccurrence;
			}

			if (endPosition > remainingExpression.length()) {
				endPosition = remainingExpression.length();
			}
			
			//prevents repeatedly added columns --begin
			//TODO: refactor this part. Can be done using some Vector<String> method.
			isDuplicated = false;
			for (String columnMappingID : columnMappingIDs) {
				if (columnMappingID.equals(remainingExpression.substring(startPosition, endPosition).replaceAll("\"", ""))) {
					isDuplicated = true;
					break;
				}
			}

			if (!isDuplicated) {
				columnMappingIDs.add(remainingExpression.substring(startPosition, endPosition).replaceAll("\"", ""));
			}
			//prevents repeatedly added columns --end

			expressionBegins += endPosition;
			sExpr = sCalculation.substring(expressionBegins);
			remainingExpression = sExpr.toString();
		}

		return columnMappingIDs;
	}
}
