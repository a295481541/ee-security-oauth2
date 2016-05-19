/*
Navicat MySQL Data Transfer

Source Server         : VM测试机2
Source Server Version : 50712
Source Host           : 172.16.165.223:3306
Source Database       : passport

Target Server Type    : MYSQL
Target Server Version : 50712
File Encoding         : 65001

Date: 2016-05-13 17:42:50
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for AUTHEN_LOGIN_ACCOUNT
-- ----------------------------
DROP TABLE IF EXISTS `AUTHEN_LOGIN_ACCOUNT`;
CREATE TABLE `AUTHEN_LOGIN_ACCOUNT` (
  `ID` varchar(32) NOT NULL COMMENT '内部账号ID',
  `USER_ACCOUNT` varchar(32) NOT NULL COMMENT '用户主账号',
  `ACCOUNT_TYPE` varchar(10) NOT NULL COMMENT '登录账号类型：ID-身份证;MOBILE-手机;EMAIL-邮箱;USERNAME-用户名',
  `LOGIN_ACCOUNT` varchar(32) NOT NULL COMMENT '登录账号',
  `CREATED_BY` varchar(32) NOT NULL COMMENT '创建用户主账号',
  `UPDATED_BY` varchar(32) NOT NULL COMMENT '修改用户主账号',
  `CREATED_DT` datetime NOT NULL COMMENT '创建时间',
  `UPDATED_DT` datetime NOT NULL COMMENT '修改时间',
  `CREATED_SID` varchar(32) NOT NULL COMMENT '创建系统ID',
  `UPDATED_SID` varchar(32) NOT NULL COMMENT '修改系统ID',
  `REMARK` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`ID`),
  KEY `AK_UNIQUE_LOGIN_ACCOUNT` (`LOGIN_ACCOUNT`),
  KEY `AK_UNIQUE_ACCOUNT_ID_TYPE` (`USER_ACCOUNT`,`ACCOUNT_TYPE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='登录登录账号';

-- ----------------------------
-- Table structure for AUTHEN_OUTTER_ACCOUNT
-- ----------------------------
DROP TABLE IF EXISTS `AUTHEN_OUTTER_ACCOUNT`;
CREATE TABLE `AUTHEN_OUTTER_ACCOUNT` (
  `ID` varchar(32) NOT NULL COMMENT '外部账号ID',
  `USER_ACCOUNT` varchar(32) NOT NULL COMMENT '用户主账号',
  `OUTTER_APP_NAME` varchar(32) DEFAULT NULL COMMENT '外部系统名称（引用字典）',
  `ACCOUNT_NO` varchar(32) DEFAULT NULL COMMENT '登录账号',
  `CREATED_BY` varchar(32) NOT NULL COMMENT '创建用户主账号',
  `UPDATED_BY` varchar(32) NOT NULL COMMENT '修改用户主账号',
  `CREATED_DT` datetime NOT NULL COMMENT '创建时间',
  `UPDATED_DT` datetime NOT NULL COMMENT '修改时间',
  `CREATED_SID` varchar(32) NOT NULL COMMENT '创建系统ID',
  `UPDATED_SID` varchar(32) NOT NULL COMMENT '修改系统ID',
  `REMARK` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`ID`),
  KEY `AK_UNIQUE_APP_ACCOUNT` (`OUTTER_APP_NAME`,`ACCOUNT_NO`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='外部登录账号';

-- ----------------------------
-- Table structure for AUTHEN_SECRET_KEY
-- ----------------------------
DROP TABLE IF EXISTS `AUTHEN_SECRET_KEY`;
CREATE TABLE `AUTHEN_SECRET_KEY` (
  `ID` varchar(32) NOT NULL COMMENT '登录秘钥ID',
  `MAIN_ACCOUNT` varchar(32) NOT NULL COMMENT '用户主账号',
  `LOGIN_PASSORD` varchar(512) NOT NULL COMMENT '密码',
  `CREATED_BY` varchar(32) NOT NULL COMMENT '创建用户主账号',
  `UPDATED_BY` varchar(32) NOT NULL COMMENT '修改用户主账号',
  `CREATED_DT` datetime NOT NULL COMMENT '创建时间',
  `UPDATED_DT` datetime NOT NULL COMMENT '修改时间',
  `CREATED_SID` varchar(32) NOT NULL COMMENT '创建系统ID',
  `UPDATED_SID` varchar(32) NOT NULL COMMENT '修改系统ID',
  `REMARK` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`ID`),
  KEY `AK_UNIQUE_MAIN_ACCOUNT` (`MAIN_ACCOUNT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='登录秘钥';

-- ----------------------------
-- Table structure for AUTHEN_SERVICE_CONSUMER
-- ----------------------------
DROP TABLE IF EXISTS `AUTHEN_SERVICE_CONSUMER`;
CREATE TABLE `AUTHEN_SERVICE_CONSUMER` (
  `ID` varchar(96) DEFAULT NULL,
  `CONSUMER_N` varchar(120) DEFAULT NULL,
  `SECRT_KEY` longtext,
  `CREATED_BY` varchar(96) DEFAULT NULL,
  `UPDATED_BY` varchar(96) DEFAULT NULL,
  `CREATED_DT` date DEFAULT NULL,
  `UPDATED_DT` date DEFAULT NULL,
  `CREATED_SI` varchar(96) DEFAULT NULL,
  `UPDATED_SI` varchar(96) DEFAULT NULL,
  `REMARK` longtext
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for AUTHEN_SSO_SYSTEM
-- ----------------------------
DROP TABLE IF EXISTS `AUTHEN_SSO_SYSTEM`;
CREATE TABLE `AUTHEN_SSO_SYSTEM` (
  `ID` varchar(32) NOT NULL COMMENT 'ID（同时作为编码）',
  `APP_NAME` varchar(40) NOT NULL COMMENT '系统中文名',
  `APP_TYPE` varchar(10) NOT NULL COMMENT '系统类型：webapp - 普通web应用;iosapp - ios应用;andapp - 安卓应用;native - 桌面应用',
  `SECRT_KEY` varchar(512) NOT NULL COMMENT '秘钥',
  `REDIRECT_URI_PREFIX` varchar(2000) DEFAULT NULL COMMENT '跳转地址前缀（仅WEB）',
  `CREATED_BY` varchar(32) NOT NULL COMMENT '创建用户主账号',
  `UPDATED_BY` varchar(32) NOT NULL COMMENT '修改用户主账号',
  `CREATED_DT` datetime NOT NULL COMMENT '创建时间',
  `UPDATED_DT` datetime NOT NULL COMMENT '修改时间',
  `CREATED_SID` varchar(32) NOT NULL COMMENT '创建系统ID',
  `UPDATED_SID` varchar(32) NOT NULL COMMENT '修改系统ID',
  `REMARK` varchar(256) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`ID`),
  KEY `AK_UNIQUE_SSO_SYS` (`APP_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='单点登录接入系统';

-- ----------------------------
-- Table structure for V_USER_AUTH
-- ----------------------------
DROP TABLE IF EXISTS `V_USER_AUTH`;
CREATE TABLE `V_USER_AUTH` (
  `ACCOUNT` varchar(32) NOT NULL COMMENT '用户主账号',
  `STUDENT_ID` varchar(32) DEFAULT NULL COMMENT '学员基本信息ID',
  `STAFF_ID` varchar(32) DEFAULT NULL COMMENT '员工基本信息ID',
  `CUSTOMER_USER_ID` varchar(32) DEFAULT NULL COMMENT '客户用户基本信息ID',
  KEY `AK_UNIQUE_MAIN_ACCOUNT` (`ACCOUNT`),
  KEY `AK_UNIQUE_STUDENT` (`STUDENT_ID`),
  KEY `AK_UNIQUE_STAFF` (`STAFF_ID`),
  KEY `AK_UNIQUE_CSM_USER` (`CUSTOMER_USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主账号';
