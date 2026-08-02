package com.wk.ti.route.service;

import com.wk.ti.route.config.MsUriConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DownstreamProtectUrlService extends DownstreamService {

    public DownstreamProtectUrlService(MsUriConfiguration msUriConfiguration) {
        super(msUriConfiguration);
    }

    @Override
    protected Map<String, String> getServiceNameToUriMap() {
        Map<String, String> resultMap = super.getServiceNameToUriMap();
        return resultMap.entrySet()
                .stream()
                .filter(entry -> !entry.getKey().endsWith("-agent"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}


