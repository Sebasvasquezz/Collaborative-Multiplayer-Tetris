import React from 'react';
import styled from 'styled-components';
import { StyledDisplay } from './styles/StyledDisplay';

const ModalWrapper = styled.div`
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.5);
`;

const ModalContent = styled(StyledDisplay)`
  max-width: 400px;
  width: 80%;
  text-align: center;
`;

const CloseButton = styled.button`
  margin-top: 20px;
  padding: 10px 20px;
  border: none;
  border-radius: 5px;
  background: #333;
  color: #fff;
  font-family: Pixel, Arial, Helvetica, sans-serif;
  cursor: pointer;

  &:hover {
    background: #555;
  }
`;

const Modal = ({ scores, onClose }) => {
  const totalScore = scores.reduce((total, score) => total + score.score, 0);

  return (
    <ModalWrapper>
      <ModalContent>
        <h2>Final Scores</h2>
        <ul>
          {scores.map((score, index) => (
            <li key={index}>{`${score.name}: ${score.score}`}</li>
          ))}
        </ul>
        <h3>Total Score: {totalScore}</h3>
        <CloseButton onClick={onClose}>Close</CloseButton>
      </ModalContent>
    </ModalWrapper>
  );
};

export default Modal;
