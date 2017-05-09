package com.yjl.hotupdate.http;

public abstract interface NetworkCallback {

	public abstract void onSuccess(int tag, Object data);

	public abstract void onFail(int tag, Object data);

}