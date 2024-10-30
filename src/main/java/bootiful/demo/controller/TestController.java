package bootiful.demo.controller;

import bootiful.demo.model.CityAddDTO;
import bootiful.demo.model.CityQueryDTO;
import bootiful.demo.model.CityVO;
import bootiful.demo.service.CityService;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * @author chenyaolin 2024/10/17 14:45
 **/
@RestController(value = "/test")
public class TestController {
    private final CityService cityService;

    @NacosValue(value = "${useLocalCache}", autoRefreshed = true)
    private String testValue;

    ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    ObjectMapper objectMapper;

    @Autowired
    public TestController(CityService cityService, ReactiveRedisTemplate<String, String> reactiveRedisTemplate, ObjectMapper objectMapper) {
        this.cityService = cityService;
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/addCity")
    public Mono<Integer> addCity(@RequestBody CityAddDTO dto) {
        return Mono.just(cityService.insert(dto));
    }

    @GetMapping("/getCity/{id}")
    public Mono<CityVO> getCityById(@PathVariable("id") Integer id) {
        return Mono.just(cityService.getCityById(id));
    }

    @GetMapping("/getAllCity")
    public Mono<PageInfo<CityVO>> getAllCity(CityQueryDTO dto) {
        String key = dto.toString();
        return reactiveRedisTemplate.opsForValue().get(key).map(p -> {
            try {
                final PageInfo<CityVO> cityVOPageInfo = objectMapper.readValue(p, new TypeReference<PageInfo<CityVO>>() {
                });
                return cityVOPageInfo;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).defaultIfEmpty(cityService.getAllCity(dto)).doOnSuccess(r -> {
            try {
                reactiveRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(r)).subscribe();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

//    @GetMapping("/test")
//    public ActivityFormVO hello1(TestModel testModel) {
//        testService.test();
//        return testService.getFormId(testModel.getId());
//    }
}
