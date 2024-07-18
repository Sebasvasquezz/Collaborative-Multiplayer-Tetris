import React, { useState, useEffect } from "react";
import { createStage, checkCollision } from "../gameHelpers";
import { StyledTetrisWrapper, StyledTetris } from "./styles/StyledTetris";
import { TETROMINOS } from "../tetrominos";

// Custom Hooks
import { useInterval } from "../hooks/useInterval";
import { usePlayer } from "../hooks/usePlayer";
import { useStage } from "../hooks/useStage";
import { useGameStatus } from "../hooks/useGameStatus";
import { useWebSocket } from "../WebSocketContext";

// Components
import Stage from "./Stage";
import Display from "./Display";
import SavedPieceDisplay from "./SavedPieceDisplay";

const Tetris = () => {
  const { socket } = useWebSocket();

  const [dropTime, setDropTime] = useState(null);
  const [gameOver, setGameOver] = useState(false);
  const [savedPiece, setSavedPiece] = useState(TETROMINOS[0].shape);
  const [player, updatePlayerPos, resetPlayer, playerRotate, setPlayer] = usePlayer();
  const [stage, setStage, rowsCleared] = useStage(player, resetPlayer, gameOver);
  const [score, setScore, rows, setRows, level, setLevel] = useGameStatus(rowsCleared);


  const sendGameState = () => {
    const gameState = {
      type: "GAME_STATE",
      gameBoard: stage.map(row =>
        row.map(cell => ({
          value: cell[0],    
          status: cell[1]    // Asume que cell[1] es el estado de la celda
        }))
      ),
    };
    console.log("Enviando estado del juego:", JSON.stringify(gameState));
    socket.send(JSON.stringify(gameState));
  };

  useEffect(() => {
    const handleWSMessage = (msg) => {
      const data = JSON.parse(msg);
      console.log("Received message:", data); // Log para ver todos los mensajes entrantes

      if (data.type === "GAME_STATES") {
        const gameState = data.gameState;
        const transformedGameState = gameState.map(row => 
          row.map(cell => [cell.value, cell.status])
        );
        setStage(transformedGameState);

      } else if (data.type === "NEW_TETROMINO") {
        console.log("Received NEW_TETROMINO:", data); // Log adicional
        setPlayer((prev) => ({
          ...prev,
          tetromino: data.tetromino,
          pos: { x: data.posX, y: 0 },
          collided: false,
        }));
        console.log("Updated player state with new tetromino:", data.tetromino);
      } else if (data.type === "START_GAME") {
        console.log("Received START_GAME message"); // Log adicional para el nuevo mensaje
        startGame();
      }
    };

    if (socket) {
      socket.onmessage = (event) => handleWSMessage(event.data);
    }

    return () => {
      if (socket) {
        socket.onmessage = null;
      }
    };
  }, [socket]);

  useEffect(() => {
    const timer = setTimeout(() => {
      startGame();
    }, 500);

    return () => clearTimeout(timer);
  }, []);

  const keyUp = ({ keyCode }) => {
    if (!gameOver) {
      // Activate the interval again when user releases down arrow.
      if (keyCode === 40 || keyCode === 83) {
        setDropTime(1000 / (level + 1));
      }
    }
  };

  const startGame = () => {
    // Reset everything
    setStage(createStage());
    setDropTime(1000);
    resetPlayer();
    setScore(0);
    setLevel(0);
    setRows(0);
    setGameOver(false);
  };

  // Llama a sendGameState en cada acción relevante
  const movePlayer = (dir) => {
    if (!checkCollision(player, stage, { x: dir, y: 0 })) {
      updatePlayerPos({ x: dir, y: 0 });
      sendGameState(stage);
    }
  };

  const drop = () => {
    if (rows > (level + 1) * 10) {
      setLevel((prev) => prev + 1);
      setDropTime(1000 / (level + 1) + 200);
    }

    if (!checkCollision(player, stage, { x: 0, y: 1 })) {
      updatePlayerPos({ x: 0, y: 1, collided: false });
    } else {
      if (player.pos.y < 1) {
        setGameOver(true);
        setDropTime(null);
      }
      updatePlayerPos({ x: 0, y: 0, collided: true });
    }
    sendGameState(stage);
  };

  const dropPlayer = () => {
    setDropTime(null);
    drop();
  };

  const dropToBottom = () => {
    let dropCount = 0;
    while (!checkCollision(player, stage, { x: 0, y: dropCount + 1 })) {
      dropCount += 1;
    }
    updatePlayerPos({ x: 0, y: dropCount, collided: true });
    sendGameState(stage);
  };

  const handleSavePiece = () => {
    if (savedPiece === TETROMINOS[0].shape) {
      setSavedPiece(JSON.parse(JSON.stringify(player.tetromino)));
      resetPlayer();
    } else {
      const temp = JSON.parse(JSON.stringify(player.tetromino));
      setSavedPiece(temp);
      resetPlayer(savedPiece);
    }
    sendGameState(stage);
  };

  // Custom hook by Dan Abramov
  useInterval(() => {
    drop();
  }, dropTime);

  const move = ({ keyCode }) => {
    if (!gameOver) {
      if (keyCode === 37 || keyCode === 65) {
        movePlayer(-1);
      } else if (keyCode === 39 || keyCode === 68) {
        movePlayer(1);
      } else if (keyCode === 40 || keyCode === 83) {
        dropPlayer();
      } else if (keyCode === 38 || keyCode === 87) {
        playerRotate(stage, 1);
      } else if (keyCode === 32) {
        // Space key
        dropToBottom();
      } else if (keyCode === 67) {
        // 'C' key
        handleSavePiece();
      }
    }
  };

  return (
    <StyledTetrisWrapper
      role="button"
      tabIndex="0"
      onKeyDown={(e) => move(e)}
      onKeyUp={keyUp}
    >
      <StyledTetris>
        <>
        <Stage stage={stage} />
          <aside>
            {gameOver ? (
              <div>
                <Display $gameOver={gameOver} text="Game Over" />
                <Display text={`Score: ${score}`} />
                <Display text={`Rows: ${rows}`} />
              </div>
            ) : (
              <div>
                <Display text={`Score: ${score}`} />
                <Display text={`Rows: ${rows}`} />
                <Display text={`Level: ${level}`} />
              </div>
            )}
            <div>
              <Display text="Saved Piece" />
              <SavedPieceDisplay savedPiece={savedPiece} />
            </div>
          </aside>
        </>
      </StyledTetris>
    </StyledTetrisWrapper>
  );
};

export default Tetris;
