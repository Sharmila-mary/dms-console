package com.example.reportportal.service;

import com.example.reportportal.model.RagChunk;
import com.example.reportportal.repository.RagChunkRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final RagChunkRepository ragChunkRepository;
    private final JdbcTemplate jdbcTemplate;

    @Value("${rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${rag.chunk-overlap:150}")
    private int chunkOverlap;

    public RagService(RagChunkRepository ragChunkRepository, JdbcTemplate jdbcTemplate) {
        this.ragChunkRepository = ragChunkRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initFulltextIndex() {
        try {
            jdbcTemplate.execute("ALTER TABLE rag_chunk ADD FULLTEXT INDEX idx_content_fulltext (content)");
            log.info("FULLTEXT index created on rag_chunk.content");
        } catch (Exception e) {
            log.debug("FULLTEXT index already exists or could not be created: {}", e.getMessage());
        }
    }

    @Transactional
    public void ingestFile(String fileName, String content) {
        String fileType = classifyFileType(fileName, content);

        // Delete existing chunks for same file
        ragChunkRepository.deleteByFileName(fileName);

        // Chunk content
        List<String> chunks = chunkContent(content, chunkSize, chunkOverlap);

        List<RagChunk> entities = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < chunks.size(); i++) {
            RagChunk chunk = new RagChunk();
            chunk.setFileName(fileName);
            chunk.setFileType(fileType);
            chunk.setChunkIndex(i);
            chunk.setContent(chunks.get(i));
            chunk.setUploadedAt(now);
            entities.add(chunk);
        }

        ragChunkRepository.saveAll(entities);
        log.info("Ingested file '{}' as type '{}' with {} chunks", fileName, fileType, chunks.size());
    }

    public String searchRelevantChunks(String query) {
        // Extract keywords from query
        String[] words = query.split("\\s+");
        List<String> keywords = Arrays.stream(words)
                .filter(w -> w.length() > 2)
                .map(w -> "+" + w.replaceAll("[^a-zA-Z0-9_]", ""))
                .filter(w -> w.length() > 1)
                .collect(Collectors.toList());

        if (keywords.isEmpty()) {
            return "";
        }

        String searchTerm = String.join(" ", keywords);

        try {
            List<RagChunk> chunks = ragChunkRepository.searchByContent(searchTerm);
            if (chunks.isEmpty()) {
                return "";
            }
            return chunks.stream()
                    .map(c -> "[" + c.getFileName() + " chunk#" + c.getChunkIndex() + "]\n" + c.getContent())
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception e) {
            log.warn("RAG search failed: {}", e.getMessage());
            return "";
        }
    }

    public List<Map<String, Object>> listFiles() {
        List<Object[]> rows = ragChunkRepository.listFiles();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> file = new LinkedHashMap<>();
            file.put("fileName", row[0]);
            file.put("fileType", row[1]);
            file.put("uploadedAt", row[2]);
            file.put("chunkCount", row[3]);
            result.add(file);
        }
        return result;
    }

    @Transactional
    public void deleteFile(String fileName) {
        ragChunkRepository.deleteByFileName(fileName);
        log.info("Deleted all chunks for file '{}'", fileName);
    }

    private String classifyFileType(String fileName, String content) {
        String lower = fileName.toLowerCase();

        if (lower.endsWith(".java")) {
            if (lower.contains("entity") || lower.contains("model")) {
                return "entity";
            }
            return "java";
        }
        if (lower.endsWith(".hql") || (lower.endsWith(".xml") && content != null && content.toLowerCase().contains("hql"))) {
            return "hql";
        }
        if (lower.endsWith(".sql") || lower.endsWith(".ddl")) {
            return "ddl";
        }
        if (lower.contains("procedure") || lower.contains("proc")) {
            return "proc";
        }

        // Extension-based fallback
        int dot = lower.lastIndexOf('.');
        if (dot >= 0) {
            return lower.substring(dot + 1);
        }
        return "other";
    }

    private List<String> chunkContent(String content, int size, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return chunks;
        }
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + size, content.length());
            chunks.add(content.substring(start, end));
            start += size - overlap;
            if (start >= content.length()) break;
        }
        return chunks;
    }
}
