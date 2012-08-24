package BYUIS.app.grade_checker;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public class ActivityData  {

	private HashMap<String,LinkedList<Dates>> map;
	
	public ActivityData(){
		map=new HashMap<String,LinkedList<Dates>>();
	}
	
	/**
	 * Returns:  previous value associated with specified key, 
	 * 				or null if there was no mapping for key.
	 */	   
	public LinkedList<Dates> addScreen(String screen){
		return map.put(screen, new LinkedList<Dates>());
	}
	
	public void addDate(String screen, Dates dates){
		LinkedList<Dates> datesList=map.get(screen);
		datesList.add(dates);
	}	
	
	//this method isn't complete yet
	public String toString(){
		String build="";
		Set<String> keyset=map.keySet();
		Iterator<String> itr=keyset.iterator();
		while(itr.hasNext()){
			String screen=itr.next();
			build+=screen+":";
			LinkedList<Dates> dates=map.get(screen);
			Iterator<Dates> itr2=dates.iterator();
			int count=0;
			while(itr2.hasNext()){
				Dates temp=itr2.next();
				build+=++count+". ";
				build+=temp.toString();				
			}
		}
		return build;
	}
}
