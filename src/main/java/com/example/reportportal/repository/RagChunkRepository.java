package com.example.reportportal.repository;

import com.example.reportportal.model.RagChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RagChunkRepository extends JpaRepository<RagChunk, Long> {

    List<RagChunk> findByFileName(String fileName);

    void deleteByFileName(String fileName);

    @Query(value = "SELECT * FROM rag_chunk WHERE MATCH(content) AGAINST(:keyword IN BOOLEAN MODE) LIMIT 8", nativeQuery = true)
    List<RagChunk> searchByContent(@Param("keyword") String keyword);

    @Query(value = "SELECT DISTINCT file_name, file_type, MIN(uploaded_at) as uploaded_at, COUNT(*) as chunk_count FROM rag_chunk GROUP BY file_name, file_type", nativeQuery = true)
    List<Object[]> listFiles();
}
