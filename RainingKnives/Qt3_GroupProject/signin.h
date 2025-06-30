#include <Qstring>
#include <QDialog>
#include <QLineEdit>

class SignInDialog : public QDialog {
    Q_OBJECT
public:
    explicit SignInDialog(QWidget *parent = nullptr);

    QString username() const { return userEdit->text(); }
    QString password() const { return passEdit->text(); }

private slots:
    void onSubmit();

private:
    //username and password input lines
    QLineEdit *userEdit, *passEdit;

    //submit button and cancel button
    QPushButton *submitBtn, *cancelBtn;
};
