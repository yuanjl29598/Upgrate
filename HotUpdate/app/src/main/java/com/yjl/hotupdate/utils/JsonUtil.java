/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yjl.hotupdate.utils;

import android.annotation.SuppressLint;

import com.yjl.hotupdate.entity.ReceiverEntity;
import com.yjl.hotupdate.entity.ServiceEntity;
import com.yjl.hotupdate.loader.PluginEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class JsonUtil {
	public static void jsonObjectClear(JSONObject jsonObject) {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObject.keys();
		while (keys.hasNext()) {
			keys.next();
			keys.remove();
		}
	}

	public static boolean jsonObjectContainsValue(JSONObject jsonObject,
			Object value) {
		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObject.keys();
		while (keys.hasNext()) {
			Object thisValue = jsonObject.opt(keys.next());
			if (thisValue != null && thisValue.equals(value)) {
				return true;
			}
		}
		return false;
	}

	private final static class JSONObjectEntry implements
			Map.Entry<String, Object> {
		private final String key;
		private final Object value;

		JSONObjectEntry(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		@SuppressLint("FieldGetter")
		@Override
		public String getKey() {
			return this.key;
		}

		@Override
		public Object getValue() {
			return this.value;
		}

		@Override
		public Object setValue(Object object) {
			throw new UnsupportedOperationException(
					"JSONObjectEntry is immutable");
		}

	}

	public static Set<Map.Entry<String, Object>> jsonObjectEntrySet(
			JSONObject jsonObject) {
		HashSet<Map.Entry<String, Object>> result = new HashSet<Map.Entry<String, Object>>();

		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObject.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object value = jsonObject.opt(key);
			result.add(new JSONObjectEntry(key, value));
		}

		return result;
	}

	public static Set<String> jsonObjectKeySet(JSONObject jsonObject) {
		HashSet<String> result = new HashSet<String>();

		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObject.keys();
		while (keys.hasNext()) {
			result.add(keys.next());
		}

		return result;
	}

	public static void jsonObjectPutAll(JSONObject jsonObject,
			Map<String, Object> map) {
		Set<Map.Entry<String, Object>> entrySet = map.entrySet();
		for (Map.Entry<String, Object> entry : entrySet) {
			try {
				jsonObject.putOpt(entry.getKey(), entry.getValue());
			} catch (JSONException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	public static Collection<Object> jsonObjectValues(JSONObject jsonObject) {
		ArrayList<Object> result = new ArrayList<Object>();

		@SuppressWarnings("unchecked")
		Iterator<String> keys = (Iterator<String>) jsonObject.keys();
		while (keys.hasNext()) {
			result.add(jsonObject.opt(keys.next()));
		}

		return result;
	}

	@SuppressWarnings("rawtypes")
	public static String toJsonString(Map map) {
		JSONObject jsonObject = new JSONObject();
		for (Object k : map.keySet()) {
			try {
				jsonObject.put(String.valueOf(k), map.get(k));
			} catch (Exception e) {

			}
		}
		return jsonObject.toString();
	}

	public static JSONObject TransMapToJson(Map<String, Object> map) {
		JSONObject jsonObject = new JSONObject();
		for (Object k : map.keySet()) {
			try {
				jsonObject.put(String.valueOf(k), map.get(k));
			} catch (Exception e) {

			}
		}

		return jsonObject;
	}

	public static String toJsonString(List<PluginEntity> list) {
		JSONArray jsonObject = new JSONArray();
		for (PluginEntity k : list) {
			try {

				jsonObject.put(toJson(k));
			} catch (Exception e) {

			}
		}
		return jsonObject.toString();
	}

	@SuppressWarnings("rawtypes")
	public static Object toJson(Object obj) {
		if (obj instanceof List) {
			JSONArray jsonObject = new JSONArray();
			try {
				for (Object oo : (List) obj) {
					jsonObject.put(toJson(oo));
				}
			} catch (Exception e) {

			}
			return jsonObject;
		} else if (obj instanceof String) {
			JSONObject job = new JSONObject();
			try {
				job.put("name", obj);
			} catch (Exception e) {

			}
			return job;
		} else {
			Field[] fields = obj.getClass().getDeclaredFields();
			JSONObject job = new JSONObject();
			for (Field f : fields) {
				try {
					f.setAccessible(true);
					if (f.get(obj) != null)
						if (f.getType().equals(String.class)
								|| f.getType().equals(Integer.class)) {
							job.put(f.getName(), String.valueOf(f.get(obj)));
						} else if (f.getType().equals(List.class)) {
							job.put(f.getName(), toJson((List) f.get(obj)));
						}
				} catch (Exception e) {

				}
			}
			return job;
		}
	}

	public static void fromJson(Object obj, JSONObject job) {
		Field[] fields = obj.getClass().getDeclaredFields();
		Object t = null;
		// Method method = null;
		for (Field f : fields) {
			try {
				if (job.get(f.getName()) != null) {
					f.setAccessible(true);
					if (f.getType().equals(String.class))
						f.set(obj, job.get(f.getName()));
					else if (f.getType().equals(Integer.class))
						f.set(obj, job.get(f.getName()));
					else if (f.getType().equals(List.class)) {
						List<Object> list = new ArrayList<Object>();
						f.set(obj, list);
						JSONArray array = (JSONArray) job.get(f.getName());
						for (int i = 0; i < array.length(); i++) {
							if (f.getName().equals("receivers")) {
								t = new ReceiverEntity();
								fromJson(t, array.getJSONObject(i));
							} else if (f.getName().equals("services")) {
								t = new ServiceEntity();
								fromJson(t, array.getJSONObject(i));
							} else
								t = array.getJSONObject(i).get("name");
							list.add(t);
						}
					}
				}
			} catch (Exception e) {

			}
		}
	}
}
