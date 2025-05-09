import React, { useState } from 'react';
import InputWidget from './InputWidget';

function LoginPage({ onJoinRoom }) {
  const [username, setUsername] = useState('');
  const [room, setRoom] = useState('');

  const handleJoinClick = () => {

    // Validate room name: only lowercase letters, no spaces
    if (room !== room.toLowerCase()) {
      alert('Room name must contain only lowercase letters and no spaces.');
      return; // Stops further execution
    }

    if (username && room) {
      onJoinRoom(username, room);
    } else {
      alert('Please enter both a Username and Room.');
    }
  };

  return (
    <div>
      <h2>Login</h2>
      <InputWidget label="Username" placeholder="Enter your name" value={username} setValue={setUsername} />
      <InputWidget label="Room" placeholder="Enter room name" value={room} setValue={setRoom} />
      <button onClick={handleJoinClick}>Join Room</button>
    </div>
  );
}

export default LoginPage;