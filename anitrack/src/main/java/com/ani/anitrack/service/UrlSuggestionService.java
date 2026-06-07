package com.ani.anitrack.service;

import com.ani.anitrack.entity.UrlSuggestion;
import com.ani.anitrack.mapper.UrlSuggestionMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UrlSuggestionService {

    private final UrlSuggestionMapper mapper;

    public UrlSuggestionService(UrlSuggestionMapper mapper) {
        this.mapper = mapper;
    }

    public List<UrlSuggestion> findAllVisible(Integer userId) {
        return mapper.findAllVisible(userId);
    }

    public UrlSuggestion addCustom(UrlSuggestion suggestion) {
        suggestion.setIsPreset(false);
        mapper.insert(suggestion);
        return suggestion;
    }

    public boolean deleteCustom(Integer id, Integer userId) {
        return mapper.deleteCustom(id, userId) > 0;
    }

    public void togglePin(Integer id) {
        mapper.togglePin(id);
    }
}
