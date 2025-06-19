require('dotenv').config();
const cors = require('cors');

const express = require('express');
const sql = require('mssql');
const bodyParser = require('body-parser');

const app = express();
const port = 3000;


//origin: ['http://your-android-device-ip:port', 'http://localhost:8081'] // Add allowed origins
// Enable CORS for all origins
app.use(cors({
    origin: ['http://24.16.152.140:3000'], // Replace with your actual client port
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type']
}));

app.use(bodyParser.json());

// SQL Server config
const dbConfig = {
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    server: process.env.DB_SERVER,
    database: process.env.DB_NAME,
    options: {
        encrypt: true,
        trustServerCertificate: true
    }
};

// Retry logic for SQL connection
async function connectWithRetry(retries = 5, delay = 2000) {
    for (let i = 0; i < retries; i++) {
        try {
            const pool = await sql.connect(dbConfig);
            return pool;
        } catch (err) {
            console.error(`Connection attempt ${i + 1} failed: ${err.message}`);
            if (i === retries - 1) throw err;
            await new Promise(res => setTimeout(res, delay));
        }
    }
}

app.get('/', (req, res) => {
    res.send('nodes.js SERVER is running and reachable!');
});

app.post('/add_sensor_reading', async (req, res) => {
    try {
        const { sensorName, sensorValue, timestamp, userNote, targetLocation } = req.body;

        if (!sensorName || !sensorValue || !timestamp || !userNote || !targetLocation) {
            return res.status(400).json({ error: 'All fields are required' });
        }

        const pool = await connectWithRetry(); // Use retry logic here

        await pool.request()
            .input('sensorName', sql.VarChar, sensorName)
            .input('sensorValue', sql.VarChar, sensorValue)
            .input('timestamp', sql.DateTime, timestamp)
            .input('userNote', sql.Text, userNote)
            .input('targetLocation', sql.VarChar, targetLocation)
            .query(`
                INSERT INTO SensorReading (sensorName, sensorValue, timestamp, userNote, targetLocation)
                VALUES (@sensorName, @sensorValue, @timestamp, @userNote, @targetLocation)
            `);

        res.status(200).json({ message: 'Sensor reading added successfully!' });

    } catch (err) {
        console.error('Error inserting data:', err);
        res.status(500).json({ error: err.message });
    }
});

//app.listen(port, () => {
//    console.log(`Server is running on http://localhost:${port}`);
//});

app.listen(port, '24.16.152.140', () => {
    console.log(`Server is running on http://24.16.152.140:${port}`);
    });