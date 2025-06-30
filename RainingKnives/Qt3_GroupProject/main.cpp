#include <QApplication>
#include <QtWidgets/QGraphicsView>
#include <QMediaPlayer>
#include <QAudioOutput>
#include "game1scene.h"
#include "mainwindow.h"

int main(int argc, char *argv[])
{
    QApplication app (argc, argv);

    // QGraphicsView* gameView = new QGraphicsView();
    // Game1Scene* gameScene = new Game1Scene();



    // // Set the scene and set size of scene
    // gameView->setScene(gameScene);
    // gameView->setFixedSize(910,512);

    // // Turn off Scroll Bars
    // gameView->setHorizontalScrollBarPolicy((Qt::ScrollBarAlwaysOff));
    // gameView->setVerticalScrollBarPolicy((Qt::ScrollBarAlwaysOff));

    // gameView->show();

    // Set up background music
    static QMediaPlayer *player = new QMediaPlayer;
    static QAudioOutput *audioOutput = new QAudioOutput;

    player->setAudioOutput(audioOutput);
    audioOutput->setVolume(50); // Adjust volume as needed
    player->setSource(QUrl("qrc:/audio/BossMain.wav"));
    player->setLoops(QMediaPlayer::Infinite);
    player->play();


    MainWindow w;
    w.show();


    app.exec();
}
