import React, { useState } from 'react';
import LoginPage from './LoginPage';
import ChatPage from './ChatPage';
import './App.css';

function App() {
  const [page, setPage] = useState('login');
  const [userData, setUserData] = useState({ username: '', room: '' });

  const handleJoinRoom = (username, room) => {
    setUserData({ username, room });
    setPage('chat');
  };

  return (
    <div className="App">
      {page === 'login' ? (
        <LoginPage onJoinRoom={handleJoinRoom} />
      ) : (
        <ChatPage username={userData.username} room={userData.room} />
      )}
    </div>
  );
}

export default App;