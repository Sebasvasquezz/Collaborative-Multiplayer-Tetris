package Tetris.Tetris.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import Tetris.Tetris.model.Score;

/**
 * Repository interface for performing CRUD operations on the Score collection in MongoDB.
 */
public interface ScoreRepository extends MongoRepository<Score, String> {
    
    /**
     * Finds the top scores in descending order.
     *
     * @return a list of top scores ordered by score in descending order.
     */
    @Query(value = "{}", sort = "{ 'score' : -1 }")
    List<Score> findTopByOrderByScoreDesc();
}
