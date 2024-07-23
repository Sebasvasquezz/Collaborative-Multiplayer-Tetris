import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { StyledLoginWrapper, StyledLogin, StyledButton, StyledScores } from "./styles/StyledLogin";
import { useWebSocket } from "../WebSocketContext";
import axios from "axios";
import { WShostURL, RESThostURL } from '../gameHelpers';

const Login = () => {
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [topScores, setTopScores] = useState([]);
  const navigate = useNavigate();
  const { setSocket } = useWebSocket();

  const handlePlay = (e) => {
    e.preventDefault();
    if (!name.trim()) {
      setError("Name is required");
      return;
    }
    const playerName = {
      name: name.trim(),
    };

    const socket = new WebSocket(WShostURL()+ "/lobby");
    socket.onopen = () => {
      console.log("WebSocket connection established");
      socket.send(JSON.stringify({ type: "JOIN", ...playerName }));
    };

    setSocket(socket);
    navigate("/lobby", { state: { playerName: playerName } });
  };

  const fetchTopScores = async () => {
    try {
      const response = await axios.get(RESThostURL() + "/api/scores/top");
      setTopScores(response.data);
    } catch (error) {
      console.error("Error fetching top scores:", error);
    }
  };

  return (
    <StyledLoginWrapper>
      <StyledLogin>
        <h1>Welcome Tetris Co-op</h1>
        <form onSubmit={handlePlay}>
          <div>
            <label>
              Name:
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="Enter your name"
                required
              />
            </label>
          </div>
          {error && <div className="error">{error}</div>}
          <div>
            <StyledButton type="submit">Enter Lobby</StyledButton>
          </div>
        </form>
        <div>
          <StyledButton onClick={fetchTopScores}>Top Scores</StyledButton>
        </div>
        {topScores.length > 0 && (
          <StyledScores>
            <h2>Top Scores</h2>
            <ul>
              {topScores.map((score, index) => (
                <li key={index}>
                  {score.name}: {score.score}
                </li>
              ))}
            </ul>
          </StyledScores>
        )}
      </StyledLogin>
    </StyledLoginWrapper>
  );  
};

export default Login;
