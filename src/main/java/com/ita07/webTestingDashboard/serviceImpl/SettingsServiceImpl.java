package com.ita07.webTestingDashboard.serviceImpl;


import com.ita07.webTestingDashboard.config.EnvConfigService;
import com.ita07.webTestingDashboard.model.SettingsDTO;
import com.ita07.webTestingDashboard.service.SettingsService;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final EnvConfigService envConfigService;

    public SettingsServiceImpl(EnvConfigService envConfigService) {
        this.envConfigService = envConfigService;
    }

    @Override
    public SettingsDTO getCurrentSettings() {
        int concurrency = TestServiceImpl.getExecutorService().getCorePoolSize();
        String browser = envConfigService.getProperty("browser", "chrome");
        return new SettingsDTO(concurrency, browser);
    }
}
