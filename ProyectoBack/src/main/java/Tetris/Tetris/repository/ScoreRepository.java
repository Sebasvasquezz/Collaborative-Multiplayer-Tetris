package Tetris.Tetris.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import Tetris.Tetris.model.Score;

public interface ScoreRepository extends MongoRepository<Score, String> {
    @Query(value = "{}", sort = "{ 'score' : -1 }")
    List<Score> findTop5ByOrderByScoreDesc();
}

