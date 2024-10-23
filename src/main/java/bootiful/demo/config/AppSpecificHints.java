//package bootiful.demo.config;
//
//import bootiful.demo.City;
//import org.springframework.aot.hint.MemberCategory;
//import org.springframework.aot.hint.RuntimeHints;
//import org.springframework.aot.hint.RuntimeHintsRegistrar;
//
//import java.util.Set;
//
///**
// * @author chenyaolin 2024/10/22 16:53
// **/
//public class AppSpecificHints implements RuntimeHintsRegistrar {
//
//    @Override
//    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
//
//        // there's no easy way for Spring to know at compile time that
//        // we're using City with mybatis so you'll have to tell it
//
//        for (var c : Set.of(City.class))
//            hints.reflection().registerType(c, MemberCategory.values());
//
//    }
//
//}
