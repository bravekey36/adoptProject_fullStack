package petcare.service;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchChatbotQueryService {
    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> executeRawQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
