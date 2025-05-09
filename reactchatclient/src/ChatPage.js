
import React, { useEffect, useState } from 'react';

function ChatPage({ username, room }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [socket, setSocket] = useState(null);



    // Helper function to check if a string is JSON
  function isJsonString(str) {
    try {
      JSON.parse(str);
      return true;
    } catch (e) {
      return false;
    }
  }

  useEffect(() => {
    const ws = new WebSocket('ws://localhost:8080');

    ws.onopen = () => {
      console.log('Connected to WebSocket server');
      ws.send(JSON.stringify({ type: 'join', user: username, room: room }));
    };

    ws.onmessage = (message) => {
      const messageData = message.data;

      // Check if the message is JSON before parsing
      if (isJsonString(messageData)) {
        const data = JSON.parse(messageData);
        console.log('Received JSON message from server:', data);
        setMessages((prev) => [...prev, data]);
      } else {
        // If it's not JSON, display it as a plain text message
        setMessages((prev) => [...prev, {message: messageData }]);
      }
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    ws.onclose = () => {
      console.log('Disconnected from the server');
    };

    setSocket(ws);

    return () => {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send('leave');
      }
      ws.close();
    };
  }, [username, room]);

  const handleSendMessage = () => {
    if (input && socket) {
      socket.send(JSON.stringify({ type: 'message', user: username, room: room, message: input }));
      setInput('');
    }


  };

  // Function to handle "Enter" key press for sending messages
  const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
      handleSendMessage();
    }
  };

  return (
    <div className="ChatPage">
      <h2>Room: {room}</h2>
      <div className="chat-window">
        {messages.map((msg, index) => (
          <div key={index} className="chat-message">
            <strong>{msg.user}</strong> {msg.message}
          </div>
        ))}
      </div>
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="Type a message"
        onKeyPress={handleKeyPress}
      />
      <button onClick={handleSendMessage}>Send</button>
    </div>
  );
}

export default ChatPage;