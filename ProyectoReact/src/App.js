import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Lobby from './components/Lobby';
import Tetris from './components/Tetris';
import Login from './components/Login';


const App = () => {
  const [playerName, setPlayerName] = useState(localStorage.getItem('playerName') || '');

  const startGame = () => {
    // Aquí puedes poner la lógica para iniciar el juego, como inicializar el estado del juego, etc.
    console.log('Game started!');
  };

  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Login setPlayerName={setPlayerName} />} />
          <Route path="/lobby" element={<Lobby />} />
          <Route path="/tetris" element={<Tetris startGame={startGame} />} />
        </Routes>
      </div>
    </Router>
  );
};

export default App;
