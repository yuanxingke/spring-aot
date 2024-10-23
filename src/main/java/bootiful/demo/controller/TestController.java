package bootiful.demo.controller;

import bootiful.demo.model.CityQueryDTO;
import bootiful.demo.model.CityVO;
import bootiful.demo.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author chenyaolin 2024/10/17 14:45
 **/
@RestController(value="/test")
public class TestController {
    private final CityService cityService;


    @Autowired
    public TestController(CityService cityService ) {
         this.cityService = cityService;
    }

    @PostMapping("/addCity")
    public Mono<Integer> addCity(@RequestBody CityQueryDTO dto) {
        return Mono.just(cityService.insert(dto));
    }

    @GetMapping("/getCity/{id}")
    public Mono<CityVO> getCityById(@PathVariable("id") Integer id) {
        return Mono.just(cityService.getCityById(id));
    }

    @GetMapping("/getAllCity")
    public Mono<List<CityVO>> getAllCity() {
        return Mono.just(cityService.getAllCity());
    }

//    @GetMapping("/test")
//    public ActivityFormVO hello1(TestModel testModel) {
//        testService.test();
//        return testService.getFormId(testModel.getId());
//    }
}
