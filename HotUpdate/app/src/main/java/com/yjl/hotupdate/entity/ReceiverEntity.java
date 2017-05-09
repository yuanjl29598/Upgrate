package com.yjl.hotupdate.entity;

import android.content.BroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Receiver 对象
 * @author yjl
 *
 */
public class ReceiverEntity {

	private String name;
	private String priority;
	private List<String> actions;
	private String scheme;
	private BroadcastReceiver receiver;
	
	public BroadcastReceiver getReceiver() {
		return receiver;
	}
	public void setReceiver(BroadcastReceiver receiver) {
		this.receiver = receiver;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getPriority() {
		return priority == null?0:
			priority.startsWith("0x")?Integer.parseInt(priority.substring(2), 16)
					: Integer.parseInt(priority);
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	public List<String> getActions() {
		return actions;
	}
	public void setActions(List<String> actions) {
		this.actions = actions;
	}
	public String getScheme() {
		return scheme;
	}
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
	public void addAction(String action) {
		if(actions == null)
			actions = new ArrayList<String>();
		actions.add(action);
	}

	public String[] getActionArray() {
		return actions == null?new String[0]:actions.toArray(new String[actions.size()]);
	}
}
