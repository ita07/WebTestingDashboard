package com.ita07.webTestingDashboard.selenium.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum LocatorTypes {
    ID("id"),
    NAME("name"),
    XPATH("xpath"),
    CSS_SELECTOR("cssSelector"),
    CLASS_NAME("className"),
    TAG_NAME("tagName"),
    LINK_TEXT("linkText"),
    PARTIAL_LINK_TEXT("partialLinkText");

    private final String value;

    public static List<String> getSupportedLocatorTypes() {
        return Arrays.stream(values())
                .map(LocatorTypes::getValue)
                .collect(Collectors.toList());
    }

    public static LocatorTypes fromString(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown locator type: " + value));
    }
}
// This enum defines the supported locator types for Selenium actions.
