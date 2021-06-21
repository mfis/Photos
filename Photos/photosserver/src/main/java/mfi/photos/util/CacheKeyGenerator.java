package mfi.photos.util;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Arrays;

@Component("PhotosCacheKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {

    @Override
    public @Nonnull Object generate(Object target, Method method, @Nonnull Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(target.getClass().getSimpleName()).append("#");
        sb.append(method.getName());
        Arrays.stream(params).forEach(p -> sb.append("#").append(p));
        return sb.toString();
    }

}
