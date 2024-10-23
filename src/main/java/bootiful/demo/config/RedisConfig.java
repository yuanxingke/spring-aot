package bootiful.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

/**
 * @author chenyaolin 2024/10/23 15:08
 **/
@Component
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {

        return new ReactiveRedisTemplate<>(factory,
                RedisSerializationContext.<String, String>newSerializationContext()
                        .key(new StringRedisSerializer()).value(new StringRedisSerializer())
                        .hashKey(new StringRedisSerializer()).hashValue(new StringRedisSerializer()).build());
    }
}
