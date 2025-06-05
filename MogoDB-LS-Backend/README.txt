ADMINISTRATOR CMD

1. copy over file directory with nodes and DB
>gcloud compute scp --recurse "C:\Node\SailsProjects\sensor_reading_api\sensor_reading_api\" p94100687@skyturtle:/home/p94100687/

2. ssh to the GVM
>gcloud compute ssh skyturtle

GVM CMD

=========
INSTALLING
1. install nodes (historic)

    1  ls -la ~
    2  cd sensor_reading_api/
    3  ls -la ~
    4  ls
    5  node -v
    6  npm -v
    7  sudo apt update
    8  sudo apt install nodejs npm -y
    9  npm install
   10  node server.js
   11  p94100687@skyturtle:~/sensor_reading_api$ node server.js
   12  Server is running on http://24.16.152.140:3000

2. check MogoDB installation in GVM   

>sudo apt install -y gnupg curl   
>curl -fsSL https://pgp.mongodb.com/server-7.0.asc | sudo gpg --dearmor -o /usr/share/keyrings/mongodb-server-7.0.gpg
>echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-7.0.gpg ] https://repo.mongodb.org/apt/debian bookworm/mongodb-org/7.0 main" | sudo tee /etc/apt/sources.list.d/mongodb-org-7.0.list
 
>sudo apt update
>sudo apt install -y mongodb-org

>sudo systemctl start mongod
>sudo systemctl enable mongod
>sudo systemctl status mongod
>npm install mongoose

add to server.js:
const mongoose = require('mongoose');

mongoose.connect('mongodb://localhost:27017/yourDatabaseName', {
    useNewUrlParser: true,
    useUnifiedTopology: true
}).then(() => {
    console.log('MongoDB connected!');
}).catch(err => {
    console.error('Connection error', err);
});


CORS
>npm install express cors body-parser
GVM Firewall 3000 allow TCP tag "http-server"


CURL
>sudo node server.js
>curl http://localhost:3000/  or from anyother machine >curl http://35.247.54.23:3000/

========================
CREATING THE COLLECTIONS
// models/User.js
const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  userId: { type: String, required: true, unique: true },
  username: String,
  email: String
});

module.exports = mongoose.model('User', userSchema);

// models/Location.js
const mongoose = require('mongoose');

const locationSchema = new mongoose.Schema({
  locationId: { type: String, required: true, unique: true },
  name: String,
  latitude: Number,
  longitude: Number
});

module.exports = mongoose.model('Location', locationSchema);

// models/Story.js
const mongoose = require('mongoose');

const storySchema = new mongoose.Schema({
  storyId: { type: String, required: true, unique: true },
  title: String,
  description: String,
  dateOfFact: Date,
  photoPath: String,
  locationId: String,
  userId: String
});

module.exports = mongoose.model('Story', storySchema);

// models/Report.js
const mongoose = require('mongoose');

const reportSchema = new mongoose.Schema({
  reportId: { type: String, required: true, unique: true },
  userId: String,
  reason: String,
  reportDate: Date,
  storyId: String
});

module.exports = mongoose.model('Report', reportSchema);

// models/CompassUsage.js
const mongoose = require('mongoose');

const compassUsageSchema = new mongoose.Schema({
  locationName: String,
  userId: String,
  timestamp: Date,
  heading: Number
});

module.exports = mongoose.model('CompassUsage', compassUsageSchema);

=========================================
POPULATING THE DB using SEED database.json
>node seedDatabase.js

create seedDatabase.js:
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');

// Load models
const User = require('./models/User');
const Location = require('./models/Location');
const Story = require('./models/Story');
const Report = require('./models/Report');
const CompassUsage = require('./models/CompassUsage');

// Connect to MongoDB
mongoose.connect('mongodb://localhost:27017/local_stories', {
  useNewUrlParser: true,
  useUnifiedTopology: true
}).then(() => {
  console.log('Connected to MongoDB');
  seedData();
}).catch(err => {
  console.error('MongoDB connection error:', err);
});

async function seedData() {
  try {
    const data = JSON.parse(fs.readFileSync(path.join(__dirname, 'database.json'), 'utf-8'));

    await User.insertMany(data.User);
    await Location.insertMany(data.Location);
    await Story.insertMany(data.Story);
    await Report.insertMany(data.Report);
    await CompassUsage.insertMany(data.CompassUsage);

    console.log('Database seeded successfully!');
    mongoose.disconnect();
  } catch (err) {
    console.error('Error seeding database:', err);
    mongoose.disconnect();
  }
}

===================================================
NODES BACKEND ALLOWING THE FRONTEND TO BE UNCHANGED

server.js:

const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const User = require('./models/User');
const Location = require('./models/Location');
const Story = require('./models/Story');
const Report = require('./models/Report');
const CompassUsage = require('./models/CompassUsage');

const app = express();
const port = 3000;

app.use(cors({
    origin: ['http://35.247.54.23:3000'],
    methods: ['GET', 'POST'],
    allowedHeaders: ['Content-Type']
}));

mongoose.connect('mongodb://localhost:27017/local_stories').then(() => {
  console.log('MongoDB connected');
}).catch(err => {
  console.error('MongoDB connection error:', err);
});

app.use(bodyParser.json());

app.get('/', (req, res) => {
    res.send('Node.js SERVER is running and reachable!');
});

app.post('/add_compass_usage', async (req, res) => {
    const { locationName, userId, timestamp, heading } = req.body;
    if (!locationName || !userId || !timestamp || heading === undefined) {
        return res.status(400).json({ error: 'All fields are required' });
    }

    try {
        await CompassUsage.create({ locationName, userId, timestamp, heading });
        res.status(200).json({ message: 'Compass usage added successfully!' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to add compass usage', details: err.message });
    }
});

app.post('/add_user', async (req, res) => {
    const { userId, username, email } = req.body;
    if (!userId || !username || !email) {
        return res.status(400).json({ error: 'All fields are required' });
    }

    try {
        await User.create({ userId, username, email });
        res.status(200).json({ message: 'User added successfully!' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to add user', details: err.message });
    }
});

app.post('/add_report', async (req, res) => {
    const { userId, reportId, reason, reportDate, storyId } = req.body;
    if (!userId || !reportId || !reason || !reportDate || !storyId) {
        return res.status(400).json({ error: 'All fields are required' });
    }

    try {
        await Report.create({ userId, reportId, reason, reportDate, storyId });
        res.status(200).json({ message: 'Report added successfully!' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to add report', details: err.message });
    }
});

app.post('/add_story', async (req, res) => {
    const { storyId, title, description, dateOfFact, photoPath, locationId, userId } = req.body;
    if (!storyId || !title || !description || !dateOfFact || !photoPath || !locationId || !userId) {
        return res.status(400).json({ error: 'All fields are required' });
    }

    try {
        await Story.create({ storyId, title, description, dateOfFact, photoPath, locationId, userId });
        res.status(200).json({ message: 'Story added successfully!' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to add story', details: err.message });
    }
});

app.post('/add_location', async (req, res) => {
    const { locationId, name, latitude, longitude } = req.body;
    if (!locationId || !name || latitude === undefined || longitude === undefined) {
        return res.status(400).json({ error: 'All fields are required' });
    }

    try {
        await Location.create({ locationId, name, latitude, longitude });
        res.status(200).json({ message: 'Location added successfully!' });
    } catch (err) {
        res.status(500).json({ error: 'Failed to add location', details: err.message });
    }
});

app.get('/stories', async (req, res) => {
    const { search = '', page = 1, limit = 10 } = req.query;
    try {
        let stories = await Story.find({
            $or: [
                { title: { $regex: search, $options: 'i' } },
                { description: { $regex: search, $options: 'i' } }
            ]
        }).skip((page - 1) * limit).limit(parseInt(limit));

        const total = await Story.countDocuments({
            $or: [
                { title: { $regex: search, $options: 'i' } },
                { description: { $regex: search, $options: 'i' } }
            ]
        });

        res.status(200).json({
            total,
            page: parseInt(page),
            limit: parseInt(limit),
            results: stories
        });
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch stories', details: err.message });
    }
});

app.get('/locations', async (req, res) => {
    const { search = '', page = 1, limit = 10 } = req.query;
    try {
        let locations = await Location.find({
            name: { $regex: search, $options: 'i' }
        }).skip((page - 1) * limit).limit(parseInt(limit));

        const total = await Location.countDocuments({
            name: { $regex: search, $options: 'i' }
        });

        res.status(200).json({
            total,
            page: parseInt(page),
            limit: parseInt(limit),
            results: locations
        });
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch locations', details: err.message });
    }
});

function getDistance(lat1, lon1, lat2, lon2) {
    const toRad = deg => deg * Math.PI / 180;
    const R = 6371;
    const dLat = toRad(lat2 - lat1);
    const dLon = toRad(lon2 - lon1);
    const a = Math.sin(dLat / 2) ** 2 +
              Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
              Math.sin(dLon / 2) ** 2;
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

app.get('/nearest_location', async (req, res) => {
    const { latitude, longitude } = req.query;
    if (!latitude || !longitude) {
        return res.status(400).json({ error: 'Latitude and longitude are required' });
    }

    try {
        const locations = await Location.find();
        if (locations.length === 0) {
            return res.status(404).json({ error: 'No locations found' });
        }

        let nearest = locations[0];
        let minDistance = getDistance(latitude, longitude, nearest.latitude, nearest.longitude);

        for (const loc of locations) {
            const dist = getDistance(latitude, longitude, loc.latitude, loc.longitude);
            if (dist < minDistance) {
                nearest = loc;
                minDistance = dist;
            }
        }

        const relatedStories = await Story.find({ locationId: nearest.locationId });

        res.status(200).json({
            nearestLocation: nearest,
            distanceKm: minDistance.toFixed(2),
            relatedStories
        });
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch nearest location', details: err.message });
    }
});

app.get('/user/:userId', async (req, res) => {
    const { userId } = req.params;
    try {
        const user = await User.findOne({ userId });
        if (!user) {
            return res.status(404).json({ error: 'User not found' });
        }

        const reports = await Report.find({ userId });

        res.status(200).json({
            user,
            reports
        });
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch user', details: err.message });
    }
});

app.get('/compass_usage/:userId', async (req, res) => {
    const { userId } = req.params;
    try {
        const usage = await CompassUsage.find({ userId });

        res.status(200).json({
            userId,
            compassUsage: usage
        });
    } catch (err) {
        res.status(500).json({ error: 'Failed to fetch compass usage', details: err.message });
    }
});

app.listen(port, '0.0.0.0', () => {
    console.log(`Server is running on http://35.247.54.23:${port}`);
});

===============
CURL CHECKING IT

curl http://35.247.54.23:3000/

curl http://35.247.54.23:3000/user/user999
curl "http://35.247.54.23:3000/nearest_location?latitude=47.61&longitude=-122.33"
curl "http://35.247.54.23:3000/stories?search=ghost&page=1&limit=5"
curl -X POST http://35.247.54.23:3000/add_story ^
  -H "Content-Type: application/json" ^
  -d "{\"storyId\":\"story999\",\"title\":\"Ghost in the Alley\",\"description\":\"A shadowy figure was seen.\",\"dateOfFact\":\"2025-06-01\",\"photoPath\":\"/images/ghost.jpg\",\"locationId\":\"location999\",\"userId\":\"user999\"}"
curl -X POST http://35.247.54.23:3000/add_location ^
  -H "Content-Type: application/json" ^
  -d "{\"locationId\":\"location999\",\"name\":\"Haunted Alley\",\"latitude\":47.61,\"longitude\":-122.33}"
curl -X POST http://35.247.54.23:3000/add_user ^
  -H "Content-Type: application/json" ^
  -d "{\"userId\":\"user999\",\"username\":\"ghosthunterX\",\"email\":\"ghostx@example.com\"}"
curl http://35.247.54.23:3000/user/user999

this should cover all functions defined in the ERD

===================
TEST PROTOCOL: PASS June 5 2025 TP
C:\Node\cmder (cmder@1.0.0)
λ curl http://35.247.54.23:3000/
Node.js SERVER is running and reachable!
C:\Node\cmder (cmder@1.0.0)
λ curl http://35.247.54.23:3000/user/user999
{"error":"User not found"}
C:\Node\cmder (cmder@1.0.0)
λ ^C
C:\Node\cmder (cmder@1.0.0)
λ curl "http://35.247.54.23:3000/nearest_location?latitude=47.61&longitude=-122.33"
{"nearestLocation":{"_id":"6841aaf76ab1736cbd43cf9a","locationId":"location001","name":"Pike Place Market","latitude":47.6097,"longitude":-122.3425,"__v":0},"distanceKm":"0.94","relatedStories":[{"_id":"6841aaf76ab1736cbd43cf9f","storyId":"story001","title":"The Ghost of Pike Place Market","description":"A ghostly figure has been seen wandering the market at night.","dateOfFact":"2023-01-01T00:00:00.000Z","photoPath":"/images/pike_place_ghost.jpg","locationId":"location001","userId":"user123","__v":0}]}
C:\Node\cmder (cmder@1.0.0)
λ curl "http://35.247.54.23:3000/stories?search=ghost&page=1&limit=5"
{"total":2,"page":1,"limit":5,"results":[{"_id":"6841aaf76ab1736cbd43cf9f","storyId":"story001","title":"The Ghost of Pike Place Market","description":"A ghostly figure has been seen wandering the market at night.","dateOfFact":"2023-01-01T00:00:00.000Z","photoPath":"/images/pike_place_ghost.jpg","locationId":"location001","userId":"user123","__v":0},{"_id":"6841aaf76ab1736cbd43cfa1","storyId":"story003","title":"The Phantom of Smith Tower","description":"A ghostly figure has been seen on the observation deck.","dateOfFact":"2023-03-01T00:00:00.000Z","photoPath":"/images/smith_tower_ghost.jpg","locationId":"location003","userId":"user789","__v":0}]}
C:\Node\cmder (cmder@1.0.0)
λ curl -X POST http://35.247.54.23:3000/add_story ^   -H "Content-Type: application/json" ^
 -d "{\"storyId\":\"story999\",\"title\":\"Ghost in the Alley\",\"description\":\"A shadowy figure was seen.\",\"dateOfFact\":\"2025-06-01\",\"photoPath\":\"/images/ghost.jpg\",\"locationId\":\"location999\",\"userId\":\"user999\"}"
{"message":"Story added successfully!"}
C:\Node\cmder (cmder@1.0.0)
λ curl -X POST http://35.247.54.23:3000/add_location ^   -H "Content-Type: application/json" ^   -d "{\"locationId\":\"location999\",\"name\":\"Haunted Alley\",\"latitude\":47.61,\"longitude\":-122.33}"
{"message":"Location added successfully!"}
C:\Node\cmder (cmder@1.0.0)
λ curl -X POST http://35.247.54.23:3000/add_user ^   -H "Content-Type: application/json" ^
-d "{\"userId\":\"user999\",\"username\":\"ghosthunterX\",\"email\":\"ghostx@example.com\"}" {"message":"User added successfully!"}
C:\Node\cmder (cmder@1.0.0)
λ curl http://35.247.54.23:3000/user/user999
{"user":{"_id":"6841b91d014beaef0c4b70ed","userId":"user999","username":"ghosthunterX","email":"ghostx@example.com","__v":0},"reports":[]}
C:\Node\cmder (cmder@1.0.0)
λ

