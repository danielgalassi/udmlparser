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
	 * @param prefix where to get the Subject Area prefix from
	 * @param calculation contains the expression to parse
	 * @param isDerived points out the type of expression to evaluate 
	 * 			(derived column or mapping calculation)
	 * @return Vector containing (logical or physical) column IDs
	 */
	public static Vector<String> CalculationParser (String prefix, String calculation, boolean isDerived) {
		String subjectAreaPrefix	= "\"" + prefix.substring(0, prefix.indexOf(".")) + "\"";
		String expression			= calculation.toString();
		String remainingExpression	= calculation.toString();
		
		Vector <String>	columnMappingIDs = null;

		int expressionBegins = 0;
		int startPosition = 0;
		int firstOccurrence = 0;
		int endPosition = 0;
		int loop = 8;

		boolean isDuplicated;

		if(expression.indexOf(subjectAreaPrefix) != -1) {
			columnMappingIDs = new Vector<String>();
		}

		if (isDerived) {
			loop = 4;
		}

		while (expression.indexOf(subjectAreaPrefix) != -1) {
			startPosition = expression.indexOf(subjectAreaPrefix);
			//gets the first occurrence of ."
			firstOccurrence = expression.indexOf(".\"");
			endPosition = expression.indexOf(subjectAreaPrefix) + subjectAreaPrefix.length();
			//4 = derived calculation; 8 = LTS calculation
			for (int i=0; i<loop; i++) {
				expression = expression.substring(firstOccurrence);
				firstOccurrence = expression.indexOf("\"") + 1;
				endPosition += firstOccurrence;
			}

			if (endPosition > remainingExpression.length()) {
				endPosition = remainingExpression.length();
			}

			//prevents repeatedly added columns --begin
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
			expression = calculation.substring(expressionBegins);
			remainingExpression = expression.toString();
		}
		return columnMappingIDs;
	}
}
