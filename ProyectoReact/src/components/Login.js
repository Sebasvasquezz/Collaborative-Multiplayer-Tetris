import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { StyledLoginWrapper, StyledLogin } from "./styles/StyledLogin";
import { useWebSocket } from "../WebSocketContext";

const Login = () => {
  const [name, setName] = useState("");
  const [error, setError] = useState("");
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

    const socket = new WebSocket("ws://localhost:8080/lobby");
    socket.onopen = () => {
      console.log("WebSocket connection established");
      socket.send(JSON.stringify({ type: "JOIN", ...playerName }));
    };

    setSocket(socket);
    navigate("/lobby", { state: { playerName: playerName } });
  };

  return (
    <StyledLoginWrapper>
      <StyledLogin>
        <h1>Login</h1>
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
            <button type="submit">Enter Lobby</button>
          </div>
        </form>
      </StyledLogin>
    </StyledLoginWrapper>
  );  
};

export default Login;
