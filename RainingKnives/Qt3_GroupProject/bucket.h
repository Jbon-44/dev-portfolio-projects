#ifndef BUCKET_H
#define BUCKET_H

#include <QGraphicsPixmapItem>
#include <QKeyEvent>
#include <QTimer>
#include <QPixmap>

class Bucket : public QObject, public QGraphicsPixmapItem
{
    Q_OBJECT

public:
    explicit Bucket(QObject *parent = nullptr);

protected:
    // Handle key presses and releases
    void keyPressEvent(QKeyEvent *event) override;
    void keyReleaseEvent(QKeyEvent *event) override;

private slots:
    void updateFrame();

public slots:
    void onDeath();

signals:
    void playAgainRequested();

private:
    enum class Direction { Idle_Left, Idle_Right, Left, Right };

    // Animation helpers
    QPixmap currentFrame();

    // Sprite sheet and animation
    QPixmap spriteSheet_;
    QTimer *animationTimer_;
    Direction direction_;
    int frameIndex_;
    int frameWidth_;
    int frameHeight_;
    int paddingColumns_;

    // Movement and drops
    const int moveAmount_ = 20;
};

#endif // BUCKET_H
