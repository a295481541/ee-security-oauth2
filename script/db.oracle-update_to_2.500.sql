/*==============================================================*/
/* 新建表: AUTHEN_BIZ_SERIES                                    */
/*==============================================================*/
create table AUTHEN_BIZ_SERIES 
(
   BIZ_SERIES_ID        VARCHAR2(32)         not null,
   BIZ_SERIES_NAME      VARCHAR2(40)         not null,
   CREATED_BY           VARCHAR2(32)         not null,
   UPDATED_BY           VARCHAR2(32)         default 'NEVER_CHANGE' not null,
   CREATED_DT           DATE                 default SYSDATE not null,
   UPDATED_DT           DATE                 default SYSDATE not null,
   CREATED_SID          VARCHAR2(32)         not null,
   UPDATED_SID          VARCHAR2(32)         default 'NEVER_CHANGE' not null,
   REMARK               VARCHAR2(256),
   constraint PK_AUTHEN_BIZ_SERIES primary key (BIZ_SERIES_ID),
   constraint AK_UNIQUE_SERIES_NAME_AUTHEN_B unique (BIZ_SERIES_NAME)
);

comment on table AUTHEN_BIZ_SERIES is
'业务体系';

comment on column AUTHEN_BIZ_SERIES.BIZ_SERIES_ID is
'业务体系ID';

comment on column AUTHEN_BIZ_SERIES.BIZ_SERIES_NAME is
'业务体系中文名';

comment on column AUTHEN_BIZ_SERIES.CREATED_BY is
'创建用户主账号';

comment on column AUTHEN_BIZ_SERIES.UPDATED_BY is
'修改用户主账号';

comment on column AUTHEN_BIZ_SERIES.CREATED_DT is
'创建时间';

comment on column AUTHEN_BIZ_SERIES.UPDATED_DT is
'修改时间';

comment on column AUTHEN_BIZ_SERIES.CREATED_SID is
'创建系统ID';

comment on column AUTHEN_BIZ_SERIES.UPDATED_SID is
'修改系统ID';

comment on column AUTHEN_BIZ_SERIES.REMARK is
'备注';



/*==============================================================*/
/* 修改业务体系id: AUTHEN_ENDUSER_LOGIN_ACCOUNT                                        */
/*==============================================================*/
UPDATE AUTHEN_ENDUSER_LOGIN_ACCOUNT A
SET A .BIZ_SERIES_ID = (
	SELECT
		abss.BIZ_SERIES_ID
	FROM
		AUTHEN_BIZ_APP aba
	INNER JOIN AUTHEN_BIZ_SERIES abss ON aba.BIZ_SERIES_ID = abss.BIZ_SERIES_ID
	WHERE
		A .CREATED_SID = aba.BIZ_APP_ID
)

UPDATE AUTHEN_ENDUSER_SECRET_KEY A SET A .BIZ_SERIES_ID = 'UNKNOW' where a.BIZ_SERIES_ID is null 


/*==============================================================*/
/* 修改业务体系id: AUTHEN_ENDUSER_SECRET_KEY                                        */
/*==============================================================*/
UPDATE AUTHEN_ENDUSER_SECRET_KEY A
SET A .BIZ_SERIES_ID = (
	SELECT
		abss.BIZ_SERIES_ID
	FROM
		AUTHEN_BIZ_APP aba
	INNER JOIN AUTHEN_BIZ_SERIES abss ON aba.BIZ_SERIES_ID = abss.BIZ_SERIES_ID
	WHERE
		A .CREATED_SID = aba.BIZ_APP_ID
)

UPDATE AUTHEN_ENDUSER_SECRET_KEY A SET A .BIZ_SERIES_ID = 'UNKNOW' where a.BIZ_SERIES_ID is null 


/*==============================================================*/
/* 扩字段: AUTHEN_BIZ_APP                                        */
/*==============================================================*/
ALTER TABLE AUTHEN_BIZ_APP ADD BIZ_SERIES_ID VARCHAR2(32) DEFAULT 'UNKNOW' NOT NULL ENABLE;
COMMENT ON COLUMN AUTHEN_BIZ_APP.BIZ_SERIES_ID IS '业务体系ID';

/*==============================================================*/
/* 扩字段: AUTHEN_ENDUSER_LOGIN_ACCOUNT                          */
/*==============================================================*/
ALTER TABLE AUTHEN_ENDUSER_LOGIN_ACCOUNT ADD BIZ_SERIES_ID VARCHAR2(32) DEFAULT 'UNKNOW' NOT NULL ENABLE;
COMMENT ON COLUMN AUTHEN_ENDUSER_LOGIN_ACCOUNT.BIZ_SERIES_ID IS '业务体系ID';
ALTER TABLE AUTHEN_ENDUSER_LOGIN_ACCOUNT DROP CONSTRAINT AK_UNIQUE_LOGIN_AC1HEN_E;  
ALTER TABLE AUTHEN_ENDUSER_LOGIN_ACCOUNT ADD CONSTRAINT UNIQUE_EU_LOGIN_ACCOUNT UNIQUE (LOGIN_ACCOUNT, BIZ_SERIES_ID) ENABLE;

/*==============================================================*/
/* 扩字段: AUTHEN_ENDUSER_SECRET_KEY                                        */
/*==============================================================*/
ALTER TABLE AUTHEN_ENDUSER_SECRET_KEY ADD BIZ_SERIES_ID VARCHAR2(32) DEFAULT 'UNKNOW' NOT NULL ENABLE;
COMMENT ON COLUMN AUTHEN_ENDUSER_SECRET_KEY.BIZ_SERIES_ID IS '业务体系ID';
DROP INDEX AK_UNIQUE_USER_ID_AUTHEN_E1;  
ALTER TABLE AUTHEN_ENDUSER_SECRET_KEY ADD CONSTRAINT UNIQUE_EU_SERIES_SECRT UNIQUE (USER_ID, BIZ_SERIES_ID) ENABLE;

