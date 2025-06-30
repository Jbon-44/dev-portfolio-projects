#ifndef SCOREBOARD_H
#define SCOREBOARD_H

#include <QObject>
#include <QGraphicsTextItem>
#include <QGraphicsItemGroup>
#include <QGraphicsRectItem>
#include <QGraphicsScene>
#include <QMediaPlayer>
#include <QAudioOutput>


class ScoreBoard : public QObject
{
    Q_OBJECT
public:
    explicit ScoreBoard(QGraphicsScene* scene, QObject* parent = nullptr);
    void setScore(int score);
    int getScore() {return this->score_;};

    void setLives(int dropCount);
    void removeLife();
    int getLives() {return this->lives_;};

    void updateDisplay();

private slots:
    void updateScore();

signals:
    void death();
    void gameOver(int finalScore);




private:

    int score_ = 0;
    int lives_ = 3;

    QGraphicsTextItem* scoreText_;
    QGraphicsTextItem* livesText_;
    QGraphicsRectItem* background_;
    QGraphicsItemGroup* group_;
    QGraphicsScene* scene_;

    QTimer* timer_;
    QTimer* flashTimer_;
    QGraphicsRectItem* flashOverlay_;

    QMediaPlayer *hitSoundPlayer_;
    QAudioOutput *hitAudioOutput_;
    QMediaPlayer *deathSoundPlayer_;
    QAudioOutput *deathAudioOutput_;

};

#endif // SCOREBOARD_H
