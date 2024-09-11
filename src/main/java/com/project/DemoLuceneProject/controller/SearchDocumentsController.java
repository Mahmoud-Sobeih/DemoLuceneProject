package com.project.DemoLuceneProject.controller;

import com.project.DemoLuceneProject.DTO.DocumentResponse;
import com.project.DemoLuceneProject.service.LuceneIndexDocumentsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/docs")
public class SearchDocumentsController {

    private final LuceneIndexDocumentsService luceneIndexDocumentsService;

    public SearchDocumentsController(LuceneIndexDocumentsService luceneIndexDocumentsService) {
        this.luceneIndexDocumentsService = luceneIndexDocumentsService;
    }

    @GetMapping("/search")
    public List<DocumentResponse> search(@RequestParam String query){
        log.info("Search for '{}' the documents", query);
        return luceneIndexDocumentsService.searchIndex(query);
    }
}
