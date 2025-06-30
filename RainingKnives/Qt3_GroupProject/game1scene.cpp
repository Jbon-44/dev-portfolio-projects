#include "game1scene.h"
#include "bucket.h"
#include "raindrop.h"
#include <iostream>


Game1Scene::Game1Scene(QString difficulty, QWidget *parent) : QGraphicsScene{parent}
{
    if (difficulty == "Hard"){
        dropletSpeed_ = 7;
        maxDropletSpeed_ = 14.5;
        maxCloudSpeed_ = 8.5;
    } else if (difficulty == "Medium"){
        dropletSpeed_ = 4;
        maxDropletSpeed_ = 9.5;
        maxCloudSpeed_ = 6.5;
    } else { // Easy
        dropletSpeed_ = 2;
        maxDropletSpeed_ = 5.5;
        maxCloudSpeed_ = 4.5;
    }

    // Set background
    setBackgroundBrush(QBrush(QImage(":/images/knivesBackground.png").scaledToHeight(512).scaledToWidth(910)));

    // Set bucket picture
    bucket_ = new Bucket();
    addItem(bucket_);


    //Set clouds picture
    cloud_ = new Cloud();
    addItem(cloud_);


    //Set Scoreboard
    scoreboard_ = new ScoreBoard(this);

    // Set Scene Dimensions
    setSceneRect(0,0,908,510);

    // Timer to add more raindrops
    QTimer *timer_drop = new QTimer(this);
    connect(timer_drop, &QTimer::timeout, this, &Game1Scene::addRaindrop);
    timer_drop->start(100);

    // Timer to increase dropletSpeed every 10 seconds
    QTimer *timer_DropletSpeed = new QTimer(this);
    connect(timer_DropletSpeed, &QTimer::timeout, this, &Game1Scene::increaseDropletSpeed);
    timer_DropletSpeed->start(10000);

    // Timer to increase cloud speed every 15 seconds
    QTimer *timer_CloudSpeed = new QTimer(this);
    connect(timer_CloudSpeed, &QTimer::timeout, cloud_, &Cloud::increaseSpeed);
    timer_CloudSpeed->start(15000);

    //calls bucket death animation
    connect(scoreboard_, &ScoreBoard::death, bucket_, &Bucket::onDeath);
    connect(bucket_, &Bucket::playAgainRequested, this, &Game1Scene::playAgainRequested);
}



// Helper Methods
void Game1Scene::increaseDropletSpeed(){
    if (dropletSpeed_ < maxDropletSpeed_){
        this->dropletSpeed_ += (dropletSpeed_ * .25);
    }
    std::cout << this->dropletSpeed_ << std::endl;
}


void Game1Scene::increaseCloudSpeed(){
    if (cloud_->getSpeed() < maxCloudSpeed_){
        cloud_->increaseSpeed();
    }
}



// Slots
void Game1Scene::addRaindrop(){

    // Create a random x
    float startX = arc4random_uniform(907) + 1;

    // Create a point object to check if the raindrop spawn point is contained
    // within the cloud if so create drop with x
    QPointF startPoint(startX, 50);
    if (cloud_->contains(cloud_->mapFromScene(startPoint))){
        Raindrop* drop = new Raindrop(startX, dropletSpeed_);
        addItem(drop);
        // Add connection for losing life
        connect(drop, &Raindrop::lostLife, scoreboard_, &ScoreBoard::removeLife);
    }

}






