#include "bucket.h"
#include <QGraphicsScene>
#include <QKeyEvent>
#include <QTimer>
#include <QPixmap>
#include <QTransform>
#include <QDebug>
#include <QGraphicsTextItem>
#include <QGraphicsRectItem>
#include <QPushButton>
#include <QGraphicsProxyWidget>

Bucket::Bucket(QObject *parent) : QObject{parent}, direction_(Direction::Idle_Right), frameIndex_(0)
{
    // Load the sprite sheet
    spriteSheet_ = QPixmap(":/images/character.png");
    if (spriteSheet_.isNull()) {
        qDebug() << "ERROR: Failed to load sprite sheet!";
        return;
    }

    frameWidth_ = 16;
    frameHeight_ = 16;
    paddingColumns_ = 1;

    setPixmap(currentFrame());
    setPos(450, 450);
    setScale(4); // Enlarge the sprite for visibility

    // Allow the item to receive key events
    setFlag(QGraphicsItem::ItemIsFocusable);
    setFocus();

    // Animation timer setup
    animationTimer_ = new QTimer(this);
    connect(animationTimer_, &QTimer::timeout, this, &Bucket::updateFrame);
    animationTimer_->start(150); // Adjust timing for animation speed
}

void Bucket::keyPressEvent(QKeyEvent* event)
{
    int xMovement;
    if (event->key() == Qt::Key_Left) {
        xMovement = this->pos().x() - moveAmount_;
        if (xMovement > -30){
            direction_ = Direction::Left;
            moveBy(-moveAmount_, 0);
        }
    }
    else if (event->key() == Qt::Key_Right) {
        xMovement = this->pos().x() - moveAmount_;
        if (xMovement < 825){
            direction_ = Direction::Right;
            moveBy(moveAmount_, 0);
        }
    }
}

void Bucket::keyReleaseEvent(QKeyEvent *event)
{
    if (event->key() == Qt::Key_Right && direction_ == Direction::Right) {
        direction_ = Direction::Idle_Right;
        frameIndex_ = 1;
        setPixmap(currentFrame());
    } else if (event->key() == Qt::Key_Left && direction_ == Direction::Left) {
        direction_ = Direction::Idle_Left;
        frameIndex_ = 1;
        setPixmap(currentFrame());
    }
}

void Bucket::onDeath() {
    qDebug() << "[Bucket] Player has died.";

    // Disable movement
    setEnabled(false);

    // Death sprite
    frameIndex_ = 0;
    paddingColumns_ = 5;
    setPixmap(currentFrame());
    animationTimer_->stop();

    // Create overlay
    QGraphicsRectItem* overlay = new QGraphicsRectItem();
    overlay->setRect(scene()->sceneRect());
    overlay->setBrush(QColor(0, 0, 0, 180));  // semi-transparent black
    overlay->setZValue(10);  // On top of everything
    scene()->addItem(overlay);

    // Game Over text
    QGraphicsTextItem* gameOverText = scene()->addText("GAME OVER");
    QFont font("Arial", 60, QFont::Bold);
    gameOverText->setFont(font);
    gameOverText->setDefaultTextColor(Qt::red);
    gameOverText->setZValue(11);
    gameOverText->setPos(scene()->width()/2 - gameOverText->boundingRect().width()/2, 150);

    // Play Again button
    QPushButton* playAgainBtn = new QPushButton("Play Again");
    QGraphicsProxyWidget* proxyBtn = scene()->addWidget(playAgainBtn);
    proxyBtn->setZValue(11);
    proxyBtn->setPos(scene()->width()/2 - playAgainBtn->width()/2, 250);

    // Connect button click to signal
    connect(playAgainBtn, &QPushButton::clicked, this, [=]() {
        emit playAgainRequested();
    });
}

void Bucket::updateFrame()
{
    frameIndex_ = (frameIndex_ + 1) % 4;  // Only 4 frames for idle and run
    setPixmap(currentFrame());
}

QPixmap Bucket::currentFrame()
{
    int row = 0;
    int col = frameIndex_;

    switch (direction_) {
    case Direction::Idle_Right:
    case Direction::Idle_Left:
        row = 1;
        break;
    case Direction::Left:
    case Direction::Right:
        row = 2;
        break;
    }

    int x = (col + paddingColumns_) * frameWidth_;
    int y = row * frameHeight_;

    QPixmap rawFrame = spriteSheet_.copy(x, y, frameWidth_, frameHeight_);
    QImage frameImg = rawFrame.toImage().convertToFormat(QImage::Format_ARGB32);

    QColor bgColor = QColor(145, 145, 145); // Set this to the actual background color
    for (int y = 0; y < frameImg.height(); ++y) {
        for (int x = 0; x < frameImg.width(); ++x) {
            if (frameImg.pixelColor(x, y) == bgColor) {
                frameImg.setPixelColor(x, y, Qt::transparent);
            }
        }
    }

    QPixmap frame = QPixmap::fromImage(frameImg);

    if (direction_ == Direction::Left || direction_ == Direction::Idle_Left) {
        QTransform flip;
        flip.scale(-1, 1);
        flip.translate(-frame.width(), 0);
        frame = frame.transformed(flip);
    }

    return frame;
}

