#include "mainwindow.h"
#include "signupdialog.h"
#include "signin.h"
#include "game1scene.h"
#include "scoreboard.h"
#include <QtWidgets/QGraphicsView>
#include <QPushButton>
#include <QVBoxLayout>
#include <QLabel>
#include <QPixmap>
#include <QFile>
#include <QStandardPaths>
#include <QJsonDocument>
#include <QJsonArray>
#include <QJsonObject>
#include <QMessageBox>
#include <QDir>
#include <QJsonArray>
#include <iostream>



MainWindow::MainWindow(QWidget *parent) : QMainWindow(parent) {

    QWidget *centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    // Create a QLabel to display the game image
    gameImageLabel = new QLabel(this);

    QPixmap pixmap(":/images/fallingKnivesMain.png");


    pixmap = pixmap.scaled(600, 500, Qt::KeepAspectRatio, Qt::SmoothTransformation);

    gameImageLabel->setPixmap(pixmap);
    gameImageLabel->setAlignment(Qt::AlignCenter);

    // Buttons for navigation
    signUpButton = new QPushButton("Sign Up", this);
    signInButton = new QPushButton("Sign In", this);
    guestButton  = new QPushButton("Play as Guest", this);

    //vertical layout that first shows the image and then the buttons
    QVBoxLayout *layout = new QVBoxLayout(centralWidget);
    layout->addWidget(gameImageLabel);
    layout->addWidget(signUpButton);
    layout->addWidget(signInButton);
    layout->addWidget(guestButton);


    //Welcome Page Labels
    welcomeLabel = new QLabel(this);
    profilePicLabel = new QLabel(this);
    dateLabel = new QLabel(this);

    userBestLabel = new QLabel(this);
    globalBestLabel = new QLabel(this);

    //add welcome page Labels to layout
    layout->addWidget(welcomeLabel);
    layout->addWidget(profilePicLabel);
    layout->addWidget(dateLabel);
    layout->addWidget(userBestLabel);
    layout->addWidget(globalBestLabel);


    dificultyCombo  = new QComboBox(this);
    dificultyCombo->addItems({ "Easy", "Medium", "Hard"});


    //Center QComboBox modes
    for (int i = 0; i < dificultyCombo->count(); ++i) {
        dificultyCombo->setItemData(i, Qt::AlignCenter, Qt::TextAlignmentRole);
    }

    dificultyCombo->hide();


    //initially hidden
    welcomeLabel->hide();
    profilePicLabel->hide();
    dateLabel->hide();
    userBestLabel->hide();
    globalBestLabel->hide();




    playButton = new QPushButton("Play Game", this);
    playButton->hide(); //playbutton hidden

    layout->addWidget(playButton);
    layout->addWidget(dificultyCombo);
    layout->setAlignment(Qt::AlignCenter);



    //SIGNAl when signupButton is clicked
    connect(signUpButton, &QPushButton::clicked, this, &MainWindow::onSignUpClicked);
    connect(signInButton, &QPushButton::clicked, this, &MainWindow::onSignInClicked);
    connect(guestButton, &QPushButton::clicked,this, &MainWindow::onGuestClicked);
    connect(playButton, &QPushButton::clicked,this, &MainWindow::onPlayClicked);


    connect(dificultyCombo, QOverload<int>::of(&QComboBox::currentIndexChanged),this, &MainWindow::onComboBoxChanged);

}


void MainWindow::onSignUpClicked() {

    SignUpDialog dlg(this);

    if (dlg.exec() == QDialog::Accepted) {
        // access data:
        QString fn = dlg.firstName();
        QString ln = dlg.lastName();
        QDate dob  = dlg.dateOfBirth();
        QString g  = dlg.gender();
        QString user = dlg.username();
        QString pass = dlg.password();
        QString pic  = dlg.picturePath();



        QString dataDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
        QDir().mkpath(dataDir); // ensure it exists
        QString filePath = dataDir + "/users.json";


        // std::cout << "User store path: "
        //           << filePath.toStdString()
        //           << std::endl;


        // Load existing users
        QJsonArray usersArray; {

            QFile in(filePath);
            if (in.open(QIODevice::ReadOnly)) {
                // Read the raw JSON bytes from the file
                QByteArray raw = in.readAll();

                //Parse them into a QJsonDocument
                QJsonDocument doc = QJsonDocument::fromJson(raw);

                if (doc.isArray()) {
                    usersArray = doc.array();
                }
                else {
                    usersArray = QJsonArray();
                }

                in.close();
            }
        }

        // Check uniqueness
        for (auto v : usersArray) {
            if (v.toObject()["username"].toString() == user) {
                QMessageBox::warning(this, "Username Taken","That username is already in use.");
                return;
            }
        }


        // Append new user
        QJsonObject newUser;
        newUser["firstName"]  = fn;
        newUser["lastName"]   = ln;
        newUser["dob"]        = dob.toString(Qt::ISODate);
        newUser["gender"]     = g;
        newUser["username"]   = user;
        newUser["password"]   = pass;
        newUser["picture"]    = pic;
        newUser["lastScore"]  = 0;
        usersArray.append(newUser);

        // Write back
        QFile out(filePath);
        if (!out.open(QIODevice::WriteOnly)) {

            QMessageBox::critical(this, "Save Error","Could not write user file.");

            return;
        }

        out.write(QJsonDocument(usersArray).toJson());

        out.close();

        QMessageBox::information(this, "Account Created", "Your account has been created!");

    }

}

void MainWindow::onSignInClicked() {


    SignInDialog dlg(this);
    if (dlg.exec() != QDialog::Accepted) //if successful login
        return;

    // load users.json
    QString dataDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QString filePath = dataDir + "/users.json";
    QFile in(filePath);

    // QJsonArray usersArray;
    usersArray_ = QJsonArray();



    if (in.open(QIODevice::ReadOnly)) {
        //read in all bytes, parse it as a JSON array, stash that array in usersArray.
        // usersArray = QJsonDocument::fromJson(in.readAll()).array();
        usersArray_ = QJsonDocument::fromJson(in.readAll()).array();

        in.close();
    }

    // find matching user
    for (auto v : usersArray_) {

        auto obj = v.toObject(); //Convert the JSON value to an object so we can read its fields

        // Compare the stored username and password against what the user entered
        if (obj["username"].toString() == dlg.username() &&
            obj["password"].toString() == dlg.password()){

            // store for later updates
            currentUser_ = obj;

            // success login
            showUserDashboard(obj);
            return;
        }
    }

    QMessageBox::critical(this, "Login Failed","Username or password incorrect.");
}



void MainWindow::onGuestClicked() {


    // Build a “guest” profile
    QJsonObject guest;

    guest["firstName"] = "Guest";
    guest["lastName"] = "";

    // Point to a default avatar in your resources
    guest["picture"] = ":/images/defaultAvatar.png";


    showUserDashboard(guest);


}


void MainWindow::showUserDashboard(const QJsonObject &u) {

    // Name
    QString name = u["firstName"].toString() + " " + u["lastName"].toString();
    welcomeLabel->setText("Welcome, " + name);
    welcomeLabel->show();

    // Profile pic
    QPixmap pm(u["picture"].toString());
    profilePicLabel->setPixmap(pm.scaled(80,80, Qt::KeepAspectRatio));
    profilePicLabel->show();

    // Today’s date
    QString today = QDate::currentDate().toString("MMMM d, yyyy");
    dateLabel->setText(today);
    dateLabel->show();

    int myScore = u.value("lastScore").toInt(0);
    userBestLabel->setText(QString("Personal Best: %1").arg(myScore));
    userBestLabel->show();

    // fetch & display the global best
    int best = fetchGlobalBestScore();
    globalBestLabel->setText(QString("Global Best: %1").arg(best));
    globalBestLabel->show();




    // Birthday greeting
    if (u["dob"].toString() == QDate::currentDate().toString(Qt::ISODate)){

        QMessageBox msg(this);
        msg.setIconPixmap(QPixmap(":/images/birthdayCake.png").scaled(64, 64, Qt::KeepAspectRatio, Qt::SmoothTransformation));
        msg.setWindowTitle("Happy Birthday!");
        msg.setText("Happy Birthday, " + u["firstName"].toString() + "!");
        msg.exec();
    }

    // hide the onboarding buttons
    signUpButton->hide();
    signInButton->hide();
    guestButton->hide();

    // show the “Play Game” launch button
    playButton->show();

    dificultyCombo->show();


}





void MainWindow::onPlayClicked() {

    // hide the login/dashboard window
    this->hide();


    if (difficulty_ == "Hard"){
        std::cout << "Hard Selected" << std::endl;
    } else if (difficulty_ == "Medium"){
        std::cout << "Medium Selected" << std::endl;
    } else {
        std::cout << "Easy Selected" << std::endl;
    }

    // Create the scene and view
    Game1Scene *gameScene = new Game1Scene(difficulty_, /*parent=*/nullptr);
    QGraphicsView *gameView  = new QGraphicsView(gameScene);


    ScoreBoard *sb = gameScene->scoreboard();;

    // *** NEW: hook the scene’s internal scoreboard **   

    //calls onGameEnded to update scores to .json files
    connect(sb, &ScoreBoard::gameOver,this, &MainWindow::onGameEnded);

    // Associate them
    gameView->setScene(gameScene);

    // Parent the scene to the view so it gets cleaned up later
    gameScene->setParent(gameView);

    // Fix the window size & turn off scrollbars
    gameView->setFixedSize(910, 512);
    gameView->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
    gameView->setVerticalScrollBarPolicy  (Qt::ScrollBarAlwaysOff);

    // Smooth out rendering
    gameView->setRenderHint(QPainter::Antialiasing);

    gameView->setWindowTitle("Run from the knives!");

    gameView->show();

    activeGameView_ = gameView;

    // when play again is requested, close game view and show login
    connect(gameScene, &Game1Scene::playAgainRequested, this, &MainWindow::resetToLoginScreen);

}




void MainWindow::onComboBoxChanged(int index) {

    this->difficulty_ = dificultyCombo->itemText(index);

}




int MainWindow::fetchGlobalBestScore() {

    // locate global.json alongside users.json
    QString dataDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QString path = dataDir + "/global.json";

    // std::cout << "User store path: "
    //           << path.toStdString()
    //           << std::endl;

    // qDebug() << "[DEBUG] global.json path =" << path;

    QFile f(path);
    if (!f.open(QIODevice::ReadOnly)) return 0;

    QJsonDocument allScores = QJsonDocument::fromJson(f.readAll());
    f.close();

    if (allScores.isObject())
        return allScores.object().value("score").toInt(0);

    // fallback
    return 0;
}



void MainWindow::writeUsersJson(const QJsonArray &usersArray) {

    QString dataDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QDir().mkpath(dataDir);
    QString filePath = dataDir + "/users.json";

    QFile out(filePath);
    if (!out.open(QIODevice::WriteOnly)) {
        QMessageBox::critical(this, "Save Error", "Could not write users.json");
        return;
    }
    out.write(QJsonDocument(usersArray).toJson());
    out.close();

}



void MainWindow::appendGlobalScore(int newScore) {

    QString dataDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QDir().mkpath(dataDir);
    QString filePath = dataDir + "/global.json";

    // check current best
    int best = fetchGlobalBestScore();
    if (newScore <= best)
        return;   // no need to rewrite if we didn't beat it

    // build new JSON object
    QJsonObject rec;
    rec["score"] = newScore;
    QJsonDocument doc(rec);

    // overwrite the file with the new best
    QFile out(filePath);
    if (!out.open(QIODevice::WriteOnly)) {
        QMessageBox::critical(this, "Save Error","Could not write global.json");
        return;
    }
    out.write(doc.toJson());
    out.close();

}




void MainWindow::onGameEnded(int finalScore) {
    // // qDebug() << "[DEBUG] onGameEnded called with score=" << finalScore;
    // std::cout<<"FINALSCORE = " <<finalScore;
    // Reload users.json from disk
    QString dataDir = QStandardPaths::writableLocation(QStandardPaths::AppDataLocation);
    QString filePath = dataDir + "/users.json";
    QJsonArray users;
    {
        QFile in(filePath);
        if (in.open(QIODevice::ReadOnly)) {
            users = QJsonDocument::fromJson(in.readAll()).array();
            in.close();
        }
    }
    //If this was a real user, find & possibly update
    QString uname = currentUser_.value("username").toString();
    bool wrote = false;
    if (!uname.isEmpty()) {
        for (int i = 0; i < users.size(); ++i) {
            QJsonObject obj = users[i].toObject();
            if (obj["username"].toString() == uname) {
                int prevBest = obj.value("lastScore").toInt(0);
                if (finalScore > prevBest) {
                    obj["lastScore"] = finalScore;
                    users[i] = obj;
                }
                wrote = true;
                break;
            }
        }
    }
    // Only write if we actually updated a real user
    if (wrote) {
        writeUsersJson(users);
        currentUser_ = QJsonObject();       // clear stale copy
        // (you can re-load and pass to showUserDashboard if desired)
    }
    // Update global best
    appendGlobalScore(finalScore);
    // Show the dashboard (you can pass the updated user object)
    // showUserDashboard(...);
}

void MainWindow::resetToLoginScreen() {
    // Close the active game view if it exists
    if (activeGameView_) {
        activeGameView_->close();
        activeGameView_ = nullptr;
    }

    // Reset the UI back to welcome/login screen
    welcomeLabel->show();
    profilePicLabel->show();
    dateLabel->show();
    userBestLabel->show();
    globalBestLabel->show();
    dificultyCombo->show();
    playButton->show();

    signUpButton->hide();
    signInButton->hide();
    guestButton->hide();

    this->show();  // Bring back the login window
}


