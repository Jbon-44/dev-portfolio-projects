#include "scoreboard.h"
#include <QFont>
#include <QBrush>
#include <QPen>
#include <iostream>
#include <QTimer>

ScoreBoard::ScoreBoard(QGraphicsScene* scene, QObject* parent)
    : QObject(parent), score_(0), lives_(3), scene_(scene)
{
    // Score Text
    scoreText_ = new QGraphicsTextItem(QString("Score: %1").arg(score_));
    scoreText_->setDefaultTextColor(Qt::white);
    scoreText_->setFont(QFont("Arial", 20));
    scoreText_->setZValue(1);
    scoreText_->setPos(0,0);

    // Lives Text
    livesText_ = new QGraphicsTextItem(QString("Lives: %1").arg(lives_));
    livesText_->setDefaultTextColor(Qt::white);
    livesText_->setFont(QFont("Arial", 20));
    livesText_->setZValue(1);
    livesText_->setPos(0,25);

    // Background for Score and Lives Text
    QRectF totalBounds = scoreText_->boundingRect().united(livesText_->boundingRect()).adjusted(-5, -5, 25, 25);
    background_ = new QGraphicsRectItem(totalBounds);
    background_->setBrush(QColor(0, 0, 0, 150));
    background_->setPen(Qt::NoPen);
    background_->setZValue(0);
    group_ = scene_->createItemGroup({ background_, scoreText_, livesText_ });
    group_->setPos(10, 10);

    // Flash overlay (hidden initially)
    flashOverlay_ = scene_->addRect(scene_->sceneRect(), QPen(Qt::NoPen), QBrush(Qt::red));
    flashOverlay_->setZValue(100);  // On top of everything
    flashOverlay_->setVisible(false);

    // Flash timer
    flashTimer_ = new QTimer(this);
    flashTimer_->setSingleShot(true);
    connect(flashTimer_, &QTimer::timeout, this, [this]() {
        flashOverlay_->setVisible(false);
    });

    // Timer to update score
    timer_ = new QTimer(this);
    connect(timer_, &QTimer::timeout, this, &ScoreBoard::updateScore);
    timer_->start(1000);

    hitSoundPlayer_ = new QMediaPlayer(this);
    hitAudioOutput_ = new QAudioOutput(this);
    hitSoundPlayer_->setAudioOutput(hitAudioOutput_);
    hitSoundPlayer_->setSource(QUrl("qrc:/audio/hit.ogg"));

    deathSoundPlayer_ = new QMediaPlayer(this);
    deathAudioOutput_ = new QAudioOutput(this);
    deathSoundPlayer_->setAudioOutput(deathAudioOutput_);
    deathSoundPlayer_->setSource(QUrl("qrc:/audio/yell10.wav"));
}



void ScoreBoard::removeLife(){
    if (lives_ > 0) {
    this->lives_ -= 1;

    // Show red flash
    flashOverlay_->setRect(scene_->sceneRect()); // Resize if needed
    flashOverlay_->setVisible(true);
    flashTimer_->start(50); // flash for 100 ms

    if (this->lives_ == 0){
        // Play death sound
        if (deathSoundPlayer_->playbackState() == QMediaPlayer::PlayingState)
            deathSoundPlayer_->stop();
        deathSoundPlayer_->play();

        std::cout << "Game Over" << std::endl;

        // Signal
        emit death();
        emit gameOver(score_);
        return;
    }

    // Play hit sound
    if (hitSoundPlayer_->playbackState() == QMediaPlayer::PlayingState)
        hitSoundPlayer_->stop();
    hitSoundPlayer_->play();

    updateDisplay();
    } else {
        return;
    }
}


void ScoreBoard::updateDisplay() {
    scoreText_->setPlainText(QString("Score: %1").arg(score_));
    livesText_->setPlainText(QString("Lives: %1").arg(lives_));

    // Ensure positions are correct (in case of size change)
    scoreText_->setPos(0, 0);
    livesText_->setPos(0, 25);

    QRectF totalBounds = scoreText_->boundingRect().united(livesText_->boundingRect()).adjusted(-5, -5, 25, 25);

    background_->setRect(totalBounds);
}



// slots


void ScoreBoard::updateScore(){
    if (lives_ > 0) {
    score_ += 1;
    }
    updateDisplay();
}
