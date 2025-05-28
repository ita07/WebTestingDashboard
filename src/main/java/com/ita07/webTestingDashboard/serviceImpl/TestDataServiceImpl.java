package com.ita07.webTestingDashboard.serviceImpl;

import com.ita07.webTestingDashboard.model.TestData;
import com.ita07.webTestingDashboard.repository.TestDataRepository;
import com.ita07.webTestingDashboard.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

@Service
public class TestDataServiceImpl implements TestDataService {

    private static final Logger logger = LoggerFactory.getLogger(TestDataServiceImpl.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @Autowired
    private TestDataRepository testDataRepository;

    @Override
    public TestData createTestData(TestData testData) {
        testData.setCreatedAt(LocalDateTime.now());
        testData.setUpdatedAt(LocalDateTime.now());
        return testDataRepository.save(testData);
    }

    @Override
    public TestData updateTestData(Long id, TestData testData) {
        TestData existing = getTestData(id);
        existing.setName(testData.getName());
        existing.setDescription(testData.getDescription());
        existing.setDataJson(testData.getDataJson());
        existing.setUpdatedAt(LocalDateTime.now());
        return testDataRepository.save(existing);
    }

    @Override
    public void deleteTestData(Long id) {
        testDataRepository.deleteById(id);
    }

    @Override
    public TestData getTestData(Long id) {
        return testDataRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TestData not found with id: " + id));
    }

    @Override
    public List<TestData> getAllTestData() {
        return testDataRepository.findAll();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> resolveVariables(Map<String, Object> action, Map<String, Object> testData) {
        Map<String, Object> resolvedAction = new HashMap<>();

        for (Map.Entry<String, Object> entry : action.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                resolvedAction.put(entry.getKey(), resolveVariableInString((String) value, testData));
            } else if (value instanceof Map) {
                resolvedAction.put(entry.getKey(), resolveVariables((Map<String, Object>) value, testData));
            } else {
                resolvedAction.put(entry.getKey(), value);
            }
            logger.debug("Resolved {} to {}", entry.getKey(), resolvedAction.get(entry.getKey()));
        }

        return resolvedAction;
    }

    private String resolveVariableInString(String value, Map<String, Object> testData) {
        if (value == null) return null;

        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String path = matcher.group(1);
            Object replacement = resolveNestedPath(path, testData);

            if (replacement == null) {
                logger.error("Variable not found in test data: {}", path);
                throw new RuntimeException("Variable not found in test data: " + path);
            }

            // Convert replacement to string and escape special regex characters
            String replacementStr = replacement.toString().replace("$", "\\$");
            matcher.appendReplacement(result, replacementStr);
            logger.debug("Replaced {} with {}", path, replacementStr);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private Object resolveNestedPath(String path, Map<String, Object> data) {
        String[] parts = path.split("\\.");
        Object current = data;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
                if (current == null) {
                    logger.error("Could not resolve variable path: {}. Part '{}' not found in data.", path, part);
                    return null;
                }
            } else {
                logger.error("Could not resolve variable path: {}. '{}' is not a map.", path, part);
                return null;
            }
        }

        return current;
    }
}
