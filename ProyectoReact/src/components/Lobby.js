import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { StyledLobbyWrapper, StyledLobby } from './styles/StyledLobby';
import { useWebSocket } from '../WebSocketContext';

const Lobby = () => {
  const [players, setPlayers] = useState([]);
  const [isReady, setIsReady] = useState(false);
  const navigate = useNavigate();
  
  const location = useLocation();
  const { playerName } = location.state;
  const { socket } = useWebSocket();

  useEffect(() => {
    if (!playerName || !socket) {
      navigate('/');
      return;
    }

    socket.onmessage = (event) => {
      const data = JSON.parse(event.data);
      if (data.type === 'UPDATE_PLAYERS') {
        console.log(1)
        setPlayers(data.players);
      } else if (data.type === 'START_GAME') {
        console.log(2)
        console.log("id que llego="+data.id+" el color que llego= "+data.color)
        navigate('/tetris',{ state: { initialStage: data.stage, initialId: data.id, initialColor: data.color } });
      }
    };

    return () => {
      if (socket) {
        socket.onmessage = null;
      }
    };
  }, [navigate, playerName, socket]);

  const handleReady = () => {
    setIsReady(true);
    if (socket) {
      socket.send(JSON.stringify({ type: 'PLAYER_READY', name: playerName }));
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