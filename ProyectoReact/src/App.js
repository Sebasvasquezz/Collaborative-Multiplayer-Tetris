import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Lobby from './components/Lobby';
import Tetris from './components/Tetris';
import Login from './components/Login';
import { WebSocketProvider } from './WebSocketContext';

function App() {
  return (
    <WebSocketProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<Login />} />
            <Route path="/lobby" element={<Lobby />} />
            <Route path="/tetris" element={<Tetris />} />
          </Routes>
        </div>
      </Router>
    </WebSocketProvider>
  );
}

export default App;
