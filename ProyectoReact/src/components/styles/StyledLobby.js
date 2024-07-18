import styled from 'styled-components';
import bgImage from '../../img/bg.png';

export const StyledLobbyWrapper = styled.div`
  width: 100vw;
  height: 100vh;
  background: url(${bgImage}) #000;
  background-size: cover;
  overflow: hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  font-family: Pixel, Arial, Helvetica, sans-serif; /* Fuente añadida */
`;

export const StyledLobby = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 10px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.9);
  max-width: 500px;
  width: 100%;
  font-family: Pixel, Arial, Helvetica, sans-serif; /* Fuente añadida */

  h1 {
    color: white;
    margin-bottom: 20px;
  }

  ul {
    list-style: none;
    padding: 0;
    margin: 0 0 20px 0;
    width: 100%;

    li {
      color: white;
      background: #333;
      padding: 10px;
      margin-bottom: 10px;
      border-radius: 5px;
      text-align: left;
    }
  }

  button {
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    background: #333;
    color: white;
    cursor: pointer;
    font-size: 16px;
    font-family: Pixel, Arial, Helvetica, sans-serif; /* Fuente añadida */

    &:disabled {
      background: #555;
      cursor: not-allowed;
    }
  }
`;
