#ifndef SIGNUPDIALOG_H
#define SIGNUPDIALOG_H

#include <QDialog>
#include <QLineEdit>
#include <QDateEdit>
#include <QComboBox>
#include <QLabel>
#include <QPushButton>

class SignUpDialog : public QDialog {

    Q_OBJECT

public:

    explicit SignUpDialog(QWidget *parent = nullptr);

    // getters for the entered data:
    QString firstName() const   { return firstNameEdit->text(); }
    QString lastName() const    { return lastNameEdit->text(); }
    QDate   dateOfBirth() const { return dobEdit->date(); }
    QString gender() const      { return genderCombo->currentText(); }
    QString username() const    { return usernameEdit->text(); }
    QString password() const    { return passwordEdit->text(); }
    QString picturePath() const { return picPath; }

private slots:

    void onBrowsePicture();
    void onSubmit();

private:

    //user name and last name input
    QLineEdit *firstNameEdit;
    QLineEdit *lastNameEdit;

    //for choosing date of birth
    QDateEdit *dobEdit;

    //for choosing gender
    QComboBox *genderCombo;

    //to preview user picture uploaded
    QLabel *picPreview;

    //button to browse for picture
    QPushButton *browseBtn;

    //username and password input
    QLineEdit *usernameEdit;
    QLineEdit *passwordEdit;

    //submit or cancel buttons
    QPushButton *submitBtn;
    QPushButton *cancelBtn;

    //Qstring for picture path
    QString picPath;
};

#endif // SIGNUPDIALOG_H
