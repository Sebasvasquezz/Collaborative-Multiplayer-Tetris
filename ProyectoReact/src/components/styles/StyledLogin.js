import styled from 'styled-components';
import bgImage from '../../img/bg.png';

export const StyledLoginWrapper = styled.div`
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

export const StyledLogin = styled.div`
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

  form {
    display: flex;
    flex-direction: column;
    width: 100%;

    div {
      display: flex;
      align-items: center;
      width: 100%;
      margin-bottom: 20px;

      label {
        color: white;
        margin-right: 10px; /* Espacio entre el label y el input */
        white-space: nowrap; /* Evita que el label se divida en dos líneas */
      }

      input {
        padding: 10px;
        border-radius: 5px;
        border: none;
        font-size: 16px;
        font-family: Pixel, Arial, Helvetica, sans-serif; /* Fuente añadida */
        width: 141%
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
      width: 100%; /* Ancho del botón igual al ancho del contenedor */
      box-sizing: border-box; /* Incluir padding y border en el cálculo del ancho */
    }
  }

  .error {
    color: red;
    margin-bottom: 20px;
  }
`;
