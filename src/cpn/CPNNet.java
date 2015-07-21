package cpn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import parser.MIDParser;

public class CPNNet {

	public static final String TOKENSEPARATOR = ", ";
	
	private ArrayList<CPNPlace> places = new ArrayList<CPNPlace>();
	private ArrayList<CPNTransition> transitions = new ArrayList<CPNTransition>();
	private ArrayList<CPNArc> arcs = new ArrayList<CPNArc>();

	public CPNNet(File cpnFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(cpnFile);
		doc.getDocumentElement().normalize();
		readPlaces(doc);
		readTransitions(doc);
		readArcs(doc);
		resetPlaceNames();
		resetTransitionEvents();
		if (isGeneratedByProM(doc))
			cleanUpPromNet();
		setTransitionConditions();
	}

	private void readPlaces(Document doc){
		NodeList placeList = doc.getElementsByTagName("place");
		for (int pIndex = 0; pIndex < placeList.getLength(); pIndex++) {
			Element placeElement = (Element)placeList.item(pIndex);
			assert placeElement.hasAttributes();
			CPNPlace place = new CPNPlace(getID(placeElement), getName(placeElement));
			String initMarking = getText(placeElement, "initmark");
			if (initMarking!=null)
				place.setInitTokens(initMarking);
			places.add(place);
//System.out.println(initMarking);	
//System.out.println(place);	
		}
	}
	
	private String getID(Element element){
		NamedNodeMap attributes = element.getAttributes();
		return attributes.getNamedItem("id").getNodeValue();
	}
	
	private String getName(Element element){
		String name = "";
		NodeList textNodeList = element.getElementsByTagName("text");		
		assert textNodeList!=null && textNodeList.getLength()>0;
		Element firstTextElement = (Element) textNodeList.item(0);
		NodeList firstElement = firstTextElement.getChildNodes();
		if (firstElement!=null && firstElement.getLength()>0)
			name = ((Node) firstElement.item(0)).getNodeValue();
		return name;
	}
	
	private String getText(Element placeElement, String keyword){
		String targetText = null;
		NodeList textNodeList = placeElement.getElementsByTagName(keyword);
		if (textNodeList!=null && textNodeList.getLength()>0){
			textNodeList = ((Element)textNodeList.item(0)).getElementsByTagName("text");
			if (textNodeList!=null && textNodeList.getLength()>0){
				Element textElement = (Element) textNodeList.item(0);
				NodeList children = textElement.getChildNodes();
				if (children!=null && children.getLength()>0){
					targetText = ((Node) children.item(0)).getNodeValue();
				}
			}
		}
		return targetText;
	} 

	private void readTransitions(Document doc){
		NodeList transitionList = doc.getElementsByTagName("trans");
		for (int tIndex = 0; tIndex < transitionList.getLength(); tIndex++) {
			Element transitionElement = (Element)transitionList.item(tIndex);
			String ID = getID(transitionElement);
			String event = getName(transitionElement);
			String guard = getText(transitionElement, "cond");
			guard = transformSimpleGuardCondition(guard);
			CPNTransition transition = new CPNTransition(ID, event, guard);
			transitions.add(transition);
//System.out.println(transition);			
		}
	}
	
	private String getArcEndID(Element arcElement, String keyword){
		NodeList nodeList = arcElement.getElementsByTagName(keyword);
		assert nodeList!=null && nodeList.getLength()>0: "no place/transition id";
		Node arcEndNode = nodeList.item(0);
		return arcEndNode.getAttributes().getNamedItem("idref").getNodeValue();
	}
	
	private void readArcs(Document doc) throws Exception {
		NodeList arcList = doc.getElementsByTagName("arc");
		for (int arcIndex = 0; arcIndex < arcList.getLength(); arcIndex++) {
			Element arcElement = (Element)arcList.item(arcIndex);
			String orientationString = arcElement.getAttributes().getNamedItem("orientation").getNodeValue();
			String placeID = getArcEndID(arcElement, "placeend");
			String transitionID = getArcEndID(arcElement, "transend");
			String label = getText(arcElement, "annot");

/*
System.out.println("Place end: " +placeID);
System.out.println("Transition end: " +transitionID);
System.out.println("Orientation: " +orientationString);
System.out.println("Arc label: " +label);
*/
			CPNArc arc = createArc(placeID, transitionID, orientationString, label);
			arcs.add(arc);
		}
		
	}
	
	public static void listChildren(Node current, int depth) {
		printSpaces(depth);
		System.out.println(current.getNodeName());
		NodeList children = current.getChildNodes();
		for (int a = 0; a < children.getLength(); a++) {
			listChildren(children.item(a), depth+1);
		}
	}

	private static void printSpaces(int n) {
		for (int i = 0; i < n; i++) 
			System.out.print(' '); 
	}

	public ArrayList<CPNTransition> getTransitions(){
		return transitions;
	}
	
	public String getInitMarking(){
		String initMarking = "";
		for (CPNPlace place: places)
			if (place.hasInitTokens()){
				String tokenString = place.getInitTokens(isTraditionalToken(place));
				initMarking += initMarking.equals("")? tokenString: TOKENSEPARATOR +tokenString;
			}
		return initMarking;
	}

	private String transformSimpleGuardCondition(String guard){
		if (guard==null)
			return "";
		if (guard.indexOf("<>")>0)
			return "not equals("+guard.replace("<>",",")+")";
		else if (guard.indexOf("!=")>0)
			return "not equals("+guard.replace("!=",",")+")";
		else if (guard.indexOf("==")>0)
			return "equals("+guard.replace("==",",")+")";
		else if (guard.indexOf("<=")>0)
			return "lte("+guard.replace("<=",",")+")";
		else if (guard.indexOf("<")>0)
			return "lt("+guard.replace("<",",")+")";
		else if (guard.indexOf(">=")>0)
			return "gte("+guard.replace(">=",",")+")";
		else if (guard.indexOf(">")>0)
			return "gt("+guard.replace(">",",")+")";
		return guard;
	}
	
	private boolean isTraditionalToken(CPNPlace place){
		for (CPNArc arc: arcs)
			if (arc.getPlace()== place)
				return arc.isDefaultLabel();
		return false;
	}
	
	private CPNArc createArc(String placeID, String transitionID, String type, String label) throws Exception{
		return new CPNArc(findPlace(placeID), findTransition(transitionID), type.equalsIgnoreCase("PTOT"), label);
	}
	
	private CPNPlace findPlace(String placeID) throws Exception{
		for (CPNPlace place: places)
			if (place.getID().equals(placeID))
				return place;
		throw new IOException("Place defition for "+placeID+" not found!");
	}

	private void resetPlaceNames(){
		int placeIndex = 1;
		for (CPNPlace cpnPlace: places){
			if (!MIDParser.isIdentifier(cpnPlace.getName())){
				cpnPlace.setName("p"+placeIndex++);
			}
		}
	}
	
	private void resetTransitionEvents(){
		for (CPNTransition cpnTransition: transitions){
			String event = cpnTransition.getEvent().trim();
			if (!MIDParser.isIdentifier(event)){
				int indexOfSeprator = event.indexOf(' ');
				if (indexOfSeprator<0)
					indexOfSeprator = event.indexOf("\n");
				if (indexOfSeprator>0)
					cpnTransition.setEvent(event.substring(0, indexOfSeprator));
			}
		}
		
	}
	
	private CPNTransition findTransition(String transitionID) throws Exception{
		for (CPNTransition transition: transitions)
			if (transition.getID().equals(transitionID))
				return transition;
		throw new Exception("Transition defition for "+transitionID+" not found!");
	}
	
	private String getTransitionCondition(CPNTransition transition, boolean orientation){
		String condition = "";
		for (CPNArc arc: arcs) {
			if (arc.isCondition(transition, orientation))
				condition += condition.equals("")? arc.getCondition(): ", "+arc.getCondition();
		}
		return condition;
	}
	
	private void setTransitionConditions(){
		for (CPNTransition transition: transitions){
			transition.setPrecondition(getTransitionCondition(transition, CPNArc.PLACETOTRANSITION));
			transition.setPostcondition(getTransitionCondition(transition, CPNArc.TRANSITIONTOPLACE));
		}
	}
	
	private boolean isGeneratedByProM(Document doc){
		try {
			NodeList elementList = doc.getElementsByTagName("generator");
			for (int index = 0; index < elementList.getLength(); index++) {
				Element element = (Element)elementList.item(index);
				if (element.hasAttribute("tool") && element.getAttribute("tool").equalsIgnoreCase("ProM"));
					return true;
			}
		} catch (Exception e){
		}
		return false;
	}
	
	private boolean isTransitionUseless(String event){
		return event.equals("Environment") || event.equals("Process") || event.equals("Init") || event.equals("Clean-up");
	}
	
	private boolean isPlaceUseless(CPNPlace place){
		for (CPNArc arc: arcs){
			if (arc.getPlace() == place){
				if (transitions.contains(arc.getCondition()))
					return false;
			}
		}
		return true;
	}
	
	private void cleanUpPromNet(){
		// remove useless transitions
		for (int transitionIndex=transitions.size()-1; transitionIndex>=0; transitionIndex--){
			CPNTransition transition = transitions.get(transitionIndex);
			if (isTransitionUseless(transition.getEvent()))
				transitions.remove(transitionIndex);
		}
		// remove useless tokens of initial marking 
		for (int placeIndex=places.size()-1; placeIndex>=0; placeIndex--){
			CPNPlace place = places.get(placeIndex);
			if (place.getName().equals("pstart"))
				place.insertToken("1");
			else 
			if (isPlaceUseless(place)){
				places.remove(placeIndex);
			}
		}	
	}
	
	public static void main(String argv[]) throws Exception {
		CPNNet net = new CPNNet(new File("\\work\\JavaProjects\\TestMining\\examples\\magento\\magento1.cpn"));
		for (CPNTransition t: net.getTransitions())
			System.out.println("\n"+t);
	}
}
