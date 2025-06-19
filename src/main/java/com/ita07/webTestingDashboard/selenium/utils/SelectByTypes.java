package com.ita07.webTestingDashboard.selenium.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum SelectByTypes {
    VALUE("value"),
    VISIBLE_TEXT("visibleText"),
    INDEX("index");

    private final String value;

    public static List<String> getSupportedSelectByTypes() {
        return Arrays.stream(values())
                .map(SelectByTypes::getValue)
                .collect(Collectors.toList());
    }

    public static SelectByTypes fromString(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown select by type: " + value));
    }
}
