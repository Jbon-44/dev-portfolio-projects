#ifndef RAINDROP_H
#define RAINDROP_H

#include <QObject>
#include <QtWidgets/QGraphicsItem>
#include <QTimer>
#include <QList>

class Raindrop : public QObject, public QGraphicsPixmapItem
{
    Q_OBJECT
public:
    explicit Raindrop(QObject *parent = nullptr);
    Raindrop(float startX, float speed);

    void raindropSpeed(float speed);

public slots:
    void rainfall();
    void checkCollisions();


private:
    float raindropSpeed_ = 2;
    bool collided_ = false;

signals:
    void lostLife();
};

#endif // RAINDROP_H
