package bootiful.demo.converter;

import bootiful.demo.entity.City;
import bootiful.demo.model.CityAddDTO;
import bootiful.demo.model.CityVO;
import org.mapstruct.Mapper;

/**
 * @author chenyaolin 2024/10/22 17:15
 **/
@Mapper(componentModel = "spring")
public interface CityConverter {

    City toCity(CityAddDTO cityAddDTO);

    CityVO toCityVO(City city);
}
