package parser;

import java.util.Hashtable;

import mid.MID;

public class ExpressionTester {
	public static void main(String[] args) {
		test1();
	}

	
	public static void test1() {
		try {
			String exp = "1-1-2*(2+(5*(z2+ON))-OFF)+y";
//		System.out.println(ExpressionParser.parseExpression("(x+y)"));
//			System.out.println(ExpressionParser.parseExpression("-1-2*(z-4)+3"));
		System.out.println(MIDParser.parseExpression(exp));
		
			for (String var: MIDParser.collectExpressionVariables(exp))
				System.out.println(var);

		String exp2 = "2*(-3+4*5)+5";
		System.out.println(MIDParser.evaluateExpression(exp2, new  Hashtable <String, String>()));
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void test2 () {
		try {
			String exp = "-1-2*(2+(5*(z2+ON))-OFF)+y";
//		System.out.println(ExpressionParser.parseExpression("(x+y)"));
//			System.out.println(ExpressionParser.parseExpression("-1-2*(z-4)+3"));
		System.out.println(ExpressionParser.parseExpression(exp));
		
			for (String var: ExpressionParser.collectExpressionVariables(exp))
				System.out.println(var);

		String exp2 = "2*(-3+4*5)+5";
		System.out.println(ExpressionParser.evaluateExpression(exp2, new MID(), new  Hashtable <String, String>()));
		

		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
