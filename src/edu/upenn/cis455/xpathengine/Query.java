package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Query {
	public String query;
	PathNode head;
	PathNode tail;
	int queryID;
	
	public Query(String query, int queryID, int level){
		System.out.println(query);
		this.queryID = queryID;
		this.query = query;
		int flag = 0;
		int startindex = (query.charAt(0) == '/')? 1:0;
		List<String> nodenames = new ArrayList<String>();
		for(int i=startindex;i<query.length();i++){
			if(flag == 0 && query.charAt(i) == '/'){
				nodenames.add(query.substring(startindex, i));
				startindex = i+1;
			}
			else if(query.charAt(i) == '[') flag++;
			else if(query.charAt(i) == ']') flag--;
		}
		nodenames.add(query.substring(startindex, query.length()));
		for(String nodename:nodenames){
			if(!nodename.equals("")){
				System.out.println(nodename+" "+level);
				PathNode node = new PathNode(nodename, queryID, level++);
				if(head == null){
					head = node;
					tail = node;
				}
				else{
					tail.children.add(node);
					tail = node;
				}
				
			}
		}
		determineAncestor();
	}
	
	public PathNode getHead(){
		return head;
	}
	
	public boolean isValid(){
		return head.valid;
	}
	
	private void determineAncestor(){
		LinkedList<PathNode> pq = new LinkedList<PathNode>();
		pq.add(head);
		while(!pq.isEmpty()){
			PathNode p = pq.poll();
			System.out.println(p.level+" "+p.pathname);
			for(PathNode child:p.children){
				pq.add(child);
				child.ancestor = p;
			}
		}
	}
	
	public static void main(String[] args){
		String query = "/d/e/f[foo[apple/see]][bar][@attr = \"name\"]/r/s[sweet]";
		Query q = new Query(query, 0, 0);
		PathNode node = q.head;
		LinkedList<PathNode> pq = new LinkedList<PathNode>();
		pq.add(node);
		while(!pq.isEmpty()){
			PathNode p = pq.poll();
			System.out.println(p.level+" "+p.pathname);
			for(PathNode child:p.children){
				pq.add(child);
			}
		}
	}

}
