/**
 * 作者：王亚冬
 * 时间：2017-01-23
 * 说明：
 *
 */
package com.waidboot.addressinfo.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.waidboot.addressinfo.service.facade.AddressInfoService;
import com.waidboot.dao.secondsource.addressinfo.AddressinfoDao;
import com.waidboot.po.addressinfo.Addressinfo;
import com.waidboot.vo.addressinfo.AddressinfoVo;

/**
 * 作者：王亚冬
 * 时间：2017-01-23
 * 说明：
 */
@Service("addressInfoService")
public class AddressInfoServiceImpl implements AddressInfoService{

	@Autowired
	private AddressinfoDao addressinfoDao;
	
	/* (non-Javadoc)
	 * @see com.waidboot.addressinfo.service.facade.AddressInfoService#saveAddressInfo()
	 */
	@Override
	public void saveAddressInfo(AddressinfoVo addressinfoVo) {
		Addressinfo addressinfo = new Addressinfo();
		BeanUtils.copyProperties(addressinfoVo, addressinfo);
		addressinfoDao.insert(addressinfo);
	}

	/* (non-Javadoc)
	 * @see com.waidboot.addressinfo.service.facade.AddressInfoService#queryAddressInfo(java.lang.Long)
	 */
	@Override
	public AddressinfoVo queryAddressInfo(Long id) {
		Addressinfo addressinfo = addressinfoDao.selectByPrimaryKey(id);
		AddressinfoVo addressinfoVo = new AddressinfoVo();
		if(addressinfo != null){
			BeanUtils.copyProperties(addressinfoVo, addressinfoVo);
		}
		return addressinfoVo;
	}

}
