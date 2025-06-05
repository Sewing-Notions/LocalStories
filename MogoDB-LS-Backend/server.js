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
