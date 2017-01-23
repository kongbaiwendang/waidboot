package com.waidboot.addressinfo.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waidboot.addressinfo.service.facade.AddressInfoService;
import com.waidboot.common.web.constant.RestConstants;
import com.waidboot.common.web.vo.RestCommonData;
import com.waidboot.vo.addressinfo.AddressinfoVo;
import com.waidboot.vo.userinfo.UserinfoVo;

@RestController
public class AddressInfoController {
	
	@Autowired
	private AddressInfoService addressInfoService;
	
	@RequestMapping("addressinfo/save")
	public RestCommonData addUserInfo(){
		RestCommonData res = new RestCommonData();
		AddressinfoVo addressinfoVo = new AddressinfoVo();
		addressinfoVo.setUserid(00000000000000000001L);
		addressinfoVo.setAddressinfo("呼伦贝尔");
		addressInfoService.saveAddressInfo(addressinfoVo);
		res.setResCode(RestConstants.SUCCESS.code);
		res.setResDescribe(RestConstants.SUCCESS.describe);
		return res;
	}
}
