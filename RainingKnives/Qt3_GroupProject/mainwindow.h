#ifndef MAINWINDOW_H
#define MAINWINDOW_H
#include <QPushButton>
#include <QComboBox>
#include <QVBoxLayout>
#include <QLabel>
#include<QJsonArray>
#include<QJsonObject>

#include <QMainWindow>
#include <QtWidgets/qgraphicsview.h>


class MainWindow : public QMainWindow {
    Q_OBJECT
public:
    explicit MainWindow(QWidget *parent = nullptr);

private slots:

    void onSignUpClicked();
    void onSignInClicked();
    void onGuestClicked();
    void onPlayClicked();
    void onComboBoxChanged(int index);
    void onGameEnded(int finalScore);
    void resetToLoginScreen();


signals:
    void difficultyChanged(const QString &difficulty);


private:

    // holds every user record from users.json
    QJsonArray usersArray_;        // holds the full users.json array

    // the JSON object for the currently‑logged‑in user
    QJsonObject currentUser_;


    QPushButton *signUpButton;
    QPushButton *signInButton;
    QPushButton *guestButton;


    //Dificulty level buttons
    QComboBox *dificultyCombo;


    //Label that holds game label
    QLabel *gameImageLabel;


    QLabel *welcomeLabel; //label that shows user
    QLabel *profilePicLabel; //label for user picture
    QLabel *dateLabel; //label for date


    // //User and Global score
    QLabel *userBestLabel;
    QLabel *globalBestLabel;


    //play button that appears after signing in/play as guest
    QPushButton *playButton;


    // Difficulty
    QString difficulty_;


    //user dash board
    void showUserDashboard(const QJsonObject &user);


    int  fetchGlobalBestScore();


    // Writes the in‐memory usersArray back out to users.json
    void writeUsersJson(const QJsonArray &usersArray);


    // Appends a new score record into global.json
    void appendGlobalScore(int newScore);

    QGraphicsView *activeGameView_ = nullptr;
};

#endif // MAINWINDOW_H
