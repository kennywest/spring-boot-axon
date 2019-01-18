package info.novatec.axon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AggregateConfig {

    @Bean
    public Serializer eventSerializer(ObjectMapper objectMapper) {
        JacksonSerializer.Builder builder = new JacksonSerializer.Builder();
        return builder.objectMapper(objectMapper).build();
    }
}
