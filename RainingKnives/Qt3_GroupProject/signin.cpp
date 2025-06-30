#include "signin.h"
#include <QFormLayout>
#include <QMessageBox>
#include <QPushButton>

SignInDialog::SignInDialog(QWidget *parent) : QDialog(parent) {


    //window title
    setWindowTitle("Sign In");


    userEdit = new QLineEdit(this);
    passEdit = new QLineEdit(this);
    passEdit->setEchoMode(QLineEdit::Password);


    //submit and cancel buttons
    submitBtn = new QPushButton("Sign In", this);
    cancelBtn = new QPushButton("Cancel", this);

    //--Button Signals--//
    connect(submitBtn, &QPushButton::clicked, this, &SignInDialog::onSubmit);
    connect(cancelBtn, &QPushButton::clicked, this, &SignInDialog::reject);


    QFormLayout *form = new QFormLayout;
    form->addRow("Username:", userEdit);
    form->addRow("Password:", passEdit);


    QHBoxLayout *btns = new QHBoxLayout;
    btns->addStretch();
    btns->addWidget(submitBtn);
    btns->addWidget(cancelBtn);


    QVBoxLayout *main = new QVBoxLayout(this);
    main->addLayout(form);
    main->addLayout(btns);
    setLayout(main);
}

void SignInDialog::onSubmit() {

    if (userEdit->text().isEmpty() || passEdit->text().isEmpty()) {

        QMessageBox::warning(this, "Incomplete", "Enter username and password.");

        return;
    }

    accept();
}
