package com.yjl.hotupdate.loader;

import android.content.Context;

import com.yjl.hotupdate.BaseConfig;
import com.yjl.hotupdate.utils.LogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * 类加载器
 * @author yjl
 *
 */
public class PluginClassLoader {

	private DexClassLoader pathClassLoader;
	private Context context;
	private String name;
	private int version = 0;
	
	protected PluginClassLoader(Context context, String name) {
		this.context = context;
		this.name = name;
		initClassLoader();
	}
	
	public void reload() {
		initClassLoader();
	}
	
	private void initClassLoader() {
		String dexPath = BaseConfig.getPluginPath(context, name);
		File file = new File(dexPath);
		if(!file.exists()) {
			try {
				InputStream in = context.getAssets().open("plugin/"+this.name+".apk");
				FileOutputStream fo = new FileOutputStream(file);
				byte[] buffer = new byte[10*1024];
				int c = 0;
				while((c=in.read(buffer)) != -1)
					fo.write(buffer, 0, c);
				in.close();
				fo.flush();
				fo.close();
			}catch(Exception e) {
				LogHelper.catchExceptions(e);
			}
		}
		pathClassLoader = new DexClassLoader(dexPath, context.getDir(name+version, 0).getAbsolutePath(), 
					 null, this.getClass().getClassLoader());
			  
		LogHelper.showInfo("loadsdksuccess", name);
		if(version > 0) {
			context.getDir(name+(version-1), 0).deleteOnExit();
		}
		version ++;
	}
	
	/**
	 * 根据类名获取实例
	 * @param classname
	 * @return
	 */
	public Object getObject(String classname) 
			throws ClassNotFoundException, NoSuchMethodException
					,InstantiationException, IllegalAccessException, InvocationTargetException{
		 Class<?> class1 = pathClassLoader.loadClass(classname);
         Object object = class1.newInstance();
         return object;
		
	}
	

	public Object getObject(String classname, Class<?>[] params, Object... values) 
			throws ClassNotFoundException, NoSuchMethodException
					,InstantiationException, IllegalAccessException, InvocationTargetException{

		 Class<?> class1 = pathClassLoader.loadClass(classname);
		 Constructor<?> serviceConstructor = class1
                    .getConstructor(params);
         Object object = serviceConstructor.newInstance(values);
         return object;
	}
	
	/**
	 * 调用类中的方法
	 * @param classname
	 * @param methodName
	 * @param params
	 * @param values
	 */
	public Object invokeMethod(String classname, String methodName, Class<?>[] params, Object... values) 
			throws ClassNotFoundException, InstantiationException,
				NoSuchMethodException, IllegalAccessException, InvocationTargetException{

		Object obj = getObject(classname);
		if(obj != null) {
			return invokeMethod(obj, methodName, params, values);
		}

		return null;
	}
	
	public Object invokeMethod(Object obj, String methodName, Class<?>[] params, Object... values) 
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException{

		Method action = obj.getClass().getMethod(methodName, params);
		if(action != null)
			return action.invoke(obj, values);
		return null;
	}
	
	public Object invokeMethod(String classname, String methodName) 
			throws ClassNotFoundException, InstantiationException,
					NoSuchMethodException, IllegalAccessException, InvocationTargetException{

		Object obj = getObject(classname);
		if(obj != null) {
			return invokeMethod(obj, methodName);
		}
		
		return null;
	}
	
	public Object invokeMethod(Object obj, String methodName) 
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		Method action = obj.getClass().getMethod(methodName, new Class<?>[0]);
		if(action != null)
			return action.invoke(obj);
		return null;
	}
	
	public boolean isAvailable() {
		return pathClassLoader != null;
	}
}
