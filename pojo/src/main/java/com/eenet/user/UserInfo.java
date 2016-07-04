package com.eenet.user;

import java.util.Date;

import com.eenet.base.BaseEntity;

/**
 * 最终用户信息
 * @author Orion
 *
 */
public abstract class UserInfo extends BaseEntity {
	private static final long serialVersionUID = 7776022085824758584L;
	private String name;//真实姓名
	private String namePing;//姓名拼音
	private String sex;//性别,字典:SEX
	private Date birthday;//出生日期
	private String idCard;//身份证号
	private Boolean realnameChecked = false;//实名制验证(姓名\性别\出生日期\地址\身份证号等身份证信息)
	private Boolean mobileChecked = false;//手机是否验证
	private Boolean emailChecked = false;//邮箱是否验证
	private Long mobile;//手机
	private String email;//电子邮箱
	private String ee;//EE
	/**
	 * @return the 真实姓名
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param userName the 真实姓名 to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the 姓名拼音
	 */
	public String getNamePing() {
		return namePing;
	}
	/**
	 * @param namePing the 姓名拼音 to set
	 */
	public void setNamePing(String namePing) {
		this.namePing = namePing;
	}
	/**
	 * @return the 性别
	 */
	public String getSex() {
		return sex;
	}
	/**
	 * @param sex the 性别 to set
	 */
	public void setSex(String sex) {
		this.sex = sex;
	}
	/**
	 * @return the 出生日期
	 */
	public Date getBirthday() {
		return birthday;
	}
	/**
	 * @param birthday the 出生日期 to set
	 */
	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}
	/**
	 * @return the 身份证号
	 */
	public String getIdCard() {
		return idCard;
	}
	/**
	 * @param idCard the 身份证号 to set
	 */
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	/**
	 * @return the 实名制验证
	 */
	public Boolean isRealnameChecked() {
		return realnameChecked;
	}
	/**
	 * @param realnameChecked the 实名制验证 to set
	 */
	public void setRealnameChecked(Boolean realnameChecked) {
		this.realnameChecked = realnameChecked;
	}
	/**
	 * @return the 邮箱是否验证
	 */
	public Boolean isMobileChecked() {
		return mobileChecked;
	}
	/**
	 * @param mobileChecked the 手机是否验证 to set
	 */
	public void setMobileChecked(Boolean mobileChecked) {
		this.mobileChecked = mobileChecked;
	}
	/**
	 * @return the 手机是否验证
	 */
	public Boolean isEmailChecked() {
		return emailChecked;
	}
	/**
	 * @param emailChecked the 邮箱是否验证 to set
	 */
	public void setEmailChecked(Boolean emailChecked) {
		this.emailChecked = emailChecked;
	}
	/**
	 * @return the 手机
	 */
	public Long getMobile() {
		return mobile;
	}
	/**
	 * @param mobile the 手机 to set
	 */
	public void setMobile(Long mobile) {
		this.mobile = mobile;
	}
	/**
	 * @return the 电子邮箱
	 */
	public String getEmail() {
		return email;
	}
	/**
	 * @param email the 电子邮箱 to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}
	/**
	 * @return the EE
	 */
	public String getEe() {
		return ee;
	}
	/**
	 * @param ee the EE to set
	 */
	public void setEe(String ee) {
		this.ee = ee;
	}
}
