package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class SaxHandler extends DefaultHandler{
	QueryIndex queryIndex;
	int level = 0;
	List<PathNode> textChecklist = new ArrayList<PathNode>();
	
	public SaxHandler(QueryIndex queryIndex){
		super();
		this.queryIndex = queryIndex;
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		System.out.println("start element    : " + qName+" "+level);
		HashMap<String, List<PathNode>> candidateList = queryIndex.candidateList;
		if(candidateList.containsKey(qName)){
			List<PathNode> nodelist = candidateList.get(qName);
			for(PathNode node:nodelist){
				//check level
				if(node.level == level){
					//check attribute
					HashMap<String, String> attributeList = node.attributeList;
					int attributeFlag = 0;
					for(Map.Entry<String, String> entry:attributeList.entrySet()){
						if(attributes.getValue(entry.getKey()).equals(entry.getValue())){
							System.out.println("attribute right "+entry.getKey()+" "+entry.getValue());
							continue;
						}
						else{
							//do not process any more
							attributeFlag = 1;
						}
					}
					if(attributeFlag == 0){
						//need to check text
						System.out.println("need to check text");
						textChecklist.add(node);	
					}
				}
			}
		}
		queryIndex.print();
		level++;		
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		level--;
		System.out.println("end element      : " + qName+" "+level);
		HashMap<String, List<PathNode>> candidateList = queryIndex.candidateList;
		if(candidateList.containsKey(qName)){
			List<PathNode> nodelist = candidateList.get(qName);
			for(PathNode node:nodelist){
				if(node.level == level){
					queryIndex.removeChildren(node);
					if(!node.valid) node.setInComplete();
				}
			}
		}
		queryIndex.print();
	}
	
	public void characters(char ch[], int start, int length) throws SAXException {
		System.out.println("start characters : " + new String(ch, start, length));
		String text = new String(ch, start, length);
		for(PathNode node:textChecklist){
			boolean textfailure = false;
			List<String> containsList = node.containsList;
			List<String> textList = node.textList;
			for(String s:containsList){
				if(text.contains(s)) continue;
				else textfailure = true;
			}
			for(String s:textList){
				System.out.println("text "+s);
				if(text.equals(s)) continue;
				else textfailure = true;
			}
			if(!textfailure){
				//reach the final state
				if(node.children.size() == 0){
					node.ancestor.setComplete();
				}
				else queryIndex.insertChildren(node);
			}
		}
		textChecklist.clear();
	}
	
	
	
}
