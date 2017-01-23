package com.waidboot.userinfo.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.waidboot.dao.mainsource.userinfo.UserinfoDao;
import com.waidboot.po.userinfo.Userinfo;
import com.waidboot.userinfo.service.facade.UserInfoService;
import com.waidboot.vo.userinfo.UserinfoVo;

@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {
	
	@Autowired
	private UserinfoDao userinfoDao;

	@Override
	public void saveUserInfo(UserinfoVo userinfoVo) {
		Userinfo userinfo = new Userinfo();
		BeanUtils.copyProperties(userinfoVo, userinfo);
		userinfoDao.insert(userinfo);
	}

	@Override
	public void delUserInfo(Long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateUserInfo() {
		// TODO Auto-generated method stub

	}

	@Override
	public UserinfoVo getUserinfo(Long id) {
		Userinfo userinfo = userinfoDao.selectByPrimaryKey(id);
		UserinfoVo userInfoVo = new UserinfoVo();
		BeanUtils.copyProperties(userinfo, userInfoVo);
		return userInfoVo;
	}

}
