package com.fps.flickr;

@SuppressWarnings("serial")
public class FlickrException extends Exception {

	public FlickrException() {
		super();
	}

	public FlickrException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public FlickrException(String detailMessage) {
		super(detailMessage);
	}

	public FlickrException(Throwable throwable) {
		super(throwable);
	}

}
