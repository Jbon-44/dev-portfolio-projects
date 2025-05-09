
window.onload = function() {
    const canvas = document.getElementById('gameCanvas');
    const ctx = canvas.getContext('2d');
    const restartButton = document.getElementById('restartButton');


    // Load images (background, honey, bees, and big bee)
    const backgroundImage = new Image();
    const honeyImage = new Image();
    const beeImageLeft = new Image();
    const beeImageRight = new Image();
    const bigBeeImage = new Image();

    backgroundImage.src = 'background-garden.webp';
    honeyImage.src = 'honey.png';
    beeImageLeft.src = 'bee-left.png';
    beeImageRight.src = 'bee-right.png';
    bigBeeImage.src = 'fat-bee.png';


    let honeyPos = {x: canvas.width / 2, y: canvas.height / 2};
    let bees = [];
    let allBeesAtHoney = false;  // Flag to check if all bees reached the honey

    // reset game
    function resetGame() {

        // Create bees
        for (let i = 0; i < 10; i++) {
            bees.push({
                x: Math.random() * canvas.width,
                y: Math.random() * canvas.height,
                speed: Math.random() * 2 + 1,
                direction: 'right',
                image: beeImageRight
            });
        }

        allBeesAtHoney = false;
        restartButton.style.display = 'none'; //hide restart button
    }


    resetGame(); //call resetGame to set up game


    // Track mouse movement
    let mousePos = { x: canvas.width / 2, y: canvas.height / 2 };
    document.addEventListener('mousemove', function(event) {
        mousePos.x = event.clientX - canvas.offsetLeft;
        mousePos.y = event.clientY - canvas.offsetTop;
    });

    // Update positions and directions of bees
    function update() {
        if (!allBeesAtHoney) {
            // Move honey towards mouse
            honeyPos.x += (mousePos.x - honeyPos.x) ;
            honeyPos.y += (mousePos.y - honeyPos.y) ;

            // Move bees and adjust direction
            let beesAtHoney = true;  // Assume all bees are at honey initially
            bees.forEach(bee => {
                const dx = honeyPos.x - bee.x;
                const dy = honeyPos.y - bee.y;
                const distance = Math.sqrt(dx * dx + dy * dy);

                if (distance > 5) {  // If bee is not close enough to honey
                    bee.x += (dx / distance) * bee.speed;
                    bee.y += (dy / distance) * bee.speed;
                    beesAtHoney = false;  // If any bee is still not at honey
                }

                // Change bee direction and image
                if (mousePos.x > bee.x && bee.direction !== 'right') {
                    bee.direction = 'right';
                    bee.image = beeImageRight; //change bee to look right
                } else if (mousePos.x < bee.x && bee.direction !== 'left') {
                    bee.direction = 'left';
                    bee.image = beeImageLeft; //change be to look left
                }
            });

            // Check if all bees are at the honey
            if (beesAtHoney) {
                allBeesAtHoney = true;  // Set flag to true when all bees reach honey
                restartButton.style.display = 'block';
            }
        }
    }

    // Draw everything: background, honey, bees, and big bee if bees are gone
    function draw() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);  // Clear the canvas

        // Draw the background image
        ctx.drawImage(backgroundImage, 0, 0, canvas.width, canvas.height);  // Fill entire canvas

        if (!allBeesAtHoney) {
            // Draw honey
            ctx.drawImage(honeyImage, honeyPos.x - 25, honeyPos.y - 25, 50, 50);

            // Draw bees
            bees.forEach(bee => {
                ctx.drawImage(bee.image, bee.x - 25, bee.y - 25, 50, 50);
            });
        } else {
            // All bees are at honey, draw a big bee
            ctx.drawImage(bigBeeImage, honeyPos.x - 50, honeyPos.y - 50, 100, 100);  // Big bee size
        }
    }


    

    // The game loop
    function gameLoop() {
        update();
        draw();
        window.requestAnimationFrame(gameLoop);
    }

    // Start the game loop once all images are loaded
    backgroundImage.onload = function() {
        gameLoop();
    };

    restartButton.addEventListener('click', function (){
        resetGame(); //call reset function
    })
};