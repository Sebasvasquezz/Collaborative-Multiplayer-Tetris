import { useState, useCallback } from 'react';
import { TETROMINOS } from '../tetrominos';
import { STAGE_WIDTH, checkCollision } from '../gameHelpers';
import { useWebSocket } from '../WebSocketContext';

export const usePlayer = (sendGameState) => {
  const { socket } = useWebSocket();
  const [player, setPlayer] = useState({
    pos: { x: 0, y: 0 },
    tetromino: TETROMINOS[0].shape,
    collided: false,
    color: '0, 0, 0'
  });

  const rotate = (matrix, dir) => {
    const rotatedTetro = matrix.map((_, index) =>
      matrix.map(col => col[index])
    );
    if (dir > 0) return rotatedTetro.map(row => row.reverse());
    return rotatedTetro.reverse();
  };

  const playerRotate = (stage, dir) => {
    const clonedPlayer = JSON.parse(JSON.stringify(player));
    clonedPlayer.tetromino = rotate(clonedPlayer.tetromino, dir);

    const pos = clonedPlayer.pos.x;
    let offset = 1;
    while (checkCollision(clonedPlayer, stage, { x: 0, y: 0 })) {
      clonedPlayer.pos.x += offset;
      offset = -(offset + (offset > 0 ? 1 : -1));
      if (offset > clonedPlayer.tetromino[0].length) {
        rotate(clonedPlayer.tetromino, -dir);
        clonedPlayer.pos.x = pos;
        return;
      }
    }
    setPlayer(clonedPlayer);
    sendGameState({ ...clonedPlayer, rotated: true });
  };

  const updatePlayerPos = ({ x, y, collided }) => {
    setPlayer(prev => {
      const newX = prev.pos.x + x;
      const newY = prev.pos.y + y;
      const newCollided = collided;
      const newPlayer = {
        ...prev,
        pos: { x: newX, y: newY },
        collided: newCollided,
      };
      sendGameState(newPlayer); 
      return newPlayer;
    });
  };
  
  
  const resetPlayer = useCallback(() => {
    socket.send(JSON.stringify({ type: "REQUEST_NEW_TETROMINO" }));
  }, [socket]);

  return [player, updatePlayerPos, resetPlayer, playerRotate, setPlayer];
};
