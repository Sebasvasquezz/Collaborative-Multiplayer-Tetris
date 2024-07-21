import React, { useState, useEffect, useCallback, useRef } from "react";
import { checkCollision } from "../gameHelpers";
import { StyledTetrisWrapper, StyledTetris } from "./styles/StyledTetris";
import { TETROMINOS } from "../tetrominos";

// Custom Hooks
import { useInterval } from "../hooks/useInterval";
import { usePlayer } from "../hooks/usePlayer";
import { useStage } from "../hooks/useStage";
import { useGameStatus } from "../hooks/useGameStatus";
import { useWebSocket } from "../WebSocketContext";
import { useLocation } from 'react-router-dom';

// Components
import Stage from "./Stage";
import Display from "./Display";
import SavedPieceDisplay from "./SavedPieceDisplay";
import ColorDisplay from "./ColorDisplay"; 

const Tetris = () => {
  const location = useLocation();
  const { initialStage, initialId, initialColor } = location.state;

  const { socket } = useWebSocket();
  
  const [dropTime, setDropTime] = useState(null);
  const [gameOver, setGameOver] = useState(false);
  const [id, setId] = useState("");
  const [color, setColor] = useState("");
  const [savedPiece, setSavedPiece] = useState(TETROMINOS[0].shape);

  const sendGameState = useCallback((player) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      const gameState = {
        type: "GAME_STATE",
        sessionId: id,
        color: color,
        posX: player.pos.x,
        posY: player.pos.y,
        collided: player.collided,
        rotated: player.rotated || false,
      };
  
      console.log("Enviando estado del juego:", JSON.stringify(gameState));
      socket.send(JSON.stringify(gameState));
    } else {
      console.log("Socket no está abierto. Estado actual:", socket.readyState);
    }
  }, [socket, id, color]);

  const sendLinesCleared = useCallback((rowsCleared) => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      const message = {
        type: "LINES_CLEARED",
        sessionId: id,
        linesCleared: rowsCleared,
      };

      socket.send(JSON.stringify(message));
    } else {
      console.log("Socket no está abierto. Estado actual:", socket.readyState);
    }
  }, [socket, id]);

  const sendPlayerLost = useCallback(() => {
    if (socket && socket.readyState === WebSocket.OPEN) {
      const message = {
        type: "PLAYER_LOST",
        sessionId: id,
      };
      socket.send(JSON.stringify(message));
    } else {
      console.log("Socket no está abierto. Estado actual:", socket.readyState);
    }
  }, [socket, id]);

  const [player, updatePlayerPos, resetPlayer, playerRotate, setPlayer] = usePlayer(sendGameState);
  const [stage, setStage, rowsCleared] = useStage(player, resetPlayer, color, sendLinesCleared);
  const [score, setScore, rows, setRows, level, setLevel] = useGameStatus(rowsCleared);



  useEffect(() => {
    const handleWSMessage = (msg) => {
      const data = JSON.parse(msg);

  
      if (data.type === "GAME_STATES") {
        const gameState = data.gameState;
        const transformedGameState = gameState.map(row => 
          row.map(cell => [cell.value, cell.status, cell.color])
        );
        setStage(transformedGameState);
  
      } else if (data.type === "NEW_TETROMINO") {
        setPlayer((prev) => ({
          ...prev,
          tetromino: data.tetromino,
          pos: { x: data.posX, y: 0 },
          collided: false,
        }));
      } else if (data.type === "PLAYER_LOST") {
          setGameOver(true);
          setDropTime(null);
      }
    };

    if (socket) {
      socket.onmessage = (event) => handleWSMessage(event.data);
      socket.onclose = () => {
        console.log("WebSocket connection closed");
      };
    }

    return () => {
      if (socket) {
        socket.onmessage = null;
      }
    };
  }, [socket, setPlayer, setStage]);

  useEffect(() => {
    const timer = setTimeout(() => {
      startGame();
    }, 3000);

    return () => clearTimeout(timer);
  }, []);

  
  const keyUp = useCallback(({ keyCode }) => {
    if (!gameOver) {
      // Activate the interval again when user releases down arrow.
      if (keyCode === 40 || keyCode === 83) {
        setDropTime(1000 / (level + 1));
      }
    }
  }, [gameOver, level]);

  const startGame = () => {

    setStage(initialStage);
    setId(initialId);
    setColor(initialColor);
    setDropTime(1000);
    resetPlayer();
    setScore(0);
    setLevel(0);
    setRows(0);
    setGameOver(false);
  };

  const movePlayer = (dir) => {
    if (!checkCollision(player, stage, { x: dir, y: 0 })) {
      updatePlayerPos({ x: dir, y: 0 });
    }
  };

  const drop = () => {
    console.log("drop called");
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
        sendPlayerLost();
      } else {
        updatePlayerPos({ x: 0, y: 0, collided: true });
      }
    }
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

  };

  useInterval(() => {
    drop();
  }, dropTime);

  const move = useCallback(({ keyCode }) => {
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
        dropToBottom();

      } else if (keyCode === 67) {
        handleSavePiece();
      }
    }
  }, [gameOver, movePlayer, dropPlayer, playerRotate, dropToBottom, handleSavePiece]);

  const moveRef = useRef(move);
  moveRef.current = move;

  useEffect(() => {
    const handleKeyDown = (e) => {
      e.preventDefault();
      moveRef.current(e);
    };

    const handleKeyUp = (e) => {
      e.preventDefault();
      keyUp(e);
    };

    window.addEventListener('keydown', handleKeyDown);
    window.addEventListener('keyup', handleKeyUp);

    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      window.removeEventListener('keyup', handleKeyUp);
    };
  }, [keyUp]);

  return (
    <StyledTetrisWrapper
      role="button"
      tabIndex="0"
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
                <ColorDisplay color={color} /> 
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
