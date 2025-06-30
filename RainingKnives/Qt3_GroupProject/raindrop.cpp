#include "raindrop.h"
#include "bucket.h"
#include <QtWidgets/QGraphicsScene>
#include <iostream>


// Default Constructor
Raindrop::Raindrop(QObject *parent): QObject{parent}
{

    this->setPixmap((QPixmap(":/images/knife.png")).scaled(30,30));
    this->setPos(arc4random_uniform(907) + 1, 50);
    this->setFlag(QGraphicsItem::ItemIsFocusable, false);


    // Timer to detect for collision
    QTimer* collisionTimer = new QTimer(this);
    connect(collisionTimer, &QTimer::timeout, this, &Raindrop::checkCollisions);
    collisionTimer->start(15);

    // Timer for raindrop falling down
    QTimer *timer_drop = new QTimer(this);
    connect(timer_drop, &QTimer::timeout, this, &Raindrop::rainfall);
    timer_drop->start(1000);

}


// Constructor that takes an x coordinate value
Raindrop::Raindrop(float startX, float speed)
{
    this->setPixmap((QPixmap(":/images/knife.png")).scaled(30,30));
    this->setPos(startX, 0); //originally 50
    this->raindropSpeed_ = speed;

    this->setFlag(QGraphicsItem::ItemIsFocusable, false);

    // Timer to detect for collision
    QTimer* collisionTimer = new QTimer(this);
    connect(collisionTimer, &QTimer::timeout, this, &Raindrop::checkCollisions);
    collisionTimer->start(15);

    // Timer for raindrop falling down
    QTimer *timer_drop = new QTimer(this);
    connect(timer_drop, &QTimer::timeout, this, &Raindrop::rainfall);
    timer_drop->start(16);
}



// Slots
void Raindrop::rainfall(){

    // If raindrop is out of scene delete else move and update position
    if (this->y() < 500){
        this->setPos(this->pos().x(), this->pos().y() + raindropSpeed_);
        update();
    } else {
        std::cout << "Raindrop Left Scene" << std::endl;
        this->scene()->removeItem(this);
        delete this;
    }
}


void Raindrop::checkCollisions(){

    QList<QGraphicsItem*> collidingItems = this->collidingItems();

    for (int item = 0; item < collidingItems.size(); item++){
        QGraphicsItem* collisionItem = collidingItems.at(item);    
        Bucket* bucket = dynamic_cast<Bucket*>(collisionItem);
        Raindrop* raindrop = dynamic_cast<Raindrop*>(collisionItem);

        if (raindrop){
            this->collided_ = true;
            break;

        } else if (bucket) { // If collision with bucket add to numDrops_
            this->collided_ = true;
            emit lostLife();
            std::cout << "Got hit: " << std::endl;
            break;

        } else {
            continue;
        }

    }


    if (this->collided_){
        std::cout << "Raindrop Collision" << std::endl;
        this->scene()->removeItem(this);
        delete this;
    }
}
