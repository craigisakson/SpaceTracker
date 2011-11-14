package com.runninghusky.spacetracker;

public class Flight {
	private long id;
	private String name;
	private Boolean takePic;
	private Boolean sendSms;
	private String smsDuration;
	private String smsNumber;
	private String picDuration;
	private String distance;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getTakePic() {
		return takePic;
	}

	public void setTakePic(Boolean takePic) {
		this.takePic = takePic;
	}

	public Boolean getSendSms() {
		return sendSms;
	}

	public void setSendSms(Boolean sendSms) {
		this.sendSms = sendSms;
	}

	public String getSmsDuration() {
		return smsDuration;
	}

	public void setSmsNumber(String smsNumber) {
		this.smsNumber = smsNumber;
	}

	public String getSmsNumber(Boolean numeric) {
		return (numeric)?HlprUtil.getOnlyNumerics(smsNumber):smsNumber;
	}

	public void setSmsDuration(String duration) {
		this.smsDuration = duration;
	}

	public String getPicDuration() {
		return picDuration;
	}

	public void setPicDuration(String picDuration) {
		this.picDuration = picDuration;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getSmsNumber() {
		return smsNumber;
	}
}
