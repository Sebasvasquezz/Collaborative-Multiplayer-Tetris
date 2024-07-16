import styled from 'styled-components';

export const StyledStage = styled.div`
  display: grid;
  grid-template-rows: repeat(
    ${props => props.height},
    calc(50vw / ${props => props.width})
  );
  grid-template-columns: repeat(
    ${props => props.width},
    calc(50vw / ${props => props.width})
  );
  grid-gap: 1px;
  border: 2px solid #333;
  background: #111;
  width: 58vw; /* Ajusta este valor según sea necesario */
  max-width: 100vw;
`;
