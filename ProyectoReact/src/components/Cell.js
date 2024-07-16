import React from 'react';
import { StyledCell } from './styles/StyledCell';
import { TETROMINOS } from '../tetrominos';

const Cell = ({ type }) => {
  // Verificar si TETROMINOS[type] está definido antes de acceder a color
  const tetromino = TETROMINOS[type];
  if (!tetromino) {
    // Manejar el caso donde type no es válido o no está definido
    return null;
  }

  return (
    <StyledCell type={type} color={tetromino.color}>
      {console.log('rerender cell')}
    </StyledCell>
  );
};

export default React.memo(Cell);
