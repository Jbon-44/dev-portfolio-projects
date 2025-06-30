#include "signupdialog.h"
#include <QFormLayout>
#include <QFileDialog>
#include <QHBoxLayout>
#include <QMessageBox>

SignUpDialog::SignUpDialog(QWidget *parent) : QDialog(parent) {

    setWindowTitle("Sign Up");


    //first and last name input widgets
    firstNameEdit = new QLineEdit(this);
    lastNameEdit = new QLineEdit(this);
    dobEdit = new QDateEdit(QDate::currentDate(), this); //current date as default

    dobEdit->setCalendarPopup(true);

    //gender widget box
    genderCombo = new QComboBox(this);
    genderCombo->addItems({ "Prefer not to say", "Male", "Female", "Other" });

    //picture preview widget
    picPreview = new QLabel(this);
    picPreview->setFixedSize(80,80);
    picPreview->setFrameShape(QFrame::Box);
    picPreview->setAlignment(Qt::AlignCenter);

    //browse file button
    browseBtn = new QPushButton("Browse...", this);


    //browse button //------SIGNAL---//
    connect(browseBtn, &QPushButton::clicked, this, &SignUpDialog::onBrowsePicture);

    //Line edit widget for username and password input
    usernameEdit = new QLineEdit(this);
    passwordEdit = new QLineEdit(this);
    passwordEdit->setEchoMode(QLineEdit::Password);


    //submit and cancel buttons
    submitBtn = new QPushButton("Submit", this);
    cancelBtn = new QPushButton("Cancel", this);



    //submit and cancel button //----SIGNALS -----//
    connect(submitBtn, &QPushButton::clicked, this, &SignUpDialog::onSubmit);
    connect(cancelBtn, &QPushButton::clicked, this, &SignUpDialog::reject); //sets QDialog::Rejected

    //form widget layout
    QFormLayout *form = new QFormLayout;
    form->addRow("First Name:", firstNameEdit);
    form->addRow("Last Name:",  lastNameEdit);
    form->addRow("Date of Birth:", dobEdit);
    form->addRow("Gender:",     genderCombo);


    //Horizontal layout for picture
    QHBoxLayout *picLayout = new QHBoxLayout;
    picLayout->addWidget(picPreview);
    picLayout->addWidget(browseBtn);

    //add widgets to form
    form->addRow("Profile Picture:", picLayout);
    form->addRow("Username:",   usernameEdit);
    form->addRow("Password:",   passwordEdit);


    //Horizontal layout for submit and cancel buttons
    QHBoxLayout *btnLayout = new QHBoxLayout;
    btnLayout->addStretch(); //adds a stretchable space at the end of a QBoxLayout
    btnLayout->addWidget(submitBtn);
    btnLayout->addWidget(cancelBtn);

    //Main vertical layout
    QVBoxLayout *main = new QVBoxLayout(this);
    main->addLayout(form);
    main->addLayout(btnLayout);
    setLayout(main);
}


//-----method when browser button is clicked-----//

void SignUpDialog::onBrowsePicture() {
    QString file = QFileDialog::getOpenFileName(this, "Select Profile Picture", QString(),"Images (*.png *.jpg *.jpeg)");
    if (!file.isEmpty()) {
        picPath = file;
        QPixmap pm(file);//load picture to display
        picPreview->setPixmap(pm.scaled(picPreview->size(), Qt::KeepAspectRatio, Qt::SmoothTransformation));
    }
}


//----method when onsubmit button is clicked --//

void SignUpDialog::onSubmit() {

    // Basic non‑empty checks
    if (firstNameEdit->text().isEmpty() ||
        lastNameEdit->text().isEmpty()  ||
        usernameEdit->text().isEmpty()  ||
        passwordEdit->text().isEmpty()){

        QMessageBox::warning(this, "Missing Information", "Please fill in First Name, Last Name, Username and Password.");

        return;
    }

    QString pwd = passwordEdit->text();

    // Length check
    if (pwd.length() < 8) {
        QMessageBox::warning(this, "Weak Password","Password must be at least 8 characters long.");
        return;
    }

    // Character type checks
    bool hasDigit   = false;
    bool hasLower   = false;
    bool hasUpper   = false;

    for (QChar c : pwd) {
        if (c.isDigit()) hasDigit = true;
        else if (c.isLower()) hasLower = true;
        else if (c.isUpper()) hasUpper = true;

        // early exit if all found
        if (hasDigit && hasLower && hasUpper)
            break;
    }

    // Report if any rule failed
    if (!hasDigit || !hasLower || !hasUpper) {
        QStringList missing;
        if (!hasDigit) missing << "• at least one number";
        if (!hasLower) missing << "• at least one lowercase letter";
        if (!hasUpper) missing << "• at least one uppercase letter";

        QMessageBox::warning(this, "Weak Password","Password must contain:\n" + missing.join("\n"));

        return;
    }

    // Password good
    accept();  // closes dialog with QDialog::Accepted


}
