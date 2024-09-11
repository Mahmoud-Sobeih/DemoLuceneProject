package com.project.DemoLuceneProject.service;

import com.project.DemoLuceneProject.DTO.DataResponse;
import com.project.DemoLuceneProject.model.Category;
import com.project.DemoLuceneProject.model.Film;
import com.project.DemoLuceneProject.repository.CategoryRepository;
import com.project.DemoLuceneProject.repository.FilmRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LuceneIndexDataService {

    private final FilmRepository filmRepository;
    private final CategoryRepository categoryRepository;

    private Directory index;
    private Analyzer analyzer;

    public LuceneIndexDataService(FilmRepository filmRepository, CategoryRepository categoryRepository) {
        this.filmRepository = filmRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void init() throws IOException {
        index = FSDirectory.open(Paths.get("D://var/index/data"));
        analyzer = new StandardAnalyzer();

        // Open the index in create mode (deletes the existing index and create new one)
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // Replaces the index
        try (IndexWriter writer = new IndexWriter(index, config)) {
            indexBuilder(writer);
        }

        // Open the index in append mode (open the existing index and append new data, preserving the old documents.
        //If the index doesn't exist, it will create a new one.)
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
//        try (IndexWriter writer = new IndexWriter(index, config)) {
//            indexBuilder(writer);
//        }
    }

    private void indexBuilder(IndexWriter writer) {
        try {
            log.info("***** Start Build The Index for data ******");

            List<Film> films = filmRepository.findAll();
            for (Film film : films) {
                Document document = new Document();
                document.add(new TextField("id", String.valueOf(film.getFilmId()), Field.Store.YES));
                document.add(new TextField("title", "Film", Field.Store.YES));
                document.add(new TextField("name", film.getTitle(), Field.Store.YES));
                document.add(new TextField("description", film.getDescription(), Field.Store.YES));

                writer.addDocument(document);
            }

            List<Category> categories = categoryRepository.findAll();
            for (Category category : categories) {
                Document document = new Document();
                document.add(new TextField("id", String.valueOf(category.getCategoryId()), Field.Store.YES));
                document.add(new TextField("title", "Category", Field.Store.YES));
                document.add(new TextField("name", category.getName(), Field.Store.YES));
//                document.add(new TextField("description", category.getName(), Field.Store.YES));

                writer.addDocument(document);
            }

            writer.close();

            log.info("****** End Build The Index for data ********");

        } catch (Exception e) {
            log.error("Exception in data index builder", e);
        }
    }

    public List<DataResponse> searchIndex(String queryStr) {

        List<DataResponse> response = null;

        try {
            QueryParser queryParser = new QueryParser("name", analyzer);
            Query query = queryParser.parse(queryStr);

            DirectoryReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs results = searcher.search(query, 10);
            log.info("Result count films and category: {}", results.totalHits);

            response = new ArrayList<>();
            for (ScoreDoc scoreDoc : results.scoreDocs) {
                Document document = searcher.doc(scoreDoc.doc);
                log.info("File score: {} {}", document.getField("name").stringValue(), scoreDoc);
                DataResponse dataResponse = new DataResponse();
                dataResponse.setType(document.getField("title").stringValue());
                dataResponse.setId(Integer.parseInt(document.getField("id").stringValue()));
                dataResponse.setName(document.getField("name").stringValue());

                response.add(dataResponse);
            }

            reader.close();

        } catch (ParseException e) {
            log.error("ParseException in search data", e);
        } catch (IOException e) {
            log.error("IOException in search data", e);
        } catch (Exception e) {
            log.error("Exception in search data", e);
        }

        return response;
    }
}
