#ifndef CLOUD_H
#define CLOUD_H

#include <QObject>
#include <QtWidgets/QGraphicsItem>
#include <QTimer>

class Cloud : public QObject, public QGraphicsPixmapItem
{
    Q_OBJECT
public:
    explicit Cloud(QObject *parent = nullptr);

    void increaseSpeed();
    float getSpeed(){return this->cloudSpeed_;};

public slots:
    void cloudMovement();

private:
    float cloudSpeed_ = 1;
    float direction_;


signals:
};

#endif // CLOUD_H
