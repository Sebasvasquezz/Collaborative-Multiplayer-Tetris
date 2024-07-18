import React from 'react';
import { StyledStage } from './styles/StyledStage';
import Cell from './Cell';

const Stage = ({ stage }) => (
  <StyledStage width={stage[0].length} height={stage.length}>
    {stage.map(row => 
      row.map((cell, x) => {
        console.log('cell[0] value:', cell[0]);
        return <Cell key={x} type={cell[0]} />;
      })
    )}
  </StyledStage>
);

export default Stage;
