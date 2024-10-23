package bootiful.demo.dao;

import bootiful.demo.entity.City;
import bootiful.demo.model.CityVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author chenyaolin 2024/10/22 16:55
 **/
@Mapper
public interface CityDao {


    void insert(City city) ;

    List<CityVO> findAll();

    City findById(@Param("id") Integer id);

}
