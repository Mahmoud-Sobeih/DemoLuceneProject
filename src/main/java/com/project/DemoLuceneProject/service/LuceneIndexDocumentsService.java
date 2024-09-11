package com.project.DemoLuceneProject.service;

import com.project.DemoLuceneProject.DTO.DocumentResponse;
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
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LuceneIndexDocumentsService {

    private Directory index;
    private Analyzer analyzer;
    private Tika tika;

    @PostConstruct
    public void init() throws IOException {
        index = FSDirectory.open(Paths.get("D://var/index/docs")); // Store the index on disk
        analyzer = new StandardAnalyzer();
        tika = new Tika();

        // Index files from a directory
        File documentsDir = new File("D://var/log/TestLucene"); // Path to your local documents
        // Open the index in create mode (deletes the existing index and create new one)
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // Replaces the index
        try (IndexWriter writer = new IndexWriter(index, config)) {
            indexBuilder(documentsDir, writer);
        }

        // Open the index in append mode (open the existing index and append new data, preserving the old documents.
        //If the index doesn't exist, it will create a new one.)
//        IndexWriterConfig config = new IndexWriterConfig(analyzer);
//        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
//        try (IndexWriter writer = new IndexWriter(index, config)) {
//            indexBuilder(documentsDir, writer);
//        }
    }

    private void indexBuilder(File folder, IndexWriter writer){
        try {
            log.info("***** Start Build The Index for documents ******");

            for (File file : folder.listFiles()){
                if(file.isFile()){
                    indexDocument(file, writer);
                }
            }

            writer.close();
            log.info("****** End Build The Index for documents ********");

        } catch (Exception e) {
            log.error("Exception in document index builder", e);
        }
    }

    private void indexDocument(File file, IndexWriter writer){
        Document document = new Document();

        try {
            String content = tika.parseToString(new FileInputStream(file));

            document.add(new TextField("fileName", file.getName(), Field.Store.YES));
            document.add(new TextField("content", content, Field.Store.YES));

            writer.addDocument(document);

        } catch (IOException e) {
            log.error("IOException in document index document", e);
        } catch (TikaException e) {
            log.error("TikaException in document index document", e);
        }
    }

    public List<DocumentResponse> searchIndex(String queryStr){

        List<DocumentResponse> response = null;
        try {
            QueryParser queryParser = new QueryParser("content", analyzer);
            Query query = queryParser.parse(queryStr);

            DirectoryReader directoryReader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(directoryReader);
            TopDocs results = searcher.search(query, 10);
            log.info("Result count docs: {}", results.totalHits );

            response = new ArrayList<>();
            for (ScoreDoc scoreDoc : results.scoreDocs){
                Document document = searcher.doc(scoreDoc.doc);
                log.info("File score: {} {}", document.getField("fileName").stringValue(), scoreDoc);
                DocumentResponse documentResponse = new DocumentResponse();
                documentResponse.setName(document.getField("fileName").stringValue());
                documentResponse.setPath("D://var/log/TestLucene/" + document.getField("fileName").stringValue());

                response.add(documentResponse);
            }

            directoryReader.close();

        } catch (ParseException e) {
            log.error("ParseException in search document", e);
        } catch (IOException e) {
            log.error("IOException in search document", e);
        }

        return response;
    }
}
