package bootiful.demo.service;

import bootiful.demo.converter.CityConverter;
import bootiful.demo.dao.CityDao;
import bootiful.demo.entity.City;
import bootiful.demo.model.CityAddDTO;
import bootiful.demo.model.CityQueryDTO;
import bootiful.demo.model.CityVO;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chenyaolin 2024/10/22 17:12
 **/
@Service
@Slf4j
public class CityService {

    private CityDao cityDao;

    CityConverter cityConverter;

    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Autowired
    public CityService(CityDao cityDao, CityConverter cityConverter, ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.cityDao = cityDao;
        this.cityConverter = cityConverter;
        this.reactiveRedisTemplate = reactiveRedisTemplate;

    }

    public PageInfo<CityVO> getAllCity(CityQueryDTO dto) {
//        PageH.startPage(1, 10);
//        PageHelper.startPage(1,5);
        final String key = "demo:test";
        String time=System.currentTimeMillis()+"";
        reactiveRedisTemplate.opsForValue().set(key, time).doOnSuccess(s->{
            log.info("set time is {},value is {}", s,time);
        }).subscribe();
        reactiveRedisTemplate.opsForValue().get(key).doOnSuccess(s -> {
            log.info("value is {}", s);
        }).subscribe();
        return new PageInfo<>(cityDao.findAll(dto));
    }

    @Transactional
    public Integer insert(CityAddDTO dto) {
        City city = cityConverter.toCity(dto);
        cityDao.insert(city);
        return city.getId();
    }

    public CityVO getCityById(Integer id) {
        return cityConverter.toCityVO(cityDao.findById(id));
    }
}
