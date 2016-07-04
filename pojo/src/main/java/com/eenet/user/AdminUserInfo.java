package com.eenet.user;

import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;

/**
 * 服务人员信息
 * @author Orion
 *
 */
public class AdminUserInfo extends UserInfo implements BackupDeletedData,BackupUpdatedData {
	private static final long serialVersionUID = -1166594694123977150L;
	
	private String adminCode;//用户编码
	private String cardPhoto;//证件照片
	private String folk;//民族,字典:FOLK_CODE
	private String unitCode;//单位编码
	private String nativePlace;//籍贯,区域码表:CM_AREA
	private String bloodType;//血型
	private Integer workTime;//参加工作年份
	private String degree;//学历，字典:DEGREE_CODE
	private String graduate;//毕业院校，编码:CM_COMPANY_INFO
	private Integer zipCode;//邮政编码
	private String houseHodeType;//户口性质
	private String faith;//宗教信仰,字典FAITH_CODE
	private String province;//现居住省,区域码表:CM_AREA
	private String city;//现居住市,区域码表:CM_AREA
	private String district;//现居住区/县,区域码表:CM_AREA
	private String street;//现居住街道/镇,区域码表:CM_AREA
	private String address;//现住址
	private String homepage;//个人主页
	private String companyTele;//公司电话
	private String homeTele;//家庭电话
	private String wechat;//微信
	private String qq;//QQ
	/**
	 * @return the 用户编码
	 */
	public String getAdminCode() {
		return adminCode;
	}
	/**
	 * @param userCode the 用户编码 to set
	 */
	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}
	/**
	 * @return the 证件照片
	 */
	public String getCardPhoto() {
		return cardPhoto;
	}
	/**
	 * @param cardPhoto the 证件照片 to set
	 */
	public void setCardPhoto(String cardPhoto) {
		this.cardPhoto = cardPhoto;
	}
	/**
	 * @return the 民族
	 */
	public String getFolk() {
		return folk;
	}
	/**
	 * @param folk the 民族 to set
	 */
	public void setFolk(String folk) {
		this.folk = folk;
	}
	/**
	 * @return the 单位编码
	 */
	public String getUnitCode() {
		return unitCode;
	}
	/**
	 * @param unitCode the 单位编码 to set
	 */
	public void setUnitCode(String unitCode) {
		this.unitCode = unitCode;
	}
	/**
	 * @return the 籍贯
	 */
	public String getNativePlace() {
		return nativePlace;
	}
	/**
	 * @param nativePlace the 籍贯 to set
	 */
	public void setNativePlace(String nativePlace) {
		this.nativePlace = nativePlace;
	}
	/**
	 * @return the 血型
	 */
	public String getBloodType() {
		return bloodType;
	}
	/**
	 * @param bloodType the 血型 to set
	 */
	public void setBloodType(String bloodType) {
		this.bloodType = bloodType;
	}
	/**
	 * @return the 参加工作年份
	 */
	public Integer getWorkTime() {
		return workTime;
	}
	/**
	 * @param workTime the 参加工作年份 to set
	 */
	public void setWorkTime(Integer workTime) {
		this.workTime = workTime;
	}
	/**
	 * @return the 学历
	 */
	public String getDegree() {
		return degree;
	}
	/**
	 * @param degree the 学历 to set
	 */
	public void setDegree(String degree) {
		this.degree = degree;
	}
	/**
	 * @return the 毕业院校
	 */
	public String getGraduate() {
		return graduate;
	}
	/**
	 * @param graduate the 毕业院校 to set
	 */
	public void setGraduate(String graduate) {
		this.graduate = graduate;
	}
	/**
	 * @return the 邮政编码
	 */
	public Integer getZipCode() {
		return zipCode;
	}
	/**
	 * @param zipCode the 邮政编码 to set
	 */
	public void setZipCode(Integer zipCode) {
		this.zipCode = zipCode;
	}
	/**
	 * @return the 户口性质
	 */
	public String getHouseHodeType() {
		return houseHodeType;
	}
	/**
	 * @param houseHodeType the 户口性质 to set
	 */
	public void setHouseHodeType(String houseHodeType) {
		this.houseHodeType = houseHodeType;
	}
	/**
	 * @return the 宗教信仰
	 */
	public String getFaith() {
		return faith;
	}
	/**
	 * @param faith the 宗教信仰 to set
	 */
	public void setFaith(String faith) {
		this.faith = faith;
	}
	/**
	 * @return the 现居住省
	 */
	public String getProvince() {
		return province;
	}
	/**
	 * @param province the 现居住省 to set
	 */
	public void setProvince(String province) {
		this.province = province;
	}
	/**
	 * @return the 现居住市
	 */
	public String getCity() {
		return city;
	}
	/**
	 * @param city the 现居住市 to set
	 */
	public void setCity(String city) {
		this.city = city;
	}
	/**
	 * @return the 现居住区/县
	 */
	public String getDistrict() {
		return district;
	}
	/**
	 * @param district the 现居住区/县 to set
	 */
	public void setDistrict(String district) {
		this.district = district;
	}
	/**
	 * @return the 现居住街道/镇
	 */
	public String getStreet() {
		return street;
	}
	/**
	 * @param street the 现居住街道/镇 to set
	 */
	public void setStreet(String street) {
		this.street = street;
	}
	/**
	 * @return the 现住址
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the 现住址 to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}
	/**
	 * @return the 个人主页
	 */
	public String getHomepage() {
		return homepage;
	}
	/**
	 * @param homepage the 个人主页 to set
	 */
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	/**
	 * @return the 公司电话
	 */
	public String getCompanyTele() {
		return companyTele;
	}
	/**
	 * @param companyTele the 公司电话 to set
	 */
	public void setCompanyTele(String companyTele) {
		this.companyTele = companyTele;
	}
	/**
	 * @return the 家庭电话
	 */
	public String getHomeTele() {
		return homeTele;
	}
	/**
	 * @param homeTele the 家庭电话 to set
	 */
	public void setHomeTele(String homeTele) {
		this.homeTele = homeTele;
	}
	/**
	 * @return the 微信
	 */
	public String getWechat() {
		return wechat;
	}
	/**
	 * @param wechat the 微信 to set
	 */
	public void setWechat(String wechat) {
		this.wechat = wechat;
	}
	/**
	 * @return the QQ
	 */
	public String getQq() {
		return qq;
	}
	/**
	 * @param qq the QQ to set
	 */
	public void setQq(String qq) {
		this.qq = qq;
	}
}
