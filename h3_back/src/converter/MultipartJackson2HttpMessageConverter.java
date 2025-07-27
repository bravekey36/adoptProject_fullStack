package petcare.converter;

import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * multipart/form-data 요청 안에 들어있는 JSON 파트를 Jackson(ObjectMapper)으로 직렬화‧역직렬화
 * (파일,JSON 함께 API 요청 수신해야할 경우 사용)
 */
@Component
public class MultipartJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    /**
     * Converter for support http request with header Content-Type: multipart/form-data
     */
    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.APPLICATION_OCTET_STREAM);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public boolean canWrite(Type type, Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    protected boolean canWrite(MediaType mediaType) {
        return false;
    }
}
