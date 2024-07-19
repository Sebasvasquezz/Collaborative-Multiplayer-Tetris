import React from 'react';
import { StyledCell } from './styles/StyledCell';

const Cell = ({ color }) => {
  return <StyledCell color={color} />;
};

export default React.memo(Cell);