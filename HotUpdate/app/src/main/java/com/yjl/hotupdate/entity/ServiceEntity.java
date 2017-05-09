package com.yjl.hotupdate.entity;

/**
 * Service对象
 * Keep表示是否一直保持
 * @author yjl
 *
 */
public class ServiceEntity {

	private String name;
	private String keep;
	private String pluginName;
	
	public String getPluginName() {
		return pluginName;
	}
	public void setPluginName(String pluginName) {
		this.pluginName = pluginName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getKeep() {
		return keep;
	}
	public void setKeep(String keep) {
		this.keep = keep;
	}
}
