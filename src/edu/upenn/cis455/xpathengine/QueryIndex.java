package edu.upenn.cis455.xpathengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class QueryIndex {
	String[] queries;
	public Query[] querypool;
	HashMap<String, List<PathNode>> candidateList;
	HashMap<String, List<PathNode>> waitList;
	
	public QueryIndex(String[] queries){
		this.queries = queries;
		candidateList = new HashMap<String, List<PathNode>>();
		waitList = new HashMap<String, List<PathNode>>();
		querypool = new Query[queries.length];
		for(int i=0;i<queries.length;i++){
			System.out.println(queries[i]);
			querypool[i] = new Query(queries[i], i, 0);
			insertQuery(querypool[i]);
		}
	}
	
	private void insertQuery(Query query){
		insertCandidate(query);
		insertWait(query);
	}
	
	private void insertCandidate(Query query){
		PathNode head = query.head;
		if(!candidateList.containsKey(head.pathname)){
			candidateList.put(head.pathname, new ArrayList<PathNode>());
		}
		candidateList.get(head.pathname).add(head);
	}
	
	private void insertWait(Query query){
		PathNode node = query.head;
		LinkedList<PathNode> pq = new LinkedList<PathNode>();
		pq.add(node);
		while(!pq.isEmpty()){
			PathNode n = pq.poll();
			if(n != node){
				if(!waitList.containsKey(n.pathname)){
					waitList.put(n.pathname, new ArrayList<PathNode>());
				}
				waitList.get(n.pathname).add(n);
			}
			for(PathNode child:n.children){
				pq.add(child);
			}
		}
	}
	
	private void upCandidate(PathNode node){
		if(!candidateList.containsKey(node.pathname)){
			candidateList.put(node.pathname, new ArrayList<PathNode>());
		}
		candidateList.get(node.pathname).add(node);
	}
	
	private void downCandidate(PathNode node){
		if(candidateList.containsKey(node.pathname)){
			candidateList.get(node.pathname).remove(node);
		}
	}
	
	public void insertChildren(PathNode node){
		for(PathNode child:node.children){
			upCandidate(child);
		}
	}
	
	public void removeChildren(PathNode node){
		for(PathNode child:node.children){
			downCandidate(child);
		}
	}
	
	public void print(){
		System.out.println("candidate list");
		for(String key:candidateList.keySet()){
			System.out.println(key);
			for(PathNode n:candidateList.get(key)){
				System.out.println(n);
			}
		}
		System.out.println("wait list");
		for(String key:waitList.keySet()){
			System.out.println(key);
			for(PathNode n:waitList.get(key)){
				System.out.println(n);
			}
		}
		
	}
}
