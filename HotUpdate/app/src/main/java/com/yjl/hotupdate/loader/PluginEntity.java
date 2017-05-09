package com.yjl.hotupdate.loader;

import android.content.BroadcastReceiver;
import android.content.Context;

import com.yjl.hotupdate.entity.ReceiverEntity;
import com.yjl.hotupdate.entity.ServiceEntity;
import com.yjl.hotupdate.utils.LogHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class PluginEntity  implements Serializable {
	/**
	 * 序列化id
	 */
	private static final long serialVersionUID = 562018156083252308L;

	// plugin id
//	public String pluginId;
	
	// 插件名
	private String name;

	// plugin 版本号
	private String version;

	// plugin  md5
//	public String pluginMD5;

	// 下载 url
	private String url;

	//
	private PluginClassLoader classloader;
	
	private String mainClass;
	
	private List<ServiceEntity> services;
	
	private List<ReceiverEntity> receivers;
	// 本地存储
//	public String pluginPath;

	// 缓存路径
//	public String cachePath;

	public List<ServiceEntity> getServices() {
		return services;
	}

	public void setServices(List<ServiceEntity> services) {
		this.services = services;
	}

	public List<ReceiverEntity> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<ReceiverEntity> receivers) {
		this.receivers = receivers;
	}
	
	public void addReceiver(ReceiverEntity r) {
		if(receivers == null)
			receivers = new ArrayList<ReceiverEntity>();
		receivers.add(r);
	}

	public String getMainClass() {
		return mainClass;
	}

	public void setMainClass(String mainClass) {
		this.mainClass = mainClass;
	}

	public String getName() {
		return name;
	}

	public void setName(String pluginName) {
		this.name = pluginName;
	}

	public String getVersion() {
		return this.isAvailable()?version:"0";
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public PluginClassLoader getClassloader() {
		return classloader;
	}
	
	public boolean isAvailable() {
		return classloader != null;
	}

	public void init(Context context) {
		try {
			classloader = new PluginClassLoader(context, this.name);
			if(this.mainClass != null) {
				classloader.invokeMethod(this.mainClass, "init",new Class<?>[]{Context.class}, context);
			}
			if(services != null) {
				for(ServiceEntity entity:services) {
					entity.setPluginName(name);
					ServiceActivator.registerService(context, entity);
				}
			}
			if(receivers != null) {
				for(ReceiverEntity entity:receivers) {
					binderReceiver(context, entity);
				}
			}
		}catch(Exception e) {
			LogHelper.catchExceptions(e);
		}
	}
	
	public void reload(Context context) {
		if(this.classloader == null) {
			init(context);
			return ;
		} 
			
		this.classloader.reload();
		try {
			if(this.mainClass != null) {
				classloader.invokeMethod(this.mainClass, "init",new Class<?>[]{Context.class}, context);
			}
			if(services != null) {
				for(ServiceEntity entity:services) {
					ServiceActivator.restartService(context, name, entity.getName());
				}
			}
			if(receivers != null) {
				for(ReceiverEntity entity:receivers) {
					binderReceiver(context, entity);
				}
			}
		}catch(Exception e) {
			LogHelper.catchExceptions(e);
		}
	}
	
	private void binderReceiver(Context context, ReceiverEntity entity) {
		try {
			if(entity.getReceiver() != null)
				ServiceActivator.unBinderReceiver(context, entity.getReceiver());
		}catch(Exception e) {
			LogHelper.catchExceptions(e);
		}
		try {
			BroadcastReceiver object = (BroadcastReceiver)classloader.getObject(entity.getName());
			entity.setReceiver(object);
			if(entity.getScheme() != null)
				ServiceActivator.binderReceiver(context, object, entity.getScheme(), entity.getPriority(), entity.getActionArray());
			else 
				ServiceActivator.binderReceiver(context, object, entity.getPriority(), entity.getActionArray());
		}catch(Exception e) {
			LogHelper.catchExceptions(e);
		}
	}

	
}
