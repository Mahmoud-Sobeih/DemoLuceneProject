package com.project.DemoLuceneProject.controller;

import com.project.DemoLuceneProject.DTO.DataResponse;
import com.project.DemoLuceneProject.service.LuceneIndexDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/data")
public class SearchDataController {

    private final LuceneIndexDataService  luceneIndexDataService;

    public SearchDataController(LuceneIndexDataService luceneIndexDataService) {
        this.luceneIndexDataService = luceneIndexDataService;
    }

    @GetMapping("/search")
    public List<DataResponse> search(@RequestParam String query){
        log.info("Search for '{}' in the data", query);
        return luceneIndexDataService.searchIndex(query);
    }
}
