import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { StyledLoginWrapper, StyledLogin } from './styles/StyledLogin';

const Login = ({ setPlayerName }) => {
  const [name, setName] = useState('');
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    if (name.trim() !== '') {
      setPlayerName(name);
      localStorage.setItem('playerName', name);
      navigate('/lobby');
    }
  };

  return (
    <StyledLoginWrapper>
      <StyledLogin>
        <h1>Login</h1>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter your name"
            required
          />
          <button type="submit">Enter Lobby</button>
        </form>
      </StyledLogin>
    </StyledLoginWrapper>
  );
};

export default Login;
