package Tetris.Tetris.controller;

import Tetris.Tetris.model.Score;
import Tetris.Tetris.repository.ScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScoreRepository scoreRepository;

    private List<Score> topScores;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        topScores = Arrays.asList(
                new Score("Player1", 100),
                new Score("Player2", 90),
                new Score("Player3", 80),
                new Score("Player4", 70),
                new Score("Player5", 60)
        );
    }

    @Test
    public void testGetTopScores() throws Exception {
        when(scoreRepository.findTopByOrderByScoreDesc()).thenReturn(topScores);

        mockMvc.perform(get("/api/scores/top")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0].name", is("Player1")))
                .andExpect(jsonPath("$[0].score", is(100)))
                .andExpect(jsonPath("$[1].name", is("Player2")))
                .andExpect(jsonPath("$[1].score", is(90)))
                .andExpect(jsonPath("$[2].name", is("Player3")))
                .andExpect(jsonPath("$[2].score", is(80)))
                .andExpect(jsonPath("$[3].name", is("Player4")))
                .andExpect(jsonPath("$[3].score", is(70)))
                .andExpect(jsonPath("$[4].name", is("Player5")))
                .andExpect(jsonPath("$[4].score", is(60)));
    }
}
