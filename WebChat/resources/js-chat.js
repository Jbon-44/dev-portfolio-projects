
// Get the username and room from localStorage
let username = localStorage.getItem('username');
let room = localStorage.getItem('room');

// Establish WebSocket connection and send a 'join' message
if (username && room) {
    let ws = new WebSocket('ws://' + location.host);

    ws.onopen = function() {
        console.log("WebSocket connection established.");
        ws.send(`join ${username} ${room}`);
    };

    ws.onmessage = function(event) {
        const data = JSON.parse(event.data);
        handleServerResponse(data);
    };

    ws.onclose = function() {
        displayMessage("Connection closed.");
    };

    ws.onerror = function(error) {
        console.error("WebSocket Error: ", error);
    };

    // Send a message when 'Send' button is clicked
    document.getElementById('sendBtn').addEventListener('click', function() {
        const message = document.getElementById('message').value;
        if (message && ws) {
            ws.send(`message ${message}`);
            document.getElementById('message').value = ''; // Clear the input field after sending
        }
    });
}

// Function to handle responses from the server
function handleServerResponse(data) {
    if (data.type === 'message') {
        displayMessage(`${data.user}: ${data.message}`);
    } else if (data.type === 'join') {
        displayMessage(`${data.user} has joined the room.`);
    } else if (data.type === 'leave') {
        displayMessage(`${data.user} has left the room.`);
    }
}

// Function to display messages in the chat window
function displayMessage(message) {
    const chat = document.getElementById('chat');
    chat.innerHTML += `<p>${message}</p>`;
    chat.scrollTop = chat.scrollHeight; // Auto-scroll to the latest message
}


//run RoomChatServer.jar with:
//java -jar RoomChatServer.jar -v
