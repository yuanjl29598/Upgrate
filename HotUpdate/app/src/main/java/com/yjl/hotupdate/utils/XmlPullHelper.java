package com.yjl.hotupdate.utils;

import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class XmlPullHelper {
	/**
	 * 解析xml文件
	 */

	public static  JSONArray pullXml(InputStream in) {
		JSONArray array = null;
		try {
			JSONObject object = null;
			JSONObject service = null;
			JSONObject receiver = null;
			JSONObject action = null;
			XmlPullParser parser = Xml.newPullParser();// 获取xml解析器
			//parser.setInput(context.getAssets().open("pluginConfig.xml"), "utf-8");
			parser.setInput(in, "utf-8");
			int type = parser.getEventType();
			JSONArray services = null;
			JSONArray receivers = null;
			JSONArray actions = null;
			while (type != XmlPullParser.END_DOCUMENT) {// 如果事件不等于文档结束事件就继续循环
				switch (type) {
				case XmlPullParser.START_TAG:
					if ("plugins".equals(parser.getName())) {
						array = new JSONArray();
					} else if ("plugin".equals(parser.getName())) {
						object = new JSONObject();
						array.put(object);
						object.put("name", parser.getAttributeValue(null, "name"));
						object.put("version", parser.getAttributeValue(null, "version"));
						object.put("mainClass", parser.getAttributeValue(null, "mainClass"));
					} else if ("services".equals(parser.getName())) {
						services = new JSONArray();
						object.put("services", services);
					} else if ("service".equals(parser.getName())) {
						service = new JSONObject();
						services.put(service);
						service.put("name", parser.getAttributeValue(null, "name"));
						service.put("keep", parser.getAttributeValue(null, "keep"));
					} else if ("receivers".equals(parser.getName())) {
						receivers = new JSONArray();
						object.put("receivers", receivers);
					} else if ("receiver".equals(parser.getName())) {
						receiver = new JSONObject();
						receivers.put(receiver);
						receiver.put("name", parser.getAttributeValue(null, "name"));
					} else if ("intent-filter".equals(parser.getName())) {
						actions  = new JSONArray();
						receiver.put("actions", actions);
						receiver.put("priority", parser.getAttributeValue(null, "priority"));
					}else if ("action".equals(parser.getName())) {
						action  = new JSONObject();
						actions.put(action);
						action.put("name", parser.getAttributeValue(null, "name"));
					}else if ("data".equals(parser.getName())) {
						receiver.put("scheme", parser.getAttributeValue(null, "scheme"));
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				type = parser.next();// 继续下一个事件
			}
			return array;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch(Exception e){
			LogHelper.catchExceptions(e);
		}
		return array;
	}
}