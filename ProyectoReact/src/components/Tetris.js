import React, { useState, useEffect, useRef } from 'react';
import { createStage, checkCollision } from '../gameHelpers';
import { StyledTetrisWrapper, StyledTetris } from './styles/StyledTetris';
import { TETROMINOS } from '../tetrominos';

// Custom Hooks
import { useInterval } from '../hooks/useInterval';
import { usePlayer } from '../hooks/usePlayer';
import { useStage } from '../hooks/useStage';
import { useGameStatus } from '../hooks/useGameStatus';

// Components
import Stage from './Stage';
import Display from './Display';
import SavedPieceDisplay from './SavedPieceDisplay';
import { Await } from 'react-router-dom';

const Tetris = () => {
  const [dropTime, setDropTime] = useState(null);
  const [gameOver, setGameOver] = useState(false);
  const [savedPiece, setSavedPiece] = useState(TETROMINOS[0].shape);
  const [countdown, setCountdown] = useState(3);
  const [gameBoard, setGameBoard] = useState(createStage(60, 50));
  const [socketReady, setSocketReady] = useState(false); // Estado para rastrear la conexión del WebSocket
  const socketRef = useRef(null);

  const [player, updatePlayerPos, resetPlayer, playerRotate, setPlayer] = usePlayer(socketRef.current);
  const [stage, setStage, rowsCleared] = useStage(player, resetPlayer, gameOver);
  const [score, setScore, rows, setRows, level, setLevel] = useGameStatus(rowsCleared);

  useEffect(() => {
    if (countdown > 0) {
      const timer = setTimeout(() => setCountdown(countdown - 1), 1000);
      return () => clearTimeout(timer);
    } else {
      startGame();
    }
  }, [countdown]);

  useEffect(() => {
    const socket = new WebSocket('ws://localhost:8080/lobby');
    socketRef.current = socket;
  
    socket.onopen = () => {
      console.log('WebSocket connection established, ID:', socketRef.current.id);
      setSocketReady(true); // Actualiza el estado a listo
    };
  
    socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        console.log('Received message:', data); // Agrega este log para ver todos los mensajes entrantes
        if (data.type === 'GAME_STATES') {
          const newGameBoard = createStage(60, 50);
          data.gameState.gameBoard.forEach((row, y) => {
            row.forEach((value, x) => {
              if (value !== "0") {
                newGameBoard[y][x] = [value, `${data.gameState.playerId}-merged`];
              }
            });
          });
          setGameBoard(newGameBoard);
        } else if (data.type === 'NEW_TETROMINO') {
          console.log('Received NEW_TETROMINO:', data); // Log adicional
          setPlayer(prev => ({
            ...prev,
            tetromino: data.tetromino,
            pos: { x: data.pos, y: 0 },
            collided: false,
          }));
        }
      } catch (error) {
        console.error('Error parsing JSON:', error);
      }
    };
  
    socket.onclose = () => {
      console.log('WebSocket connection closed');
      setSocketReady(false); // Actualiza el estado a no listo
    };
  
    return () => {
      if (socketRef.current) {
        socketRef.current.close();
      }
    };
  }, []);

  useEffect(() => {
    if (socketReady && player) { // Solo enviar cuando la conexión esté lista
      const playerState = {
        type: 'PLAYER_STATE',
        tetromino: player.tetromino.map(row => row.join(",")).join(";"), // Serializar tetromino
        posX: player.pos.x,
        posY: player.pos.y,
        playerId: socketRef.current.id, // Asignar ID del socket al jugador
      };
      socketRef.current.send(JSON.stringify(playerState));
      //console.log(playerState);
    }
  }, [player, socketReady]);

  console.log('re-render');

  const movePlayer = dir => {
    if (!checkCollision(player, gameBoard, { x: dir, y: 0 })) {
      updatePlayerPos({ x: dir, y: 0 });
    }
  };

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
    setStage(createStage(60, 50));
    setDropTime(1000);
    setScore(0);
    setLevel(0);
    setRows(0);
    setGameOver(false);
     // Espera de 1 segundos antes de pedir un tetromino
    setTimeout(() => {
    // Lógica para pedir un tetromino
      socketRef.current.send(JSON.stringify({ type: "REQUEST_NEW_TETROMINO" }));
    }, 1000);
  };

 

  const drop = () => {
    // Increase level when player has cleared 10 rows
    if (rows > (level + 1) * 10) {
      setLevel(prev => prev + 1);
      // Also increase speed
      setDropTime(1000 / (level + 1) + 200);
    }

    if (!checkCollision(player, gameBoard, { x: 0, y: 1 })) {
      updatePlayerPos({ x: 0, y: 1, collided: false });
    } else {
      // Game over!
      if (player.pos.y < 1) {
        console.log('GAME OVER!!!');
        setGameOver(true);
        setDropTime(null);
      }
      updatePlayerPos({ x: 0, y: 0, collided: true });
    }
  };

  const dropPlayer = () => {
    // We don't need to run the interval when we use the arrow down to
    // move the tetromino downwards. So deactivate it for now.
    setDropTime(null);
    drop();
  };

  const dropToBottom = () => {
    // Move the tetromino down until it collides
    let dropCount = 0;
    while (!checkCollision(player, gameBoard, { x: 0, y: dropCount + 1 })) {
      dropCount += 1;
    }
    updatePlayerPos({ x: 0, y: dropCount, collided: true });
  };

  const handleSavePiece = () => {
    if (savedPiece === TETROMINOS[0].shape) {
      // Save a clone of the current player's tetromino
      setSavedPiece(JSON.parse(JSON.stringify(player.tetromino)));
      // Generate a new random tetromino for the player
      resetPlayer();
    } else {
      // Swap current player's tetromino with saved piece
      const temp = JSON.parse(JSON.stringify(player.tetromino));
      setSavedPiece(temp);
      resetPlayer(savedPiece);
    }
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
        playerRotate(gameBoard, 1);
      } else if (keyCode === 32) { // Space key
        dropToBottom();
      } else if (keyCode === 67) { // 'C' key
        handleSavePiece();
      }
    }
  };

  return (
    <StyledTetrisWrapper
      role="button"
      tabIndex="0"
      onKeyDown={e => move(e)}
      onKeyUp={keyUp}
    >
      <StyledTetris>
        <>
          <Stage stage={gameBoard} height={50} width={60} />
          <aside>
            {gameOver ? (
              <div>
                <Display gameOver={gameOver} text="Game Over" />
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

