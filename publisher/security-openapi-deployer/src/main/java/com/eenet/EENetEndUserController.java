package com.eenet;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.eenet.base.SimpleResponse;
import com.eenet.util.EEBeanUtils;

@Controller
public class EENetEndUserController {
	
	@RequestMapping(value = "/getEndUser", method = RequestMethod.GET)
	@ResponseBody
	public String getEndUser(String appId, String appSecretKey,String currentUserId, String accessToken, String userType, String getUserId) {
		EENetEndUser endUser = new EENetEndUser();
		endUser.setSuccessful(true);
		endUser.setUserName("张先生");
		
		List<UserEDucation> education = new ArrayList<UserEDucation>();
		for (int i=0;i<2;i++) {
			UserEDucation school = new UserEDucation();
			school.setSchool("school"+i);
			education.add(school);
		}
		endUser.setEducation(education);
		
		UserJob job = new UserJob();
		job.setCompany("current job co.");
		endUser.setCurrentJob(job);
		
		return EEBeanUtils.object2Json(endUser);
	}
}

class EENetEndUser extends SimpleResponse{
	private static final long serialVersionUID = 548989324742274999L;
	private String userName;
	private List<UserEDucation> education;
	private UserJob currentJob;
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public List<UserEDucation> getEducation() {
		return education;
	}
	public void setEducation(List<UserEDucation> education) {
		this.education = education;
	}
	public UserJob getCurrentJob() {
		return currentJob;
	}
	public void setCurrentJob(UserJob currentJob) {
		this.currentJob = currentJob;
	}
}

class UserEDucation {
	private String school;

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}
}

class UserJob {
	private String company;

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}
}
