/* 	
	Author Dianxiang Xu
*/
package edit;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;

import locales.LocaleBundle;
import mid.MID;
import mid.Mapping;
import mid.Predicate;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

import parser.MIDParser;
import parser.ParseException;
import parser.TokenMgrError;

import testcode.TargetLanguage;

public abstract class MIMPanel extends JPanel implements DocumentListener{
	private static final long serialVersionUID = 1L;
	
	protected XMIDEditor editor;
	protected TargetLanguage language;
	
	protected String systemNameLabel;
	protected JTextArea systemNameArea;

	protected GeneralTablePanel methodTablePanel;
	protected Vector<Vector<Object>> methodTable;

	public MIMPanel(){
		super();
	}
	
	protected void setSystemNameLabel(){
		if (language==TargetLanguage.HTML || language==TargetLanguage.RPC || language==TargetLanguage.SELENIUMDRIVER)
			systemNameLabel = "URL";
		else 
			systemNameLabel = XMIDProcessor.getSystemNameKeyword(language);
	}
	
	protected void parseSystemName(MID mid) throws ParseException{
		String name = systemNameArea.getText().trim();
		if (name.equals("") || language==TargetLanguage.KBT || language==TargetLanguage.UFT)
			return;
		if (language==TargetLanguage.HTML || language==TargetLanguage.RPC || language==TargetLanguage.SELENIUMDRIVER){
            try {
                new URL (name);
   				mid.setSystemName(name);
            }
            catch (MalformedURLException e) {
				throw new ParseException(LocaleBundle.bundleString("MIM")+" - "+LocaleBundle.bundleString("Invalid URL"));
			}
		}
		else { 
			if (!MIDParser.isIdentifier(name))
				throw new ParseException(LocaleBundle.bundleString("MIM")+" - "+LocaleBundle.bundleString("Invalid class-system name"));
			else
				mid.setSystemName(name);
		}
	}
	
	protected boolean isEmpty(Object object){
		return object==null || object.toString().trim().equals("");
	}

	
	public void updateLanguage(TargetLanguage newLanguage){
		language = newLanguage;
		setSystemNameLabel();
		updateMIMPanel();
	}

	protected void parseMethods(MID mid) throws ParseException {
		for (int index=0; index<methodTable.size(); index++){
			Vector<Object> row = methodTable.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			String rowInfo = LocaleBundle.bundleString("Methods Row")+" "+(index+1)+" - ";
			if (isEmpty(row.get(1)) || isEmpty(row.get(2)))
				throw new ParseException(rowInfo+LocaleBundle.bundleString("both model-level event and implementation code are expected"));
			Predicate predicate = null;
			try { 
				predicate = MIDParser.parseMappingPredicateString(row.get(1).toString());
			}
			catch (ParseException e) {
				throw new ParseException(rowInfo+e.toString());
			}
			catch (TokenMgrError e){
				throw new ParseException(rowInfo+LocaleBundle.bundleString("Lexical error"));				
			}
			String cmd = row.get(2).toString();
			if (language==TargetLanguage.HTML || language==TargetLanguage.SELENIUMDRIVER){
				String target = row.get(3)!=null? row.get(3).toString():""; 
				String value = row.get(4)!=null? row.get(4).toString():"";
				String seleniumCommand = language==TargetLanguage.HTML?
						MID.getSeleniumCommandHTML(cmd, target, value):
							MID.getSeleniumCommandCall(cmd, target, value);	
				mid.addMethod(new Mapping(predicate, seleniumCommand));
			}
			else {
				if (language==TargetLanguage.RPC) {
					MIDParser.parseRPCString(cmd);
				}
				mid.addMethod(new Mapping(predicate, cmd));
			}
		}
	}
	
	static protected enum StateOperatorType {STATEACCESSOR, STATEMUTATOR};

	// accessor and mutator for the same predicate are described separately
	protected void parseStateOperators(MID mid, Vector<Vector<Object>> stateTable, StateOperatorType operatorType) throws ParseException {
		for (int index=0; index<stateTable.size(); index++){
			Vector<Object> row = stateTable.get(index);
			if (XMIDProcessor.isRecordEmpty(row))
				continue;
			String rowInfo = operatorType==StateOperatorType.STATEACCESSOR? LocaleBundle.bundleString("Accessors")+" ": LocaleBundle.bundleString("Mutators")+" ";
			rowInfo = LocaleBundle.bundleString("Row")+" "+(index+1)+" - ";
			if (isEmpty(row.get(1)) || isEmpty(row.get(2)))
				throw new ParseException(rowInfo+LocaleBundle.bundleString("both model-level event and implementation code are expected"));
			Predicate predicate = null;
			try { 
				predicate = MIDParser.parseMappingPredicateString(row.get(1).toString());
			}
			catch (ParseException e) {
				throw new ParseException(rowInfo+e.toString());
			}
			catch (TokenMgrError e){
				throw new ParseException(rowInfo+LocaleBundle.bundleString("Lexical error"));				
			}
			String command = row.get(2).toString();
			if (language==TargetLanguage.HTML || language==TargetLanguage.SELENIUMDRIVER){
				String target = row.get(3)!=null? row.get(3).toString(): ""; 
				String value = row.get(4)!=null? row.get(4).toString(): ""; 
				String seleniumCommand = language==TargetLanguage.HTML?
					MID.getSeleniumCommandHTML(command, target, value):
						MID.getSeleniumCommandCall(command, target, value);	
				addOperator(mid, new Mapping(predicate, seleniumCommand), operatorType);
			} else {
				if (language==TargetLanguage.RPC) {
					MIDParser.parseRPCString(command);
				}
				addOperator(mid, new Mapping(predicate, command), operatorType);
			}
		}
	}

	private void addOperator(MID mid, Mapping mapping, StateOperatorType operatorType){
		if (operatorType == StateOperatorType.STATEACCESSOR)
			mid.addAccessor(mapping);
		else 
			mid.addMutator(mapping);
	} 
	
	static protected ArrayList<String> parseStringList(String fieldInfo, String readString) throws ParseException {
		ArrayList<String> list = null;
		try {
			list = MIDParser.parseIdentifierListString(readString);
		}
		catch (ParseException e) {
			throw new ParseException(fieldInfo+e.toString());
		}
		catch (TokenMgrError e){
			throw new ParseException(fieldInfo+LocaleBundle.bundleString("Lexical error"));				
		}
		return list;
	}

	// implements TableModelListener
	public void tableChanged(TableModelEvent e) {
	    editor.setXMIDSaved(false);
	}

	// implements DocumentListener
	public void insertUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	public void removeUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}
	
	public void changedUpdate(DocumentEvent e) {
	    editor.setXMIDSaved(false);
	}

	public abstract void updateMIMPanel();
	
	public abstract void updateFont();	
	
	public abstract JMenu getMIMMenu();

	public abstract void saveMIM(Sheet sheet, CellStyle lineWrapStyle);
		
	public abstract void parse(MID mid) throws ParseException;
}
