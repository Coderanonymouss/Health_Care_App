const express = require('express');
const admin = require('firebase-admin');
const bodyParser = require('body-parser');
const cors = require('cors');

const serviceAccount = require('./serviceAccountKey.json'); // Путь к твоему ключу

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

const app = express();
app.use(cors());
app.use(bodyParser.json());

app.post('/createPatient', async (req, res) => {
  try {
    const { firstName, lastName, middleName, email, doctorEmail } = req.body;

    if (!firstName || !lastName || !email || !doctorEmail) {
      return res.status(400).json({ error: 'Отсутствуют обязательные поля' });
    }

    // Генерируем временный пароль
    const tempPassword = Math.random().toString(36).slice(-8);

    // Создаем пользователя Firebase Authentication
    const userRecord = await admin.auth().createUser({
      email,
      password: tempPassword,
      displayName: `${firstName} ${lastName}`
    });

    // Сохраняем данные пациента в Firestore
    const patientData = {
      firstName,
      lastName,
      middleName: middleName || null,
      email,
      doctorEmail,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    };
    await db.collection('Patient').doc(email).set(patientData);

    // Создаем запись роли в коллекции User
    const userRole = {
      email,
      type: 'Patient',
      firstSigninCompleted: false
    };
    await db.collection('User').doc(email).set(userRole);

    // TODO: Добавь отправку email с tempPassword через nodemailer или сторонний сервис

    res.json({ message: 'Пациент создан', tempPassword });

  } catch (error) {
    console.error('Ошибка создания пациента:', error);
    res.status(500).json({ error: error.message });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => console.log(`Server started on port ${PORT}`));
