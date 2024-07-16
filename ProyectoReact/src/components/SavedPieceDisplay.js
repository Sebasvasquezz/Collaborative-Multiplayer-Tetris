import React from 'react';
import { TETROMINOS } from '../tetrominos';
import { StyledSavedPiece } from './styles/StyledSavedPiece'; // Asegúrate de tener estilos para la imagen
import iImage from '../img/i.png';
import jImage from '../img/j.png';
import lImage from '../img/l.png';
import oImage from '../img/o.png';
import sImage from '../img/s.png';
import tImage from '../img/t.png';
import zImage from '../img/z.png';

const SavedPieceDisplay = ({ savedPiece }) => {
  const getTetrominoImage = () => {
    const shape1x1 = savedPiece[1][1];

    switch (shape1x1) {
      case 'I':
        return iImage;
      case 'J':
        return jImage;
      case 'L':
        return lImage;
      case 'O':
        return oImage;
      case 'S':
        return sImage;
      case 'T':
        return tImage;
      case 'Z':
        return zImage;
      default:
        return null;
    }
  };

  return (
    <StyledSavedPiece>
      {savedPiece !== TETROMINOS[0].shape && (
        <img src={getTetrominoImage()} alt="Saved Tetromino" />
      )}
    </StyledSavedPiece>
  );
};

export default SavedPieceDisplay;
