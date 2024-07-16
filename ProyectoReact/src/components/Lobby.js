import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { StyledLobbyWrapper, StyledLobby } from './styles/StyledLobby';

const Lobby = () => {
  const [players, setPlayers] = useState([]);
  const [isReady, setIsReady] = useState(false);
  const navigate = useNavigate();
  const socketRef = useRef(null);
  const playerName = localStorage.getItem('playerName') || 'Unknown';

  useEffect(() => {
    const socket = new WebSocket('ws://localhost:8080/lobby');
    socketRef.current = socket;

    socket.onopen = () => {
      console.log('WebSocket connection established');
      socket.send(JSON.stringify({ type: 'JOIN', name: playerName }));
    };

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      if (data.type === 'UPDATE_PLAYERS') {
        console.log(1)
        setPlayers(data.players);
      } else if (data.type === 'START_GAME') {
        console.log(2)
        navigate('/tetris');
      }
    };

    socket.onclose = () => {
      console.log('WebSocket connection closed');
    };

    return () => {
      if (socketRef.current) {
        socketRef.current.close();
      }
    };
  }, [navigate, playerName]);

  const handleReady = () => {
    setIsReady(true);
    if (socketRef.current) {
      socketRef.current.send(JSON.stringify({ type: 'PLAYER_READY', name: playerName }));
    }
  };

  return (
    <StyledLobbyWrapper>
      <StyledLobby>
        <h1>Lobby</h1>
        <ul>
          {players.map((player, index) => (
            <li key={index}>
              {player.name} {player.isReady === 'true' ? '✔️' : '❌'}
            </li>
          ))}
        </ul>
        <button onClick={handleReady} disabled={isReady}>Ready</button>
      </StyledLobby>
    </StyledLobbyWrapper>
  );
};

export default Lobby;