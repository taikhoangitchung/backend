package app.service.factory;

import app.service.strategy.OAuth2UserStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2UserStrategyFactory {

    private final Map<String, OAuth2UserStrategy> strategies;

    public OAuth2UserStrategyFactory(Map<String, OAuth2UserStrategy> strategies) {
        this.strategies = strategies;
    }

    public OAuth2UserStrategy getStrategy(String provider) {
        OAuth2UserStrategy strategy = strategies.get(provider);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy found for provider: " + provider);
        }
        return strategy;
    }
}
