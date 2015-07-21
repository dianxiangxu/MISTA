/* 	
	Author Dianxiang Xu
	All Rights Reserved
*/
package mid;

import java.util.ArrayList;

public class ThreatTreeNode {

	public static enum Relation {AND, OR, NONE};
	
	private Transition transition;
	private ArrayList<ThreatTreeNode> children = new ArrayList<ThreatTreeNode>();
	private Relation relation = Relation.NONE;
	
	public ThreatTreeNode(Transition transition){
		this.transition = transition;
	}
	
	public Transition getTransition(){
		return transition;
	}
	
	public void addChildNode(ThreatTreeNode child){
		children.add(child);
	}
	
	public ArrayList<ThreatTreeNode> getChildren(){
		return children;
	}
	
	public static boolean isValidRelation(String relationString){
		return relationString!=null && !relationString.equals("") && (relationString.equalsIgnoreCase("AND") || relationString.equalsIgnoreCase("OR"));
	}
	
	public void setRelation(String relationString){
		if (relationString.equalsIgnoreCase("AND"))
			relation = Relation.AND;
		else 
		if (relationString.equalsIgnoreCase("OR"))
			relation = Relation.OR;
		else
			relation = Relation.NONE;
	}
	
	public String getEvent(){
		return transition.getEvent();
	}

	public ArrayList<String> getChildEvents(){
		ArrayList<String> childEvents = new ArrayList<String>();
		for (ThreatTreeNode child: children)
			childEvents.add(child.transition.getEvent());
		return childEvents;	
	}
	
	public boolean isLeaf(){
		return children.size()==0;
	}
	
	public boolean isRelationOR(){
		return relation == Relation.OR;
	}

	public boolean isRelationAND(){
		return relation == Relation.AND;
	}

	public String getRelationString(){
		if (relation == Relation.OR)
			return "OR";
		else 
		if (relation == Relation.AND)
			return "AND";
		else
			return "";			
	}
	
	public boolean containsTransition(Transition transition){
		for (ThreatTreeNode child: children)
			if (child.transition == transition)
				return true;
		return false;
	}
	
	public String toString(){
		String result = transition.getEvent();
		if (!isLeaf()){
			result += " ";
			for (int i=0; i<children.size(); i++){
				if (i>0)
					result+=", ";
				result += children.get(i).getTransition().getEvent(); 			
			}
			result += " "+getRelationString();
		}
		return result;
	}
}
