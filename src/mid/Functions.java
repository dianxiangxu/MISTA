/* 	
	Author Dianxiang Xu
*/
package mid;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Stack;

import kernel.Kernel;

import parser.MIDParser;

public class Functions {

	public static final String EQUALS = "equals";
	public static final String BOUND = "bound";
	public static final String GreaterThan = "gt";
	public static final String GreaterThanOrEqualTo = "gte";
	public static final String LessThan = "lt";
	public static final String LessThanOrEqualTo = "lte";
	public static final String ISEVEN = "isEven";
	public static final String ISODD = "isOdd";
	public static final String ADD = "add";
	public static final String SUBTRACT = "subtract";
	public static final String MULTIPLY = "multiply";
	public static final String DIVIDE = "divide";
	public static final String MODULUS = "modulus";
	public static final String ASSERT = "assert";		// assert that a predicate is true (has one or more token)
	public static final String TOKENCOUNT = "tokenCount";	// count the number of tokens in a place
	public static final String BELONGSTO = "belongsTo";	// count the number of tokens in a place
	public static final String OR = "OR";	// count the number of tokens in a place
	
	public static String[] FUNCTIONS = { 
		EQUALS, BOUND, 
		GreaterThan, GreaterThanOrEqualTo,
		LessThan, LessThanOrEqualTo,
		ISEVEN, ISODD,
		ADD, SUBTRACT,
		MULTIPLY, DIVIDE,
		MODULUS,
		ASSERT, TOKENCOUNT,
		BELONGSTO,
		OR
	};
	// assert(p) <==> tokenCount(p, x) and gt(x, 0)
	
	public static byte[] ARITIES = {
		2, // EQUALS
		1, // BOUND
		2, // GreaterThan
		2, // GreaterThanOrEqualTo,
		2, // Less than
		2, // LessThanOrEqualTo
		1, // ISEVEN
		1, // ISODD
		3, // ADD
		3, // Subtract
		3, // Multiple	
		3, // Divide
		3, // Modula
		1, // ASSERT
		2, // Token count
		0,  // Belongs to
		1	// OR
	};

	public static final int ADDITION = 0;
	public static final int SUBTRACTION = 1;
	public static final int MULTIPLICATION = 2;
	public static final int DIVISION = 3;
	public static final int MODULUSOP = 4;
	
	public static int getArithmeticOperator(String opStr){
		if (opStr.equals("+"))
			return ADDITION;
		else 
		if (opStr.equals("-"))
		return SUBTRACTION;
		else 
		if (opStr.equals("*"))
			return MULTIPLICATION;
		else 
		if (opStr.equals("/"))
			return DIVISION;
		else 
			return MODULUSOP;
	}
	
	public static boolean isFunction(Predicate predicate) {
		for (int i=0; i<FUNCTIONS.length; i++) {
			if (FUNCTIONS[i].equalsIgnoreCase(predicate.getName()))
				return true;
		}
		return false;
	}

	public static boolean isEqualFunction(Predicate predicate){
		String name = predicate.getName();
		return name.equalsIgnoreCase(EQUALS);
	}

	public static boolean isComparisonFunction(Predicate predicate){
		String name = predicate.getName();
		return name.equalsIgnoreCase(EQUALS) 
			|| name.equalsIgnoreCase(GreaterThan)	
			|| name.equalsIgnoreCase(GreaterThanOrEqualTo)
			|| name.equalsIgnoreCase(LessThan)
			|| name.equalsIgnoreCase(LessThanOrEqualTo);		
	}

	public static boolean isArithmeticFunction(Predicate predicate){
		String name = predicate.getName();
		return name.equalsIgnoreCase(ADD) 
			|| name.equalsIgnoreCase(SUBTRACT)
			|| name.equalsIgnoreCase(MULTIPLY)
			|| name.equalsIgnoreCase(DIVIDE)
			||  name.equalsIgnoreCase(MODULUS)
			;		
			
	}
	
	// precondition: isComparisonFunction(Predicate predicate) || isArithmeticFunction(Predicate predicate)
	public static String printArithmeticOperation(Predicate predicate){
		String name = predicate.getName();
		ArrayList<String> arguments = predicate.getArguments();
		if (name.equalsIgnoreCase(EQUALS))
			return predicate.getNegation()? arguments.get(0)+" != "+arguments.get(1):
				arguments.get(0)+" = "+arguments.get(1);
		else	
		if (name.equalsIgnoreCase(GreaterThan))
			return  arguments.get(0)+" > "+arguments.get(1);
		else	
		if(name.equalsIgnoreCase(GreaterThanOrEqualTo))
			return  arguments.get(0)+" >= "+arguments.get(1);
		else if (name.equalsIgnoreCase(LessThan))
			return arguments.get(0)+" < "+arguments.get(1);
		else		
		if (name.equalsIgnoreCase(LessThanOrEqualTo))
			return arguments.get(0)+" <= "+arguments.get(1);
		else
		if (name.equalsIgnoreCase(ADD))
			return arguments.get(2)+" = "+arguments.get(0)+" + "+arguments.get(1);
		else
		if (name.equalsIgnoreCase(SUBTRACT))
			return arguments.get(2)+" = "+arguments.get(0)+" - "+arguments.get(1);
		else
		if (name.equalsIgnoreCase(MULTIPLY))
			return arguments.get(2)+" = "+arguments.get(0)+" * "+arguments.get(1);
		else
		if (name.equalsIgnoreCase(DIVIDE))
			return arguments.get(2)+" = "+arguments.get(0)+" / "+arguments.get(1);
		else
//		if (name.equalsIgnoreCase(MODULUS))
			return arguments.get(2)+" = "+arguments.get(0)+" % "+arguments.get(1);		
	}
	
	public static boolean isBoundFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(BOUND);
	}
	
	public static boolean isAssertFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(ASSERT);
	}

	public static boolean isTokenCountFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(TOKENCOUNT);
	}

	public static boolean isBelongsToFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(BELONGSTO);
	}

	public static boolean isORFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(OR);
	}

	public static boolean isEvenFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(ISEVEN);
	}

	public static boolean isOddFunction(Predicate predicate){
		return predicate.getName().equalsIgnoreCase(ISODD);
	}

	public static boolean hasCorrectArguments(Predicate predicate){
		if (isBelongsToFunction(predicate))
			return predicate.arity()>1;
		else if (isORFunction(predicate)){
			return parseORFunctionArguments(predicate.getArguments());
		}
		for (int i=0; i<FUNCTIONS.length; i++) {
			if (FUNCTIONS[i].equalsIgnoreCase(predicate.getName()) && ARITIES[i]==predicate.arity())
				if (hasCorrectTypes(predicate))
					return true;
		}
		return false;		
	}
		
	private static boolean hasCorrectTypes(Predicate predicate){
		if (isComparisonFunction(predicate)){
			if (predicate.getName().equalsIgnoreCase(EQUALS))
				return true;
			String firstArg = predicate.getArguments().get(0);
			return (MID.isVariable(firstArg) || MID.containsSymbolKey(firstArg) || isIntegerArgument(firstArg));
		} else
		if (isBoundFunction(predicate)){
			String firstArg = predicate.getArguments().get(0);
			return MID.isVariable(firstArg);
			
		} else
		if (isArithmeticFunction(predicate)){
			String firstArg = predicate.getArguments().get(0);
			String secondArg = predicate.getArguments().get(1);
			String thirdArg = predicate.getArguments().get(2);
			return (MID.isVariable(firstArg) || MID.containsSymbolKey(firstArg) || isIntegerArgument(firstArg)) &&
				(MID.isVariable(secondArg) || MID.containsSymbolKey(secondArg) || isIntegerArgument(secondArg)) &&
				(MID.isVariable(thirdArg) || MID.containsSymbolKey(thirdArg) || isIntegerArgument(thirdArg));			
		} else
		if (isTokenCountFunction(predicate)) {
			String secondArg = predicate.getArguments().get(1);
			return MID.isVariable(secondArg)|| MID.containsSymbolKey(secondArg) || isIntegerArgument(secondArg);
		} else
		if (isEvenFunction(predicate) || isOddFunction(predicate)) {
				String arg = predicate.getArguments().get(0);
				return MID.isVariable(arg) || MID.containsSymbolKey(arg) || isIntegerArgument(arg);
		}
			
		return true;
	}
	
	private static boolean isIntegerArgument(String arg){
		try {
			String newArg=removeQuotesFromString(arg.trim());
			Integer.parseInt(newArg); 
			return true;
		}
		catch (Exception e) {
		}
		return false;
	}
	
	public static boolean assertTrue(Predicate predicate, Marking marking){
		String place = predicate.getArguments().get(0);
		ArrayList<Tuple> tokens = marking.getTuples(place);
//if (tokens!=null && tokens.size()>0)
//System.out.println("Tokens: "+tokens.size());
		return 
			predicate.getNegation()? tokens==null || tokens.size()==0:
			tokens!=null && tokens.size()>0;
	}
	
	public static boolean tokenCount(Predicate predicate, Marking marking, Hashtable <String, String> bindings, Stack<BindingRecord> bindingHistory, int inputIndex){
		String place = predicate.getArguments().get(0);
		ArrayList<Tuple> tokens = marking.getTuples(place);
		int count = tokens==null? 0: tokens.size();
		String arg = predicate.getArguments().get(1);
		if (MID.isVariable(arg) && bindings.get(arg)==null) {
			bindings.put(arg, Integer.toString(count));
			bindingHistory.push(new BindingRecord(arg, inputIndex));
			return true;
		}
		//else
		// !MID.isVariable(arg) || bindings.get(arg)!=null
		if (bindings.get(arg)!=null)
			arg = bindings.get(arg);
		try {
			int countArg = Integer.parseInt(arg); 
			return predicate.getNegation()? count!=countArg: count==countArg;
		}
		catch (Exception e) {
			System.out.println("Exception in tokenCount: "+arg+" is not integer");			
		}
		return false;	
	}
	
	public static boolean isTrue(Predicate predicate, Hashtable <String, String> bindings, Stack<BindingRecord> bindingHistory, int inputIndex) {
		String name = predicate.getName();
		ArrayList<String> arguments = predicate.getArguments();
		try {
			if (name.equalsIgnoreCase(EQUALS)) {
				String secondArgument = MIDParser.evaluateExpression(arguments.get(1),bindings);
				if (MID.containsNumberKey(secondArgument))
					secondArgument = MID.getSymbolForNumber(secondArgument);
				if (MID.isVariable(arguments.get(0)) && bindings.get(arguments.get(0))==null){
					bindings.put(arguments.get(0), secondArgument);
					bindingHistory.push(new BindingRecord(arguments.get(0), inputIndex));
				}
//System.out.println(arguments.get(0)+"="+secondArgument);				
				return isEqualTo(predicate.getNegation(), arguments.get(0), secondArgument, bindings);
			} else
			if (name.equalsIgnoreCase(BOUND)) {
				String binding = bindings.get(arguments.get(0));
				return predicate.getNegation()? binding==null: binding!=null;
			} else
			if (name.equalsIgnoreCase(GreaterThanOrEqualTo)) {
				String secondArgument = MIDParser.evaluateExpression(arguments.get(1),bindings);
//System.out.println(arguments.get(0)+">="+secondArgument);				
				return isGreaterThanOrEqualTo(arguments.get(0), secondArgument, bindings, true);
			} else
			if (name.equalsIgnoreCase(GreaterThan)) {
				String secondArgument = MIDParser.evaluateExpression(arguments.get(1),bindings);
//System.out.println(arguments.get(0)+">"+secondArgument);				
				return isGreaterThanOrEqualTo(arguments.get(0), secondArgument, bindings, false);
			} else
			if (name.equalsIgnoreCase(LessThanOrEqualTo)) {
				String secondArgument = MIDParser.evaluateExpression(arguments.get(1),bindings);
//System.out.println(arguments.get(0)+"<="+secondArgument);				
				return isLessThanOrEqualTo(arguments.get(0), secondArgument, bindings, true);
			} else
			if (name.equalsIgnoreCase(LessThan)) {
				String secondArgument = MIDParser.evaluateExpression(arguments.get(1),bindings);
//System.out.println(arguments.get(0)+"<"+secondArgument);				
				return isLessThanOrEqualTo(arguments.get(0), secondArgument, bindings, false);
			} else
			if (name.equalsIgnoreCase(ISEVEN)) {
					return isEven(predicate.getNegation(), arguments.get(0), bindings);
			} else
			if (name.equalsIgnoreCase(ISODD)) {
				return isOdd(predicate.getNegation(), arguments.get(0), bindings);
			} else
			if (name.equalsIgnoreCase(ADD)) {
				return compute(ADDITION, arguments, bindings, bindingHistory, inputIndex);
			} else
			if (name.equalsIgnoreCase(SUBTRACT)) {
				return compute(SUBTRACTION, arguments, bindings, bindingHistory, inputIndex);
			} else
			if (name.equalsIgnoreCase(MULTIPLY)) {
				return compute(MULTIPLICATION, arguments, bindings, bindingHistory, inputIndex);
			} else
			if (name.equalsIgnoreCase(DIVIDE)) {
				return compute(DIVISION, arguments, bindings, bindingHistory, inputIndex);
			} else
			if (name.equalsIgnoreCase(MODULUS)) {
				return compute(MODULUSOP, arguments, bindings, bindingHistory, inputIndex);
			} else
			if (name.equalsIgnoreCase(BELONGSTO)) {
				return belongsTo(predicate.getNegation(), arguments, bindings);
			} else
			if (name.equalsIgnoreCase(OR)) {
				return orFunction(predicate.getNegation(), arguments, bindings, bindingHistory, inputIndex);
			}
		}
		catch (Exception e) {
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
		}
//		System.out.println("Predicate  "+predicate);
		return false;
	}

	private static boolean isEqualTo(boolean negation, String arg1, String arg2, Hashtable <String, String> bindings){
		String value1 = getBoundValue(arg1, bindings);
		String value2 = getBoundValue(arg2, bindings);
		return (!negation && value1.equals(value2)  ||
			negation && !value1.equals(value2));
	}
	
	private static boolean isGreaterThanOrEqualTo(String arg1, String arg2, Hashtable <String, String> bindings, boolean orEqualTo){
		try {
			if (orEqualTo)
				return Integer.parseInt(getBoundValue(arg1, bindings)) 
					>= Integer.parseInt(getBoundValue(arg2, bindings));
			else {
				return Integer.parseInt(getBoundValue(arg1, bindings)) 
					> Integer.parseInt(getBoundValue(arg2, bindings));
			}
		}
		catch (Exception e) {
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
			System.out.println("Non-integer in comparison: "+ arg1+" vs "+arg2);			
		}
		return false;
	}

	private static boolean isLessThanOrEqualTo(String arg1, String arg2, Hashtable <String, String> bindings, boolean orEqualTo){
		return isGreaterThanOrEqualTo(arg2, arg1, bindings, orEqualTo);
	}

	private static boolean isEven(boolean negation, String arg, Hashtable <String, String> bindings){
		try {
			int value = Integer.parseInt(getBoundValue(arg, bindings));
			return negation? value % 2!=0: value % 2==0;
		}
		catch (Exception e) {
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
			System.out.println("Non-integer occurred in isEven: "+ arg);			
			return false;
		}
	}

	private static boolean isOdd(boolean negation, String arg, Hashtable <String, String> bindings){
		try {
			int value = Integer.parseInt(getBoundValue(arg, bindings));
			return negation? value % 2==0: value % 2!=0;
		}
		catch (Exception e) {
			System.out.println("Non-integer occurred in isEven: "+ arg);			
			return false;
		}
	}

	private static boolean compute(int operator, ArrayList<String> arguments, Hashtable <String, String> bindings, Stack<BindingRecord> bindingHistory, int inputIndex) {
		String arg1 = getBoundValue(arguments.get(0), bindings);
		String arg2 = getBoundValue(arguments.get(1), bindings);		
		String arg3 = arguments.get(2);
		int result = 0;
		try {
			switch (operator){
				case ADDITION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							+ Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case SUBTRACTION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							- Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case MULTIPLICATION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							* Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case DIVISION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							/ Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case MODULUSOP: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
						% Integer.parseInt(getBoundValue(arg2, bindings));
						break;
			}
		}	
		catch (Exception e) {
			System.out.println("Non-integer occurred in an arithmetic operation: "+ arg1+" and "+arg2);			
			return false;
		}
		if (MID.isVariable(arg3) && bindings.get(arg3)==null) {
			String resultNumber = Integer.toString(result);
			if (MID.containsNumberKey(resultNumber))
				resultNumber = MID.getSymbolForNumber(resultNumber);
			bindings.put(arg3, resultNumber);
			bindingHistory.push(new BindingRecord(arg3, inputIndex));
			return true;
		}
		try {
				return Integer.parseInt(getBoundValue(arg3, bindings)) == result;
			}
		catch (Exception e) {
			System.out.println("Non-integer occurred in an arithmetic operation: " + operator +": "+ arg3);			
		}
		return false;
	}

	public static String compute(int operator, String arg1, String arg2, Hashtable <String, String> bindings) {
		int result = 0;
		try {
			switch (operator){
				case ADDITION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							+ Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case SUBTRACTION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							- Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case MULTIPLICATION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							* Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case DIVISION: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
							/ Integer.parseInt(getBoundValue(arg2, bindings));
							break;
				case MODULUSOP: result = Integer.parseInt(getBoundValue(arg1, bindings)) 
						% Integer.parseInt(getBoundValue(arg2, bindings));
						break;
			}
			return Integer.toString(result);
		}	
		catch (Exception e) {
			System.out.println("Non-integer occurred in an arithmetic operation: "+ arg1+" and "+arg2);			
		}
		return null;
	}

	private static boolean belongsTo(boolean negation, ArrayList<String> arguments, Hashtable <String, String> bindings){
		String element = getBoundValue(arguments.get(0), bindings);
		for (int i=1; i<arguments.size(); i++)
			if (element.equals(getBoundValue(arguments.get(i), bindings)))
				return !negation;
		return negation;
	}
	
	private static boolean parseORFunctionArguments(ArrayList<String> arguments){
		if (arguments.size()!=1)
			return false;
		String arg = arguments.get(0);
		if (arg.charAt(0)!='"')
			return false;
		try {
			ArrayList<Predicate> predicates = MIDParser.parseConditionString(removeQuotesFromString(arg));
			if (predicates.size()<2)
				return false;
			for (Predicate predicate: predicates){
				if (!isFunction(predicate) || !hasCorrectArguments(predicate))
					return false;
			}
		} catch (Exception e) {
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
			return false;
		}
		return true;
	}

	private static boolean orFunction(boolean negation, ArrayList<String> arguments, Hashtable <String, String> bindings, Stack<BindingRecord> bindingHistory, int inputIndex){
		boolean result=false;
		try {
			ArrayList<Predicate> predicates = MIDParser.parseConditionString(removeQuotesFromString(arguments.get(0)));
			for (Predicate predicate: predicates)
				if (isTrue(predicate, bindings, bindingHistory, inputIndex)){
					result = true;
					break;
				}
		} catch (Exception e) {
			if (Kernel.IS_DEBUGGING_MODE)
				e.printStackTrace();
			return false;
		}
		return negation? !result: result;	
	}

	
	public static String getBoundValue(String argument, Hashtable <String, String> bindings){
		String value;
		if (MID.isVariable(argument) && bindings.get(argument)!=null)
			value = bindings.get(argument);
		else
			value = argument;
		if (MID.containsSymbolKey(value))
			value = MID.getNumberForSymbol(value);
			
		return removeQuotesFromString(value);
	}
	
	// e.g., "-1000"
	private static String removeQuotesFromString(String s) {
		if (s.charAt(0)!='"')
			return s;
		else
			return s.substring(1, s.length()-1).trim(); 
	}
}
