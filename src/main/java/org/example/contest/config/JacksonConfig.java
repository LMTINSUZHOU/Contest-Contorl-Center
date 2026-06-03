package org.example.contest.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 和 MVC JSON 序列化共用的 Jackson 对象。
 *
 * Spring Boot 4 的 WebMVC starter 虽然引入了 Jackson 依赖，但在当前测试上下文中没有自动暴露
 * ObjectMapper Bean。显式声明一个兜底 Bean 可以保证 JWT 服务和接口 JSON 编解码稳定可用。
 */
@Configuration
public class JacksonConfig {
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }
}
