package BYUIS.app.grade_checker;

import java.util.Date;

public class Dates {
	private Date start;
	private Date end;

	public Dates(){
		start=new Date();
	}
	
	public void setEndDate(){
		end=new Date();
	}
	
	public Date getStart(){
		return start;
	}
	
	public Date getEnd(){
		return end;
	}
	
	public String toString(){
		String build="";
		build+="Start Date: "+start.toString()+"\n";
		build+="End Date: "+end.toString()+"\n";
		return build;
	}
}
