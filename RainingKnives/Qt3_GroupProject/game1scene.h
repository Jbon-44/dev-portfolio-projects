#ifndef GAME1SCENE_H
#define GAME1SCENE_H

#include <QWidget>
#include <QGraphicsScene>
#include <QGraphicsPixmapItem>
#include <QGraphicsTextItem>
#include <QPoint>
#include "cloud.h"
#include "bucket.h"
#include "scoreboard.h"


class Game1Scene : public QGraphicsScene
{
    Q_OBJECT
public:
    explicit Game1Scene(QString difficulty, QWidget *parent = nullptr);

    void increaseDropletSpeed();
    void increaseCloudSpeed();


    //Newly added
    ScoreBoard* scoreboard() const { return scoreboard_; }

private:
    Cloud *cloud_;
    Bucket* bucket_;
    ScoreBoard* scoreboard_;

    float dropletSpeed_;
    float maxDropletSpeed_;
    float maxCloudSpeed_;


public slots:
    void addRaindrop();

signals:
    void playAgainRequested();
};

#endif // GAME1SCENE_H
