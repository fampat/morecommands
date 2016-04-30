package com.mrnobody.morecommands.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * A class to parse calculations.<br>
 * Supported operators are <b>^, /, *, %, +, -</b> (This matches the operator precedence, except for + and - which are treated equally).<br>
 * Brackets are allowed and are parsed first.<br>
 * <p>
 * Example: 5.264/48(7-9^2)+46.486*55%7 = -6.385333333333316
 * <p>
 * Note that omitting a '*' sign before brackets does not affect the operator precedence<br>
 * e.g. 5/2(3) does NOT first multiply 2 by 3, it is parsed as if the calculation would be<br>
 * 5/2*(3) and therefore 5/2 is calculated first and then the result is multiplied by the term in the bracket
 * 
 * @author MrNobody98
 */
public final class CalculationParser {
	private CalculationParser() {}
	
	/**
	 * Parses a calculation. The operator precedence is ^ > / > * > % > +, -<br>
	 * Brackets are allowed.
	 * 
	 * @param calc the calculation string
	 * @return the result
	 * @throws NumberFormatException if the calculation string is invalid
	 */
	public static double parseCalculation(String calc) throws NumberFormatException {
		if (GlobalSettings.useRegexCalcParser) return parseCalculationViaRegex(calc.replace(",", ".").replace(" ", ""));
		else return parseCalculationManually(calc.replace(",", ".").replace(" ", ""));
	}
	
	// --------------------------------------------- PARSE CALCULATION VIA REGEX -----------------------------------------------
	/**
	 * An enumeration of operators
	 */
	private static enum Operator {
		PLUS     {@Override double perform(double op1, double op2) {return op1 + op2;}},
		MINUS    {@Override double perform(double op1, double op2) {return op1 - op2;}},
		MODULO   {@Override double perform(double op1, double op2) {return op1 % op2;}},
		MULTIPLY {@Override double perform(double op1, double op2) {return op1 * op2;}},
		DIVIDE   {@Override double perform(double op1, double op2) {return op1 / op2;}},
		POWER    {@Override double perform(double op1, double op2) {return Math.pow(op1, op2);}};
		
		/**
		 * applies this operator to two operands
		 * 
		 * @param op1 operand 1
		 * @param op2 operand 2
		 * @return the result
		 */
		abstract double perform(double op1, double op2);
		
		/**
		 * A map from the operator strings to the operator object
		 */
		public static final ImmutableMap<String, Operator> stringToOperatorMap = ImmutableMap.<String, Operator>builder()
				.put("+", PLUS).put("-", MINUS).put("%", MODULO).put("*", MULTIPLY).put("/", DIVIDE).put("^", POWER).build();
	}
	
	/** This matches an entire calculation, e.g. 16.165+4^4.4-5%2*9.684/46. Important: does not match brackets, because regex does not support nested structures */
	private static final Pattern calcPattern = Pattern.compile("^-?\\d+(?:\\.\\d+)?(?:[\\^\\+\\*/%-]-?\\d+(?:\\.\\d+)?)*$");
	/** This pattern groups the calculation into operand and operator*/
	private static final Pattern operandOperatorPattern = Pattern.compile("(?:^|\\G)(-?\\d+(?:\\.\\d+)?)($|[\\^\\+\\*/%-])");
	/** All allowed operators*/
	private static final ImmutableList<Character> allowedOperators = ImmutableList.of('^', '/', '*', '%', '+', '-');
	/** The operator precedence. The calculation is parsed from left to right, all operators in a list in this list are equal. The precedence of the operator lists is descending from left to right*/
	private static final ImmutableList<ImmutableList<String>> operatorPrecedence = ImmutableList.of(ImmutableList.of("^"), ImmutableList.of("/"), ImmutableList.of("*"), ImmutableList.of("%"), ImmutableList.of("+", "-"));
	
	/**
	 * Parses a calculation using the regexes above. Additionally parses brackets, which is not possible<br>
	 * with only regexes.
	 * 
	 * @param calc the calculation string
	 * @return the result
	 * @throws NumberFormatException if the calculation string is invalid
	 */
	private static double parseCalculationViaRegex(String calc) throws NumberFormatException {
		//parse brackets
		if (calc.contains("(") || calc.contains(")")) {
			StringBuilder builder = new StringBuilder(""), nested = new StringBuilder(""); 
			int nestedCount = 0; char prev = 0, beforeBracket = 0;
			
			for (char ch : calc.toCharArray()) {
				if (ch == '(') {
					if (nestedCount > 0) nested.append(ch);
					else if (nestedCount == 0) beforeBracket = prev;
					nestedCount++;
				}
				else if (ch == ')') {
					nestedCount--;
					if (nestedCount > 0) nested.append(ch);
					if (nestedCount == 0) {
						if (beforeBracket != 0 && !allowedOperators.contains(beforeBracket)) builder.append("*");
						builder.append(parseCalculationViaRegex(nested.toString()));
						nested = new StringBuilder(""); beforeBracket = 0;
					}
				}
				else if (nestedCount > 0) nested.append(ch);
				else builder.append(ch);
				prev = ch;
			}
			calc = builder.toString();
		}
		
		if (!calcPattern.matcher(calc).matches()) throw new NumberFormatException("Invalid calculation");
		else {
			Matcher operandOperatorMatcher = operandOperatorPattern.matcher(calc);
			List<MutablePair<Double, String>> operandOperatorList = new ArrayList<MutablePair<Double, String>>();
			
			while (operandOperatorMatcher.find()) {
				String operand = operandOperatorMatcher.group(1), operator = operandOperatorMatcher.group(2);
				operandOperatorList.add(new MutablePair<Double, String>(Double.parseDouble(operand), operator));
			}
			
			for (List<String> operators : operatorPrecedence) {
				List<MutablePair<Double, String>> temp = new ArrayList<MutablePair<Double, String>>(operandOperatorList);
				int removed = 0;
				
				for (int index = 0; index < operandOperatorList.size(); index++) {
					String operator = operandOperatorList.get(index).getRight();
					
					if (operators.contains(operator) && Operator.stringToOperatorMap.containsKey(operator)) {
						double operand1 = operandOperatorList.get(index).getLeft(), operand2 = operandOperatorList.get(index + 1).getLeft();
						temp.get(index + 1 - removed).setLeft(Operator.stringToOperatorMap.get(operator).perform(operand1, operand2));
						temp.remove(index - removed++);
					}
				}
				
				operandOperatorList = temp;
			}
			
			if (operandOperatorList.size() != 1) throw new NumberFormatException("Invalid Calculation");
			else if (operandOperatorList.get(0).getRight() != null && !operandOperatorList.get(0).getRight().isEmpty()) throw new NumberFormatException("Invalid Calculation");
			else return operandOperatorList.get(0).getLeft();
		}
	}
	// -------------------------------------------------------------------------------------------------------------------------
	
	// ---------------------------------------------- PARSE CALCULATION MANUALLY -----------------------------------------------
	/**
	 * An enumeration of operators which are NOT parsed from left to right (plus and minus are parsed from left to right)<br>
	 * The order of the enumeration values defines the precedence for parsing. The last value is parsed at first (power).
	 */
	private static enum Operation {
		MODULO {
			@Override double perform(double op1, double op2) {return op1 % op2;}
			@Override String[] split(String calc) {return calc.split("%");}
		},
		MULTIPLY {
			@Override double perform(double op1, double op2) {return op1 * op2;}
			@Override String[] split(String calc) {return calc.split("\\*");}
		},
		DIVIDE {
			@Override double perform(double op1, double op2) {return op1 / op2;}
			@Override String[] split(String calc) {return calc.split("/");}
		},
		POWER {
			@Override double perform(double op1, double op2) {return Math.pow(op1, op2);}
			@Override String[] split(String calc) {return calc.split("\\^");}
		};
		
		/**
		 * applies this operator to two operands
		 * 
		 * @param op1 operand 1
		 * @param op2 operand 2
		 * @return the result
		 */
		abstract double perform(double op1, double op2);
		
		/**
		 * splits a string with by this operator
		 * 
		 * @param calc the string to split
		 * @return the splitted elements
		 */
		abstract String[] split(String calc);
	}
	
	/**
	 * Parses a calculation using only string and char methods, no regex.
	 * 
	 * @param calc the calculation string
	 * @return the result
	 * @throws NumberFormatException if the calculation string is invalid
	 */
	private static double parseCalculationManually(String calc) throws NumberFormatException {
		char[] check = calc.toCharArray();
    
		for(char ch : check) {
			if (!Character.isDigit(ch) && 
					ch != '(' && ch != ')' && 
					ch != '+' && ch != '*' && 
					ch != '-' && ch != '/' &&
					ch != '.' && ch != ',' &&
					ch != '^' && ch != ' '
					 && ch != '%') throw new NumberFormatException("Invalid calculation");
		}
		
		return parseBrackets(calc);
 	}
	
	/**
	 * parses brackets
	 */
	private static double parseBrackets(String calc) throws NumberFormatException {
		calc = calc.replace(")(", ")*(");
		
		for (int j = 0; j <= 9; j++)
			calc = calc.replace(")" + j, ")*" + j).replace(j + "(", j + "*(");

		char[] chars = calc.toCharArray();
		boolean bracketOpen = false;
		int bracketCount = 0;
		StringBuilder expr = new StringBuilder("");
		StringBuilder parsedExpr = new StringBuilder("");
		double result = 0.0;
		
		if (calc.contains("(") || calc.contains(")")) {
			for (char ch : chars) {
				if (ch == ')') {
					bracketCount--;
					
					if (bracketCount == 0) {
						bracketOpen = false;
						
						if (expr.length() != 0) {
							parsedExpr.append(parseBrackets(expr.toString()));
							expr = new StringBuilder("");
						}
					}
				}
				
				if (bracketOpen) expr.append(ch);
				else if (ch != '(' && ch != ')') parsedExpr.append(ch);
				
				if (ch == '(') {bracketOpen = bracketCount == 0 ? true : bracketOpen; bracketCount++;}
			}
			
			result = parseFromLeftToRight(parsedExpr.toString().replace("+-", "-").replace("-+", "-").replace("--", "+").replace("++", "+"));
		}
		else result = parseFromLeftToRight(calc);
		
		return result;
	}
	  
	/**
	 * parses the calculation from left to right
	 */
	private static double parseFromLeftToRight(String calc) throws NumberFormatException {
		char[] chars = calc.toCharArray();
		StringBuilder expr = new StringBuilder("0");
		double result = 0.0;
		char operator = '+';
		boolean valid = true;
		
		if (calc.contains("+") || calc.contains("-")) {
			if (chars[0] == '-') operator = '-';
			
			for (char ch : chars) {
				if ((ch == '+' || ch == '-') && valid) {
					if (operator == '+') result += parseOperators(expr.toString());
					if (operator == '-') result -= parseOperators(expr.toString());
					
					if (ch == '+') operator = '+';
					if (ch == '-') operator = '-';
					
					expr = new StringBuilder("");
				}
				else {expr.append(ch); valid = !(ch == '*' || ch == '/' || ch == '^');}
			}
			
			if (operator == '+') result += parseOperators(expr.toString());
			if (operator == '-') result -= parseOperators(expr.toString());
		}
		else result = parseOperators(calc);
		
		return result;
	}
	
	/**
	 * parses the operators defined in {@link Operation}
	 */
	private static double parseOperators(String calc) throws NumberFormatException {
		return parseOperators(calc, 0);
	}
	
	/**
	 * parses the operators defined in {@link Operation}
	 */
	private static double parseOperators(String calc, int opIndex) throws NumberFormatException {
		if (opIndex >= Operation.values().length) return Double.parseDouble(calc);
		Operation operation = Operation.values()[opIndex];
		
		String[] operands = operation.split(calc);
		double result = parseOperators(operands[0], opIndex + 1);
		
		for (int i = 1; i < operands.length; i++)
			result = operation.perform(result, parseOperators(operands[i], opIndex + 1));
		
		return result;
	}
	//--------------------------------------------------------------------------------------------------------------------------
}
