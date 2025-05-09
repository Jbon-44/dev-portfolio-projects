

document.getElementById('joinBtn').addEventListener('click', function() {
    let username = document.getElementById('username').value;
    let room = document.getElementById('room').value;

    //check for lowercase of room name
    if(room!== room.toLowerCase()){
        alert("Room must be entered in lower case. ");
        return;
    }

    // Store username and room in localStorage
    localStorage.setItem('username', username);
    localStorage.setItem('room', room);

    // Open chat.html
    window.open('chat.html', '_blank');
});