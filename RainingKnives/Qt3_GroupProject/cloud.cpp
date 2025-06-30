#include "cloud.h"

Cloud::Cloud(QObject *parent): QObject{parent}
{
    this->setPixmap((QPixmap(":/images/cloud.png")).scaled(300,100));
    this->setPos(arc4random_uniform(650) + 0, -20);

    // Set focus to false so that player (bucket) can move
    this->setFlag(QGraphicsItem::ItemIsFocusable, false);

    // Randomize initial direction of cloud movement
    int initialD = arc4random_uniform(100);
    if (initialD > 49){
        this->direction_ = 1;
    } else {
        this->direction_ = -1;
    }

    // Timer for clouds to move
    QTimer *timer_cloud = new QTimer(this);
    connect(timer_cloud, &QTimer::timeout, this, &Cloud::cloudMovement);
    timer_cloud->start(20);


}



void Cloud::increaseSpeed(){
    this->cloudSpeed_ += cloudSpeed_ * .25;
}


// Slots

void Cloud::cloudMovement(){
    if(this->x() < 20){
        this->direction_ = 1;
    } else if (this->x() > 650){
        this->direction_ = -1;
    }

    this->setPos(this->pos().x() + (cloudSpeed_ * direction_), this->pos().y());

}
