package cpn;

import java.util.ArrayList;

public class CPNPlace {

	private String id;
	private String name;
	private ArrayList<String> initTokens = new ArrayList<String>();
	
	public CPNPlace(String id, String name){
		this.id = id;
		this.name = name;
	}
	
	public String getID(){
		return id;
	}
	
	public String getName(){
		return name!=null? name.trim(): "";
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setInitTokens(String tokenString){
		// break tokens 
		String[] cpnTokens = tokenString.split("\\++");
		for (String cpnToken: cpnTokens){
			cpnToken = cpnToken.trim();
//			System.out.println("CPN Token "+cpnToken);
			if (!cpnToken.equals("")){
				int quoteIndex = cpnToken.indexOf('`');
				if (quoteIndex>=0) {
					try {
						String weight = cpnToken.substring(0, quoteIndex).trim();
						if (Integer.parseInt(weight.trim())!=1)
							System.out.println("Token weight "+weight+" in "+cpnToken+" ignored.");
					}
					catch (Exception e){};
					String token = cpnToken.substring(quoteIndex+1).trim();
					token = token.replace("(", "");
					token = token.replace(")", "");
					token = token.replace("\"", "");
					initTokens.add(token);
				}
				else {
					initTokens.add(cpnToken);
				}
					
			}
		}
	}

	public void insertToken(String token){
		initTokens.add(token);
	}

	public boolean hasInitTokens(){
		return initTokens.size()>0;
	}
	
	public String getInitTokens(boolean isTraditionalToken){
		String tokenString = "";
		for (String initToken: initTokens){
			if (!tokenString.equals(""))
				tokenString += CPNNet.TOKENSEPARATOR;
			tokenString += isTraditionalToken || initToken.equals("") ? name: name+"("+initToken+")";
		}
		return tokenString;
	}
	
	public String toString(){
		return id+" "+name+" ["+getInitTokens(false)+"]";
	}
	
	public static void main(String[] args) {
		CPNPlace place = new CPNPlace("1", "p1");
//		place.setInitTokens("1 ' \"John\" ++ 1 ' \"Henry\"");
//		place.setInitTokens("1 ++  2 ++ 3 ");
//		place.setInitTokens("1 ' A ++  2 ' B ++ 3 ' C");
		place.setInitTokens("1 ' (A, A) ++ 2 ' (B, B) ++ 3 ' (C, C)");
		System.out.println(place.getInitTokens(false));	
	}

	
}
