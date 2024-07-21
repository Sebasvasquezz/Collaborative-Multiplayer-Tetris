import React from 'react';
import { StyledDisplay } from './styles/StyledDisplay';
import styled from 'styled-components';

const ColorBox = styled.div`
  width: 50px;
  height: 50px;
  background-color: rgb(${props => props.color});
  border: 1px solid #000;
  margin-left: 10px;
`;

const ColorDisplay = ({ color }) => (
  <StyledDisplay>
    <span>Color:</span>
    <ColorBox color={color} />
  </StyledDisplay>
);

export default ColorDisplay;
