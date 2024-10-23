package bootiful.demo.service;

import bootiful.demo.converter.CityConverter;
import bootiful.demo.dao.CityDao;
import bootiful.demo.entity.City;
import bootiful.demo.model.CityQueryDTO;
import bootiful.demo.model.CityVO;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author chenyaolin 2024/10/22 17:12
 **/
@Service
public class CityService {

    private CityDao cityDao;

    CityConverter cityConverter;

    @Autowired
    public CityService(CityDao cityDao, CityConverter cityConverter) {
        this.cityDao = cityDao;
        this.cityConverter = cityConverter;

    }

    public List<CityVO> getAllCity() {
//        PageH.startPage(1, 10);
        PageHelper.startPage(1,5);
        return cityDao.findAll();
    }

    @Transactional
    public Integer insert(CityQueryDTO dto) {
        City city = cityConverter.toCity(dto);
        cityDao.insert(city);
        return city.getId();
    }

    public CityVO getCityById(Integer id) {
        return cityConverter.toCityVO(cityDao.findById(id));
    }
}
