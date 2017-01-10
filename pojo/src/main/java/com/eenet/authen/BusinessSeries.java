package com.eenet.authen;

import com.eenet.base.BaseEntity;
import com.eenet.common.BackupDeletedData;
import com.eenet.common.BackupUpdatedData;
/**
 * 业务体系信息
 * @author koop
 */
public class BusinessSeries extends BaseEntity implements BackupDeletedData,BackupUpdatedData {

	private static final long serialVersionUID = 7977557101846308996L;
	private String seriesName;//业务系统中文名
	public String getSeriesName() {
		return seriesName;
	}
	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}
}
