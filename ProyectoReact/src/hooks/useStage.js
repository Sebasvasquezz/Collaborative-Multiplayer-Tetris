import { useState, useEffect } from 'react';
import { createStage } from '../gameHelpers';


export const useStage = (player, resetPlayer, color, sendLinesCleared) => {
  const [stage, setStage] = useState(createStage());
  const [rowsCleared, setRowsCleared] = useState(0);

  useEffect(() => {
    setRowsCleared(0);

    const sweepRows = newStage => {
      const rowsToRemove = [];

      newStage.forEach((row, y) => {
        if (row.findIndex(cell => cell[0] === 0) === -1) {
          rowsToRemove.push(y);
          setRowsCleared(prev => prev + 1);
        }
      });

      rowsToRemove.forEach(y => {
        newStage.splice(y, 1);
        newStage.unshift(new Array(newStage[0].length).fill([0, 'clear', '0, 0, 0']));
      });

      return newStage;
    };

    const updateStage = prevStage => {
      // First flush the stage
      const newStage = prevStage.map(row =>
        row.map(cell => (cell[1] === 'clear' ? [0, 'clear', '0, 0, 0'] : cell))
      );

      // Then draw the tetromino
      player.tetromino.forEach((row, y) => {
        row.forEach((value, x) => {
          if (value !== '0') {
            newStage[y + player.pos.y][x + player.pos.x] = [
              value,
              `${player.collided ? 'merged' : 'clear'}`,
              color // Agregar el color del tetromino aquí
            ];
          }
        });
      });

      // Then check if we got some score if collided
      if (player.collided) {
        const clearedRows = sweepRows(newStage);
        if (rowsCleared > 0) {
          sendLinesCleared(rowsCleared);
        }
        resetPlayer();
        return clearedRows;
      }
      return newStage;
    };

    setStage(prev => updateStage(prev));
  
  }, [
    player.collided,
    player.pos.x,
    player.pos.y,
    player.tetromino,
    resetPlayer,
    color,
    sendLinesCleared,
    rowsCleared,
  ]);

  return [stage, setStage, rowsCleared];
};
